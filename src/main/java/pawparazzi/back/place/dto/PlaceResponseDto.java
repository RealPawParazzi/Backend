package pawparazzi.back.place.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pawparazzi.back.place.entity.Place;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlaceResponseDto {
    private Long id;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;

    public PlaceResponseDto(Place place) {
        this.id = place.getId();
        this.name = place.getName();
        this.address = place.getAddress();
        this.latitude = place.getLatitude();
        this.longitude = place.getLongitude();
    }
}
