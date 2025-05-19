package pawparazzi.back.diary.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiaryResponse {
    private Long diaryId;
    private String title;
    private String content;
    private LocalDateTime createdAt;
}