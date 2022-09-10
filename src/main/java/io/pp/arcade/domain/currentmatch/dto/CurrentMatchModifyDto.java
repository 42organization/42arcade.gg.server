package io.pp.arcade.domain.currentmatch.dto;

import io.pp.arcade.domain.game.dto.GameDto;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CurrentMatchModifyDto {
    private Integer userId;
    private Boolean isMatched;
    private Boolean matchImminent;
    private GameDto gameDto;

    @Override
    public String toString() {
        return "CurrentMatchModifyDto{" +
                "userId=" + userId +
                ", isMatched=" + isMatched +
                ", matchImminent=" + matchImminent +
                ", gameDto=" + gameDto +
                '}';
    }
}
