package io.pp.arcade.domain.noti.dto;

import io.pp.arcade.domain.slot.dto.SlotDto;
import io.pp.arcade.global.type.NotiType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotiCanceledTypeDto {
    SlotDto slotDto;
    NotiType notiType;

    @Override
    public String toString() {
        return "NotiCanceledTypeDto{" +
                "slotDto=" + slotDto +
                ", notiType=" + notiType +
                '}';
    }
}
