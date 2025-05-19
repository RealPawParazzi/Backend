package pawparazzi.back.backoffice.dto.member;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pawparazzi.back.member.entity.Member;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberResponse {

    private Long id;
    private String email;
    private String nickName;
    private String name;
    private String profileImageUrl;

    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getEmail(),
                member.getNickName(),
                member.getName(),
                member.getProfileImageUrl()
        );
    }
}
