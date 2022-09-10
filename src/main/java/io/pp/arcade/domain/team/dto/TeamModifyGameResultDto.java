package io.pp.arcade.domain.team.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TeamModifyGameResultDto {
    private Integer teamId;
    private Integer score;
    private Boolean win;

    @Override
    public String toString() {
        return "TeamModifyGameResultDto{" +
                "teamId=" + teamId +
                ", score=" + score +
                ", win=" + win +
                '}';
    }
}
