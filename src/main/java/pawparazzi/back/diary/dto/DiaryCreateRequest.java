package pawparazzi.back.diary.dto;

import lombok.Data;

@Data
public class DiaryCreateRequest {
    private String title;
    private String content;
}
