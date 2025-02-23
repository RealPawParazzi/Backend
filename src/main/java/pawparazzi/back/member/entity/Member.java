package pawparazzi.back.member.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password; // 암호화 저장 필요

    @Column(nullable = false, unique = true)
    private String nickName;

    @Column
    private String profileImageUrl;

    public Member(String email, String password, String nickName, String profileImageUrl) {
        this.email = email;
        this.password = password;
        this.nickName = nickName;
        this.profileImageUrl = profileImageUrl;
    }
}