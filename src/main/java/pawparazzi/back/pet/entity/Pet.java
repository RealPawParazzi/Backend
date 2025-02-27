package pawparazzi.back.pet.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import pawparazzi.back.member.entity.Member;

import java.time.LocalDate;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Pet {

    @Id
    @GeneratedValue
    private Long petId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Type type;

    @Column(nullable = false)
    private LocalDate birthDate;

    private String petImg;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Member member;

//    public void setMember(Member member) {
//        this.member = member;
//        if(!member.getPets().contains(this)) {
//            member.getPets().add(this);
//        }
//    }

    public void setMember(Member member) {
        this.member = member;
        // 이미 포함되어 있는지 확인
        if(member != null && !member.getPets().contains(this)) {
            member.getPets().add(this);
        }
    }
}
