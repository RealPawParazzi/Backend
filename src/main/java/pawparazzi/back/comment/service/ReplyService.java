package pawparazzi.back.comment.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pawparazzi.back.comment.entity.Comment;
import pawparazzi.back.comment.repository.CommentRepository;
import pawparazzi.back.member.entity.Member;
import pawparazzi.back.member.repository.MemberRepository;
import pawparazzi.back.comment.dto.request.ReplyRequestDto;
import pawparazzi.back.comment.dto.response.ReplyListResponseDto;
import pawparazzi.back.comment.dto.response.ReplyResponseDto;
import pawparazzi.back.comment.entity.Reply;
import pawparazzi.back.comment.repository.ReplyRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReplyService {

    private final ReplyRepository replyRepository;
    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * 대댓글 작성
     */
    @Transactional
    public ReplyResponseDto createReply(Long commentId, Long memberId, ReplyRequestDto requestDto) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다."));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        Reply reply = new Reply(comment, member, requestDto.getContent());
        replyRepository.save(reply);

        comment.increaseReplyCount();
        entityManager.flush();
        entityManager.refresh(comment);

        return convertToDto(reply);
    }

    /**
     * 대댓글 수정
     */
    @Transactional
    public ReplyResponseDto updateReply(Long replyId, Long memberId, String content) {
        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new EntityNotFoundException("대댓글을 찾을 수 없습니다."));

        if (!reply.getMember().getId().equals(memberId)) {
            throw new IllegalArgumentException("본인이 작성한 대댓글만 수정할 수 있습니다.");
        }

        reply.updateContent(content);
        return convertToDto(reply);
    }

    /**
     * 대댓글 삭제
     */
    @Transactional
    public void deleteReply(Long replyId, Long memberId) {
        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new EntityNotFoundException("대댓글을 찾을 수 없습니다."));

        Comment comment = reply.getComment();

        Long commentAuthorId = comment.getMember().getId();
        Long replyAuthorId = reply.getMember().getId();

        if (!replyAuthorId.equals(memberId) && !commentAuthorId.equals(memberId)) {
            throw new IllegalArgumentException("대댓글 삭제 권한이 없습니다.");
        }

        replyRepository.delete(reply);

        if (comment.getReplyCount() > 0) {
            comment.decreaseReplyCount();
        }

        entityManager.persist(comment);
        entityManager.flush();
        entityManager.refresh(comment);
    }

    /**
     * 특정 댓글의 대댓글 목록 조회
     */
    @Transactional(readOnly = true)
    public ReplyListResponseDto getRepliesByComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다."));

        List<Reply> replies = replyRepository.findByComment(comment);
        List<ReplyResponseDto> replyDtos = replies.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return new ReplyListResponseDto(commentId, replies.size(), replyDtos);
    }

    /**
     * 엔티티 → DTO 변환
     */
    private ReplyResponseDto convertToDto(Reply reply) {
        return ReplyResponseDto.builder()
                .replyId(reply.getId())
                .replyMember(new ReplyResponseDto.MemberDto(
                        reply.getMember().getId(),
                        reply.getMember().getNickName(),
                        reply.getMember().getProfileImageUrl()
                ))
                .content(reply.getContent())
                .likeCount(reply.getLikeCount())

                .createdAt(reply.getCreatedAt())
                .updatedAt(reply.getUpdatedAt())
                .build();
    }
}