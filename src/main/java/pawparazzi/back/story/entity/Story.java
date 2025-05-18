package pawparazzi.back.story.entity;

import jakarta.persistence.*;
import lombok.*;
import pawparazzi.back.member.entity.Member;

import java.time.LocalDateTime;

@Entity
@Table(name = "story")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Story {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private String mediaUrl;

    @Column(length = 500, nullable = true)
    private String caption;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private boolean expired;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.expired = false;
    }
}