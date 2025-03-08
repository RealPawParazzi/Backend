package pawparazzi.back.comment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class CommentLikeResponseDto {
    private final Long memberId;
    private final Long commentId;
    private final boolean liked;
    private final int commentsLikeCount;
}