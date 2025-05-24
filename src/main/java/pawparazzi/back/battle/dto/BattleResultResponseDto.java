package pawparazzi.back.battle.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BattleResultResponseDto {
    @JsonProperty("battleId")
    private Long battleId;

    @JsonProperty("winner")
    private String winner;

    @JsonProperty("result")
    private String result;

    public BattleResultResponseDto(Long battleId, String winner, String result) {
        this.battleId = battleId;
        this.winner = winner;
        this.result = result;
    }


}
