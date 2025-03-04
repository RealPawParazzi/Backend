package pawparazzi.back.comment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pawparazzi.back.comment.dto.request.ReplyRequestDto;
import pawparazzi.back.comment.dto.response.ReplyResponseDto;
import pawparazzi.back.comment.dto.response.ReplyListResponseDto;
import pawparazzi.back.comment.service.ReplyService;
import pawparazzi.back.security.util.JwtUtil;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/replies")
@RequiredArgsConstructor
public class ReplyController {

    private final ReplyService replyService;
    private final JwtUtil jwtUtil;

    /**
     * 대댓글 작성
     */
    @PostMapping("/{commentId}")
    public ResponseEntity<ReplyResponseDto> createReply(
            @PathVariable Long commentId,
            @RequestHeader("Authorization") String token,
            @RequestBody @Valid ReplyRequestDto requestDto) {

        Long memberId = jwtUtil.extractMemberId(token.replace("Bearer ", ""));
        ReplyResponseDto response = replyService.createReply(commentId, memberId, requestDto);
        return ResponseEntity.ok(response);
    }

    /**
     * 대댓글 수정
     */
    @PutMapping("/{replyId}")
    public ResponseEntity<ReplyResponseDto> updateReply(
            @PathVariable Long replyId,
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, String> request) {

        Long memberId = jwtUtil.extractMemberId(token.replace("Bearer ", ""));
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
            @RequestHeader("Authorization") String token) {

        Long memberId = jwtUtil.extractMemberId(token.replace("Bearer ", ""));
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
}