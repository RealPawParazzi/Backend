package pawparazzi.back.board.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pawparazzi.back.board.dto.BoardCreateRequestDto;
import pawparazzi.back.board.dto.BoardListResponseDto;
import pawparazzi.back.board.dto.BoardDetailDto;
import pawparazzi.back.board.entity.*;
import pawparazzi.back.board.repository.*;
import pawparazzi.back.security.util.JwtUtil;
import pawparazzi.back.member.entity.Member;
import pawparazzi.back.member.repository.MemberRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final BoardContentRepository boardContentRepository;
    private final BoardMediaRepository boardMediaRepository;
    private final BoardOrderRepository boardOrderRepository;
    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;

    /**
     * 게시글 등록
     */
    @Transactional
    public BoardDetailDto createBoard(BoardCreateRequestDto requestDto, String token) {
        Long userId = jwtUtil.extractMemberId(token.replace("Bearer ", ""));

        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Board board = new Board(requestDto.getTitle(), member);
        boardRepository.save(board);

        List<BoardContent> contentList = new ArrayList<>();
        List<BoardMedia> mediaList = new ArrayList<>();
        List<BoardOrder> orderList = new ArrayList<>();
        int orderIndex = 1;

        for (BoardCreateRequestDto.ContentDto contentDto : requestDto.getContents()) {
            if (contentDto.getContentData() != null) {
                BoardContent content = new BoardContent(board, contentDto.getContentData());
                boardContentRepository.save(content);
                contentList.add(content);
                orderList.add(new BoardOrder(board, content, null, orderIndex++));
            } else if (contentDto.getMediaUrl() != null) {
                BoardMedia media = new BoardMedia(board, contentDto.getMediaUrl(), BoardMedia.MediaType.valueOf(contentDto.getMediaType()));
                boardMediaRepository.save(media);
                mediaList.add(media);
                orderList.add(new BoardOrder(board, null, media, orderIndex++));
            }
        }

        boardOrderRepository.saveAll(orderList);

        board.setTitleContent(contentList.isEmpty() ? null : contentList.get(0).getContentData());
        board.setTitleImage(mediaList.stream()
                .filter(m -> m.getMediaType() == BoardMedia.MediaType.IMAGE)
                .map(BoardMedia::getMediaUrl)
                .findFirst()
                .orElse(null));

        return getBoardDetailDto(board);
    }

    /**
     * 게시글 상세 조회
     */
    @Transactional(readOnly = true)
    public BoardDetailDto getBoardDetail(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        return getBoardDetailDto(board);
    }

    /**
     * 게시글 전체 조회 ( 게시물의 대해서는 타이틀 이미지, 타이틀 텍스트만 포함)
     */
    @Transactional(readOnly = true)
    public List<BoardListResponseDto> getBoardList() {
        return boardRepository.findAll().stream()
                .map(this::convertToBoardListResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Board → BoardListResponseDto 변환
     */
    private BoardListResponseDto convertToBoardListResponseDto(Board board) {
        BoardListResponseDto dto = new BoardListResponseDto();
        dto.setId(board.getId());
        dto.setTitle(board.getTitle());
        dto.setTitleImage(board.getTitleImage());
        dto.setTitleContent(board.getTitleContent());
        dto.setWriteDatetime(board.getWriteDatetime());

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
     * Board→ BoardDetailDto 변환
     */
    private BoardDetailDto getBoardDetailDto(Board board) {
        BoardDetailDto responseDto = new BoardDetailDto();
        responseDto.setId(board.getId());
        responseDto.setTitle(board.getTitle());
        responseDto.setTitleImage(board.getTitleImage());
        responseDto.setTitleContent(board.getTitleContent());
        responseDto.setWriteDatetime(board.getWriteDatetime());
        responseDto.setFavoriteCount(board.getFavoriteCount());
        responseDto.setCommentCount(board.getCommentCount());
        responseDto.setViewCount(board.getViewCount());

        if (board.getAuthor() != null) {
            BoardDetailDto.AuthorDto authorDto = new BoardDetailDto.AuthorDto();
            authorDto.setId(board.getAuthor().getId());
            authorDto.setNickname(board.getAuthor().getNickName());
            authorDto.setProfileImageUrl(board.getAuthor().getProfileImageUrl());
            responseDto.setAuthor(authorDto);
        }

        List<BoardOrder> orders = boardOrderRepository.findByBoardIdOrderByOrderIndexAsc(board.getId());
        List<BoardDetailDto.ContentDto> contents = new ArrayList<>();

        for (BoardOrder order : orders) {
            BoardDetailDto.ContentDto contentDto = new BoardDetailDto.ContentDto();
            contentDto.setOrderIndex(order.getOrderIndex());

            if (order.getContent() != null) {
                contentDto.setContentData(order.getContent().getContentData());
            } else if (order.getMedia() != null) {
                contentDto.setMediaUrl(order.getMedia().getMediaUrl());
                contentDto.setMediaType(order.getMedia().getMediaType().name());
            }

            contents.add(contentDto);
        }

        responseDto.setContents(contents);
        return responseDto;
    }
}