package io.pp.arcade.domain.rank.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RankSaveAllDto {
    private List<RankRedisDto> rankRedisDtos;
    private Integer seasonId;

    @Override
    public String toString() {
        return "RankSaveAllDto{" +
                "rankRedisDtos=" + rankRedisDtos +
                ", seasonId=" + seasonId +
                '}';
    }
}
