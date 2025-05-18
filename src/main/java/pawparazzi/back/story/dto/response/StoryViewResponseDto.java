package pawparazzi.back.story.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class StoryViewResponseDto {
    private Long viewerId;
    private String viewerNickname;
    private String viewerProfileImageUrl;
    private LocalDateTime viewedAt;
}