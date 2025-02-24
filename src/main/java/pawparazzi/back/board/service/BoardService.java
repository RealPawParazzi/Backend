package pawparazzi.back.board.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pawparazzi.back.board.dto.BoardCreateRequestDto;
import pawparazzi.back.board.dto.BoardListResponseDto;
import pawparazzi.back.board.dto.BoardDetailDto;
import pawparazzi.back.board.entity.Board;
import pawparazzi.back.board.entity.BoardDocument;
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

    @Transactional
    public BoardDetailDto createBoard(BoardCreateRequestDto requestDto, String token) {
        Long userId = jwtUtil.extractMemberId(token.replace("Bearer ", ""));

        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<BoardDocument.ContentDto> contents = requestDto.getContents().stream()
                .map(dto -> new BoardDocument.ContentDto(dto.getType(), dto.getValue()))
                .collect(Collectors.toList());

        String firstText = contents.stream()
                .filter(c -> "text".equals(c.getType()))
                .map(BoardDocument.ContentDto::getValue)
                .findFirst()
                .orElse(null);

        String firstImage = contents.stream()
                .filter(c -> "image".equals(c.getType()))
                .map(BoardDocument.ContentDto::getValue)
                .findFirst()
                .orElse(null);

        BoardDocument boardDocument = new BoardDocument(null, requestDto.getTitle(), firstImage, firstText, contents);
        boardMongoRepository.save(boardDocument);


        Board board = new Board(member, boardDocument.getId());
        boardRepository.save(board);

        boardDocument.setMysqlId(board.getId());
        boardMongoRepository.save(boardDocument);

        return convertToBoardDetailDto(board, boardDocument);
    }

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
}