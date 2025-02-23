package pawparazzi.back.comment.entity;

import jakarta.persistence.*;
import lombok.*;
import pawparazzi.back.member.entity.Member;
import pawparazzi.back.board.entity.Board;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "comment")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentNumber;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private LocalDateTime writeDatetime;

    // 🔹 댓글 작성자(Member)와 관계 설정 (email을 FK로 사용)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email", referencedColumnName = "email", nullable = false)
    private Member member;

    // 🔹 댓글이 달린 게시글(Board)와 관계 설정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_number", nullable = false)
    private Board board;

    @PrePersist
    protected void onCreate() {
        this.writeDatetime = LocalDateTime.now();
    }
}