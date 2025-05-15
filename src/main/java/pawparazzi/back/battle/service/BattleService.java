package pawparazzi.back.battle.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pawparazzi.back.battle.dto.BattleResponseDto;
import pawparazzi.back.battle.entity.Battle;
import pawparazzi.back.pet.entity.Pet;
import pawparazzi.back.pet.service.PetService;

import java.util.List;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class BattleService {
    private final PetService petService;
    private final JpaRepository<Battle, Long> battleRepository;

    @Transactional
    public void createBattle(Pet pet1, Pet pet2, String battleResult, String winner) {
        Long pet1Id = pet1.getPetId();
        Long pet2Id = pet2.getPetId();
        if(pet1Id == null || pet2Id == null) {
            throw new IllegalArgumentException("Invalid pet IDs");
        }

        if (Objects.equals(winner, pet1.getName())){
            makeBattle(pet1, pet2, battleResult, pet1Id, pet2Id);
        } else {
            makeBattle(pet1, pet2, battleResult, pet2Id, pet1Id);
        }
    }

    private void makeBattle(Pet pet1, Pet pet2, String battleResult, Long winnerId, Long loserId) {
        Battle battle = new Battle();
        battle.setPet1(pet1);
        battle.setPet2(pet2);
        battle.setWinnerId(winnerId);
        battle.setLoserId(loserId);
        battle.setBattleResult(battleResult);
        battleRepository.save(battle);
    }

}
