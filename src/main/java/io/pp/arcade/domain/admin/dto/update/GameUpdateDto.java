package io.pp.arcade.domain.admin.dto.update;

import io.pp.arcade.global.type.GameType;
import io.pp.arcade.global.type.StatusType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class GameUpdateDto {
    private Integer gameId;
    private Integer team1Id;
    private Integer team2Id;
    private LocalDateTime time;
}
