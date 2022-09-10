package io.pp.arcade.domain.admin.dto.update;

import io.pp.arcade.global.type.NotiType;
import lombok.Getter;

@Getter
public class NotiUpdateRequestDto {
    private Integer notiId;
    private Integer userId;
    private Integer slotId;
    private NotiType notiType;
    private Boolean isChecked;
    private String message;
}
