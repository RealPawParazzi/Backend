package pawparazzi.back.comment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CommentListResponseDto {
    private Long boardId;
    private int commentCount;
    private List<CommentResponseDto> comments;
}