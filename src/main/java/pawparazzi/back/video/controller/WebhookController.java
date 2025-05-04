package pawparazzi.back.video.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pawparazzi.back.video.entity.VideoRequest;
import pawparazzi.back.video.repository.VideoRequestRepository;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class WebhookController {
    private final VideoRequestRepository videoRequestRepository;

    @PostMapping("/video-result")
    public ResponseEntity<?> handleVideoResult(@RequestBody Map<String, Object> payload) {
        log.info("Received video result: {}", payload);
        String jobId = (String) payload.get("jobId");  // camelCase로 변경
        String status = ((String) payload.get("status")).toUpperCase();
        String resultUrl = (String) payload.get("resultUrl");  // resultUrl 직접 받기

        VideoRequest videoRequest = videoRequestRepository.findByJobId(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found: " + jobId));

        videoRequest.setStatus(status);

        if ("COMPLETED".equals(status) && resultUrl != null) {
            videoRequest.setResultUrl(resultUrl);
            // 썸네일은 별도로 처리하거나 생략
        }

        if ("FAILED".equals(status)) {
            String error = (String) payload.get("error");
            videoRequest.setErrorMessage(error);
        }

        videoRequestRepository.save(videoRequest);
        return ResponseEntity.ok().build();
    }
}
