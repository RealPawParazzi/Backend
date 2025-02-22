package pawparazzi.back.member.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import pawparazzi.back.pet.entity.Pet;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue
    private Long userId;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String userImg;


    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pet> pets = new ArrayList<>();

    @Builder
    public Member(String name, String email, String password, String userImg) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.userImg = userImg;
    }

    //연관관계 메서드
    public void addPet(Pet pet) {
        pets.add(pet);
        pet.setMember(this);
    }
}
