package pawparazzi.back.video.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoResponseViewDto {
    private Long requestId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String prompt;
    private String imageUrl;
    private String resultUrl;
}
