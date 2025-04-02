package pawparazzi.back.comment.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pawparazzi.back.comment.dto.response.CommentLikeResponseDto;
import pawparazzi.back.comment.dto.response.CommentLikesResponseDto;
import pawparazzi.back.comment.entity.Comment;
import pawparazzi.back.comment.entity.CommentLike;
import pawparazzi.back.comment.repository.CommentLikeRepository;
import pawparazzi.back.comment.repository.CommentRepository;
import pawparazzi.back.member.entity.Member;
import pawparazzi.back.member.repository.MemberRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentLikeService {

    private final CommentLikeRepository commentLikeRepository;
    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;

    /**
     * 댓글 좋아요 등록/삭제 (토글)
     */
    @Transactional
    public CommentLikeResponseDto toggleCommentLike(Long commentId, Long memberId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다."));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        CommentLike existingLike = commentLikeRepository.findByCommentAndMember(comment, member).orElse(null);

        boolean liked;
        if (existingLike != null) {
            commentLikeRepository.delete(existingLike);
            comment.decreaseLikeCount();
            liked = false;
        } else {
            commentLikeRepository.save(new CommentLike(comment, member));
            comment.increaseLikeCount();
            liked = true;
        }

        return new CommentLikeResponseDto(memberId, commentId, liked, comment.getLikeCount());
    }


    /**
     * 특정 댓글에 좋아요를 누른 회원 목록 조회
     */
    @Transactional(readOnly = true)
    public CommentLikesResponseDto getLikedMembersByComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다."));

        List<CommentLike> commentLikes = commentLikeRepository.findByComment(comment);

        List<CommentLikesResponseDto.LikedMemberDto> likedMembers = commentLikes.stream()
                .map(like -> new CommentLikesResponseDto.LikedMemberDto(
                        like.getMember().getId(),
                        like.getMember().getNickName(),
                        like.getMember().getProfileImageUrl()
                ))
                .collect(Collectors.toList());

        return new CommentLikesResponseDto(commentId, likedMembers.size(), likedMembers);
    }

    /**
     * 댓글에 달린 좋아요 삭제 (댓글 삭제 시 호출)
     */
    @Transactional
    public void deleteCommentLikes(Long commentId) {
        commentLikeRepository.deleteByCommentId(commentId);
    }
}