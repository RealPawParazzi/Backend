package pawparazzi.back.pet.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pawparazzi.back.pet.dto.PetRegisterRequestDto;
import pawparazzi.back.pet.dto.PetResponseDto;
import pawparazzi.back.pet.dto.PetUpdateDto;
import pawparazzi.back.pet.service.PetService;
import pawparazzi.back.security.user.CustomUserDetails;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/pets")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;

    /**
     * 반려동물 등록
     */
    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CompletableFuture<ResponseEntity<PetResponseDto>> registerPet(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart("petData") PetRegisterRequestDto requestDto,
            @RequestPart(value = "petImage", required = false) MultipartFile petImage) {

        return petService.registerPet(userDetails.getId(), requestDto, petImage)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.badRequest().build());
    }

    /**
     * 회원별 반려동물 목록 조회
     */
    @GetMapping("/all")
    public ResponseEntity<List<PetResponseDto>> getAllPets(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getId();
        List<PetResponseDto> pets = petService.getPetsByMember(userId);
        return ResponseEntity.ok(pets);
    }

    /**
     * 반려동물 상세 조회
     */
    @GetMapping("/{petId}")
    public ResponseEntity<PetResponseDto> getPet(@PathVariable Long petId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getId();
        return ResponseEntity.ok(petService.getPetById(petId, userId));
    }

    /**
     * 반려동물 정보 수정
     */
    @PatchMapping(value = "/{petId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CompletableFuture<ResponseEntity<PetResponseDto>> updatePet(
            @PathVariable Long petId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart(value = "petData", required = false) PetUpdateDto petData,
            @RequestPart(value = "petImage", required = false) MultipartFile petImage) {

        Long userId = userDetails.getId();

        return petService.updatePet(petId, userId, petData, petImage)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.badRequest().build());
    }

    /**
     * 반려동물 삭제
     */
    @DeleteMapping("/{petId}")
    public CompletableFuture<ResponseEntity<Map<String, String>>> deletePet(
            @PathVariable Long petId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getId();

        return petService.deletePet(petId, userId)
                .thenApply(ignored -> ResponseEntity.ok(Map.of("message", "반려동물이 삭제되었습니다.")));
    }
}