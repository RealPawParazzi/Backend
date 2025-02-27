package pawparazzi.back.pet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pawparazzi.back.pet.entity.Pet;

import java.util.List;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {
    @Query("select p from Pet p where p.member.id = :id")
    List<Pet> findByMemberId(Long id);

    boolean existsByName(String name);

    @Query("SELECT p FROM Pet p JOIN FETCH p.member WHERE p.member.id = :userId")
    List<Pet> findPetsWithMemberByUserId(Long userId);
}
