package io.pp.arcade.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class UserHistoricResponseDto {
    List<UserHistoricDto> historics;

    @Override
    public String toString() {
        return "UserHistoricResponseDto{" +
                "historics=" + historics +
                '}';
    }
}
