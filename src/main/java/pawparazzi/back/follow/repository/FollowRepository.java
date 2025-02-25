package pawparazzi.back.follow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pawparazzi.back.follow.entity.Follow;
import pawparazzi.back.member.entity.Member;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    List<Follow> findByFollower(Member member);

    List<Follow> findByFollowing(Member member);

    Optional<Follow> findByFollowerAndFollowing(Member follower, Member following);

}
