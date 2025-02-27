package pawparazzi.back.comment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pawparazzi.back.comment.dto.CommentRequestDto;
import pawparazzi.back.comment.dto.CommentResponseDto;
import pawparazzi.back.comment.dto.CommentResponseWrapperDto;
import pawparazzi.back.comment.service.CommentService;
import pawparazzi.back.security.util.JwtUtil;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final JwtUtil jwtUtil;

    /**
     * 댓글 작성
     */
    @PostMapping("/{boardId}")
    public ResponseEntity<CommentResponseDto> createComment(
            @PathVariable Long boardId,
            @RequestHeader("Authorization") String token,
            @RequestBody @Valid CommentRequestDto requestDto) { // DTO 적용

        Long memberId = jwtUtil.extractMemberId(token.replace("Bearer ", ""));
        CommentResponseDto response = commentService.createComment(boardId, memberId, requestDto);
        return ResponseEntity.ok(response);
    }

    /**
     * 댓글 수정
     */
    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponseDto> updateComment(
            @PathVariable Long commentId,
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, String> request) {

        Long memberId = jwtUtil.extractMemberId(token.replace("Bearer ", ""));
        String content = request.get("content");
        CommentResponseDto response = commentService.updateComment(commentId, memberId, content);
        return ResponseEntity.ok(response);
    }

    /**
     * 댓글 삭제
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Map<String, String>> deleteComment(
            @PathVariable Long commentId,
            @RequestHeader("Authorization") String token) {

        Long memberId = jwtUtil.extractMemberId(token.replace("Bearer ", ""));
        commentService.deleteComment(commentId, memberId);

        return ResponseEntity.ok(Map.of("message", "댓글이 삭제되었습니다."));
    }

    /**
     * 특정 게시글의 댓글 목록 조회
     */
    @GetMapping("/{boardId}")
    public ResponseEntity<CommentResponseWrapperDto> getComments(@PathVariable Long boardId) {
        return ResponseEntity.ok(commentService.getCommentsByBoard(boardId));
    }
}