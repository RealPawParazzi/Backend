package pawparazzi.back.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pawparazzi.back.board.entity.BoardContent;

@Repository
public interface BoardContentRepository extends JpaRepository<BoardContent, Long> {
}