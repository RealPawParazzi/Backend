package pawparazzi.back.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pawparazzi.back.member.entity.RefreshToken;
import pawparazzi.back.member.entity.Member;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByMember(Member member);
    Optional<RefreshToken> findByToken(String token);
    void deleteByMember(Member member);
}