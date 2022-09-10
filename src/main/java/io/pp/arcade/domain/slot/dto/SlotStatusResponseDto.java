package io.pp.arcade.domain.slot.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SlotStatusResponseDto {
    Integer intervalMinute;
    List<List<SlotStatusDto>> matchBoards;

    @Override
    public String toString() {
        return "SlotStatusResponseDto{" +
                "intervalMinute=" + intervalMinute +
                ", matchBoards=" + matchBoards +
                '}';
    }
}
