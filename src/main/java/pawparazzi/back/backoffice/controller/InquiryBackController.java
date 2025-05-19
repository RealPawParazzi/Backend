package pawparazzi.back.backoffice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pawparazzi.back.backoffice.dto.inquiry.InquiryListWrapperResponse;
import pawparazzi.back.backoffice.dto.inquiry.InquiryResponse;
import pawparazzi.back.backoffice.service.inquiryBackService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/inquiry")
@RequiredArgsConstructor
public class InquiryBackController {

    private final inquiryBackService inquiryService;

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
        return Map.of("message", "Inquiry deleted successfully");
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
