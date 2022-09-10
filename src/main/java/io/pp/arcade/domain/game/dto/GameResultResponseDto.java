package io.pp.arcade.domain.game.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GameResultResponseDto {
    List<GameResultDto> games;
    Integer lastGameId;
    Integer currentPage;
    Integer totalPage;

    @Override
    public String toString() {
        return "GameResultResponseDto{" +
                "games=" + games +
                ", lastGameId=" + lastGameId +
                ", currentPage=" + currentPage +
                ", totalPage=" + totalPage +
                '}';
    }
}
