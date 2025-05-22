package pawparazzi.back.video.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pawparazzi.back.security.util.JwtUtil;
import pawparazzi.back.video.dto.VideoRequestDto;
import pawparazzi.back.video.dto.VideoResponseDto;
import pawparazzi.back.video.service.VideoRequestService;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class VideoRequestController {
    private final VideoRequestService videoRequestService;
    private final JwtUtil jwtUtil;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VideoResponseDto> createVideoRequest(
            @RequestPart(value = "request") String requestJson,
            @RequestPart(value = "images") List<MultipartFile> imageFiles, // 여러 이미지 처리
            @RequestHeader("Authorization") String token) throws IOException {

        // JSON 문자열을 객체로 수동 변환
        ObjectMapper objectMapper = new ObjectMapper();
        VideoRequestDto requestDto;
        try {
            requestDto = objectMapper.readValue(requestJson, VideoRequestDto.class);
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body(
                    VideoResponseDto.builder()
                            .status("FAILED")
                            .errorMessage("Invalid JSON format: " + e.getMessage())
                            .build()
            );
        }

        Long userId;
        try {
            userId = jwtUtil.extractMemberId(token.replace("Bearer ", ""));
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        CompletableFuture<VideoResponseDto> responseFuture = videoRequestService.createVideoRequest(
                requestDto,
                imageFiles, // 여러 이미지 전달
                userId
        );

        VideoResponseDto response = responseFuture.join();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{battleId}")
    public ResponseEntity<VideoResponseDto> createBattleVideoRequest(
            @PathVariable Long battleId,
            @RequestHeader("Authorization") String token) throws IOException {

        Long userId;
        try {
            userId = jwtUtil.extractMemberId(token.replace("Bearer ", ""));
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        CompletableFuture<VideoResponseDto> responseFuture = videoRequestService.createVideoRequestFromBattle(
                battleId,
                userId
        );

        VideoResponseDto response = responseFuture.join();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{jobId}")
    public ResponseEntity<VideoResponseDto> checkStatus(
            @PathVariable String jobId,
            @RequestHeader("Authorization") String token){
        VideoResponseDto response = videoRequestService.checkStatus(jobId);
        return ResponseEntity.ok(response);
    }
}
