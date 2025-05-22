package pawparazzi.back.battle.dto;

import lombok.Getter;
import lombok.Setter;
import pawparazzi.back.battle.entity.Battle;
import pawparazzi.back.pet.entity.Pet;

@Getter
@Setter
public class BattleResponseDto {

    private Long battleId;
    private Long winnerId;
    private Long loserId;
    private String battleResult;
    private String runwayPrompt;
    private PetInfo pet1;
    private PetInfo pet2;

    public BattleResponseDto(Battle battle) {
        this.battleId = battle.getBattleId();
        this.winnerId = battle.getWinnerId();
        this.loserId = battle.getLoserId();
        this.battleResult = battle.getBattleResult();
        this.runwayPrompt = battle.getRunwayPrompt();
        this.pet1 = new PetInfo(battle.getPet1());
        this.pet2 = new PetInfo(battle.getPet2());
    }

    @Getter
    @Setter
    public static class PetInfo {
        private Long petId;
        private String name;
        private String type;
        private String petImg;
        private Integer winCount;
        private Integer loseCount;

        public PetInfo(Pet pet) {
            this.petId = pet.getPetId();
            this.name = pet.getName();
            this.type = pet.getType().toString();
            this.petImg = pet.getPetImg();
            this.winCount = pet.getWinCount();
            this.loseCount = pet.getLoseCount();
        }
    }
}