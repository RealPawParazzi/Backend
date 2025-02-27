package pawparazzi.back.follow.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FollowRequestDto {
    private Long followerId;
    private Long followingId;
}
