package pawparazzi.back.battle.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pawparazzi.back.battle.entity.Battle;
import pawparazzi.back.pet.entity.Pet;

import java.util.List;

@Repository
public interface BattleRepository extends JpaRepository<Battle, Long> {
    // 배틀 결과 조회
    List<Battle> findAllByPet1(Pet pet1);
    List<Battle> findAllByPet2(Pet pet2);
}
