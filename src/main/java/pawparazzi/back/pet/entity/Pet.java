package pawparazzi.back.pet.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import pawparazzi.back.member.entity.Member;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long petId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Type type;

    @Column(nullable = false)
    private LocalDate birthDate;

    private String petImg;

    @Column(length = 100)
    private String petDetail;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private Member member;

    public void setMember(Member member) {
        this.member = member;
        if (member != null && !member.getPets().contains(this)) {
            member.getPets().add(this);
        }
    }

    public Pet(String name, Type type, LocalDate birthDate, String petImg, Member member) {
        this.name = name;
        this.type = type;
        this.birthDate = birthDate;
        this.petImg = petImg;
        this.member = member;
    }
}
