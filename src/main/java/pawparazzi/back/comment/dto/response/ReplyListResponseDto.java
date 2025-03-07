package pawparazzi.back.comment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ReplyListResponseDto {
    private Long commentId;
    private int replyCount;
    private List<ReplyResponseDto> replies;
}