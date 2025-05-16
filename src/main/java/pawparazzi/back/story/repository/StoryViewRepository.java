package pawparazzi.back.story.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pawparazzi.back.story.entity.StoryView;

import java.util.List;

@Repository
public interface StoryViewRepository extends JpaRepository<StoryView, Long> {

    // 중복 조회 방지
    boolean existsByStoryIdAndViewerId(Long storyId, Long viewerId);

    // 특정 스토리를 본 모든 사용자 조회
    List<StoryView> findAllByStoryId(Long storyId);

    // 특정 스토리에 대한 모든 조회 기록 삭제
    void deleteAllByStoryId(Long storyId);
}