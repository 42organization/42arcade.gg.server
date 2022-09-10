package io.pp.arcade.domain.rank.dto;

import io.pp.arcade.global.type.GameType;
import lombok.Getter;

@Getter
public class RankListRequestDto {
    private GameType type;

    @Override
    public String toString() {
        return "RankListRequestDto{" +
                "type=" + type +
                '}';
    }
}
