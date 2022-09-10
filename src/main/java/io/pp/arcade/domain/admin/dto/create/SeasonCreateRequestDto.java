package io.pp.arcade.domain.admin.dto.create;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class SeasonCreateRequestDto {
    private String seasonName;
    private LocalDateTime startTime;
    private Integer startPpp;
    private Integer pppGap;
}
