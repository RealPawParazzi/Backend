package pawparazzi.back.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pawparazzi.back.board.entity.BoardMedia;

@Repository
public interface BoardMediaRepository extends JpaRepository<BoardMedia, Long> {
}