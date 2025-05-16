package pawparazzi.back.story.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class UserStoryGroupDto {
    private Long memberId;
    private String nickname;
    private String profileImageUrl;
    private List<StoryDto> stories;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class StoryDto {
        private Long storyId;
        private String mediaUrl;
        private String caption;
        private LocalDateTime createdAt;
        private boolean expired;
        private boolean viewed;
    }
}