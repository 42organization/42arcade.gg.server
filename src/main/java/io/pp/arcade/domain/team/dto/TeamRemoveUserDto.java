package io.pp.arcade.domain.team.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TeamRemoveUserDto {
    private Integer teamId;
    private Integer userId;

    @Override
    public String toString() {
        return "TeamRemoveUserDto{" +
                "teamId=" + teamId +
                ", userId=" + userId +
                '}';
    }
}


