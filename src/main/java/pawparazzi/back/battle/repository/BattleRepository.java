package pawparazzi.back.battle.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pawparazzi.back.battle.entity.Battle;

@Repository
public interface BattleRepository extends JpaRepository<Battle, Long> {

}
