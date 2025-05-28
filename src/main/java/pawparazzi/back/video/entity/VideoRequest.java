package pawparazzi.back.video.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "video_requests")
public class VideoRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 10000)
    private String prompt;

    @Convert(converter = StringListConverter.class)
    @Column(name = "image_url", length = 2000)
    private List<String> imageUrl;

    @Column(name = "winner_id")
    private Long winnerId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "job_id")
    private String jobId;

    @Column(name = "duration")
    private Integer duration;

    @Column
    private String status; //PENDING, PROCESSING, COMPLETED, FAILED

    @Column(name = "result_url")
    private String resultUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "error_message", length = 1000)  // 에러 메시지가 길 수 있으므로 충분한 길이 지정
    private String errorMessage;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        status = "PENDING";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
