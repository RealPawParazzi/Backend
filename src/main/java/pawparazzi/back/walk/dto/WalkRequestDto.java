package pawparazzi.back.walk.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

@Data
public class WalkRequestDto {
    private Long petId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endTime;

    private List<LocationPointDto> route;

    private Double distance;

    private Double averageSpeed;
}