package pawparazzi.back.backoffice.dto.inquiry;

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
public class InquiryResponse {
    private Long id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private boolean answered;
    private String answer;
    private Long memberId;
    private String memberName;
    private String memberEmail;

    public static InquiryResponse from(Inquiry inquiry) {
        return InquiryResponse.builder()
                .id(inquiry.getId())
                .title(inquiry.getTitle())
                .content(inquiry.getContent())
                .createdAt(inquiry.getCreatedAt())
                .answered(inquiry.isAnswered())
                .answer(inquiry.getAnswer())
                .memberId(inquiry.getMember().getId())
                .memberName(inquiry.getMember().getName())
                .memberEmail(inquiry.getMember().getEmail())
                .build();
    }
}
