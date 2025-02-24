package pawparazzi.back.board.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BoardCreateRequestDto {

    private String title;
    private List<ContentDto> contents;

    @Getter
    @Setter
    public static class ContentDto {
        private String contentData;
        private String mediaUrl;
        private String mediaType;
    }
}