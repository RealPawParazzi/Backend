package pawparazzi.back.story.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pawparazzi.back.story.entity.Story;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

@Repository
public interface StoryRepository extends JpaRepository<Story, Long> {

    Optional<Story> findById(Long id);

    // 만료되지 않은 스토리 조회
    Optional<Story> findByIdAndExpiredFalse(Long id);

    // 특정 작성자의 만료되지 않은 스토리 전체 조회
    List<Story> findByMemberIdAndExpiredFalse(Long memberId);

    List<Story> findByExpiredFalse();

    List<Story> findByExpiredFalseAndCreatedAtBefore(LocalDateTime cutoff);
}