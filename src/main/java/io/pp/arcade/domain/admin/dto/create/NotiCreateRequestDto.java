package io.pp.arcade.domain.admin.dto.create;

import io.pp.arcade.global.type.NotiType;
import lombok.Getter;

@Getter
public class NotiCreateRequestDto {
    private Integer userId;
    private Integer slotId;
    private NotiType notiType;
    private Boolean isChecked;
    private String message;
    private Boolean sendMail;
}
