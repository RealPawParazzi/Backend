package pawparazzi.back.follow.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 언팔로우 시 반환하는 DTO
 * unfollow 메서드에서 사용
 */
@Getter
@Setter
public class UnfollowResponseDto {
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
