package pawparazzi.back.follow.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pawparazzi.back.follow.dto.FollowRequestDto;
import pawparazzi.back.follow.dto.FollowResponseDto;
import pawparazzi.back.follow.entity.Follow;
import pawparazzi.back.follow.repository.FollowRepository;
import pawparazzi.back.member.entity.Member;
import pawparazzi.back.member.repository.MemberRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public FollowResponseDto follow(FollowRequestDto requestDto) {
        Member follower = memberRepository.findById(requestDto.getFollowerId())
                .orElseThrow(() -> new IllegalArgumentException("팔로워가 존재하지 않습니다."));
        Member following = memberRepository.findById(requestDto.getFollowingId())
                .orElseThrow(() -> new IllegalArgumentException("팔로우할 사용자가 존재하지 않습니다."));

        if (followRepository.findByFollowerAndFollowing(follower, following).isPresent()) {
            throw new IllegalStateException("이미 팔로우한 사용자입니다.");
        }

        Follow follow = new Follow(follower, following);
        followRepository.save(follow);
        return new FollowResponseDto(follow.getId(), follower.getId(), following.getId());
    }

    @Transactional
    public void unfollow(Long followerId, Long followingId) {
        Member follower = memberRepository.findById(followerId)
                .orElseThrow(() -> new IllegalArgumentException("팔로워가 존재하지 않습니다."));
        Member following = memberRepository.findById(followingId)
                .orElseThrow(() -> new IllegalArgumentException("팔로우할 사용자가 존재하지 않습니다."));

        Follow follow = (Follow) followRepository.findByFollowerAndFollowing(follower, following)
                .orElseThrow(() -> new IllegalArgumentException("팔로우 관계가 존재하지 않습니다."));

        followRepository.delete(follow);
    }

    public List<FollowResponseDto> getFollowers(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));

        return followRepository.findByFollowing(member).stream()
                .map(f -> new FollowResponseDto(f.getId(), f.getFollower().getId(), f.getFollowing().getId()))
                .collect(Collectors.toList());
    }

    public List<FollowResponseDto> getFollowing(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));

        return followRepository.findByFollower(member).stream()
                .map(f -> new FollowResponseDto(f.getId(), f.getFollower().getId(), f.getFollowing().getId()))
                .collect(Collectors.toList());
    }
}
