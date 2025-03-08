package pawparazzi.back.comment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import pawparazzi.back.board.entity.Board;
import pawparazzi.back.member.entity.Member;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@DynamicUpdate
@Table(name = "comment")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(nullable = false)
    private int likeCount = 0;

    @Column(nullable = false)
    private int replyCount = 0;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Comment(Board board, Member member, String content) {
        this.board = board;
        this.member = member;
        this.content = content;
        this.createdAt = LocalDateTime.now().withNano(0);
        this.updatedAt = LocalDateTime.now().withNano(0);
    }

    public void updateContent(String content) {
        this.content = content;
        this.updatedAt = LocalDateTime.now().withNano(0);
    }

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    public void increaseReplyCount() {
        this.replyCount++;
    }

    public void decreaseReplyCount() {
        if (this.replyCount > 0) {
            this.replyCount--;
        }
    }
}