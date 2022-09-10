package io.pp.arcade.domain.rank.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RankAddDto {
    private Integer userId;
    private Integer ppp;

    @Override
    public String toString() {
        return "RankAddDto{" +
                "userId=" + userId +
                ", ppp=" + ppp +
                '}';
    }
}
