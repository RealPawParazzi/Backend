package pawparazzi.back.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberLoginDto {

    @Email
    @NotBlank(message = "아이디(이메일)는 필수 값입니다")
    private String email;

    @NotBlank(message = "비밀번호는 필수 값입니다.")
    private String password;
}
