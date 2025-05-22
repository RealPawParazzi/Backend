package pawparazzi.back.walk.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import pawparazzi.back.pet.entity.Pet;
import pawparazzi.back.pet.entity.Type;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

@Data
public class WalkResponseDto {
    private Long id;
    private Long memberId;
    private PetDto pet;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endTime;

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

    public WalkResponseDto(Long id, Long memberId, Pet pet, LocalDateTime startTime, LocalDateTime endTime, List<LocationPointDto> route, Double distance, Double averageSpeed) {
        this.id = id;
        this.memberId = memberId;
        this.pet = new PetDto(pet);
        this.startTime = startTime;
        this.endTime = endTime;
        this.route = route;
        this.distance = distance;
        this.averageSpeed = averageSpeed;
    }
}
