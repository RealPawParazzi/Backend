package pawparazzi.back.follow.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 팔로우시 반환하는 DTO
 * follow 메서드에서 사용
 */
@Getter
@Setter
public class FollowResponseDto {
    private Long followerId;
    private Long followingId;
    private String followerNickName;
    private String followingNickName;
    private String followerProfileImageUrl;
    private String followingProfileImageUrl;
    private int followerCount;
    private int followingCount;
    private boolean followedStatus;
}
