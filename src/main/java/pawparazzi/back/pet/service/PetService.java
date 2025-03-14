package pawparazzi.back.pet.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pawparazzi.back.S3.S3UploadUtil;
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

    /**
     * 반려동물 등록
     */
    @Transactional
    public CompletableFuture<Pet> registerPet(Long userId, PetRegisterRequestDto registerDto, MultipartFile petImage) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        String pathPrefix = "pet_images/" + member.getNickName();
        String defaultImageUrl = "https://default-image-url.com/default-pet.png";

        CompletableFuture<String> petImageUrlFuture = s3UploadUtil.uploadImageAsync(petImage, pathPrefix, defaultImageUrl);

        return petImageUrlFuture.thenApply(petImageUrl -> {
            Pet pet = new Pet(registerDto.getName(), registerDto.getType(), registerDto.getBirthDate(), petImageUrl, member);
            return petRepository.save(pet);
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
    public PetResponseDto updatePet(Long petId, Long userId, PetUpdateDto updateDto) {
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new EntityNotFoundException("반려동물을 찾을 수 없습니다."));

        if (!pet.getMember().getId().equals(userId)) {
            throw new IllegalArgumentException("해당 반려동물을 수정할 권한이 없습니다.");
        }

        pet.setName(updateDto.getName());
        pet.setType(updateDto.getType());
        pet.setBirthDate(updateDto.getBirthDate());
        pet.setPetImg(updateDto.getPetImg());

        return new PetResponseDto(petRepository.save(pet));
    }

    /**
     * 반려동물 삭제
     */
    @Transactional
    public void deletePet(Long petId, Long userId) {
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new EntityNotFoundException("반려동물을 찾을 수 없습니다."));

        if (!pet.getMember().getId().equals(userId)) {
            throw new IllegalArgumentException("해당 반려동물을 삭제할 권한이 없습니다.");
        }

        petRepository.delete(pet);
    }
}