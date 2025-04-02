package pawparazzi.back.comment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pawparazzi.back.comment.dto.request.CommentRequestDto;
import pawparazzi.back.comment.dto.response.CommentLikeResponseDto;
import pawparazzi.back.comment.dto.response.CommentLikesResponseDto;
import pawparazzi.back.comment.dto.response.CommentResponseDto;
import pawparazzi.back.comment.dto.response.CommentListResponseDto;
import pawparazzi.back.comment.service.CommentLikeService;
import pawparazzi.back.comment.service.CommentService;
import pawparazzi.back.security.user.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;


import java.util.Map;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final CommentLikeService commentLikeService;

    /**
     * 댓글 작성
     */
    @PostMapping("/{boardId}")
    public ResponseEntity<CommentResponseDto> createComment(
            @PathVariable Long boardId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid CommentRequestDto requestDto) {

        Long memberId = userDetails.getId();
        CommentResponseDto response = commentService.createComment(boardId, memberId, requestDto);
        return ResponseEntity.ok(response);
    }

    /**
     * 댓글 수정
     */
    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponseDto> updateComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody Map<String, String> request) {

        Long memberId = userDetails.getId();
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
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long memberId = userDetails.getId();
        commentService.deleteComment(commentId, memberId);

        return ResponseEntity.ok(Map.of("message", "댓글이 삭제되었습니다."));
    }

    /**
     * 특정 게시글의 댓글 목록 조회
     */
    @GetMapping("/{boardId}")
    public ResponseEntity<CommentListResponseDto> getComments(@PathVariable Long boardId) {
        return ResponseEntity.ok(commentService.getCommentsByBoard(boardId));
    }

    /**
     * 댓글 좋아요 등록/삭제 (토글)
     */
    @PostMapping("/{commentId}/like")
    public ResponseEntity<CommentLikeResponseDto> toggleCommentLike(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long memberId = userDetails.getId();
        CommentLikeResponseDto response = commentLikeService.toggleCommentLike(commentId, memberId);

        return ResponseEntity.ok(response);
    }

    /**
     * 특정 댓글에 좋아요를 누른 회원 목록 조회
     */
    @GetMapping("/{commentId}/likes")
    public ResponseEntity<CommentLikesResponseDto> getLikedMembersByComment(@PathVariable Long commentId) {
        return ResponseEntity.ok(commentLikeService.getLikedMembersByComment(commentId));
    }
}