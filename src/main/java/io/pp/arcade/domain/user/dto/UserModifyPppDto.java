package io.pp.arcade.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserModifyPppDto {
    private Integer userId;
    private Integer ppp;

    @Override
    public String toString() {
        return "UserModifyPppDto{" +
                "userId=" + userId +
                ", ppp=" + ppp +
                '}';
    }
}
