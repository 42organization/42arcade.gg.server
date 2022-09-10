package io.pp.arcade.domain.game.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameResultUserPageRequestDto {
    private Integer gameId = Integer.MAX_VALUE;
    private Integer count = 20;

    public Integer getGameId() {
        return gameId < 1 ? Integer.MAX_VALUE : gameId;
    }

    public Integer getCount() {
        if (count > 100)
            count = 100;
        else if (count < 1)
            count = 20;
        return count;
    }

    @Override
    public String toString() {
        return "GameResultUserPageRequestDto{" +
                "gameId=" + gameId +
                ", count=" + count +
                '}';
    }
}
