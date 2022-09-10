package io.pp.arcade.domain.rank.dto;

import io.pp.arcade.domain.rank.Rank;
import io.pp.arcade.domain.user.dto.UserDto;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RankFindTestDto {
    private UserDto user;
    private Integer ranking;
    private Integer ppp;
    private Integer wins;
    private Integer losses;

    public static RankFindTestDto from(Rank rank) {
        RankFindTestDto findRankDto =  RankFindTestDto.builder()
                .user(rank.getUser() == null ? null : UserDto.from(rank.getUser()))
                .ppp(rank.getPpp())
                .ranking(rank.getRanking())
                .wins(rank.getWins())
                .losses(rank.getLosses())
                .build();
        return findRankDto;
    }

    @Override
    public String toString() {
        return "RankFindTestDto{" +
                "user=" + user +
                ", ranking=" + ranking +
                ", ppp=" + ppp +
                ", wins=" + wins +
                ", losses=" + losses +
                '}';
    }
}
