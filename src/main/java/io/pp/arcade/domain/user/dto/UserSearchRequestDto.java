package io.pp.arcade.domain.user.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Builder
@Getter
public class UserSearchRequestDto {
    @NotNull(message = "")
    String intraId;

    @Override
    public String toString() {
        return "UserSearchRequestDto{" +
                "intraId='" + intraId + '\'' +
                '}';
    }
}
