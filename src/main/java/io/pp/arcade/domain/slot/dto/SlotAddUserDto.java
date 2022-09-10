package io.pp.arcade.domain.slot.dto;

import io.pp.arcade.global.type.GameType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SlotAddUserDto {
    private Integer slotId;
    private GameType type;
    private Integer joinUserPpp;

    @Override
    public String toString() {
        return "SlotAddUserDto{" +
                "slotId=" + slotId +
                ", type=" + type +
                ", joinUserPpp=" + joinUserPpp +
                '}';
    }
}
