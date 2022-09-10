package io.pp.arcade.domain.noti.dto;

import io.pp.arcade.domain.slot.dto.SlotDto;
import io.pp.arcade.domain.user.dto.UserDto;
import io.pp.arcade.global.type.NotiType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotiAddDto {
    private UserDto user;
    private SlotDto slot;
    private NotiType type;
    private String message;

    @Override
    public String toString() {
        return "NotiAddDto{" +
                "user=" + user +
                ", slot=" + slot +
                ", type=" + type +
                ", message='" + message + '\'' +
                '}';
    }
}
