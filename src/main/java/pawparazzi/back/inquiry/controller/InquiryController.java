package pawparazzi.back.inquiry.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pawparazzi.back.inquiry.dto.InquiryRequest;
import pawparazzi.back.exception.ApiResponse;
import pawparazzi.back.inquiry.service.InquiryService;
import pawparazzi.back.security.user.CustomUserDetails;

@RestController
@RequestMapping("/api/inquiry")
@RequiredArgsConstructor
public class InquiryController {

    @Autowired
    private final InquiryService inquiryService;

    @PostMapping
    public ResponseEntity<ApiResponse<String>> createInquiry(
            @RequestBody InquiryRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long memberId = userDetails.getId();
        inquiryService.createInquiry(request, memberId);
        return ResponseEntity.ok(new ApiResponse<>(200, "문의가 등록되었습니다.", null));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<?>> getMyInquiries(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long memberId = userDetails.getId();
        return ResponseEntity.ok(new ApiResponse<>(200, "나의 문의 목록 조회 성공", inquiryService.getMyInquiries(memberId)));
    }

    @GetMapping("/{inquiryId}")
    public ResponseEntity<ApiResponse<?>> getInquiryDetail(
            @PathVariable Long inquiryId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long memberId = userDetails.getId();
        var response = inquiryService.getInquiryDetail(inquiryId, memberId);
        return ResponseEntity.ok(new ApiResponse<>(200, "문의 상세 조회 성공", response));
    }


    @DeleteMapping("/{inquiryId}")
    public ResponseEntity<ApiResponse<String>> deleteInquiry(
            @PathVariable Long inquiryId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long memberId = userDetails.getId();
        inquiryService.deleteInquiry(inquiryId, memberId);
        return ResponseEntity.ok(new ApiResponse<>(200, "문의가 삭제되었습니다.", null));
    }
}
