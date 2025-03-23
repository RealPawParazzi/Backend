package pawparazzi.back.board.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;
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

    private String titleContent;

    private MultipartFile titleImage;

    private List<MultipartFile> mediaFiles;


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContentDto {
        private String type;
        private String value;
    }
}