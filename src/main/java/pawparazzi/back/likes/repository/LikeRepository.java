package pawparazzi.back.likes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pawparazzi.back.likes.entity.Like;
import pawparazzi.back.member.entity.Member;
import pawparazzi.back.board.entity.Board;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByBoardAndMember(Board board, Member member);
    List<Like> findByBoard(Board board);

    @Modifying
    @Query("DELETE FROM Like l WHERE l.board.id = :boardId")
    void deleteByBoardId(@Param("boardId") Long boardId);
}