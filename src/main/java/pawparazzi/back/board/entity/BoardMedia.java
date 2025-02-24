package pawparazzi.back.board.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class BoardMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String mediaUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaType mediaType; // IMAGE or VIDEO

    public BoardMedia(Board board, String mediaUrl, MediaType mediaType) {
        this.board = board;
        this.mediaUrl = mediaUrl;
        this.mediaType = mediaType;
    }

    public enum MediaType {
        IMAGE, VIDEO
    }
}