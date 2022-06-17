package io.pp.arcade.domain.rank.dto;


import lombok.Builder;

import java.util.List;

@Builder
public class RankListResponseDto {
    private Integer myRank;
    private Integer currentPage;
    private Integer totalPage;
    private List<RankUserDto> rankList;
}
