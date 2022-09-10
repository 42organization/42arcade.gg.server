package io.pp.arcade.domain.admin.dto.create;

import io.pp.arcade.domain.game.Game;
import io.pp.arcade.global.type.GameType;
import io.pp.arcade.global.type.SlotStatusType;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class SlotCreateRequestDto {
    private Integer tableId;
    private Integer team1Id;
    private Integer team2Id;
    private LocalDateTime time;
    private Integer gamePpp;
    private Integer headCount;
    private GameType type;
}
