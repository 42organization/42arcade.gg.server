package io.pp.arcade.domain.pchange.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PChangePageDto {
    List<PChangeDto> pChangeList;
    Integer currentPage;
    Integer totalPage;

    @Override
    public String toString() {
        return "PChangePageDto{" +
                "pChangeList=" + pChangeList +
                ", currentPage=" + currentPage +
                ", totalPage=" + totalPage +
                '}';
    }
}
