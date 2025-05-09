package pawparazzi.back.member.dto;

import lombok.*;

@Data
@AllArgsConstructor
@Builder
public class NaverUserDto {
    private String id;
    private String email;
    private String name;
    private String profileImage;
}
