package pawparazzi.back.diary.controller;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pawparazzi.back.diary.service.ChatGPTService;
import pawparazzi.back.security.util.JwtUtil;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/diary")
public class ChatGPTController {

    private final ChatGPTService chatGPTService;
    private final JwtUtil jwtUtil;

    @PostMapping("/diary")
    public ResponseEntity<String> createDiary(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, String> body) {

        try {
            Long userId = jwtUtil.extractMemberId(token.replace("Bearer ", ""));
            String userPrompt = body.get("prompt");

            String diaryText = ChatGPTService.invokeLLMForDiary(userId, userPrompt);
            return ResponseEntity.ok(diaryText);

        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 실패");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("일기 작성 중 오류 발생");
        }
    }
}
