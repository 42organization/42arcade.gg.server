package io.pp.arcade.domain.rank.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RankUserDetailDto {
    private String intraId;
    private Integer rank;
    private Integer ppp;
    private String statusMessage;
    private Integer wins;
    private Integer losses;
    private Double winRate;

    @Override
    public String toString() {
        return "RankUserDetailDto{" +
                "intraId='" + intraId + '\'' +
                ", rank=" + rank +
                ", ppp=" + ppp +
                ", statusMessage='" + statusMessage + '\'' +
                ", wins=" + wins +
                ", losses=" + losses +
                ", winRate=" + winRate +
                '}';
    }
}
