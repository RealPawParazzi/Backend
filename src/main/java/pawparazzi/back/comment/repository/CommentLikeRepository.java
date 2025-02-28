package pawparazzi.back.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pawparazzi.back.comment.entity.Comment;
import pawparazzi.back.comment.entity.CommentLike;
import pawparazzi.back.member.entity.Member;

import java.util.List;
import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    Optional<CommentLike> findByCommentAndMember(Comment comment, Member member);
    void deleteByCommentId(Long commentId);
    List<CommentLike> findByComment(Comment comment);
}