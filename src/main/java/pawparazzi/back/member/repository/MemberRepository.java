package pawparazzi.back.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pawparazzi.back.member.entity.Member;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
    Optional<Member> findByNickName(String nickName);
    boolean existsByEmail(String email);
    boolean existsByNickName(String nickname);

    @Query("SELECT MONTH(m.createdAt), COUNT(m) FROM Member m GROUP BY MONTH(m.createdAt)")
    List<Object[]> countMembersGroupedByMonth();
}