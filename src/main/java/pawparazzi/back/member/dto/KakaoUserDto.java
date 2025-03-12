package pawparazzi.back.member.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class KakaoUserDto {
    private Long id;
    private String email;
    private String nickname;
    private String profileImageUrl;
}
