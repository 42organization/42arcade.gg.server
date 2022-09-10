package io.pp.arcade.domain.noti.dto;

import io.pp.arcade.domain.user.dto.UserDto;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
public class NotiFindDto {
    UserDto user;
    Integer notiId;

    @Override
    public String toString() {
        return "NotiFindDto{" +
                "user=" + user +
                ", notiId=" + notiId +
                '}';
    }
}
