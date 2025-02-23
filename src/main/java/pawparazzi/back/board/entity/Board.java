package pawparazzi.back.board.entity;

import jakarta.persistence.*;
import lombok.*;
import pawparazzi.back.board.dto.PostBoardRequestDto;
import pawparazzi.back.member.entity.Member;

import java.time.LocalDateTime;

@Entity
@Table(name = "board")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long boardNumber;

    private String title;

    private String content;

    private LocalDateTime writeDatetime;

    private int favoriteCount;

    private int commentCount;

    private int viewCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", referencedColumnName = "id")
    private Member member;

    public Board(PostBoardRequestDto dto, Member member) {
        this.title = dto.getTitle();
        this.content = dto.getContent();
        this.writeDatetime = LocalDateTime.now();
        this.favoriteCount = 0;
        this.commentCount = 0;
        this.viewCount = 0;
        this.member = member;
    }
}