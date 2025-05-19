package pawparazzi.back.inquiry.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pawparazzi.back.inquiry.entity.Inquiry;
import java.util.List;

@Repository
public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
    List<Inquiry> findAllByMemberId(Long memberId);
}
