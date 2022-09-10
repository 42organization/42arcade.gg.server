package io.pp.arcade.domain.game.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GameResultPageDto {
    List<GameDto> gameList;
    Integer currentPage;
    Integer totalPage;

    @Override
    public String toString() {
        return "GameResultPageDto{" +
                "gameList=" + gameList +
                ", currentPage=" + currentPage +
                ", totalPage=" + totalPage +
                '}';
    }
}
