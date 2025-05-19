package pawparazzi.back.backoffice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pawparazzi.back.backoffice.dto.inquiry.InquiryListWrapperResponse;
import pawparazzi.back.backoffice.dto.inquiry.InquiryResponse;
import pawparazzi.back.inquiry.entity.Inquiry;
import pawparazzi.back.inquiry.repository.InquiryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class inquiryBackService {

    private final InquiryRepository inquiryRepository;

    public InquiryListWrapperResponse getAllInquiries() {
        List<Inquiry> inquiries = inquiryRepository.findAll();
        return InquiryListWrapperResponse.from(inquiries);
    }

    public InquiryResponse getInquiryDetailForAdmin(Long inquiryId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("문의글을 찾을 수 없습니다."));
        return InquiryResponse.from(inquiry);
    }

    public void deleteInquiryByAdmin(Long inquiryId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("문의글을 찾을 수 없습니다."));
        inquiryRepository.delete(inquiry);
    }

    public void answerInquiry(Long inquiryId, String answer) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("문의글을 찾을 수 없습니다."));
        inquiry.setAnswer(answer);
        inquiry.setAnswered(true);
        inquiryRepository.save(inquiry);
    }
}
