package pawparazzi.back.board.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
// 게시물 수정 RequestDto
public class BoardUpdateRequestDto {
    @NotBlank
    private String title;

    private String titleImage;

    private List<ContentDto> contents;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContentDto {
        private String type;
        private String value;
    }
}