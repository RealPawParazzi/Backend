package pawparazzi.back.battle.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pawparazzi.back.battle.dto.BattleResponseDto;
import pawparazzi.back.battle.dto.BattleResultResponseDto;
import pawparazzi.back.battle.service.BattleService;
import pawparazzi.back.member.service.MemberService;
import pawparazzi.back.pet.dto.PetRegisterRequestDto;
import pawparazzi.back.pet.dto.PetResponseDto;
import pawparazzi.back.pet.entity.Pet;
import pawparazzi.back.pet.service.PetService;
import pawparazzi.back.security.util.JwtUtil;

import java.util.List;

@RestController
@RequestMapping("/api/battle")
@RequiredArgsConstructor
public class BattleController {

    private final ObjectMapper objectMapper;
    private final BattleService battleService;
    private final PetService petService;
    private final JwtUtil jwtUtil;

    @GetMapping("/all")
    public ResponseEntity<List<BattleResponseDto>> getBattles(@RequestHeader("Authorization") String token) {
        Long userId;
        try {
            userId = jwtUtil.extractMemberId(token.replace("Bearer ", ""));
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<BattleResponseDto> battles = battleService.getBattlesById(userId);
        return ResponseEntity.ok(battles);
    }

    @GetMapping("/{battleId}")
    public ResponseEntity<BattleResponseDto> getBattle(@PathVariable Long battleId, @RequestHeader("Authorization") String token) {
        Long userId;
        try {
            userId = jwtUtil.extractMemberId(token.replace("Bearer ", ""));
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        BattleResponseDto battle = battleService.getBattleById(battleId);

        if (battle == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(battle);
    }

    @PostMapping("/{targetPetId}")
    public ResponseEntity<?> battle(
            @PathVariable Long targetPetId,
            @RequestParam Long myPetId,
            @RequestHeader("Authorization") String token) {

        Long userId;
        try {
            userId = jwtUtil.extractMemberId(token.replace("Bearer ", ""));
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();

            String fullBattleResult = battleService.invokeLLMForBattle(myPetId, targetPetId, userId);
            JsonNode rootNode = objectMapper.readTree(fullBattleResult);
            JsonNode winnerNode = rootNode.get("winner");
            JsonNode resultNode = rootNode.get("result");
            JsonNode runwayPromptNode = rootNode.get("runway_prompt");

            if (winnerNode == null || winnerNode.asText().isBlank()){
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("배틀 결과에 승자 정보가 없습니다.");
            }

            String winner = winnerNode.asText().replace("\"", "");

            String resultRaw = resultNode != null ? resultNode.asText() : "배틀 결과를 가져오지 못했습니다.";
            String splitToken = "[Runway용 프롬프트]";
            String battleResultText = resultRaw.contains(splitToken)
                    ? resultRaw.substring(0, resultRaw.indexOf(splitToken)).trim()
                    : resultRaw;
            String runwayPrompt = runwayPromptNode != null && !runwayPromptNode.asText().isBlank()
                    ? runwayPromptNode.asText()
                    : (resultRaw.contains(splitToken)
                    ? resultRaw.substring(resultRaw.indexOf(splitToken) + splitToken.length()).trim()
                    : "배틀 결과를 가져오지 못했습니다.");

            petService.battleCountUpdate(myPetId, targetPetId, winner);
            Long battleId = battleService.createBattle(myPetId, targetPetId, battleResultText, runwayPrompt, winner);

            BattleResultResponseDto responseDto = new BattleResultResponseDto(battleId, winner, battleResultText);
            return ResponseEntity.ok(responseDto);

        } catch (IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 반려동물을 찾을 수 없습니다.");
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("배틀 결과를 처리하는 중 JSON 파싱 오류가 발생했습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("배틀 중 오류가 발생했습니다.");
        }
    }

    /**
     * 배틀 인스턴스 생성
     * 2명이 모두 새로 생성하는 원리
     * 시연용 api => 다소 규칙 깨질 수 있음
     */
    @PostMapping("/instance/createTwo")
    public ResponseEntity<?> createBattleTwoInstance(
            @RequestPart(value = "petImage1") MultipartFile petImage1,
            @RequestPart(value = "petImage2") MultipartFile petImage2,
            @RequestPart("petData1") String petDataJson1,
            @RequestPart("petData2") String petDataJson2,
            @RequestHeader("Authorization") String token) {

        Long userId;
        try {
            userId = jwtUtil.extractMemberId(token.replace("Bearer ", ""));
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            // PetRegisterRequestDto 파싱
            PetRegisterRequestDto petDto1 = objectMapper.readValue(petDataJson1, PetRegisterRequestDto.class);
            PetRegisterRequestDto petDto2 = objectMapper.readValue(petDataJson2, PetRegisterRequestDto.class);

            if (petDto1.getBirthDate() == null) {
                petDto1.setBirthDate(java.time.LocalDate.now());
            }
            if (petDto2.getBirthDate() == null) {
                petDto2.setBirthDate(java.time.LocalDate.now());
            }

            // 펫 2개 등록 (동기)
            PetResponseDto pet1 = petService.registerPetSync(userId, petDto1, petImage1);
            PetResponseDto pet2 = petService.registerPetSync(userId, petDto2, petImage2);

            // 배틀 실행
            String fullBattleResult = battleService.invokeLLMForBattle(pet1.getPetId(), pet2.getPetId(), userId);
            ObjectMapper om = new ObjectMapper();
            JsonNode rootNode = om.readTree(fullBattleResult);
            JsonNode winnerNode = rootNode.get("winner");
            JsonNode resultNode = rootNode.get("result");
            JsonNode runwayPromptNode = rootNode.get("runway_prompt");

            if (winnerNode == null || winnerNode.asText().isBlank()){
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("배틀 결과에 승자 정보가 없습니다.");
            }

            String winner = winnerNode.asText().replace("\"", "");

            String resultRaw = resultNode != null ? resultNode.asText() : "배틀 결과를 가져오지 못했습니다.";
            String splitToken = "[Runway용 프롬프트]";
            String battleResultText = resultRaw.contains(splitToken)
                    ? resultRaw.substring(0, resultRaw.indexOf(splitToken)).trim()
                    : resultRaw;
            String runwayPrompt = runwayPromptNode != null && !runwayPromptNode.asText().isBlank()
                    ? runwayPromptNode.asText()
                    : (resultRaw.contains(splitToken)
                    ? resultRaw.substring(resultRaw.indexOf(splitToken) + splitToken.length()).trim()
                    : "배틀 결과를 가져오지 못했습니다.");

            petService.battleCountUpdate(pet1.getPetId(), pet2.getPetId(), winner);
            Long battleId = battleService.createBattle(pet1.getPetId(), pet2.getPetId(), battleResultText, runwayPrompt, winner);

            BattleResultResponseDto responseDto = new BattleResultResponseDto(battleId, winner, battleResultText);
            return ResponseEntity.ok(responseDto);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("배틀 인스턴스 생성 중 오류: " + e.getMessage());
        }

    }

    /**
     * 배틀 인스턴스 생성
     * 1명만 즉석에서 생성하는 원리
     * 시연용 api => 다소 규칙 깨질 수 있음
     */
    @PostMapping("/instance/createOne/{targetPetId}")
    @Transactional
    public ResponseEntity<?> createBattleOneInstance(
            @PathVariable Long targetPetId,
            @RequestPart(value = "petImage") MultipartFile petImage,
            @RequestPart("petData") String petDataJson,
            @RequestHeader("Authorization") String token) {

        Long userId;
        try {
            userId = jwtUtil.extractMemberId(token.replace("Bearer ", ""));
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {

            // PetRegisterRequestDto 파싱
            PetRegisterRequestDto petDto = objectMapper.readValue(petDataJson, PetRegisterRequestDto.class);
            if( petDto.getBirthDate() == null) {
                petDto.setBirthDate(java.time.LocalDate.now());
            }
            PetResponseDto pet = petService.registerPetSync(userId, petDto, petImage);

            // 배틀 실행
            String fullBattleResult = battleService.invokeLLMForBattle(pet.getPetId(), targetPetId, userId);

            // JSON 파싱 시작
            ObjectMapper om = new ObjectMapper();
            JsonNode rootNode = om.readTree(fullBattleResult);

            JsonNode winnerNode = rootNode.get("winner");
            JsonNode resultNode = rootNode.get("result");
            JsonNode runwayPromptNode = rootNode.get("runway_prompt");

            if (winnerNode == null || winnerNode.asText().isBlank()){
                System.out.println("[ERROR] Winner node is null or blank");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("배틀 결과에 승자 정보가 없습니다.");
            }

            Pet instancePet = petService.getPetEntityById(pet.getPetId());
            String winner = winnerNode.asText().replace("\"", "");
            System.out.println("[DEBUG] Winner extracted: " + winner);

            String resultRaw = resultNode != null ? resultNode.asText() : "배틀 결과를 가져오지 못했습니다.";
            String splitToken = "[Runway용 프롬프트]";
            String battleResultText = resultRaw.contains(splitToken)
                    ? resultRaw.substring(0, resultRaw.indexOf(splitToken)).trim()
                    : resultRaw;
            String runwayPrompt = runwayPromptNode != null && !runwayPromptNode.asText().isBlank()
                    ? runwayPromptNode.asText()
                    : (resultRaw.contains(splitToken)
                    ? resultRaw.substring(resultRaw.indexOf(splitToken) + splitToken.length()).trim()
                    : "배틀 결과를 가져오지 못했습니다.");

            petService.battleCountUpdate(instancePet.getPetId(), targetPetId, winner);
            Long battleId = battleService.createBattle(pet.getPetId(), targetPetId, battleResultText, runwayPrompt, winner);

            BattleResultResponseDto responseDto = new BattleResultResponseDto(battleId, winner, battleResultText);
            return ResponseEntity.ok(responseDto);

        } catch (IllegalArgumentException e){
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 반려동물을 찾을 수 없습니다.");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("배틀 결과를 처리하는 중 JSON 파싱 오류가 발생했습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("배틀 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}
