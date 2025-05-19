package pawparazzi.back.diary.service;

import org.springframework.transaction.annotation.Transactional;
import pawparazzi.back.diary.dto.DiaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pawparazzi.back.diary.entity.Diary;
import pawparazzi.back.diary.repository.DiaryRepository;
import pawparazzi.back.member.entity.Member;
import pawparazzi.back.member.repository.MemberRepository;
import java.util.*;

@RequiredArgsConstructor
@Service
public class DiaryService {

    private final RestTemplate restTemplate;
    private final DiaryRepository diaryRepository;
    private final MemberRepository memberRepository;

    @Value("${openai.secret-key}")
    private String openaiApiKey;

    @Value("${openai.model}")
    private String model;

    @Value("${openai.api.url}")
    private String apiUrl;

    // 일기 생성
    public DiaryResponse generateAndSaveDiaryEntry(String userInput, Long memberId, String title) {
        // ChatGPT 요청 준비
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openaiApiKey);

        String prompt = """
            너는 사람의 하루를 짧은 일기로 정리해주는 작가야.
            다음 사용자의 입력을 바탕으로 부드럽고 감성적인 문장으로 10줄 정도 일기를 작성해줘.
            날짜나 이름은 포함하지 마.
            구어체보다는 서술형으로 써줘.

            사용자 입력: %s
        """.formatted(userInput);

        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", List.of(message));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, request, Map.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("ChatGPT API 호출 실패: " + response.getStatusCode());
        }

        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
        Map<String, Object> messageContent = (Map<String, Object>) choices.get(0).get("message");
        String diaryContent = (String) messageContent.get("content");

        // DB 저장
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Diary diary = Diary.builder()
                .title(title)
                .content(diaryContent)
                .member(member)
                .build();

        diaryRepository.save(diary);
        return new DiaryResponse(diary.getId(), diary.getTitle(), diary.getContent(), diary.getCreatedAt());
    }

    // 나의 일기 조회
    public List<DiaryResponse> getAllDiaries(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        return diaryRepository.findByMember(member).stream()
                .map(diary -> new DiaryResponse(diary.getId(), diary.getTitle(), diary.getContent(), diary.getCreatedAt()))
                .toList();
    }

    // 특정 일기 상세 조회
    public DiaryResponse getDiaryById(Long diaryId, Long memberId) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 일기입니다."));

        if (diary.getMember() == null || !diary.getMember().getId().equals(memberId)) {
            throw new IllegalArgumentException("해당 일기를 조회할 권한이 없습니다.");
        }

        return new DiaryResponse(diary.getId(), diary.getTitle(), diary.getContent(), diary.getCreatedAt());
    }


    // 일기 삭제
    public void deleteDiary(Long diaryId, Long memberId) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 일기입니다."));

        if (!diary.getMember().getId().equals(memberId)) {
            throw new IllegalArgumentException("해당 일기를 삭제할 권한이 없습니다.");
        }

        diaryRepository.delete(diary);
    }

    // 자신의 일기가 맞는지 판별 하는 코드
    @Transactional(readOnly = true)
    public boolean isDiaryOwner(Long diaryId, Long memberId) {
        return diaryRepository.findById(diaryId)
                .map(diary -> diary.getMember().getId().equals(memberId))
                .orElse(false);
    }
}