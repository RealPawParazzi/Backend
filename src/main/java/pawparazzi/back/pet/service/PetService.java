package pawparazzi.back.pet.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pawparazzi.back.member.entity.Member;
import pawparazzi.back.pet.dto.PetRegisterRequestDto;
import pawparazzi.back.pet.dto.PetUpdateDto;
import pawparazzi.back.pet.entity.Pet;
import pawparazzi.back.pet.exception.DuplicatedPetException;
import pawparazzi.back.pet.repository.PetRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PetService {

    private final PetRepository petRepository;

    //펫 등록
    @Transactional
    public Pet registerPet(Member member, PetRegisterRequestDto registerDto) {
        Pet pet = registerDto.toPet();
        member.addPet(pet);
        return petRepository.save(pet);
    }

    //펫 조회
    @Transactional(readOnly = true)
    public List<Pet> getPetsByMember(Long id) {
        return petRepository.findByMemberId(id);
    }

    //펫 상세조회
    @Transactional(readOnly = true)
    public Pet getPetById(Long id) {
        return petRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pet not found"));
    }

    @Transactional
    public Pet updatePet(Long petId, PetUpdateDto updateDto) {
        Pet pet = getPetById(petId);
        pet.setName(updateDto.getName());
        pet.setType(updateDto.getType());
        pet.setBirthDate(updateDto.getBirthDate());
        pet.setPetImg(updateDto.getPetImg());
        return petRepository.save(pet);
    }

    @Transactional
    public void deletePet(Long petId) {
        Pet pet = getPetById(petId);
        pet.getMember().getPets().remove(pet);
        petRepository.delete(pet);
    }

}
