package pawparazzi.back.likes.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LikeMemberDto {
    private Long memberId;
    private String nickname;
    private String profileImageUrl;
}