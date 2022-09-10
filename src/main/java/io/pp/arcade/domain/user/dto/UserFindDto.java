package io.pp.arcade.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class UserFindDto {
    String intraId;
    Integer userId;

    @Override
    public String toString() {
        return "UserFindDto{" +
                "intraId='" + intraId + '\'' +
                ", userId=" + userId +
                '}';
    }
}