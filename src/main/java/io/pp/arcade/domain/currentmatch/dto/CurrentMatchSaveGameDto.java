package io.pp.arcade.domain.currentmatch.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CurrentMatchSaveGameDto {
    private Integer gameId;
    private Integer userId;

    @Override
    public String toString() {
        return "CurrentMatchSaveGameDto{" +
                "gameId=" + gameId +
                ", userId=" + userId +
                '}';
    }
}
