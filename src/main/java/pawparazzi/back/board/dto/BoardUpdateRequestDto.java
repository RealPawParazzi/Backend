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
@NoArgsConstructor
@AllArgsConstructor
// 게시물 수정 RequestDto
public class BoardUpdateRequestDto {
    @NotBlank
    private String title;

    private BoardVisibility visibility;

    private String titleImage;

    private String titleContent;

    private List<ContentDto> contents;

    private List<String> deleteMediaUrls;

    private String tag;


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContentDto {
        private String type;
        private String value;
    }
}