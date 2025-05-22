package pawparazzi.back.pet.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pawparazzi.back.battle.service.BattleService;
import pawparazzi.back.pet.dto.PetRegisterRequestDto;
import pawparazzi.back.pet.dto.PetResponseDto;
import pawparazzi.back.pet.dto.PetUpdateDto;
import pawparazzi.back.pet.entity.Pet;
import pawparazzi.back.pet.service.PetService;
import pawparazzi.back.security.util.JwtUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/pets")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;
    private final BattleService battleService;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    /**
     * 반려동물 등록
     */
    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CompletableFuture<ResponseEntity<PetResponseDto>> registerPet(
            @RequestHeader("Authorization") String token,
            @RequestPart("petData") String petDataJson,
            @RequestPart(value = "petImage", required = false) MultipartFile petImage) {

        Long userId;
        try {
            userId = jwtUtil.extractMemberId(token.replace("Bearer ", ""));
        } catch (JwtException e) {
            return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }

        PetRegisterRequestDto registerDto;
        try {
            registerDto = objectMapper.readValue(petDataJson, PetRegisterRequestDto.class);
        } catch (JsonProcessingException e) {
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().build());
        }

        return petService.registerPet(userId, registerDto, petImage)
                .thenApply(ResponseEntity::ok);
    }

    /**
     * 회원별 반려동물 목록 조회
     */
    @GetMapping("/all")
    public ResponseEntity<List<PetResponseDto>> getAllPets(@RequestHeader("Authorization") String token) {
        Long userId;
        try {
            userId = jwtUtil.extractMemberId(token.replace("Bearer ", ""));
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<PetResponseDto> pets = petService.getPetsByMember(userId);
        return ResponseEntity.ok(pets);
    }

    /**
     * 반려동물 상세 조회
     */
    @GetMapping("/{petId}")
    public ResponseEntity<PetResponseDto> getPet(@PathVariable Long petId, @RequestHeader("Authorization") String token) {
        Long userId;
        try {
            userId = jwtUtil.extractMemberId(token.replace("Bearer ", ""));
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(petService.getPetById(petId, userId));
    }

    /**
     * 반려동물 정보 수정
     */
    @PatchMapping(value = "/{petId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CompletableFuture<ResponseEntity<PetResponseDto>> updatePet(
            @PathVariable Long petId,
            @RequestHeader("Authorization") String token,
            @RequestPart(value = "petData", required = false) String petDataJson,
            @RequestPart(value = "petImage", required = false) MultipartFile petImage) {

        Long userId;
        try {
            userId = jwtUtil.extractMemberId(token.replace("Bearer ", ""));
        } catch (JwtException e) {
            return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }

        PetUpdateDto updateDto;
        try {
            updateDto = (petDataJson != null && !petDataJson.isBlank())
                    ? objectMapper.readValue(petDataJson, PetUpdateDto.class)
                    : new PetUpdateDto();
        } catch (JsonProcessingException e) {
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().build());
        }

        return petService.updatePet(petId, userId, updateDto, petImage)
                .thenApply(ResponseEntity::ok);
    }

    /**
     * 반려동물 삭제
     */
    @DeleteMapping("/{petId}")
    public CompletableFuture<ResponseEntity<Map<String, String>>> deletePet(
            @PathVariable Long petId,
            @RequestHeader("Authorization") String token) {

        Long userId;
        try {
            userId = jwtUtil.extractMemberId(token.replace("Bearer ", ""));
        } catch (JwtException e) {
            return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }

        return petService.deletePet(petId, userId)
                .thenApply(ignored -> ResponseEntity.ok(Map.of("message", "반려동물이 삭제되었습니다.")));
    }

    /**
     * 포켓몬 배틀마냥 배틀
     */
    @PostMapping("/battle/{targetPetId}")
    public ResponseEntity<String> battle(
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

            String battleResult = petService.invokeLLMForBattle(myPetId, targetPetId, userId);
            JsonNode rootNode = objectMapper.readTree(battleResult);
            JsonNode winnerNode = rootNode.get("winner");

            if (winnerNode == null || winnerNode.asText().isBlank()){
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("배틀 결과에 승자 정보가 없습니다.");
            }

            Pet pet1 = petService.getPetEntityById(myPetId);
            Pet pet2 = petService.getPetEntityById(targetPetId);
            String winner = winnerNode.asText().replace("\"", "");
            petService.battleCountUpdate(myPetId, targetPetId, winner);
            battleService.createBattle(pet1, pet2, battleResult, winner);

            return ResponseEntity.ok(battleResult);
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

}
