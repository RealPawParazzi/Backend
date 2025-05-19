package pawparazzi.back.diary.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pawparazzi.back.diary.entity.Diary;
import pawparazzi.back.member.entity.Member;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long> {

    List<Diary> findByMember(Member member);
    Optional<Diary> findById(Long id);

}
