package pawparazzi.back.comment.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pawparazzi.back.comment.dto.response.ReplyLikeResponseDto;
import pawparazzi.back.comment.dto.response.ReplyLikesResponseDto;
import pawparazzi.back.comment.entity.Reply;
import pawparazzi.back.comment.entity.ReplyLike;
import pawparazzi.back.comment.repository.ReplyLikeRepository;
import pawparazzi.back.comment.repository.ReplyRepository;
import pawparazzi.back.member.entity.Member;
import pawparazzi.back.member.repository.MemberRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReplyLikeService {

    private final ReplyLikeRepository replyLikeRepository;
    private final ReplyRepository replyRepository;
    private final MemberRepository memberRepository;

    /**
     * 대댓글 좋아요 등록/삭제 (토글)
     */
    @Transactional
    public ReplyLikeResponseDto toggleReplyLike(Long replyId, Long memberId) {
        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new EntityNotFoundException("대댓글을 찾을 수 없습니다."));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        ReplyLike existingLike = replyLikeRepository.findByReplyAndMember(reply, member).orElse(null);

        boolean liked;
        if (existingLike != null) {
            replyLikeRepository.delete(existingLike);
            reply.decreaseLikeCount();
            liked = false;
        } else {
            replyLikeRepository.save(new ReplyLike(reply, member));
            reply.increaseLikeCount();
            liked = true;
        }

        return new ReplyLikeResponseDto(liked, reply.getLikeCount());
    }

    /**
     * 특정 대댓글에 좋아요를 누른 회원 목록 조회
     */
    @Transactional(readOnly = true)
    public ReplyLikesResponseDto getLikedMembersByReply(Long replyId) {
        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new EntityNotFoundException("대댓글을 찾을 수 없습니다."));

        List<ReplyLike> replyLikes = replyLikeRepository.findByReply(reply);

        List<ReplyLikesResponseDto.LikedMemberDto> likedMembers = replyLikes.stream()
                .map(like -> new ReplyLikesResponseDto.LikedMemberDto(
                        like.getMember().getId(),
                        like.getMember().getNickName(),
                        like.getMember().getProfileImageUrl()
                ))
                .collect(Collectors.toList());

        return new ReplyLikesResponseDto(replyId, likedMembers.size(), likedMembers);
    }

    /**
     * 대댓글에 달린 좋아요 삭제 (대댓글 삭제 시 호출)
     */
    @Transactional
    public void deleteReplyLikes(Long replyId) {
        replyLikeRepository.deleteByReplyId(replyId);
    }
}