package io.pp.arcade.domain.admin.dto.update;

import io.pp.arcade.global.type.RacketType;
import lombok.Getter;

@Getter
public class RankUpdateRequestDto {
    private Integer rankId;
    private Integer ppp;
    private Integer wins;
    private Integer losses;
}
