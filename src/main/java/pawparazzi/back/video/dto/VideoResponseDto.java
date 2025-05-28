package pawparazzi.back.video.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoResponseDto {
    private Long requestId;
    private String jobId;
    private String status;
    private String resultUrl;
    private Integer duration;
    private String errorMessage;
    private String imageUrl;
}
