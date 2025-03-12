package pawparazzi.back.likes.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LikeToggleResponseDto {
    private Long memberId;
    private Long boardId;
    private boolean liked;
    private int favoriteCount;
}