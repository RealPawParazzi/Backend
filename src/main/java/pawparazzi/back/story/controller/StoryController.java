package pawparazzi.back.story.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pawparazzi.back.global.response.ApiResponse;
import pawparazzi.back.story.dto.response.StoryResponseDto;
import pawparazzi.back.story.dto.response.StoryViewListResponseDto;
import pawparazzi.back.story.dto.response.UserStoryGroupDto;
import pawparazzi.back.story.service.StoryService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import pawparazzi.back.security.user.CustomUserDetails;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stories")
@RequiredArgsConstructor
public class StoryController {

    @Autowired
    private StoryService storyService;

    /**
     * 스토리 등록
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Map<String, Long>>> createStory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart(value = "caption", required = false) String caption,
            @RequestPart("mediaFile") MultipartFile mediaFile) {

        Long memberId = userDetails.getId();
        Long storyId = storyService.createStory(memberId, caption, mediaFile);

        return ResponseEntity.ok(
                ApiResponse.ok("스토리가 성공적으로 등록되었습니다.", Map.of("storyId", storyId))
        );
    }

    /**
     * 스토리 상세 조회
     */
    @GetMapping("/{storyId}")
    public ResponseEntity<ApiResponse<StoryResponseDto>> viewStory(
            @PathVariable Long storyId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long viewerId = userDetails.getId();
        StoryResponseDto response = storyService.viewStory(storyId, viewerId);

        return ResponseEntity.ok(ApiResponse.ok("스토리를 성공적으로 조회했습니다.", response));
    }

    /**
     * 사용자별 스토리 그룹 조회 (스토리를 올린 사용자만)
     */
    @GetMapping()
    public ResponseEntity<ApiResponse<List<UserStoryGroupDto>>> getGroupedStories(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long viewerId = userDetails.getId();
        List<UserStoryGroupDto> groupedStories = storyService.getGroupedStories(viewerId);
        return ResponseEntity.ok(ApiResponse.ok("사용자별 스토리 조회 성공", groupedStories));
    }

    /**
     * 나의 스토리 조회
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<StoryResponseDto>>> getMyStories(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long memberId = userDetails.getId();
        List<StoryResponseDto> stories = storyService.getMyStories(memberId);
        return ResponseEntity.ok(ApiResponse.ok("나의 스토리 조회 성공", stories));
    }

    /**
     * 특정 스토리를 본 사용자 닉네임 및 프로필 이미지 목록 조회
     */
    @GetMapping("/{storyId}/viewers")
    public ResponseEntity<ApiResponse<StoryViewListResponseDto>> getViewersOfStory(
            @PathVariable Long storyId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long requesterId = userDetails.getId();
        if (!storyService.isStoryOwner(storyId, requesterId)) {
            return ResponseEntity.status(403).body(ApiResponse.error(403, "해당 스토리에 대한 권한이 없습니다."));
        }

        StoryViewListResponseDto result = storyService.getViewersOfStory(storyId);
        return ResponseEntity.ok(ApiResponse.ok("스토리를 본 사용자 목록 조회 성공", result));
    }
    /**
     * 스토리 삭제
     */
    @DeleteMapping("/{storyId}")
    public ResponseEntity<ApiResponse<Void>> deleteStory(
            @PathVariable Long storyId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long memberId = userDetails.getId();
        storyService.deleteStory(storyId, memberId);

        return ResponseEntity.ok(ApiResponse.ok("스토리가 성공적으로 삭제되었습니다.", null));
    }

    /**
     * 스토리 수정
     */
    @PutMapping(value = "/{storyId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Map<String, Long>>> updateStory(
            @PathVariable Long storyId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart(value = "caption", required = false) String caption,
            @RequestPart(value = "mediaFile", required = false) MultipartFile mediaFile) {

        Long memberId = userDetails.getId();
        Long updatedStoryId = storyService.updateStory(storyId, memberId, caption, mediaFile).getId();
        return ResponseEntity.ok(ApiResponse.ok("스토리 수정 성공", Map.of("storyId", updatedStoryId)));
    }
}
