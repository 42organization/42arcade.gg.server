package io.pp.arcade.domain.user.dto;

import io.pp.arcade.global.type.RacketType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserModifyProfileDto {
    private Integer userId;
    private String email;
    private String userImageUri;
    private RacketType racketType;
    private String statusMessage;

    @Override
    public String toString() {
        return "UserModifyProfileDto{" +
                "userId=" + userId +
                ", email='" + email + '\'' +
                ", userImageUri='" + userImageUri + '\'' +
                ", racketType=" + racketType +
                ", statusMessage='" + statusMessage + '\'' +
                '}';
    }
}
