package io.pp.arcade.domain.rank.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class RankListDto {
    private Integer currentPage;
    private Integer totalPage;
    private List<RankUserDto> rankList;

    @Override
    public String toString() {
        return "RankListDto{" +
                "currentPage=" + currentPage +
                ", totalPage=" + totalPage +
                ", rankList=" + rankList +
                '}';
    }
}
