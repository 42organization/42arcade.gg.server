package io.pp.arcade.domain.admin.dto.update;

import io.pp.arcade.global.type.GameType;
import io.pp.arcade.global.type.StatusType;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class GameUpdateRequestDto {
    private Integer gameId;
    private Integer team1Score;
    private Integer team2Score;
}
