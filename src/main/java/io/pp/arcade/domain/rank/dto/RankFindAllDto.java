package io.pp.arcade.domain.rank.dto;

import io.pp.arcade.global.type.GameType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RankFindAllDto {
    GameType gameType;

    @Override
    public String toString() {
        return "RankFindAllDto{" +
                "gameType=" + gameType +
                '}';
    }
}
