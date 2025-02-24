package pawparazzi.back.board.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class BoardOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = true)
    private BoardContent content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_id", nullable = true)
    private BoardMedia media;

    @Column(nullable = false)
    private int orderIndex;

    public BoardOrder(Board board, BoardContent content, BoardMedia media, int orderIndex) {
        this.board = board;
        this.content = content;
        this.media = media;
        this.orderIndex = orderIndex;
    }
}