package io.pp.arcade.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponseDto {
    private String intraId;
    private String userImageUri;
    private Boolean isAdmin;

    @Override
    public String toString() {
        return "UserResponseDto{" +
                "intraId='" + intraId + '\'' +
                ", userImageUri='" + userImageUri + '\'' +
                '}';
    }
}
