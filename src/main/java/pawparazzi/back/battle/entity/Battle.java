package pawparazzi.back.battle.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pawparazzi.back.pet.entity.Pet;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Battle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long battleId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pet1_id", nullable = false)
    private Pet pet1;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pet2_id", nullable = false)
    private Pet pet2;

    private Long winnerId;

    private Long loserId;

    private String battleResult;

    private String battleDate;

    public void connectWithPets(Pet pet1, Pet pet2){
        this.pet1 = pet1;
        this.pet2 = pet2;
        // pet1의 배틀 컬렉션에 추가
        if (pet1 != null && !pet1.getBattleAsPet1().contains(this)) {
            pet1.getBattleAsPet1().add(this);
        }

        // pet2의 배틀 컬렉션에 추가
        if (pet2 != null && !pet2.getBattleAsPet2().contains(this)) {
            pet2.getBattleAsPet2().add(this);
        }
    }

}
