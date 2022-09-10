package io.pp.arcade.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class UserSearchDto {
    String intraId;

    @Override
    public String toString() {
        return "UserSearchDto{" +
                "intraId='" + intraId + '\'' +
                '}';
    }
}
