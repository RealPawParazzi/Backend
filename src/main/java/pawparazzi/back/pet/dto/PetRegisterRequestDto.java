package pawparazzi.back.pet.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import pawparazzi.back.pet.entity.Type;

import java.time.LocalDate;

@Getter
@Setter
public class PetRegisterRequestDto {

    @NotBlank
    private String name;

    @NotNull
    private Type type;

    private LocalDate birthDate;

    @Size(max = 100)
    private String petDetail;

}
