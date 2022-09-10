package io.pp.arcade.domain.rank.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.pp.arcade.domain.rank.RankRedis;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RankUserDto {
    private String intraId;
    private Integer rank;
    private Integer ppp;
    private Integer wins;
    private Integer losses;
    private String statusMessage;
    @JsonSerialize(using = DtoSerialize.CustomDoubleSerializer.class)
    private Double winRate;

    public static RankUserDto from (RankRedis userRank, Integer rank){
        RankUserDto dto = RankUserDto.builder()
                .intraId(userRank.getIntraId())
                .ppp(userRank.getPpp())
                .wins(userRank.getWins())
                .losses(userRank.getLosses())
                .winRate(userRank.getWinRate())
                .rank(rank == null ? -1 : rank.intValue())
                .statusMessage(userRank.getStatusMessage())
                .build();
        return dto;
    }

    @Override
    public String toString() {
        return "RankUserDto{" +
                "intraId='" + intraId + '\'' +
                ", rank=" + rank +
                ", ppp=" + ppp +
                ", wins=" + wins +
                ", losses=" + losses +
                ", statusMessage='" + statusMessage + '\'' +
                ", winRate=" + winRate +
                '}';
    }
}
