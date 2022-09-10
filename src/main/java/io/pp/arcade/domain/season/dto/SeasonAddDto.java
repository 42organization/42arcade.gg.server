package io.pp.arcade.domain.season.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SeasonAddDto {
    private String seasonName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer startPpp;

    @Override
    public String toString() {
        return "SeasonAddDto{" +
                "seasonName='" + seasonName + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", startPpp=" + startPpp +
                '}';
    }
}
