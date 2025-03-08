package pawparazzi.back.comment.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pawparazzi.back.comment.entity.Comment;
import pawparazzi.back.comment.entity.Reply;

import java.util.List;

@Repository
public interface ReplyRepository extends JpaRepository<Reply, Long> {
    List<Reply> findByComment(Comment comment);

    @Query("SELECT r FROM Reply r WHERE r.comment.id IN (SELECT c.id FROM Comment c WHERE c.board.id = :boardId)")
    List<Reply> findByBoardId(Long boardId);

    List<Reply> findByCommentId(Long commentId);

    @Transactional
    void deleteByCommentId(Long commentId);

    @Transactional
    @Modifying
    @Query("DELETE FROM Reply r WHERE r.comment.id IN (SELECT c.id FROM Comment c WHERE c.board.id = :boardId)")
    void deleteByBoardId(Long boardId);
}