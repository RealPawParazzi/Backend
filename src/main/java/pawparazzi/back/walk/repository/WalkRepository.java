package pawparazzi.back.walk.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pawparazzi.back.walk.entity.Walk;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface WalkRepository extends JpaRepository<Walk, Long> {
        // 반려동물 ID로 산책 목록 조회
        @Query("SELECT w FROM Walk w WHERE w.pet.petId = :petId ORDER BY w.startTime DESC")
        List<Walk> findByPetIdOrderByStartTimeDesc(@Param("petId") Long petId);

        // 특정 날짜의 산책 조회 (날짜만 비교)
        @Query("SELECT w FROM Walk w WHERE FUNCTION('DATE', w.startTime) = FUNCTION('DATE', :date) ORDER BY w.startTime DESC")
        List<Walk> findByDate(@Param("date") ZonedDateTime date);

        // Add this method to WalkRepository interface
        @Query("SELECT w FROM Walk w WHERE w.pet.petId = :petId AND FUNCTION('DATE', w.startTime) = FUNCTION('DATE', :date) ORDER BY w.startTime DESC")
        List<Walk> findByPetIdAndDate(@Param("petId") Long petId, @Param("date") ZonedDateTime date);
}
