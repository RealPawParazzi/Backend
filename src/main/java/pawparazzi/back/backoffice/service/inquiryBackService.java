package pawparazzi.back.backoffice.service;

import pawparazzi.back.backoffice.dto.inquiry.InquiryListWrapperResponse;
import pawparazzi.back.backoffice.dto.inquiry.InquiryResponse;

public interface inquiryBackService {
    InquiryListWrapperResponse getAllInquiries();
    InquiryResponse getInquiryDetailForAdmin(Long inquiryId);
    void deleteInquiryByAdmin(Long inquiryId);
    void answerInquiry(Long inquiryId, String answer);
}
