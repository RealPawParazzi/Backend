package pawparazzi.back.story.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pawparazzi.back.story.service.StoryService;

@Component
@RequiredArgsConstructor
public class StoryScheduler {

    private final StoryService storyService;

    @Scheduled(fixedRate = 86_400_000) // 24시간마다 실행
    public void runExpireOldStories() {
        storyService.expireOldStories();
    }
}