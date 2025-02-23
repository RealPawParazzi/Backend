package pawparazzi.back.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pawparazzi.back.board.entity.Board;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {
}
