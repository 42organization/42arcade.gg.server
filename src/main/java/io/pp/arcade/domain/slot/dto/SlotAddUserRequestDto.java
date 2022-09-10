package io.pp.arcade.domain.slot.dto;

import lombok.Getter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Getter
public class SlotAddUserRequestDto {
    @NotNull
    @Positive
    private Integer slotId;

    @Override
    public String toString() {
        return "SlotAddUserRequestDto{" +
                "slotId=" + slotId +
                '}';
    }
}
