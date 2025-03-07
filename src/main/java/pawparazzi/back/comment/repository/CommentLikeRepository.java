package pawparazzi.back.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pawparazzi.back.comment.entity.Comment;
import pawparazzi.back.comment.entity.CommentLike;
import pawparazzi.back.member.entity.Member;

import java.util.List;
import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    Optional<CommentLike> findByCommentAndMember(Comment comment, Member member);
    void deleteByCommentId(Long commentId);
    List<CommentLike> findByComment(Comment comment);

    @Modifying
    @Query("DELETE FROM CommentLike cl WHERE cl.comment.id IN (SELECT c.id FROM Comment c WHERE c.board.id = :boardId)")
    void deleteByBoardId(@Param("boardId") Long boardId);
}