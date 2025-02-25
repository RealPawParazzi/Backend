package pawparazzi.back.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pawparazzi.back.board.entity.Board;
import pawparazzi.back.member.entity.Member;

import java.util.List;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {
    List<Board> findByAuthor(Member member);
    void deleteByAuthor(Member member);
}