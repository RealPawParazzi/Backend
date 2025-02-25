package pawparazzi.back.follow.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FollowResponseDto {
    private Long id;
    private Long followerId;
    private Long followingId;

    public FollowResponseDto(Long id, Long followerId, Long followingId){
        this.id = id;
        this.followerId = followerId;
        this.followingId = followingId;
    }
}
