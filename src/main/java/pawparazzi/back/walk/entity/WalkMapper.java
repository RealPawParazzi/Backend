package pawparazzi.back.walk.entity;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pawparazzi.back.pet.entity.Pet;
import pawparazzi.back.pet.repository.PetRepository;
import pawparazzi.back.walk.dto.LocationPointDto;
import pawparazzi.back.walk.dto.WalkRequestDto;
import pawparazzi.back.walk.dto.WalkResponseDto;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class WalkMapper {

    private final PetRepository petRepository;

    public Walk toEntity(WalkRequestDto dto) {
        Pet pet = petRepository.findById(dto.getPetId())
                .orElseThrow(() -> new IllegalArgumentException("Pet not found with id: " + dto.getPetId()));

        Walk walk = new Walk();
        walk.setPet(pet);
        walk.setStartTime(dto.getStartTime());
        walk.setEndTime(dto.getEndTime());
        walk.setDistance(dto.getDistance());
        walk.setAverageSpeed(dto.getAverageSpeed());

        List<LocationPoint> locationPoints = dto.getRoute().stream().map(pointDto -> {
            LocationPoint point = new LocationPoint();
            point.setLatitude(pointDto.getLatitude());
            point.setLongitude(pointDto.getLongitude());
            point.setTimestamp(pointDto.getTimestamp());
            point.setWalk(walk);
            return point;
        }).collect(Collectors.toList());

        walk.setRoute(locationPoints);
        return walk;
    }

    public WalkResponseDto toDto(Walk walk) {
        return new WalkResponseDto(
                walk.getId(),
                walk.getPet(),
                walk.getStartTime(),
                walk.getEndTime(),
                walk.getRoute().stream().map(this::toLocationPointDto).collect(Collectors.toList()),
                walk.getDistance(),
                walk.getAverageSpeed()
        );
    }

    private LocationPointDto toLocationPointDto(LocationPoint point) {
        return new LocationPointDto(
                point.getLatitude(),
                point.getLongitude(),
                point.getTimestamp()
        );
    }
}
