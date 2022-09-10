package io.pp.arcade.domain.game.dto;

import io.pp.arcade.domain.game.Game;
import io.pp.arcade.domain.slot.dto.SlotDto;
import io.pp.arcade.domain.team.dto.TeamDto;
import io.pp.arcade.global.type.GameType;
import io.pp.arcade.global.type.StatusType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class GameDto {
    private Integer id;
    private SlotDto slot;
    private TeamDto team1;
    private TeamDto team2;
    private GameType type;
    private LocalDateTime time;
    private Integer season;
    private StatusType status;

    public static GameDto from(Game game) {
        return GameDto.builder()
                .id(game.getId())
                .slot(SlotDto.from(game.getSlot()))
                .team1(TeamDto.from(game.getTeam1()))
                .team2(TeamDto.from(game.getTeam2()))
                .type(game.getType())
                .time(game.getTime())
                .season(game.getSeason())
                .status(game.getStatus())
                .build();
    }

    @Override
    public String toString() {
        return "GameDto{" +
                "id=" + id +
                ", slot=" + slot +
                ", team1=" + team1 +
                ", team2=" + team2 +
                ", type=" + type +
                ", time=" + time +
                ", season=" + season +
                ", status=" + status +
                '}';
    }
}
