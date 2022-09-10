package io.pp.arcade.domain.admin.dto.update;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TeamUpdateDto {
    private Integer teamId;
    private Integer user1Id;
    private Integer user2Id;
    private Integer headCount;
    private Integer score;
    private Boolean win;
    private Integer teamPpp;
}
