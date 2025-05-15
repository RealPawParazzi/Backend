package pawparazzi.back.battle.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BattleResponseDto {

    private Long battleId;
    private Long winnerId;
    private Long loserId;
    private String battleResult;
    private String battleDate;

    public BattleResponseDto(Long battleId, Long winnerId, Long loserId, String battleResult, String battleDate) {
        this.battleId = battleId;
        this.winnerId = winnerId;
        this.loserId = loserId;
        this.battleResult = battleResult;
        this.battleDate = battleDate;
    }
}
