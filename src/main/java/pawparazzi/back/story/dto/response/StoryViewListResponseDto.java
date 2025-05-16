package pawparazzi.back.story.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class StoryViewListResponseDto {
    private Long viewCount;
    private List<StoryViewResponseDto> viewers;
}
