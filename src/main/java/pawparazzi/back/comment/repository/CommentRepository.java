package pawparazzi.back.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pawparazzi.back.comment.entity.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {
}
