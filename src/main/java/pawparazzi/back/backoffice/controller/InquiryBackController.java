package pawparazzi.back.backoffice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pawparazzi.back.backoffice.dto.inquiry.InquiryListWrapperResponse;
import pawparazzi.back.backoffice.dto.inquiry.InquiryResponse;
import pawparazzi.back.backoffice.service.inquiryBackServiceImpl;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/inquiry")
@RequiredArgsConstructor
public class InquiryBackController {

    @Autowired
    private final inquiryBackServiceImpl inquiryService;

    @GetMapping("/list")
    public InquiryListWrapperResponse getAllInquiries() {
        return inquiryService.getAllInquiries();
    }

    @GetMapping("/{inquiryId}")
    public InquiryResponse getInquiryDetail(@PathVariable Long inquiryId) {
        return inquiryService.getInquiryDetailForAdmin(inquiryId);
    }

    @DeleteMapping("/{inquiryId}/delete")
    public Map<String, String> deleteInquiry(@PathVariable Long inquiryId) {
        inquiryService.deleteInquiryByAdmin(inquiryId);
        return Map.of("message", "답변 삭제를 성공하였습니다.");
    }

    @PostMapping("/{inquiryId}/answer")
    public Map<String, String> answerInquiry(
            @PathVariable Long inquiryId,
            @RequestBody Map<String, String> request) {

        String answer = request.get("answer");
        inquiryService.answerInquiry(inquiryId, answer);
        return Map.of("message", "답변이 등록되었습니다.");
    }
}
