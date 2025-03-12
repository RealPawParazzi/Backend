package pawparazzi.back.follow.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FollowingResponseDto {
    private Long followingId;
    private String followingNickName;
    private String followingName;
    private String followingProfileImageUrl;

    public FollowingResponseDto(Long followingId, String followingNickName, String followingName, String followingProfileImageUrl) {
        this.followingId = followingId;
        this.followingNickName = followingNickName;
        this.followingName = followingName;
        this.followingProfileImageUrl = followingProfileImageUrl;
    }
}
