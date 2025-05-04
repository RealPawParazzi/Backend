package pawparazzi.back.pet.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.client.RestTemplate;
import pawparazzi.back.S3.S3UploadUtil;
import pawparazzi.back.S3.service.S3AsyncService;
import pawparazzi.back.member.entity.Member;
import pawparazzi.back.member.repository.MemberRepository;
import pawparazzi.back.pet.dto.PetRegisterRequestDto;
import pawparazzi.back.pet.dto.PetResponseDto;
import pawparazzi.back.pet.dto.PetUpdateDto;
import pawparazzi.back.pet.entity.Pet;
import pawparazzi.back.pet.repository.PetRepository;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PetService {

    private final PetRepository petRepository;
    private final MemberRepository memberRepository;
    private final S3UploadUtil s3UploadUtil;
    private final S3AsyncService s3AsyncService;
    private final RestTemplate restTemplate; // LLM 호출을 위한 RestTemplate


    /**
     * 반려동물 등록
     */
    @Transactional
    public CompletableFuture<PetResponseDto> registerPet(Long userId, PetRegisterRequestDto registerDto, MultipartFile petImage) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        String pathPrefix = "pet_images/" + member.getNickName();
        String defaultImageUrl = "https://default-image-url.com/default-pet.png";

        // S3 이미지 업로드 (비동기 처리)
        CompletableFuture<String> petImageUrlFuture = s3UploadUtil.uploadImageAsync(petImage, pathPrefix, defaultImageUrl);

        return petImageUrlFuture.thenApply(petImageUrl -> {
            Pet pet = new Pet(registerDto.getName(), registerDto.getType(), registerDto.getBirthDate(), petImageUrl, member);
            member.addPet(pet); // Member에 반려동물 추가
            petRepository.save(pet);
            return new PetResponseDto(pet);
        });
    }

    /**
     * 회원별 반려동물 목록 조회
     */
    @Transactional(readOnly = true)
    public List<PetResponseDto> getPetsByMember(Long userId) {
        memberRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        return petRepository.findByMemberId(userId).stream()
                .map(PetResponseDto::new)
                .collect(Collectors.toList());
    }

    /**
     * 반려동물 상세 조회
     */
    @Transactional(readOnly = true)
    public PetResponseDto getPetById(Long petId, Long userId) {
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new EntityNotFoundException("반려동물을 찾을 수 없습니다."));

        if (!pet.getMember().getId().equals(userId)) {
            throw new IllegalArgumentException("해당 반려동물을 조회할 권한이 없습니다.");
        }

        return new PetResponseDto(pet);
    }

    /**
     * 반려동물 정보 수정
     */
    @Transactional
    public CompletableFuture<PetResponseDto> updatePet(Long petId, Long userId, PetUpdateDto updateDto, MultipartFile petImage) {
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new EntityNotFoundException("반려동물을 찾을 수 없습니다."));

        if (!pet.getMember().getId().equals(userId)) {
            throw new IllegalArgumentException("해당 반려동물을 수정할 권한이 없습니다.");
        }

        String pathPrefix = "pet_images/" + pet.getMember().getNickName();
        String defaultImageUrl = "https://default-image-url.com/default-pet.png";
        String oldImageUrl = pet.getPetImg();

        // S3 이미지 업로드 (petImage가 있을 때만 비동기 처리)
        CompletableFuture<String> newImageFuture = (petImage != null)
                ? s3UploadUtil.uploadImageAsync(petImage, pathPrefix, defaultImageUrl)
                : CompletableFuture.completedFuture(oldImageUrl);

        // 부분 업데이트 적용 (null이 아닌 값만 반영)
        if (updateDto.getName() != null) pet.setName(updateDto.getName());
        if (updateDto.getType() != null) pet.setType(updateDto.getType());
        if (updateDto.getBirthDate() != null) pet.setBirthDate(updateDto.getBirthDate());
        if (updateDto.getPetDetail() != null) pet.setPetDetail(updateDto.getPetDetail());

        return newImageFuture.thenApply(newImageUrl -> {
            pet.setPetImg(newImageUrl);
            petRepository.save(pet);
            return new PetResponseDto(pet);
        }).thenCombineAsync( // 기존 S3 이미지 삭제를 비동기 병렬 실행
                (oldImageUrl != null && !oldImageUrl.equals(defaultImageUrl))
                        ? s3AsyncService.deleteFile("pet_images/" + extractFileName(oldImageUrl))
                        : CompletableFuture.completedFuture(null),
                (updatedPet, ignored) -> updatedPet // 이미지 삭제 완료 여부와 상관없이 업데이트된 Pet 반환
        );
    }

    /**
     * 반려동물 삭제
     */
    @Transactional
    public CompletableFuture<Void> deletePet(Long petId, Long userId) {
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new EntityNotFoundException("반려동물을 찾을 수 없습니다."));

        if (!pet.getMember().getId().equals(userId)) {
            throw new IllegalArgumentException("해당 반려동물을 삭제할 권한이 없습니다.");
        }

        String petImageUrl = pet.getPetImg();
        String defaultImageUrl = "https://default-image-url.com/default-pet.png";

        // 먼저 DB에서 반려동물 삭제
        petRepository.delete(pet);

        // S3에서 기존 반려동물 이미지 삭제
        if (petImageUrl != null && !petImageUrl.equals(defaultImageUrl)) {
            String fileName = extractFileName(petImageUrl);
            return s3AsyncService.deleteFile("pet_images/" + fileName)
                    .exceptionally(ex -> {
                        System.err.println("S3 이미지 삭제 실패: " + ex.getMessage());
                        return null;
                    });
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * LLM 모델을 호출하여 배틀 결과 생성
     */
    public String invokeLLMForBattle(Long myPetId, Long targetPetId, Long userId) {
        Pet myPet = petRepository.findById(myPetId)
                .orElseThrow(() -> new EntityNotFoundException("내 반려동물을 찾을 수 없습니다."));

        if (!myPet.getMember().getId().equals(userId)) {
            throw new IllegalArgumentException("해당 반려동물로 배틀할 권한이 없습니다.");
        }

        Pet targetPet = petRepository.findById(targetPetId)
                .orElseThrow(() -> new EntityNotFoundException("상대 반려동물을 찾을 수 없습니다."));

        // LLM 호출 로직
        String llmApiUrl = "http://llm-service/api/battle"; // LLM 서비스 URL
        String requestPayload = String.format(
                "{\"myPetDetail\": \"%s\", \"targetPetDetail\": \"%s\"}",
                myPet.getPetDetail(), targetPet.getPetDetail()
        );

        try {
            String response = restTemplate.postForObject(llmApiUrl, requestPayload, String.class);
            return response != null ? response : "배틀 결과를 가져오지 못했습니다.";
        } catch (Exception e) {
            log.error("LLM 호출 실패: {}", e.getMessage());
            throw new RuntimeException("LLM 호출 중 오류가 발생했습니다.");
        }
    }

    private String extractFileName(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return null;
        }

        if (imageUrl.contains("/pet_images/")) {
            return imageUrl.substring(imageUrl.lastIndexOf("/pet_images/") + "/pet_images/".length());
        }

        return imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
    }
}
