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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    @Builder
    public Pet (String name, Type type, LocalDate birthDate, String petImg) {
        this.name = name;
        this.type = type;
        this.birthDate = birthDate;
        this.petImg = petImg;
    }

    //연관관계 메서드
    public void setMember(Member member) {
        this.member = member;
        if(!member.getPets().contains(this)) {
            member.getPets().add(this);
        }
    }
}
