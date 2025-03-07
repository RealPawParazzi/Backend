package pawparazzi.back.comment.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReplyRequestDto {

    @NotBlank(message = "대댓글 내용은 필수 입력값입니다.")
    private String content;
}