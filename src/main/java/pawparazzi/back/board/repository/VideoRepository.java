package pawparazzi.back.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pawparazzi.back.board.entity.Video;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {
}
