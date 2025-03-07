package pawparazzi.back.comment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReplyLikeResponseDto {
    private boolean liked;
    private int likeCount;
}