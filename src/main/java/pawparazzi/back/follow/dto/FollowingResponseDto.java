package pawparazzi.back.follow.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FollowingResponseDto {
    private String followingNickName;
    private String followingName;
    private String followingProfileImageUrl;

    public FollowingResponseDto(String followingNickName, String followingName, String followingProfileImageUrl) {
        this.followingNickName = followingNickName;
        this.followingName = followingName;
        this.followingProfileImageUrl = followingProfileImageUrl;
    }
}
