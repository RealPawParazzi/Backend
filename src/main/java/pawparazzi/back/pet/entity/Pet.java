package pawparazzi.back.pet.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.netty.handler.codec.Headers;
import jakarta.persistence.*;
import lombok.*;
import pawparazzi.back.battle.entity.Battle;
import pawparazzi.back.member.entity.Member;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    private Integer winCount = 0;

    private Integer loseCount = 0;

    @Column(length = 100)
    private String petDetail;

    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL)
    private List<Battle> battleAsPet1 = new ArrayList<>();

    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL)
    private List<Battle> battleAsPet2 = new ArrayList<>();

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

    public void incrementWinCount() {
        if (this.winCount == null) {
            this.winCount = 1;
        } else {
            this.winCount = this.winCount + 1;
        }
    }

    public void incrementLoseCount() {
        if (this.loseCount == null) {
            this.loseCount = 1;
        } else {
            this.loseCount = this.loseCount + 1;
        }
    }
}
