package pawparazzi.back.pet.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import pawparazzi.back.pet.entity.Pet;
import pawparazzi.back.pet.entity.Type;

import java.time.LocalDate;

@Getter
@Setter
public class PetResponseDto {

    private Long petId;
    private String name;
    private Type type;
    private LocalDate birthDate;
    private String petImg;
    private MemberInfo member;

    public PetResponseDto(Pet pet) {
        this.petId = pet.getPetId();
        this.name = pet.getName();
        this.type = pet.getType();
        this.birthDate = pet.getBirthDate();
        this.petImg = pet.getPetImg();
        this.member = new MemberInfo(pet.getMember().getNickName(), pet.getMember().getEmail());
    }

    @Getter
    @Setter
    public static class MemberInfo {
        private String name;
        private String email;

        public MemberInfo(String name, String email) {
            this.name = name;
            this.email = email;
        }
    }
}
