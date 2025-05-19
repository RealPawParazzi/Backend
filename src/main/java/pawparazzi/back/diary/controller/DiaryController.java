package pawparazzi.back.diary.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pawparazzi.back.diary.dto.DiaryResponse;
import pawparazzi.back.diary.dto.DiaryCreateRequest;
import pawparazzi.back.diary.service.DiaryService;
import pawparazzi.back.global.response.ApiResponse;
import pawparazzi.back.security.user.CustomUserDetails;

@RestController
@RequestMapping("/api/diary")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;

    // 일기 생성
    @PostMapping()
    public ResponseEntity<ApiResponse<DiaryResponse>> createDiary(@RequestBody DiaryCreateRequest request, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberId = userDetails.getMember().getId();
        DiaryResponse responseDto = diaryService.generateAndSaveDiaryEntry(request.getContent(), memberId, request.getTitle());
        return ResponseEntity.ok(ApiResponse.ok("일기가 성공적으로 등록되었습니다.", responseDto));
    }


    // 내 일기 전체 조회 (GET /api/diary/my)
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<java.util.List<DiaryResponse>>> getAllDiaries(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberId = userDetails.getMember().getId();
        java.util.List<DiaryResponse> diaries = diaryService.getAllDiaries(memberId);
        return ResponseEntity.ok(ApiResponse.ok("내 일기 전체 조회 성공", diaries));
    }

    // 일기 상세 조회 (GET /api/diary/{diaryId})
    @GetMapping("/{diaryId}")
    public ResponseEntity<ApiResponse<DiaryResponse>> getDiaryById(
            @PathVariable Long diaryId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberId = userDetails.getMember().getId();
        DiaryResponse response = diaryService.getDiaryById(diaryId, memberId);
        return ResponseEntity.ok(ApiResponse.ok("일기 조회 성공", response));
    }


    // 일기 삭제 (DELETE /api/diary/{diaryId})
    @DeleteMapping("/{diaryId}")
    public ResponseEntity<ApiResponse<String>> deleteDiary(
            @PathVariable Long diaryId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberId = userDetails.getMember().getId();
        diaryService.deleteDiary(diaryId, memberId);
        return ResponseEntity.ok(ApiResponse.ok("일기가 성공적으로 삭제되었습니다.", "null"));
    }
}
