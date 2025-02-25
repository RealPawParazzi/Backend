package pawparazzi.back.board.service;

import jakarta.persistence.EntityNotFoundException;
import kotlin.reflect.KVisibility;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pawparazzi.back.board.dto.BoardCreateRequestDto;
import pawparazzi.back.board.dto.BoardListResponseDto;
import pawparazzi.back.board.dto.BoardDetailDto;
import pawparazzi.back.board.dto.BoardUpdateRequestDto;
import pawparazzi.back.board.entity.Board;
import pawparazzi.back.board.entity.BoardDocument;
import pawparazzi.back.board.entity.BoardVisibility;
import pawparazzi.back.board.repository.BoardRepository;
import pawparazzi.back.board.repository.BoardMongoRepository;
import pawparazzi.back.member.entity.Member;
import pawparazzi.back.member.repository.MemberRepository;
import pawparazzi.back.security.util.JwtUtil;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final BoardMongoRepository boardMongoRepository;
    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;

    /**
     * 게시물 등록
     */
    @Transactional
    public BoardDetailDto createBoard(BoardCreateRequestDto requestDto, String token) {
        Long userId = jwtUtil.extractMemberId(token.replace("Bearer ", ""));

        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<BoardDocument.ContentDto> contents = requestDto.getContents().stream()
                .map(dto -> new BoardDocument.ContentDto(dto.getType(), dto.getValue()))
                .collect(Collectors.toList());

        String titleImage = requestDto.getTitleImage();
        if (titleImage == null || titleImage.isBlank()) {
            titleImage = contents.stream()
                    .filter(c -> "image".equals(c.getType()))
                    .map(BoardDocument.ContentDto::getValue)
                    .findFirst()
                    .orElse(null);
        }

        String firstText = contents.stream()
                .filter(c -> "text".equals(c.getType()))
                .map(BoardDocument.ContentDto::getValue)
                .findFirst()
                .orElse(null);

        if (requestDto.getVisibility() == null) {
            throw new IllegalArgumentException("게시물 공개 설정(visibility)은 필수 입력값입니다.");
        }

        BoardDocument boardDocument = new BoardDocument(null, requestDto.getTitle(), titleImage, firstText, contents);
        boardMongoRepository.save(boardDocument);

        Board board = new Board(member, boardDocument.getId(), requestDto.getVisibility());
        boardRepository.save(board);

        boardDocument.setMysqlId(board.getId());
        boardMongoRepository.save(boardDocument);

        return convertToBoardDetailDto(board, boardDocument);
    }

    /**
     * 게시물 수정
     */
    /**
     * 게시물 수정
     */
    @Transactional
    public BoardDetailDto updateBoard(Long boardId, String token, BoardUpdateRequestDto requestDto) {
        Long userId = jwtUtil.extractMemberId(token.replace("Bearer ", ""));

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new EntityNotFoundException("게시물을 찾을 수 없습니다."));

        if (!board.getAuthor().getId().equals(userId)) {
            throw new IllegalArgumentException("게시물 수정 권한이 없습니다.");
        }

        BoardDocument boardDocument = boardMongoRepository.findByMysqlId(boardId)
                .orElseThrow(() -> new EntityNotFoundException("MongoDB에서 해당 게시글을 찾을 수 없습니다."));

        // 수정할 데이터만 업데이트 (null 값이면 기존 데이터 유지)
        if (requestDto.getTitle() != null && !requestDto.getTitle().isBlank()) {
            boardDocument.setTitle(requestDto.getTitle());
        }

        if (requestDto.getTitleImage() != null && !requestDto.getTitleImage().isBlank()) {
            boardDocument.setTitleImage(requestDto.getTitleImage());
        }

        if (requestDto.getContents() != null && !requestDto.getContents().isEmpty()) {
            List<BoardDocument.ContentDto> updatedContents = requestDto.getContents().stream()
                    .map(dto -> new BoardDocument.ContentDto(dto.getType(), dto.getValue()))
                    .collect(Collectors.toList());
            boardDocument.setContents(updatedContents);

            // 첫 번째 텍스트를 자동으로 `titleContent`에 반영
            String firstText = updatedContents.stream()
                    .filter(c -> "text".equals(c.getType()))
                    .map(BoardDocument.ContentDto::getValue)
                    .findFirst()
                    .orElse(null);
            boardDocument.setTitleContent(firstText);
        }

        if (requestDto.getVisibility() != null) {
            board.setVisibility(requestDto.getVisibility());
        }

        // MongoDB 업데이트
        boardMongoRepository.save(boardDocument);
        boardRepository.save(board); // MySQL도 업데이트 반영

        return convertToBoardDetailDto(board, boardDocument);
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
        BoardDocument boardDocument = boardMongoRepository.findByMysqlId(board.getId())
                .orElseThrow(() -> new IllegalArgumentException("MongoDB에 해당 게시글이 존재하지 않습니다."));

        BoardListResponseDto dto = new BoardListResponseDto();
        dto.setId(board.getId());
        dto.setTitle(boardDocument.getTitle());
        dto.setTitleImage(boardDocument.getTitleImage());
        dto.setTitleContent(boardDocument.getTitleContent());
        dto.setWriteDatetime(board.getWriteDatetime());
        dto.setFavoriteCount(board.getFavoriteCount());
        dto.setCommentCount(board.getCommentCount());
        dto.setViewCount(board.getViewCount());

        if (board.getAuthor() != null) {
            BoardListResponseDto.AuthorDto authorDto = new BoardListResponseDto.AuthorDto();
            authorDto.setId(board.getAuthor().getId());
            authorDto.setNickname(board.getAuthor().getNickName());
            authorDto.setProfileImageUrl(board.getAuthor().getProfileImageUrl());
            dto.setAuthor(authorDto);
        }

        return dto;
    }

    /**
     * 게시물 상세 조회
     */
    @Transactional(readOnly = true)
    public BoardDetailDto getBoardDetail(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("MySQL에 해당 게시글이 존재하지 않습니다."));

        BoardDocument boardDocument = boardMongoRepository.findByMysqlId(board.getId())
                .orElseThrow(() -> new IllegalArgumentException("MongoDB에 해당 게시글이 존재하지 않습니다."));

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

        if (board.getAuthor() != null) {
            BoardDetailDto.AuthorDto authorDto = new BoardDetailDto.AuthorDto();
            authorDto.setId(board.getAuthor().getId());
            authorDto.setNickname(board.getAuthor().getNickName());
            authorDto.setProfileImageUrl(board.getAuthor().getProfileImageUrl());
            dto.setAuthor(authorDto);
        }

        // MongoDB에서 가져온 contents 데이터를 변환 후 저장
        dto.setContents(boardDocument.getContents().stream()
                .map(content -> new BoardDetailDto.ContentDto(content.getType(), content.getValue()))
                .collect(Collectors.toList()));

        return dto;
    }

    /**
     * 게시물 삭제
     */
    @Transactional
    public void deleteBoard(Long boardId, String token) {
        Long memberId = jwtUtil.extractMemberId(token.replace("Bearer ", ""));

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시물을 찾을 수 없습니다."));

        if (!board.getAuthor().getId().equals(memberId)) {
            throw new IllegalArgumentException("본인이 작성한 게시물만 삭제할 수 있습니다.");
        }

        boardMongoRepository.deleteByMysqlId(board.getId());
        boardRepository.delete(board);
    }
}