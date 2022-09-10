package io.pp.arcade.domain.currentmatch.dto;

import io.pp.arcade.domain.game.dto.GameDto;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CurrentMatchRemoveDto {
    private Integer userId;
    private GameDto game;

    @Override
    public String toString() {
        return "CurrentMatchRemoveDto{" +
                "userId=" + userId +
                ", game=" + game +
                '}';
    }
}
