package pawparazzi.back.pet.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import pawparazzi.back.pet.entity.Pet;
import pawparazzi.back.pet.entity.Type;

import java.time.LocalDate;

@Getter
@Setter
public class PetUpdateDto {

    @NotBlank
    private String name;

    @NotBlank
    private Type type;

    @NotBlank
    private LocalDate birthDate;

    private String petImg;
}
