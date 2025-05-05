package pawparazzi.back.video.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import pawparazzi.back.S3.S3UploadUtil;
import pawparazzi.back.video.dto.VideoRequestDto;
import pawparazzi.back.video.dto.VideoResponseDto;
import pawparazzi.back.video.entity.VideoRequest;
import pawparazzi.back.video.repository.VideoRequestRepository;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class VideoRequestService {

    private final VideoRequestRepository videoRequestRepository;
    private final S3UploadUtil s3UploadUtil;

    @Value("${ai.server.url}")
    private String aiServerUrl;

    private final RestTemplate restTemplate;

    /**
     * 비디오 생성 요청을 처리하는 메소드 (비동기 업로드 방식으로 개선)
     */
    public CompletableFuture<VideoResponseDto> createVideoRequest(VideoRequestDto requestDto, MultipartFile imageFile, Long userId) {
        // 작업 ID 생성
        String jobId = UUID.randomUUID().toString();

        // 이미지 파일 업로드 (비동기 처리)
        CompletableFuture<String> imageUrlFuture;
        if (imageFile != null && !imageFile.isEmpty()) {
            String pathPrefix = "videos/" + System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
            imageUrlFuture = s3UploadUtil.uploadImageAsync(imageFile, pathPrefix, null);
        } else {
            imageUrlFuture = CompletableFuture.completedFuture(null);
        }

        // 이미지 업로드 완료 후 비디오 요청 처리 및 AI 서버로 전송
        return imageUrlFuture.thenApply(imageUrl -> {
            // VideoRequest 엔티티 생성
            VideoRequest videoRequest = VideoRequest.builder()
                    .prompt(requestDto.getPrompt())
                    .imageUrl(imageUrl)
                    .userId(userId)
                    .jobId(jobId)
                    .build();

            // DB에 저장
            videoRequest = videoRequestRepository.save(videoRequest);
            final VideoRequest savedRequest = videoRequest; // final 변수로 복사 (람다 내부에서 사용하기 위함)

            // AI 서버로 요청 전송
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("jobId", jobId);
            requestBody.put("prompt", requestDto.getPrompt());
            requestBody.put("imageUrl", imageUrl);
            requestBody.put("duration", requestDto.getDuration());
            requestBody.put("additionalOptions", requestDto.getAdditionalOptions());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            try {
                ResponseEntity<Map> response = restTemplate.postForEntity(
                        aiServerUrl + "/api/generate",
                        entity,
                        Map.class
                );

                if (response.getStatusCode().is2xxSuccessful()) {
                    savedRequest.setStatus("PROCESSING");
                } else {
                    savedRequest.setStatus("FAILED");
                    // 에러 메시지 처리 추가
                    if (response.getBody() != null && response.getBody().containsKey("error")) {
                        savedRequest.setErrorMessage((String) response.getBody().get("error"));
                    }
                }
            } catch (Exception e) {
                savedRequest.setStatus("FAILED");
                savedRequest.setErrorMessage(e.getMessage());
            }

            // 변경된 상태 저장
            videoRequestRepository.save(savedRequest);

            // 응답 DTO 생성
            return VideoResponseDto.builder()
                    .requestId(savedRequest.getId())
                    .jobId(savedRequest.getJobId())
                    .status(savedRequest.getStatus())
                    .build();
        });
    }

    public VideoResponseDto checkStatus(String jobId) {
        // 기존 코드 그대로 유지
        VideoRequest videoRequest = videoRequestRepository.findByJobId(jobId)
                .orElseThrow(() -> new RuntimeException("요청한 작업을 찾을 수 없습니다: " + jobId));

        if ("PENDING".equals(videoRequest.getStatus()) || "PROCESSING".equals(videoRequest.getStatus())) {
            // AI 서버에 상태 확인 요청
            try {
                ResponseEntity<Map> response = restTemplate.getForEntity(
                        aiServerUrl + "/api/status/" + jobId,
                        Map.class
                );

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    String status = (String) response.getBody().get("status");
                    videoRequest.setStatus(status);

                    if ("COMPLETED".equals(status) && response.getBody().containsKey("resultUrl")) {
                        videoRequest.setResultUrl((String) response.getBody().get("resultUrl"));
                    }

                    videoRequestRepository.save(videoRequest);
                }
            } catch (Exception e) {
                // 에러 로깅 (실제 구현시 추가)
            }
        }

        return VideoResponseDto.builder()
                .requestId(videoRequest.getId())
                .jobId(videoRequest.getJobId())
                .status(videoRequest.getStatus())
                .duration(videoRequest.getDuration())
                .resultUrl(videoRequest.getResultUrl())
                .build();
    }
}