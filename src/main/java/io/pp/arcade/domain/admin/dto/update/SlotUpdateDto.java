package io.pp.arcade.domain.admin.dto.update;

import io.pp.arcade.global.type.GameType;
import io.pp.arcade.global.type.SlotStatusType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SlotUpdateDto {
    private Integer slotId;
    private Integer gamePpp;
    private Integer headCount;
    private GameType type;
}
