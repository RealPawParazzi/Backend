package pawparazzi.back.video.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BattleVideoResponseDto {
    private Long requestId;
    private String jobId;
    private String status;
    private String resultUrl;
    private Integer duration;
    private String errorMessage;
    private String imageUrl1;
    private String imageUrl2;
}
