package pawparazzi.back.walk.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pawparazzi.back.pet.entity.Pet;
import pawparazzi.back.pet.repository.PetRepository;
import pawparazzi.back.walk.dto.WalkRequestDto;
import pawparazzi.back.walk.dto.WalkResponseDto;
import pawparazzi.back.walk.entity.Walk;
import pawparazzi.back.walk.entity.WalkMapper;
import pawparazzi.back.walk.repository.WalkRepository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WalkService {

    private final WalkRepository walkRepository;
    private final PetRepository petRepository;
    private final WalkMapper walkMapper;

    @Transactional
    public WalkResponseDto createWalk(WalkRequestDto requestDto, Long userId) {
        Pet pet = petRepository.findById(requestDto.getPetId())
                .orElseThrow(() -> new NoSuchElementException("Pet not found with Id: " + requestDto.getPetId()));

        if (!pet.getMember().getId().equals(userId)) {
            throw new NoSuchElementException("Pet does not belong to user");
        }

        Walk walk = walkMapper.toEntity(requestDto);
        walk.setPet(pet);

        Walk savedWalk = walkRepository.save(walk);
        return walkMapper.toDto(savedWalk);
    }

    @Transactional(readOnly = true)
    public WalkResponseDto getWalkById(Long walkId, Long userId){
        Walk walk = walkRepository.findById(walkId)
                .orElseThrow(() -> new NoSuchElementException("Walk not found with Id: " + walkId));

        if (!walk.getPet().getMember().getId().equals(userId)) {
            throw new NoSuchElementException("Walk does not belong to pet of user");
        }
        return walkMapper.toDto(walk);
    }

    @Transactional
    public void deleteWalk(Long walkId, Long userId){
        Walk walk = walkRepository.findById(walkId)
                .orElseThrow(() -> new NoSuchElementException("Walk not found with Id: " + walkId));

        if (!walk.getPet().getMember().getId().equals(userId)) {
            throw new NoSuchElementException("You don't have permission to delete this walk");
        }

        walkRepository.deleteById(walkId);
    }

    @Transactional(readOnly = true)
    public List<WalkResponseDto> getWalksByDate(ZonedDateTime date, Long userId) {
        List<Walk> walks = walkRepository.findByDate(date);

        // userId로 필터링: 해당 사용자의 반려동물 산책 기록만 반환
        return walks.stream()
                .filter(walk -> walk.getPet().getMember().getId().equals(userId))
                .map(walkMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WalkResponseDto> getWalksByPetId(Long petId, Long userId){
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new NoSuchElementException("Pet not found with Id: " + petId));

        // 펫 소유자 검증
        if (!pet.getMember().getId().equals(userId)) {
            throw new NoSuchElementException("You don't have permission to access this pet's walks");
        }

        // 리포지토리 메서드를 사용하여 산책 목록 조회
        List<Walk> walks = walkRepository.findByPetIdOrderByStartTimeDesc(petId);

        return walks.stream()
                .map(walkMapper::toDto)
                .collect(Collectors.toList());
    }
}