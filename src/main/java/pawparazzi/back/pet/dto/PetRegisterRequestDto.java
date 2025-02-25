package pawparazzi.back.pet.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import pawparazzi.back.pet.entity.Pet;
import pawparazzi.back.pet.entity.Type;

import java.time.LocalDate;

@Getter
@Setter
public class PetRegisterRequestDto {

    @NotBlank
    private String name;

    @NotBlank
    private Type type;

    @NotBlank
    private LocalDate birthDate;

    private String petImg;

    public Pet toPet() {
        return Pet.builder()
                .name(name)
                .type(type)
                .birthDate(birthDate)
                .petImg(petImg).build();
    }
}
