package io.pp.arcade.domain.game.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GameTeamDto {
    List<GamePlayerDto> players;
    private Boolean isWin;
    private Integer score;
}
