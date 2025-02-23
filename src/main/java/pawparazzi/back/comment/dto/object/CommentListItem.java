package pawparazzi.back.comment.dto.object;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommentListItem {
    private String nickName;
    private String profileImage;
    private String writeDateTime;
    private String content;
}
