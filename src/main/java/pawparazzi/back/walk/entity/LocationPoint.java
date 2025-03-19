package pawparazzi.back.walk.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pawparazzi.back.pet.entity.Pet;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Entity
@Table(name = "location_points")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LocationPoint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "walk_id")
    private Walk walk;
}