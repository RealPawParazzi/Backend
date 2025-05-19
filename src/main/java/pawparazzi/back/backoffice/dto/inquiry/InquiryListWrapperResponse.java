package pawparazzi.back.backoffice.dto.inquiry;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pawparazzi.back.inquiry.entity.Inquiry;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InquiryListWrapperResponse {

    private long totalInquiryCount;
    private List<InquiryListItem> inquiries;

    public static InquiryListWrapperResponse from(List<Inquiry> inquiries) {
        List<InquiryListItem> inquiryList = inquiries.stream()
                .map(InquiryListItem::from)
                .collect(Collectors.toList());
        return new InquiryListWrapperResponse(inquiryList.size(), inquiryList);
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InquiryListItem {
        private Long id;
        private String title;
        private Long writerId;
        private String writerName;
        private String writerEmail;
        private boolean answered;

        public static InquiryListItem from(Inquiry inquiry) {
            return new InquiryListItem(
                    inquiry.getId(),
                    inquiry.getTitle(),
                    inquiry.getMember().getId(),
                    inquiry.getMember().getName(),
                    inquiry.getMember().getEmail(),
                    inquiry.isAnswered()
            );
        }
    }
}
