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
    private String followerNickName;
    private String followerName;
    private String followerProfileImageUrl;

    public FollowerResponseDto(String followerNickName, String followerName, String followerProfileImageUrl) {
        this.followerNickName = followerNickName;
        this.followerName = followerName;
        this.followerProfileImageUrl = followerProfileImageUrl;
    }
}
