package io.pp.arcade.domain.admin.dto.create;

import io.pp.arcade.global.type.RacketType;
import lombok.Getter;

@Getter
public class RankCreateRequestDto {
    private Integer rankId;
    private Integer userId;
    private Integer ppp;
    private Integer seasonId;
    private Integer rangking;
    private RacketType racketType;
    private Integer wins;
    private Integer losses;
}
