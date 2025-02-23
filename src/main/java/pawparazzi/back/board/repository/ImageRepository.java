package pawparazzi.back.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.relational.core.sql.In;
import org.springframework.stereotype.Repository;
import pawparazzi.back.board.entity.Image;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
}
