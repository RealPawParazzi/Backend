package pawparazzi.back.story.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pawparazzi.back.story.entity.StoryLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@Repository
public interface StoryLogRepository extends JpaRepository<StoryLog, Long> {
    @Query("SELECT MONTH(s.createdAt), COUNT(s) FROM Story s GROUP BY MONTH(s.createdAt)")
    List<Object[]> countStoriesGroupedByMonth();
}
