package pawparazzi.back.walk.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import pawparazzi.back.pet.entity.Pet;
import pawparazzi.back.pet.entity.Type;

import java.time.ZonedDateTime;
import java.util.List;

@Data
public class WalkResponseDto {
    private Long id;
    private PetDto pet;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "UTC")
    private ZonedDateTime startTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "UTC")
    private ZonedDateTime endTime;

    private List<LocationPointDto> route;
    private Double distance;
    private Double averageSpeed;

    @Data
    public static class PetDto {
        private Long petId;
        private String name;
        private Type type;
        private String petImg;

        public PetDto(Pet pet) {
            this.petId = pet.getPetId();
            this.name = pet.getName();
            this.type = pet.getType();
            this.petImg = pet.getPetImg();
        }
    }

    public WalkResponseDto(Long id, Pet pet, ZonedDateTime startTime, ZonedDateTime endTime, List<LocationPointDto> route, Double distance, Double averageSpeed) {
        this.id = id;
        this.pet = new PetDto(pet);
        this.startTime = startTime;
        this.endTime = endTime;
        this.route = route;
        this.distance = distance;
        this.averageSpeed = averageSpeed;
    }
}
