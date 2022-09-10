package io.pp.arcade.domain.admin.dto.update;

import lombok.Builder;
import lombok.Getter;

@Getter
public class TeamUpdateRequestDto {
    private Integer teamId;
    private Integer user1Id;
    private Integer user2Id;
    private Integer teamPpp;
    private Integer headCount;
    private Integer score;
    private Boolean win;
}
