package pawparazzi.back.board.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class BoardCreateRequestDto {
    private String title; // 게시글 제목
    private List<ContentDto> contents; // 본문 및 미디어 리스트

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContentDto {
        private String type;
        private String value;
    }
}