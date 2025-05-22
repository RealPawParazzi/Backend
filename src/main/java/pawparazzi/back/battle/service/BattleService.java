package pawparazzi.back.battle.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import pawparazzi.back.battle.dto.BattleResponseDto;
import pawparazzi.back.battle.entity.Battle;
import pawparazzi.back.battle.repository.BattleRepository;
import pawparazzi.back.pet.entity.Pet;
import pawparazzi.back.pet.repository.PetRepository;
import pawparazzi.back.pet.service.PetService;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BattleService {

    private final PetService petService;
    private final BattleRepository battleRepository;
    private final RestTemplate restTemplate; // LLM 호출을 위한 RestTemplate
    private final PetRepository petRepository;

    @Value("${ai.server.url}")
    private String aiServerUrl;

    @Transactional
    public void createBattle(Long pet1Id, Long pet2Id, String battleResult, String runwayPrompt, String winner) {
        // 영속성 컨텍스트에서 Pet 엔티티 조회
        Pet pet1 = petRepository.findById(pet1Id)
                .orElseThrow(() -> new IllegalArgumentException("Pet with id " + pet1Id + " not found"));
        Pet pet2 = petRepository.findById(pet2Id)
                .orElseThrow(() -> new IllegalArgumentException("Pet with id " + pet2Id + " not found"));

        // 이제 pet1과 pet2는 영속 상태의 엔티티
        String pet1Name = pet1.getName();

        if (Objects.equals(winner, pet1Name)) {
            makeBattle(pet1, pet2, battleResult, runwayPrompt, pet1Id, pet2Id);
        } else {
            makeBattle(pet1, pet2, battleResult, runwayPrompt, pet2Id, pet1Id);
        }
    }

    private void makeBattle(Pet pet1, Pet pet2, String battleResult, String runwayPrompt, Long winnerId, Long loserId) {
        Battle battle = new Battle();
        battle.setPet1(pet1);
        battle.setPet2(pet2);
        battle.setWinnerId(winnerId);
        battle.setLoserId(loserId);
        battle.setBattleResult(battleResult);
        battle.setRunwayPrompt(runwayPrompt);
        battleRepository.save(battle);
    }

    /**
     * 배틀 결과 전체 조회
     */
    @Transactional(readOnly = true)
    public List<BattleResponseDto> getBattlesById(Long petId) {

        Pet pet = petService.getPetEntityById(petId);

        List<Battle> battlesAsPet1 = battleRepository.findAllByPet1(pet);
        List<Battle> battlesAsPet2 = battleRepository.findAllByPet2(pet);

        List<Battle> allBattles = new ArrayList<>();
        allBattles.addAll(battlesAsPet1);
        allBattles.addAll(battlesAsPet2);

        return allBattles.stream()
                .map(BattleResponseDto::new)
                .collect(Collectors.toList());
    }

    /**
     * 배틀 결과 상세 조회
     */
    @Transactional(readOnly = true)
    public BattleResponseDto getBattleById(Long battleId) {
        Battle battle = battleRepository.findById(battleId)
                .orElseThrow(() -> new EntityNotFoundException("배틀 결과를 찾을 수 없습니다."));

        return new BattleResponseDto(battle);
    }

    public String invokeLLMForBattle(Long myPetId, Long targetPetId, Long userId) {
        Pet myPet = petRepository.findById(myPetId)
                .orElseThrow(() -> new EntityNotFoundException("내 반려동물을 찾을 수 없습니다."));

        if (!myPet.getMember().getId().equals(userId)) {
            throw new IllegalArgumentException("해당 반려동물로 배틀할 권한이 없습니다.");
        }

        Pet targetPet = petRepository.findById(targetPetId)
                .orElseThrow(() -> new EntityNotFoundException("상대 반려동물을 찾을 수 없습니다."));

        // LLM 호출을 위한 요청 데이터 구성
        String llmApiUrl = aiServerUrl + "/api/battle"; // LLM 서비스 URL
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("myPetName", myPet.getName());
        requestBody.put("targetPetName", targetPet.getName());
        requestBody.put("myPetDetail", myPet.getPetDetail());
        requestBody.put("targetPetDetail", targetPet.getPetDetail());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            // AI 서버 호출 전 로깅
            System.out.println("=== AI 서버 호출 시작 ===");
            System.out.println("LLM API URL: " + llmApiUrl);
            System.out.println("Request Entity: " + entity);
            System.out.println("Request Headers: " + entity.getHeaders());
            System.out.println("Request Body: " + entity.getBody());

            // AI 서버에 POST 요청
            ResponseEntity<String> response = restTemplate.postForEntity(llmApiUrl, entity, String.class);

            // AI 서버 응답 로깅
            System.out.println("=== AI 서버 응답 ===");
            System.out.println("Response Status: " + response.getStatusCode());
            System.out.println("Response Headers: " + response.getHeaders());
            System.out.println("Response Body: " + response.getBody());
            System.out.println("Response Body Length: " + (response.getBody() != null ? response.getBody().length() : "null"));

            if (response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                if (responseBody != null) {
                    System.out.println("=== 성공적인 응답 반환 ===");
                    System.out.println("Final Response: " + responseBody);
                    return responseBody;
                } else {
                    System.out.println("=== 응답 본문이 null ===");
                    return "배틀 결과를 가져오지 못했습니다.";
                }
            } else {
                System.out.println("=== AI 서버 호출 실패 ===");
                System.out.println("Error Status: " + response.getStatusCode());
                System.out.println("Error Body: " + response.getBody());
                throw new RuntimeException("LLM 호출 실패: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.out.println("=== AI 서버 호출 중 예외 발생 ===");
            System.out.println("Exception Type: " + e.getClass().getSimpleName());
            System.out.println("Exception Message: " + e.getMessage());
            e.printStackTrace();
            throw e; // 예외를 다시 던져서 상위에서 처리하도록 함
        }
    }
}
