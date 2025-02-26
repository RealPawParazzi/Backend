package pawparazzi.back.likes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pawparazzi.back.likes.entity.Like;
import pawparazzi.back.member.entity.Member;
import pawparazzi.back.board.entity.Board;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByBoardAndMember(Board board, Member member);
    List<Like> findByBoard(Board board);
}