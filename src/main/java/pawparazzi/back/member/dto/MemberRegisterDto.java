package pawparazzi.back.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import pawparazzi.back.member.entity.Member;

@Getter
@Setter
public class MemberRegisterDto {
    @NotBlank
    private String name;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    private String userImg;

    public Member toMember() {
        return Member.builder()
                .name(name)
                .email(email)
                .password(password)
                .userImg(userImg)
                .build();
    }
}
