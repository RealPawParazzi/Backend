package pawparazzi.back.board.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pawparazzi.back.board.entity.BoardVisibility;

import java.util.List;

@Getter
@Setter
// 게시물 생성 RequestDto
public class BoardCreateRequestDto {

    @NotBlank(message = "제목은 필수 입력값입니다.")
    private String title;

    @NotBlank
    private BoardVisibility visibility;

    private List<ContentDto> contents;

    private String titleImage;

    private String titleContent;


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContentDto {
        private String type;
        private String value;
    }
}