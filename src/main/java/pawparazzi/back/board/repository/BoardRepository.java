package pawparazzi.back.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pawparazzi.back.board.entity.Board;
import pawparazzi.back.member.entity.Member;

import java.util.List;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {
    List<Board> findByAuthor(Member member);

    @Query("SELECT MONTH(b.writeDatetime), COUNT(b) FROM Board b GROUP BY MONTH(b.writeDatetime)")
    List<Object[]> countBoardsGroupedByMonth();
}