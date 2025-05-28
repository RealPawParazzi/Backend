package pawparazzi.back.video.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoResponseViewDto {
    private Long requestId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String prompt;
    private List<String> imageUrl;
    private String resultUrl;
}
