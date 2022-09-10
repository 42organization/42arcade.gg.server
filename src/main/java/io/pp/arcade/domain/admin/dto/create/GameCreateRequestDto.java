package io.pp.arcade.domain.admin.dto.create;

import io.pp.arcade.global.type.GameType;
import io.pp.arcade.global.type.StatusType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class GameCreateRequestDto {
    private Integer slotId;
    private Integer seasonId;
    private StatusType status;
}
