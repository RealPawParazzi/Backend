package pawparazzi.back.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
public class CommentResponseWrapperDto {
    private Long boardId;
    private int commentCount;
    private List<CommentResponseDto> comments;
}