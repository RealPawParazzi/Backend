package pawparazzi.back.video.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pawparazzi.back.video.entity.VideoRequest;

import java.util.List;
import java.util.Optional;

@Repository
public interface VideoRequestRepository extends JpaRepository<VideoRequest, Long> {
    List<VideoRequest> findByUserId(Long userId);
    Optional<VideoRequest> findByJobId(String jobId);

    List<VideoRequest> findAllByUserId(Long userId);

    List<VideoRequest> findAllByWinnerId(Long petId);
}
