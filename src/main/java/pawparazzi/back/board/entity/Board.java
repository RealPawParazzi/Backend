package pawparazzi.back.board.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pawparazzi.back.comment.entity.Comment;
import pawparazzi.back.likes.entity.Like;
import pawparazzi.back.member.entity.Member;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String mongoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member author;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BoardVisibility visibility;

    @Column(nullable = false)
    private int favoriteCount = 0;

    @Column(nullable = false)
    private int commentCount = 0;

    @Column(nullable = false)
    private int viewCount = 0;

    @Column(nullable = false)
    private LocalDateTime writeDatetime = LocalDateTime.now().withNano(0);;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Like> likes = new ArrayList<>();

    public void increaseFavoriteCount() {
        this.favoriteCount++;
    }

    public void decreaseFavoriteCount() {
        if (this.favoriteCount > 0) {
            this.favoriteCount--;
        }
    }

    public void increaseCommentCount() {
        this.commentCount++;
    }

    public void decreaseCommentCount() {
        if (this.commentCount > 0) {
            this.commentCount--;
        }
    }

    public Board(Member author, String mongoId, BoardVisibility visibility) {
        this.author = author;
        this.mongoId = mongoId;
        this.visibility = visibility != null ? visibility : BoardVisibility.PUBLIC;
    }
}