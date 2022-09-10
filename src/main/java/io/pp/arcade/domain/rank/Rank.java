package io.pp.arcade.domain.rank;

import io.pp.arcade.domain.rank.dto.RankDto;
import io.pp.arcade.domain.rank.dto.RankRedisDto;
import io.pp.arcade.domain.user.User;
import io.pp.arcade.domain.user.dto.UserDto;
import io.pp.arcade.global.type.GameType;
import io.pp.arcade.global.util.BaseTimeEntity;
import io.pp.arcade.global.type.RacketType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Entity
@Getter
@NoArgsConstructor
@Table(name="ranks")
public class Rank extends BaseTimeEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "intra_id")
    private User user;

    @NotNull
    @Column(name = "ppp")
    private Integer ppp;

    @NotNull
    @Column(name = "season_id")
    private Integer seasonId;

    @Column(name = "ranking")
    private Integer ranking;

    @NotNull
    @Column(name = "racket_type")
    private RacketType racketType;

    @NotNull
    @Column(name = "game_type")
    private GameType gameType;

    @NotNull
    @Column(name = "wins")
    private Integer wins;

    @NotNull
    @Column(name = "losses")
    private Integer losses;

    @Builder
    public Rank(User user, Integer ppp, Integer seasonId, Integer ranking, RacketType racketType, GameType gameType, Integer wins, Integer losses) {
        this.user = user;
        this.ppp = ppp;
        this.seasonId = seasonId;
        this.ranking = ranking;
        this.racketType = racketType;
        this.gameType = gameType;
        this.wins = wins;
        this.losses = losses;
    }


    public void update(Integer ppp, Integer wins, Integer losses) {
        this.ppp = ppp;
        this.wins = wins;
        this.losses = losses;
    }
    public void update(RankRedisDto rankDto, User user, Integer seasonId) {
        this.user = user;
        this.id = rankDto.getId();
        this.ppp = rankDto.getPpp();
        this.wins = rankDto.getWins();
        this.losses = rankDto.getLosses();
        this.ranking = rankDto.getRanking();
        this.gameType = rankDto.getGameType();
        this.seasonId = seasonId;
        this.racketType = rankDto.getRacketType();
    }
}
