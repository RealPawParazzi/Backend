package pawparazzi.back.inquiry.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pawparazzi.back.inquiry.entity.Inquiry;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InquiryDetailResponse {
    private Long id;
    private String title;
    private String content;
    private boolean answered;
    private String answer;
    private LocalDateTime createdAt;

    public static InquiryDetailResponse from(Inquiry inquiry) {
        return InquiryDetailResponse.builder()
                .id(inquiry.getId())
                .title(inquiry.getTitle())
                .content(inquiry.getContent())
                .answered(inquiry.isAnswered())
                .answer(inquiry.getAnswer())
                .createdAt(inquiry.getCreatedAt())
                .build();
    }
}
