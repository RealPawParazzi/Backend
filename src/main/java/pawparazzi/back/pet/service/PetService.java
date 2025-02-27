package pawparazzi.back.pet.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pawparazzi.back.member.entity.Member;
import pawparazzi.back.member.repository.MemberRepository;
import pawparazzi.back.member.service.MemberService;
import pawparazzi.back.pet.dto.PetRegisterRequestDto;
import pawparazzi.back.pet.dto.PetUpdateDto;
import pawparazzi.back.pet.entity.Pet;
import pawparazzi.back.pet.repository.PetRepository;
import pawparazzi.back.security.util.JwtUtil;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PetService {

    private final PetRepository petRepository;
    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;

    //펫 등록

    @Transactional
    public Pet registerPet(PetRegisterRequestDto registerDto, String token) {
        Long userId = jwtUtil.extractMemberId(token.replace("Bearer ", ""));

        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Pet pet = new Pet();
        pet.setName(registerDto.getName());
        pet.setType(registerDto.getType());
        pet.setBirthDate(registerDto.getBirthDate());
        pet.setPetImg(registerDto.getPetImg());
        pet.setMember(member);

        return petRepository.save(pet);
    }

    //펫 조회
    @Transactional(readOnly = true)
    public List<Pet> getPetsByMember(String token) {
        Long userId = jwtUtil.extractMemberId(token.replace("Bearer ", ""));

        memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        System.out.println("userId = " + userId);

        List<Pet> pets = petRepository.findPetsWithMemberByUserId(userId);

        for(Pet pet : pets) {
            System.out.println("pet.getName() = " + pet.getName());
            System.out.println("pet.getMember().getNickName() = " + pet.getMember().getNickName());
            System.out.println("pet.getMember().getNickName() = " + pet.getMember().getEmail());
        }

        for (Pet pet : pets) {
            if (pet.getMember() != null) {
                // Member 엔티티 강제 초기화
                pet.getMember().getNickName();
                pet.getMember().getEmail();
            }
        }
        return pets;
    }

    //펫 상세조회
    @Transactional(readOnly = true)
    public Pet getPetById(String token, Long petId) {
        Long userId = jwtUtil.extractMemberId(token.replace("Bearer ", ""));

        memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return petRepository.findById(petId)
                .orElseThrow(() -> new IllegalArgumentException("Pet not found"));
    }

    @Transactional
    public Pet updatePet(Long petId, PetUpdateDto updateDto, String token) {
        Long userId = jwtUtil.extractMemberId(token.replace("Bearer ", ""));

        memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Pet pet = getPetById(token, petId);
        pet.setName(updateDto.getName());
        pet.setType(updateDto.getType());
        pet.setBirthDate(updateDto.getBirthDate());
        pet.setPetImg(updateDto.getPetImg());
        return petRepository.save(pet);
    }

    @Transactional
    public void deletePet(Long petId, String token) {
        Long userId = jwtUtil.extractMemberId(token.replace("Bearer ", ""));

        memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Pet pet = getPetById(token, petId);
        pet.getMember().getPets().remove(pet);
        petRepository.delete(pet);
    }

}
