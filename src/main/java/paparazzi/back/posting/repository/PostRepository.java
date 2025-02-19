package paparazzi.back.posting.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import paparazzi.back.posting.entity.Posting;
import paparazzi.back.posting.entity.Visibility;
import java.util.List;

public interface PostRepository extends JpaRepository<Posting, Long> {
    //공개 게시물 조회
    List<Posting> findByVisibility(Visibility visibility, Sort sort);
}
