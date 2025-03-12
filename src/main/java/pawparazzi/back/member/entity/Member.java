package pawparazzi.back.member.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import pawparazzi.back.board.entity.Board;
import pawparazzi.back.follow.entity.Follow;
import pawparazzi.back.pet.entity.Pet;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "member")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String nickName;

    @Column
    private String name;

    @Column(name = "profile_image_url", length = 1024)
    private String profileImageUrl;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pet> pets = new ArrayList<>();

    @OneToMany(mappedBy = "following", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Follow> followerList = new ArrayList<>();

    @OneToMany(mappedBy = "follower", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Follow> followingList = new ArrayList<>();

    public Member(String email, String password, String nickName, String profileImageUrl, String name) {
        this.email = email;
        this.password = password;
        this.nickName = nickName;
        this.profileImageUrl = profileImageUrl;
        this.name = name;
    }

    //연관관계 메서드
    public void addPet(Pet pet) {
        pets.add(pet);
        pet.setMember(this);
    }
}