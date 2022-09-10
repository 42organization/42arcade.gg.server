package io.pp.arcade.domain.rank;

import io.pp.arcade.domain.game.Game;
import io.pp.arcade.domain.rank.dto.RankDto;
import io.pp.arcade.domain.user.User;
import io.pp.arcade.domain.user.dto.UserDto;
import io.pp.arcade.global.type.RacketType;
import io.pp.arcade.global.type.GameType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import javax.persistence.*;
import java.io.Serializable;


@Getter
@RedisHash("user")
@NoArgsConstructor
public class RankRedis implements Serializable {

    @Id
    private Integer id;

    @Indexed /* Redis 조회시 사용되는 변수 */
    private String intraId;

    private RacketType racketType;

    @Indexed
    private GameType gameType;

    private Integer ppp;

    private Integer wins;

    private Integer losses;

    private double winRate;

    @Setter
    private String statusMessage;


    @Builder
    public RankRedis(Integer id, String intraId, Integer ppp, RacketType racketType, GameType gameType, Integer wins, Integer losses, double winRate, String statusMessage) {
        this.id = id;
        this.intraId = intraId;
        this.ppp = ppp;
        this.racketType = racketType;
        this.gameType = gameType;
        this.wins = wins;
        this.losses = losses;
        this.winRate = winRate;
        this.statusMessage = statusMessage;
    }

    public static RankRedis from (UserDto user, GameType gameType){
        return RankRedis.builder()
                .id(user.getId())
                .intraId(user.getIntraId())
                .ppp(user.getPpp())
                .statusMessage(user.getStatusMessage())
                .gameType(gameType)
                .racketType(user.getRacketType())
                .wins(0)
                .losses(0)
                .winRate(0)
                .build();
    }

    public static RankRedis from (RankDto rankDto, GameType gameType){
        Integer losses = rankDto.getLosses();
        Integer wins = rankDto.getWins();
        RankRedis rankRedis = RankRedis.builder()
                .id(rankDto.getId())
                .intraId(rankDto.getUser().getIntraId())
                .ppp(rankDto.getPpp())
                .statusMessage(rankDto.getUser().getStatusMessage())
                .gameType(gameType)
                .racketType(rankDto.getRacketType())
                .wins(rankDto.getWins())
                .losses(rankDto.getLosses())
                .winRate((losses + wins) == 0 ? 0 : ((double)wins / (double)(losses + wins) * 100))
                .build();
        return rankRedis;
    }

    public void update(Boolean isWin, Integer ppp){
        if (isWin == true) {
            this.wins++;
        } else {
            this.losses++;
        }
        this.ppp = ppp;
        this.winRate = (double)wins / (wins + losses) * 100;
    }

    public void updateStatusMessage(String statusMessage){
        this.setStatusMessage(statusMessage);
    }
}
