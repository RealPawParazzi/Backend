package pawparazzi.back.pet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotNull
    private Type type;

    @NotNull
    private LocalDate birthDate;

    private String petImg;
}
