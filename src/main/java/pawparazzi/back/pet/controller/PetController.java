package pawparazzi.back.pet.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pawparazzi.back.member.entity.Member;
import pawparazzi.back.member.service.MemberService;
import pawparazzi.back.pet.dto.PetRegisterRequestDto;
import pawparazzi.back.pet.dto.PetResponseDto;
import pawparazzi.back.pet.dto.PetUpdateDto;
import pawparazzi.back.pet.entity.Pet;
import pawparazzi.back.pet.service.PetService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pets")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;
    private final MemberService memberService;

    //펫 등록
    @PostMapping("/register")
    public ResponseEntity<PetResponseDto> registerPet(
            @RequestHeader ("Authorization") String token,
            @RequestBody PetRegisterRequestDto registerDto){

        Pet pet = petService.registerPet(registerDto, token);

        if(pet.getMember() != null) {
            pet.getMember().getNickName();
            pet.getMember().getEmail();
        }
        return ResponseEntity.ok(new PetResponseDto(pet));
    }

    //회원별 전체 펫 조회
    @GetMapping("/all")
    public ResponseEntity<List<PetResponseDto>> petList(
            @RequestHeader("Authorization") String token
    ){
        List<Pet> pets = petService.getPetsByMember(token);
        List<PetResponseDto> response = pets.stream()
                .map(PetResponseDto::new)
                .toList();

        return ResponseEntity.ok(response);
    }

    //펫 상세조회
    @GetMapping("/{petId}")
    public ResponseEntity<PetResponseDto> getPet(
            @PathVariable Long petId,
            @RequestHeader("Authorization") String token){

        Pet pet = petService.getPetById(token, petId);
        return ResponseEntity.ok(new PetResponseDto(pet));
    }

    //반려동물 정보 수정
    @PutMapping("/{petId}")
    public ResponseEntity<PetResponseDto> updatePet(
            @PathVariable Long petId,
            @RequestBody PetUpdateDto updateDto,
            @RequestHeader("Authorization") String token
    ){
        Pet updatedPet = petService.updatePet(petId, updateDto, token);
        return ResponseEntity.ok(new PetResponseDto(updatedPet));
    }

    //반려동물 삭제
    @DeleteMapping("/{petId}")
    public ResponseEntity<Void> deletePet(
            @PathVariable Long petId,
            @RequestHeader("Authorization") String token){
        petService.deletePet(petId, token);
        return ResponseEntity.noContent().build();
    }

}
