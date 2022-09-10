package io.pp.arcade.domain.currentmatch.dto;

import io.pp.arcade.domain.game.dto.GameDto;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CurrentMatchFindDto {
    private GameDto game;
    private Integer userId;
    private String intraId;
}
