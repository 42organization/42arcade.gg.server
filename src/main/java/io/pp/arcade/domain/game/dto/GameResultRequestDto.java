package io.pp.arcade.domain.game.dto;

import io.pp.arcade.global.exception.BusinessException;
import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@ToString
@Getter
public class GameResultRequestDto {
    @Min(0)
    @Max(2)
    @NotNull
    private Integer myTeamScore;

    @Min(0)
    @Max(2)
    @NotNull
    private Integer enemyTeamScore;

    public Integer getMyTeamScore() {
        if (validate() == false) {
            throw new BusinessException("E0001");
        }
        return myTeamScore;
    }

    public Integer getEnemyTeamScore() {
        if (validate() == false) {
            throw new BusinessException("E0001");
        }
        return enemyTeamScore;
    }

    private boolean validate() {
        if (myTeamScore + enemyTeamScore > 3) {
            return false;
        } else if (myTeamScore == enemyTeamScore) {
            return false;
        }
        return true;
    }
}
