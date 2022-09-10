package io.pp.arcade.domain.admin.dto.update;

import io.pp.arcade.global.type.GameType;
import io.pp.arcade.global.type.SlotStatusType;
import io.pp.arcade.global.type.StatusType;
import lombok.Builder;
import lombok.Getter;

@Getter
public class SlotUpdateRequestDto {
    private Integer slotId;
    private Integer gamePpp;
    private Integer headCount;
    private GameType type;
}
