package io.pp.arcade.domain.admin.dto.create;

import io.pp.arcade.global.type.RacketType;
import lombok.Getter;

@Getter
public class UserCreateRequestDto {
    String intraId;
    String email;
    String userImageUri;
    RacketType racketType;
    String statusMessage;
    Integer ppp;
}
