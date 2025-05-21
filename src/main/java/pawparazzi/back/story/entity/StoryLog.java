package pawparazzi.back.story.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "story_log")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class StoryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    public StoryLog( Long memberId, LocalDateTime createdAt) {
        this.memberId = memberId;
        this.createdAt = createdAt;}
}
