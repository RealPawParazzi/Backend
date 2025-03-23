package pawparazzi.back.board.service;

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
import java.util.List;
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
    public BoardDetailDto createBoard(BoardCreateRequestDto requestDto, Long userId, MultipartFile titleImageFile) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (requestDto.getVisibility() == null) {
            throw new IllegalArgumentException("게시물 공개 설정은 필수 입력값입니다.");
        }

        // MongoDB에 게시물 저장 (임시 저장)
        BoardDocument boardDocument = new BoardDocument(null, requestDto.getTitle(), null, requestDto.getTitleContent(), new ArrayList<>());
        boardDocument = boardMongoRepository.save(boardDocument);

        // MySQL에 게시물 저장
        Board board = new Board(member, boardDocument.getId(), requestDto.getVisibility());
        boardRepository.save(board);

        // 컨텐츠 변환
        List<BoardDocument.ContentDto> contents = requestDto.getContents().stream()
                .map(dto -> new BoardDocument.ContentDto(dto.getType(), dto.getValue()))
                .collect(Collectors.toList());

        // S3 비동기 업로드
        CompletableFuture<List<String>> uploadFuture = uploadFilesToS3(requestDto.getMediaFiles(), board.getId());

        // S3 업로드된 이미지 URL을 MongoDB 컨텐츠에 추가
        List<String> uploadedUrls = uploadFuture.join();
        uploadedUrls.forEach(url -> contents.add(new BoardDocument.ContentDto("File", url)));

        String titleImage = getTitleImageUrl(titleImageFile, uploadedUrls);

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
                                                         BoardUpdateRequestDto requestDto,
                                                         List<MultipartFile> mediaFiles,
                                                         MultipartFile titleImageFile) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new EntityNotFoundException("게시물을 찾을 수 없습니다."));

        if (!board.getAuthor().getId().equals(userId)) {
            throw new IllegalArgumentException("게시물 수정 권한이 없습니다.");
        }

        BoardDocument boardDocument = boardMongoRepository.findByMysqlId(boardId)
                .orElseThrow(() -> new EntityNotFoundException("MongoDB에서 해당 게시글을 찾을 수 없습니다."));

        String folderPath = "board_images/" + boardId + "/";
        List<String> existingFileKeys = s3AsyncService.listFilesInFolder(folderPath);

        CompletableFuture<Void> deleteFuture = existingFileKeys.isEmpty()
                ? CompletableFuture.completedFuture(null)
                : s3AsyncService.deleteFiles(existingFileKeys)
                .exceptionally(ex -> {
                    System.err.println("S3 기존 이미지 삭제 실패: " + ex.getMessage());
                    return null;
                });

        boardDocument.setContents(
                boardDocument.getContents().stream()
                        .filter(content -> !"image".equals(content.getType()))
                        .toList()
        );

        CompletableFuture<List<String>> uploadFuture = (mediaFiles == null || mediaFiles.isEmpty())
                ? CompletableFuture.completedFuture(new ArrayList<>())
                : uploadFilesToS3(mediaFiles, board.getId());

        return CompletableFuture.allOf(deleteFuture, uploadFuture)
                .thenCompose(ignored -> uploadFuture.thenApply(uploadedUrls -> {
                    List<BoardDocument.ContentDto> updatedContents = new ArrayList<>();
                    if (requestDto.getContents() != null && !requestDto.getContents().isEmpty()) {
                        updatedContents.addAll(requestDto.getContents().stream()
                                .map(dto -> new BoardDocument.ContentDto(dto.getType(), dto.getValue()))
                                .toList());
                    }
                    uploadedUrls.forEach(url -> updatedContents.add(new BoardDocument.ContentDto("image", url)));

                    boardDocument.setContents(updatedContents);

                    String titleImage = getTitleImageUrl(titleImageFile, uploadedUrls);
                    boardDocument.setTitleImage(titleImage);

                    boardDocument.setTitleContent(requestDto.getTitleContent());

                    if (requestDto.getVisibility() != null) {
                        board.setVisibility(requestDto.getVisibility());
                    }

                    boardMongoRepository.save(boardDocument);
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