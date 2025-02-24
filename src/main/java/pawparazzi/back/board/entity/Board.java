package pawparazzi.back.board.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pawparazzi.back.member.entity.Member;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // MySQL 게시글 ID (PK)

    @Column(nullable = false, unique = true)
    private String mongoId;  // MongoDB ObjectId

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member author;  // 작성자 정보 (MySQL 관리)

    @Column(nullable = false)
    private int favoriteCount = 0;  // 좋아요 개수

    @Column(nullable = false)
    private int commentCount = 0;   // 댓글 개수

    @Column(nullable = false)
    private int viewCount = 0;  // 조회수

    @Column(nullable = false)
    private LocalDateTime writeDatetime = LocalDateTime.now();  // 작성 시간

    public Board(Member author, String mongoId) {
        this.author = author;
        this.mongoId = mongoId;
    }
}