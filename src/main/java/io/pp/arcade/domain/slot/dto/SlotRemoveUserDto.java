package io.pp.arcade.domain.slot.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SlotRemoveUserDto {
    private Integer slotId;
    private String userId;
    private Integer exitUserPpp;

    @Override
    public String toString() {
        return "SlotRemoveUserDto{" +
                "slotId=" + slotId +
                ", userId='" + userId + '\'' +
                ", exitUserPpp=" + exitUserPpp +
                '}';
    }
}
