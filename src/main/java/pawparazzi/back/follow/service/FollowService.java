package pawparazzi.back.follow.service;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pawparazzi.back.follow.dto.FollowResponseDto;
import pawparazzi.back.follow.dto.FollowerResponseDto;
import pawparazzi.back.follow.dto.FollowingResponseDto;
import pawparazzi.back.follow.entity.Follow;
import pawparazzi.back.follow.repository.FollowRepository;
import pawparazzi.back.member.entity.Member;
import pawparazzi.back.member.repository.MemberRepository;
import pawparazzi.back.security.util.JwtUtil;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;

    @Transactional
    public FollowResponseDto follow(Long targetId, String token) {
        Long userId = jwtUtil.extractMemberId(token.replace("Bearer ", ""));
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Member following = memberRepository.findById(targetId)
                .orElseThrow(() -> new IllegalArgumentException("팔로우할 사용자가 존재하지 않습니다."));

        //추후 수정 필요 (특정 회원 프로필 정보에서 체크하여 메서드 활성화 및 비활성화)
        boolean isAlreadyFollowed = followRepository.findByFollowerAndFollowing(member, following).isPresent();
        if (isAlreadyFollowed) {
            throw new IllegalStateException("이미 팔로우한 사용자입니다.");
        }

        if(member.equals(following)) {
            throw new IllegalStateException("자기 자신은 팔로우할 수 없습니다.");
        }

        Follow follow = new Follow(member, following);
        followRepository.save(follow);
        int followerCount = followRepository.countByFollower(member);
        int followingCount = followRepository.countByFollowing(member);

        FollowResponseDto dto = getFollowResponseDto(member, following, followerCount, followingCount);
        dto.setFollowedStatus(true);
        return dto;
    }

    @Transactional
    public void unfollow(Long targetId, String token) {
        Long userId = jwtUtil.extractMemberId(token.replace("Bearer ", ""));
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Member following = memberRepository.findById(targetId)
                .orElseThrow(() -> new IllegalArgumentException("언팔로우할 사용자가 존재하지 않습니다."));

        //추후 수정 필요 (특정 회원 프로필 정보에서 체크하여 메서드 활성화 및 비활성화)
        Follow follow = followRepository.findByFollowerAndFollowing(member, following)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 팔로우하고 있지 않습니다."));

        followRepository.delete(follow);
    }

    @Transactional(readOnly = true)
    public List<FollowerResponseDto> getFollowers(Long targetId) {
        Member member = memberRepository.findById(targetId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<Follow> follower = followRepository.findByFollowing(member);

        return follower.stream()
                .map(follow -> {
                    FollowerResponseDto dto = new FollowerResponseDto();
                    dto.setFollowerNickName(follow.getFollower().getNickName());
                    dto.setFollowerName(follow.getFollower().getName());
                    dto.setFollowerProfileImageUrl(follow.getFollower().getProfileImageUrl());
                    return dto;
                }).toList();
    }

    @Transactional(readOnly = true)
    public List<FollowingResponseDto> getFollowing(Long targetId) {
        Member member = memberRepository.findById(targetId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<Follow> following = followRepository.findByFollower(member);

        return following.stream()
                .map(follow -> {
                    FollowingResponseDto dto = new FollowingResponseDto();
                    dto.setFollowingNickName(follow.getFollowing().getNickName());
                    dto.setFollowingName(follow.getFollowing().getName());
                    dto.setFollowingProfileImageUrl(follow.getFollowing().getProfileImageUrl());
                    return dto;
                }).toList();
    }

    @NotNull
    private static FollowResponseDto getFollowResponseDto(Member member, Member following, int followerCount, int followingCount) {
        FollowResponseDto responseDto = new FollowResponseDto();
        responseDto.setFollowerId(member.getId());
        responseDto.setFollowingId(following.getId());
        responseDto.setFollowerNickName(member.getNickName());
        responseDto.setFollowingNickName(following.getNickName());
        responseDto.setFollowerProfileImageUrl(member.getProfileImageUrl());
        responseDto.setFollowingProfileImageUrl(following.getProfileImageUrl());
        responseDto.setFollowerCount(followerCount);
        responseDto.setFollowingCount(followingCount);
        return responseDto;
    }

}
