package pawparazzi.back.video.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoRequestDto {
    @NotBlank(message = "프롬프트는 필수입니다")
    @Size(min = 10, max = 500, message = "프롬프트는 10자에서 500자 사이여야 합니다.")
    private String prompt;

    private String imageUrl;

    @NotNull(message = "영상 길이를 선택하세요")
    @Min(value = 5, message = "영상 길이는 최소 5초입니다.")
    @Max(value = 10, message = "영상 길이는 최대 10초입니다.")
    private Integer duration = 5; // 기본값 5로 설정

    private String additionalOptions;
}
