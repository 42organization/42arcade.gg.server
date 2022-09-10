package io.pp.arcade.domain.slot.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SlotAddDto {
    private LocalDateTime time;
    private Integer tableId;

    @Override
    public String toString() {
        return "SlotAddDto{" +
                "time=" + time +
                ", tableId=" + tableId +
                '}';
    }
}
