package pawparazzi.back.board.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pawparazzi.back.S3.service.S3AsyncService;
import pawparazzi.back.board.dto.BoardCreateRequestDto;
import pawparazzi.back.board.dto.BoardListResponseDto;
import pawparazzi.back.board.dto.BoardDetailDto;
import pawparazzi.back.board.dto.BoardUpdateRequestDto;
import pawparazzi.back.board.entity.Board;
import pawparazzi.back.board.entity.BoardDocument;
import pawparazzi.back.board.entity.BoardVisibility;
import pawparazzi.back.board.repository.BoardRepository;
import pawparazzi.back.board.repository.BoardMongoRepository;
import pawparazzi.back.comment.repository.CommentLikeRepository;
import pawparazzi.back.comment.repository.CommentRepository;
import pawparazzi.back.comment.repository.ReplyLikeRepository;
import pawparazzi.back.comment.repository.ReplyRepository;
import pawparazzi.back.likes.repository.LikeRepository;
import pawparazzi.back.member.entity.Member;
import pawparazzi.back.member.repository.MemberRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final BoardMongoRepository boardMongoRepository;
    private final MemberRepository memberRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final ReplyRepository replyRepository;
    private final ReplyLikeRepository replyLikeRepository;
    private final S3AsyncService s3AsyncService;


    /**
     * 게시물 등록
     */
    @Transactional
    public BoardDetailDto createBoard(String userDataJson, Long userId,
                                      MultipartFile titleImageFile, List<MultipartFile> mediaFiles,
                                      String titleContent) {

        // 1. JSON 파싱
        BoardCreateRequestDto requestDto;
        try {
            requestDto = new ObjectMapper().readValue(userDataJson, BoardCreateRequestDto.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid JSON format", e);
        }
        requestDto.setMediaFiles(mediaFiles);
        requestDto.setTitleContent(titleContent);

        // 2. 사용자 조회
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (requestDto.getVisibility() == null) {
            throw new IllegalArgumentException("게시물 공개 설정은 필수 입력값입니다.");
        }

        // 3. MongoDB 임시 저장
        BoardDocument boardDocument = new BoardDocument(null, requestDto.getTitle(), null, requestDto.getTitleContent(), new ArrayList<>());
        boardDocument = boardMongoRepository.save(boardDocument);

        // 4. MySQL 저장
        Board board = new Board(member, boardDocument.getId(), requestDto.getVisibility());
        boardRepository.save(board);

        // 5. S3 비동기 업로드
        CompletableFuture<List<String>> uploadFuture = uploadFilesToS3(requestDto.getMediaFiles(), board.getId());
        List<String> uploadedUrls = uploadFuture.join();

        // 6. 파일명 ↔ S3 URL 매핑
        Map<String, String> fileNameToUrl = new HashMap<>();
        for (int i = 0; i < mediaFiles.size(); i++) {
            fileNameToUrl.put(mediaFiles.get(i).getOriginalFilename(), uploadedUrls.get(i));
        }

        // 7. 콘텐츠 구성 (순서 보존)
        List<BoardDocument.ContentDto> contents = new ArrayList<>();
        for (BoardCreateRequestDto.ContentDto dto : requestDto.getContents()) {
            if ("Text".equals(dto.getType())) {
                contents.add(new BoardDocument.ContentDto("Text", dto.getValue()));
            } else if ("File".equals(dto.getType())) {
                String url = fileNameToUrl.get(dto.getValue());
                if (url != null) {
                    contents.add(new BoardDocument.ContentDto("File", url));
                }
            }
        }

        // 8. 대표 이미지 추출
        String titleImage = getTitleImageUrl(titleImageFile, uploadedUrls);

        // 9. MongoDB 최종 저장
        boardDocument.setMysqlId(board.getId());
        boardDocument.setContents(contents);
        boardDocument.setTitleImage(titleImage);
        boardMongoRepository.save(boardDocument);

        return convertToBoardDetailDto(board, boardDocument);
    }

    /**
     * 비동기 방식으로 S3에 파일 업로드
     */
    private CompletableFuture<List<String>> uploadFilesToS3(List<MultipartFile> mediaFiles, Long boardId) {
        if (mediaFiles == null || mediaFiles.isEmpty()) {
            return CompletableFuture.completedFuture(new ArrayList<>());
        }

        List<CompletableFuture<String>> uploadFutures = mediaFiles.stream()
                .map(file -> {
                    String fileName = "board_images/" + boardId + "/" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
                    try {
                        return s3AsyncService.uploadFile(fileName, file.getBytes(), file.getContentType());
                    } catch (IOException e) {
                        return CompletableFuture.<String>failedFuture(new RuntimeException("파일 업로드 실패: " + e.getMessage()));
                    }
                })
                .collect(Collectors.toList());

        return CompletableFuture.allOf(uploadFutures.toArray(new CompletableFuture[0]))
                .thenApply(v -> uploadFutures.stream()
                        .map(CompletableFuture::join)
                        .toList()
                );
    }

    /**
     * titleImage
     */
    private String getTitleImageUrl(MultipartFile titleImageFile, List<String> uploadedUrls) {
        if (titleImageFile == null || titleImageFile.isEmpty()) {
            return null;
        }
        String fileName = titleImageFile.getOriginalFilename();
        return uploadedUrls.stream()
                .filter(url -> url.contains(fileName))
                .findFirst()
                .orElse(null);
    }


    /**
     * 게시물 수정
     */
    @Transactional
    public CompletableFuture<BoardDetailDto> updateBoard(Long boardId, Long userId,
                                                         String userDataJson, List<MultipartFile> mediaFiles,
                                                         MultipartFile titleImageFile, String titleContent) {

        // 1. JSON 파싱
        BoardUpdateRequestDto requestDto;
        try {
            requestDto = new ObjectMapper().readValue(userDataJson, BoardUpdateRequestDto.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid JSON format", e);
        }
        requestDto.setTitleContent(titleContent);

        // 2. 사용자 권한 확인
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new EntityNotFoundException("게시물을 찾을 수 없습니다."));
        if (!board.getAuthor().getId().equals(userId)) {
            throw new IllegalArgumentException("게시물 수정 권한이 없습니다.");
        }

        BoardDocument boardDocument = boardMongoRepository.findByMysqlId(boardId)
                .orElseThrow(() -> new EntityNotFoundException("MongoDB에서 해당 게시글을 찾을 수 없습니다."));

        // 3. 기존 S3 파일 삭제
        String folderPath = "board_images/" + boardId + "/";
        List<String> existingFileKeys = s3AsyncService.listFilesInFolder(folderPath);
        CompletableFuture<Void> deleteFuture = existingFileKeys.isEmpty()
                ? CompletableFuture.completedFuture(null)
                : s3AsyncService.deleteFiles(existingFileKeys).exceptionally(ex -> {
            System.err.println("S3 기존 이미지 삭제 실패: " + ex.getMessage());
            return null;
        });

        // 4. S3 새 파일 업로드
        CompletableFuture<List<String>> uploadFuture = (mediaFiles == null || mediaFiles.isEmpty())
                ? CompletableFuture.completedFuture(new ArrayList<>())
                : uploadFilesToS3(mediaFiles, boardId);

        return CompletableFuture.allOf(deleteFuture, uploadFuture)
                .thenCompose(ignored -> uploadFuture.thenApply(uploadedUrls -> {

                    // 5. 파일명 ↔ URL 매핑
                    Map<String, String> fileNameToUrl = new HashMap<>();
                    for (int i = 0; i < mediaFiles.size(); i++) {
                        fileNameToUrl.put(mediaFiles.get(i).getOriginalFilename(), uploadedUrls.get(i));
                    }

                    // 🔥 기존 콘텐츠 초기화 (덮어쓰기)
                    List<BoardDocument.ContentDto> updatedContents = new ArrayList<>();

                    // 6. 콘텐츠 재구성 (Text는 그대로, File은 S3 URL로 변환하여 image 타입으로)
                    for (BoardUpdateRequestDto.ContentDto dto : requestDto.getContents()) {
                        if ("Text".equals(dto.getType())) {
                            updatedContents.add(new BoardDocument.ContentDto("Text", dto.getValue()));
                        } else if ("File".equals(dto.getType())) {
                            String url = fileNameToUrl.get(dto.getValue());
                            if (url != null) {
                                updatedContents.add(new BoardDocument.ContentDto("image", url)); // 타입 통일
                            }
                        }
                    }

                    // 7. 대표 이미지 추출
                    String titleImage = getTitleImageUrl(titleImageFile, uploadedUrls);

                    // 8. MongoDB 업데이트
                    boardDocument.setContents(updatedContents);
                    boardDocument.setTitleContent(requestDto.getTitleContent());
                    boardDocument.setTitleImage(titleImage);
                    boardMongoRepository.save(boardDocument);

                    // 9. MySQL 업데이트
                    if (requestDto.getVisibility() != null) {
                        board.setVisibility(requestDto.getVisibility());
                    }
                    boardRepository.save(board);

                    return convertToBoardDetailDto(board, boardDocument);
                }));
    }

    /**
     * 특정 회원의 게시물 조회
     */
    @Transactional(readOnly = true)
    public List<BoardListResponseDto> getBoardsByMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원을 찾을 수 없습니다."));

        List<Board> boards = boardRepository.findByAuthor(member);

        return boards.stream()
                .filter(board -> board.getVisibility() == BoardVisibility.PUBLIC)
                .map(this::convertToBoardListResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * 게시물 전체 조회
     */
    @Transactional(readOnly = true)
    public List<BoardListResponseDto> getBoardList() {
        List<Board> boards = boardRepository.findAll();

        return boards.stream()
                .map(this::convertToBoardListResponseDto)
                .collect(Collectors.toList());
    }

    private BoardListResponseDto convertToBoardListResponseDto(Board board) {
        BoardListResponseDto dto = new BoardListResponseDto();
        dto.setId(board.getId());
        dto.setWriteDatetime(board.getWriteDatetime());
        dto.setFavoriteCount(board.getFavoriteCount());
        dto.setCommentCount(board.getCommentCount());
        dto.setViewCount(board.getViewCount());
        dto.setVisibility(board.getVisibility());

        BoardDocument boardDocument = boardMongoRepository.findByMysqlId(board.getId())
                .orElse(null);

        if (boardDocument != null) {
            dto.setTitle(boardDocument.getTitle());
            dto.setTitleImage(boardDocument.getTitleImage());
            dto.setTitleContent(boardDocument.getTitleContent());
        }
        else {
            dto.setTitle("제목 없음");
            dto.setTitleImage(null);
            dto.setTitleContent("내용 없음");
        }

        Long authorId = board.getAuthorId();
        if (authorId != null) {
            Member author = memberRepository.findById(authorId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다."));
            dto.setAuthor(new BoardListResponseDto.AuthorDto(author.getId(), author.getNickName(), author.getProfileImageUrl()));
        }

        return dto;
    }

    /**
     * 게시물 상세 조회
     */
    @Transactional
    public BoardDetailDto getBoardDetail(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("MySQL에 해당 게시글이 존재하지 않습니다."));

        BoardDocument boardDocument = boardMongoRepository.findByMysqlId(board.getId())
                .orElseThrow(() -> new IllegalArgumentException("MongoDB에 해당 게시글이 존재하지 않습니다."));

        board.increaseViewCount();
        boardRepository.save(board);
        boardRepository.flush();

        return convertToBoardDetailDto(board, boardDocument);
    }

    private BoardDetailDto convertToBoardDetailDto(Board board, BoardDocument boardDocument) {
        BoardDetailDto dto = new BoardDetailDto();
        dto.setId(board.getId());
        dto.setTitle(boardDocument.getTitle());
        dto.setTitleImage(boardDocument.getTitleImage());
        dto.setTitleContent(boardDocument.getTitleContent());
        dto.setWriteDatetime(board.getWriteDatetime());
        dto.setFavoriteCount(board.getFavoriteCount());
        dto.setCommentCount(board.getCommentCount());
        dto.setViewCount(board.getViewCount());
        dto.setVisibility(board.getVisibility());

        if (board.getAuthor() != null) {
            BoardDetailDto.AuthorDto authorDto = new BoardDetailDto.AuthorDto();
            authorDto.setId(board.getAuthor().getId());
            authorDto.setNickname(board.getAuthor().getNickName());
            authorDto.setProfileImageUrl(board.getAuthor().getProfileImageUrl());
            dto.setAuthor(authorDto);
        }

        dto.setContents(boardDocument.getContents().stream()
                .map(content -> new BoardDetailDto.ContentDto(content.getType(), content.getValue()))
                .collect(Collectors.toList()));

        return dto;
    }


    /**
     * 게시물 삭제
     */
    @Transactional
    public CompletableFuture<Void> deleteBoard(Long boardId, Long userId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시물을 찾을 수 없습니다."));

        if (!board.getAuthorId().equals(userId)) {
            throw new IllegalArgumentException("본인이 작성한 게시물만 삭제할 수 있습니다.");
        }

        String folderPath = "board_images/" + boardId + "/";
        List<String> fileKeys = s3AsyncService.listFilesInFolder(folderPath);

        CompletableFuture<Void> deleteS3Future = fileKeys.isEmpty()
                ? CompletableFuture.completedFuture(null)
                : s3AsyncService.deleteFiles(fileKeys)
                .exceptionally(ex -> {
                    System.err.println("게시물 이미지 삭제 실패: " + ex.getMessage());
                    return null;
                });

        replyRepository.findByBoardId(boardId).forEach(reply -> {
            replyLikeRepository.deleteByReplyId(reply.getId());
        });

        replyRepository.deleteByBoardId(boardId);
        commentLikeRepository.deleteByBoardId(boardId);
        commentRepository.deleteByBoardId(boardId);
        likeRepository.deleteByBoardId(boardId);
        boardMongoRepository.deleteByMysqlId(board.getId());
        boardRepository.delete(board);

        return deleteS3Future;
    }
}