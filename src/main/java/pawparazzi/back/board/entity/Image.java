package pawparazzi.back.board.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity(name = "image")
@Table(name = "image")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "image", columnDefinition = "TEXT")
    private String image;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_number", nullable = false)
    private Board board;

    public Image(Board board, String imageUrl) {
        this.board = board;
        this.image = imageUrl;
    }
}