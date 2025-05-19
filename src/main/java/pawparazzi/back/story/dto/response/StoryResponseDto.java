package pawparazzi.back.story.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoryResponseDto {
    private Long storyId;
    private Long memberId;
    private String mediaUrl;
    private String caption;
    private LocalDateTime createdAt;
    private boolean expired;

    public static StoryResponseDto of(pawparazzi.back.story.entity.Story story) {
        return StoryResponseDto.builder()
                .storyId(story.getId())
                .memberId(story.getMember().getId())
                .mediaUrl(story.getMediaUrl())
                .caption(story.getCaption())
                .createdAt(story.getCreatedAt())
                .expired(story.isExpired())
                .build();
    }
}

