package io.pp.arcade.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserAddDto {
    private String intraId;
    private String eMail;

    @Override
    public String toString() {
        return "UserAddDto{" +
                "intraId='" + intraId + '\'' +
                ", eMail='" + eMail + '\'' +
                '}';
    }
}
