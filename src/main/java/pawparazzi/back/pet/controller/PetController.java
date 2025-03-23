package pawparazzi.back.pet.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pawparazzi.back.pet.dto.PetRegisterRequestDto;
import pawparazzi.back.pet.dto.PetResponseDto;
import pawparazzi.back.pet.dto.PetUpdateDto;
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

        Long userId = jwtUtil.extractMemberId(token.replace("Bearer ", ""));

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
        Long userId = jwtUtil.extractMemberId(token.replace("Bearer ", ""));
        List<PetResponseDto> pets = petService.getPetsByMember(userId);
        return ResponseEntity.ok(pets);
    }

    /**
     * 반려동물 상세 조회
     */
    @GetMapping("/{petId}")
    public ResponseEntity<PetResponseDto> getPet(@PathVariable Long petId, @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.extractMemberId(token.replace("Bearer ", ""));
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

        Long userId = jwtUtil.extractMemberId(token.replace("Bearer ", ""));

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

        Long userId = jwtUtil.extractMemberId(token.replace("Bearer ", ""));

        return petService.deletePet(petId, userId)
                .thenApply(ignored -> ResponseEntity.ok(Map.of("message", "반려동물이 삭제되었습니다.")));
    }
}