package pawparazzi.back.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pawparazzi.back.board.entity.BoardOrder;

import java.util.List;

@Repository
public interface BoardOrderRepository extends JpaRepository<BoardOrder, Long> {
    List<BoardOrder> findByBoardIdOrderByOrderIndexAsc(Long boardId);
}