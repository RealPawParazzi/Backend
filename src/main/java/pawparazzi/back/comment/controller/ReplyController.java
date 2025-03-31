package pawparazzi.back.comment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import pawparazzi.back.comment.dto.request.ReplyRequestDto;
import pawparazzi.back.comment.dto.response.ReplyLikeResponseDto;
import pawparazzi.back.comment.dto.response.ReplyLikesResponseDto;
import pawparazzi.back.comment.dto.response.ReplyResponseDto;
import pawparazzi.back.comment.dto.response.ReplyListResponseDto;
import pawparazzi.back.comment.service.ReplyLikeService;
import pawparazzi.back.comment.service.ReplyService;
import pawparazzi.back.security.user.CustomUserDetails;

import java.util.Map;

@RestController
@RequestMapping("/api/replies")
@RequiredArgsConstructor
public class ReplyController {

    private final ReplyService replyService;
    private final ReplyLikeService replyLikeService;

    /**
     * 대댓글 작성
     */
    @PostMapping("/{commentId}")
    public ResponseEntity<ReplyResponseDto> createReply(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid ReplyRequestDto requestDto) {
        Long memberId = userDetails.getId();
        ReplyResponseDto response = replyService.createReply(commentId, memberId, requestDto);
        return ResponseEntity.ok(response);
    }

    /**
     * 대댓글 수정
     */
    @PutMapping("/{replyId}")
    public ResponseEntity<ReplyResponseDto> updateReply(
            @PathVariable Long replyId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody Map<String, String> request) {
        Long memberId = userDetails.getId();
        String content = request.get("content");
        ReplyResponseDto response = replyService.updateReply(replyId, memberId, content);
        return ResponseEntity.ok(response);
    }

    /**
     * 대댓글 삭제
     */
    @DeleteMapping("/{replyId}")
    public ResponseEntity<Map<String, String>> deleteReply(
            @PathVariable Long replyId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberId = userDetails.getId();
        replyService.deleteReply(replyId, memberId);
        return ResponseEntity.ok(Map.of("message", "대댓글이 삭제되었습니다."));
    }

    /**
     * 특정 댓글의 대댓글 목록 조회
     */
    @GetMapping("/{commentId}")
    public ResponseEntity<ReplyListResponseDto> getReplies(@PathVariable Long commentId) {
        return ResponseEntity.ok(replyService.getRepliesByComment(commentId));
    }

    /**
     * 대댓글 좋아요 등록/삭제 (토글)
     */
    @PostMapping("/{replyId}/like")
    public ResponseEntity<ReplyLikeResponseDto> toggleReplyLike(
            @PathVariable Long replyId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long memberId = userDetails.getId();
        ReplyLikeResponseDto response = replyLikeService.toggleReplyLike(replyId, memberId);
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 대댓글에 좋아요를 누른 회원 목록 조회
     */
    @GetMapping("/{replyId}/likes")
    public ResponseEntity<ReplyLikesResponseDto> getLikedMembersByReply(@PathVariable Long replyId) {
        return ResponseEntity.ok(replyLikeService.getLikedMembersByReply(replyId));
    }
}