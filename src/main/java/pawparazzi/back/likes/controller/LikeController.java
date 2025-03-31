package pawparazzi.back.likes.controller;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pawparazzi.back.likes.dto.LikeResponseDto;
import pawparazzi.back.likes.dto.LikeToggleResponseDto;
import pawparazzi.back.likes.service.LikeService;
import pawparazzi.back.security.user.CustomUserDetails;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    /**
     * 좋아요 등록/삭제
     */
    @PostMapping("/{boardId}/like")
    public ResponseEntity<LikeToggleResponseDto> toggleLike(
            @PathVariable Long boardId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long memberId = userDetails.getId();
        LikeToggleResponseDto response = likeService.toggleLike(boardId, memberId);

        return ResponseEntity.ok(response);
    }

    /**
     * 특정 게시글의 좋아요 누른 회원 목록 조회
     */
    @GetMapping("/{boardId}/likes")
    public ResponseEntity<LikeResponseDto> getLikes(@PathVariable Long boardId) {
        return ResponseEntity.ok(likeService.getLikesByBoard(boardId));
    }
}