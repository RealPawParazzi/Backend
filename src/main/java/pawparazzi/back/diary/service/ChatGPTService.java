package pawparazzi.back.diary.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pawparazzi.back.diary.dto.ChatRequest;
import pawparazzi.back.diary.dto.ChatResponse;
import pawparazzi.back.member.repository.MemberRepository;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatGPTService {

    @Value("${openai.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;
    private final HttpHeaders httpHeaders;
    private final MemberRepository memberRepository;

    /**
     * LLM 모델을 호출하여 감성적인 일기 생성
     */
    public String invokeLLMForDiary(Long userId, String userPrompt) {
        memberRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        // LLM API URL (ex: /api/diary)
        String llmApiUrl = apiUrl + "/api/diary";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 프롬프트 생성
        String finalPrompt = "다음 내용을 바탕으로 감성적이고 자연스러운 일기 한 편을 작성해줘:\n\n\"" + userPrompt + "\"\n\n"
                + "- 말투는 일기처럼 자연스럽고 서정적으로 써줘.\n"
                + "- 분량은 3~5문장으로 해줘.";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("prompt", finalPrompt);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(llmApiUrl, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody() != null ? response.getBody() : "일기를 생성하지 못했습니다.";
            } else {
                throw new RuntimeException("일기 생성 실패: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("일기 생성 중 오류가 발생했습니다.");
        }
    }
}
