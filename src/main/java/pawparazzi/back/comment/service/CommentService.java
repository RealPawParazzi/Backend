package pawparazzi.back.comment.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pawparazzi.back.board.entity.Board;
import pawparazzi.back.board.repository.BoardRepository;
import pawparazzi.back.comment.dto.CommentRequestDto;
import pawparazzi.back.comment.dto.CommentResponseDto;
import pawparazzi.back.comment.dto.CommentResponseDto.MemberDto;
import pawparazzi.back.comment.dto.CommentResponseWrapperDto;
import pawparazzi.back.comment.entity.Comment;
import pawparazzi.back.comment.repository.CommentRepository;
import pawparazzi.back.member.entity.Member;
import pawparazzi.back.member.repository.MemberRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;


    /**
     * 댓글 작성
     */
    @Transactional
    public CommentResponseDto createComment(Long boardId, Long memberId, CommentRequestDto requestDto) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        Comment comment = new Comment(board, member, requestDto.getContent());
        commentRepository.save(comment);

        board.increaseCommentCount();
        boardRepository.save(board);

        return convertToDto(comment);
    }

    /**
     * 댓글 수정
     */
    @Transactional
    public CommentResponseDto updateComment(Long commentId, Long memberId, String content) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다."));

        if (!comment.getMember().getId().equals(memberId)) {
            throw new IllegalArgumentException("본인이 작성한 댓글만 수정할 수 있습니다.");
        }

        comment.updateContent(content);
        return convertToDto(comment);
    }

    /**
     * 댓글 삭제
     */
    @Transactional
    public void deleteComment(Long commentId, Long memberId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다."));

        Board board = comment.getBoard();

        Long boardAuthorId = board.getAuthor().getId();
        Long commentAuthorId = comment.getMember().getId();

        // 댓글 작성자 또는 게시글 작성자만 삭제 가능
        if (!commentAuthorId.equals(memberId) && !boardAuthorId.equals(memberId)) {
            throw new IllegalArgumentException("댓글 삭제 권한이 없습니다.");
        }

        commentRepository.delete(comment);

        board.decreaseCommentCount();
        boardRepository.save(board);
    }

    /**
     * 특정 게시글의 댓글 목록 조회
     */
    @Transactional(readOnly = true)
    public CommentResponseWrapperDto getCommentsByBoard(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));

        List<Comment> comments = commentRepository.findByBoardOrderByCreatedAtAsc(board);

        List<CommentResponseDto> commentDto = comments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return new CommentResponseWrapperDto(board.getId(), board.getCommentCount(), commentDto);
    }

    /**
     * 엔티티 → DTO 변환
     */
    private CommentResponseDto convertToDto(Comment comment) {
        return CommentResponseDto.builder()
                .commentId(comment.getId())
                .commentMember(new CommentResponseDto.MemberDto(
                        comment.getMember().getId(),
                        comment.getMember().getNickName(),
                        comment.getMember().getProfileImageUrl()
                ))
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}