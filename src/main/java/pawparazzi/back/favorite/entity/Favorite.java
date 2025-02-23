package pawparazzi.back.favorite.entity;

import jakarta.persistence.*;
import lombok.*;
import pawparazzi.back.member.entity.Member;
import pawparazzi.back.board.entity.Board;

@Entity(name = "favorite")
@Table(name = "favorite")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(Favorite.class)
public class Favorite {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email", referencedColumnName = "email", nullable = false)
    private Member member;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_number", nullable = false)
    private Board board;
}