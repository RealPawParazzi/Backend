package pawparazzi.back.comment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReplyLikeResponseDto {
    private final Long memberId;
    private final Long replyId;
    private final boolean liked;
    private final int replyLikeCount;
}