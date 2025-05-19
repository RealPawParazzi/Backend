package pawparazzi.back.inquiry.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InquiryListResponse {
    private Long id;
    private String title;
    private boolean answered;
    private LocalDateTime createdAt;
}
