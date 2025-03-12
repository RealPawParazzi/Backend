package pawparazzi.back.follow.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 */
@Getter
@Setter
@NoArgsConstructor
public class FollowerResponseDto {
    private Long followerId;
    private String followerNickName;
    private String followerName;
    private String followerProfileImageUrl;

    public FollowerResponseDto(Long followerId, String followerNickName, String followerName, String followerProfileImageUrl) {
        this.followerId = followerId;
        this.followerNickName = followerNickName;
        this.followerName = followerName;
        this.followerProfileImageUrl = followerProfileImageUrl;
    }
}
