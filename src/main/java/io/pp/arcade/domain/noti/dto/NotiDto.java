package io.pp.arcade.domain.noti.dto;

import io.pp.arcade.domain.noti.Noti;
import io.pp.arcade.domain.slot.dto.SlotDto;
import io.pp.arcade.domain.user.dto.UserDto;
import io.pp.arcade.global.type.NotiType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotiDto {
    private Integer id;
    private UserDto user;
    private SlotDto slot;
    private NotiType type;
    private Boolean isChecked;
    private String message;
    private LocalDateTime creatdDate;

    public static NotiDto from(Noti noti) {
        return NotiDto.builder()
                .id(noti.getId())
                .user(UserDto.from(noti.getUser()))
                .slot(noti.getSlot() == null ? null : SlotDto.from(noti.getSlot()))
                .type(noti.getType())
                .isChecked(noti.getIsChecked())
                .message(noti.getMessage())
                .creatdDate(noti.getCreatedAt())
                .build();
    }

    @Override
    public String toString() {
        return "NotiDto{" +
                "id=" + id +
                ", user=" + user +
                ", slot=" + slot +
                ", type=" + type +
                ", isChecked=" + isChecked +
                ", message='" + message + '\'' +
                ", creatdDate=" + creatdDate +
                '}';
    }
}
