package pawparazzi.back.pet.dto;

import lombok.Getter;
import lombok.Setter;
import pawparazzi.back.pet.entity.Pet;
import pawparazzi.back.pet.entity.Type;

import java.time.LocalDate;

@Getter
@Setter
public class PetRankingResponseDto {
    private Long petId;
    private String name;
    private Type type;
    private LocalDate birthDate;
    private String petImg;
    private Integer winCount;
    private PetResponseDto.MemberInfo member;

    public PetRankingResponseDto(Pet pet) {
        this.petId = pet.getPetId();
        this.name = pet.getName();
        this.type = pet.getType();
        this.birthDate = pet.getBirthDate();
        this.petImg = pet.getPetImg();
        this.winCount = pet.getWinCount();
        this.member = new PetResponseDto.MemberInfo(pet.getMember().getId(), pet.getMember().getNickName(), pet.getMember().getEmail());
    }
}
