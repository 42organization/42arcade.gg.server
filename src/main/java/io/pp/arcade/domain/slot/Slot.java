package io.pp.arcade.domain.slot;

import io.pp.arcade.domain.game.Game;
import io.pp.arcade.domain.team.Team;
import io.pp.arcade.global.type.GameType;
import io.pp.arcade.global.type.SlotStatusType;
import io.pp.arcade.global.util.BaseTimeEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnTransformer;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor
public class Slot extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @Column(name = "table_id")
    private Integer tableId;

    @NotNull
    @OneToOne
    @JoinColumn(name = "team1_id")
    private Team team1;

    @NotNull
    @OneToOne
    @JoinColumn(name = "team2_id")
    private Team team2;

    @NotNull
    @Column(name = "time")
    private LocalDateTime time;

    @Setter
    @Column(name = "game_ppp")
    private Integer gamePpp;

    @NotNull
    @Setter
    @Column(name = "head_count")
    private Integer headCount;

    @Setter
    @Column(name = "type")
    private GameType type;

    @Builder
    public Slot(Integer tableId, Team team1, Team team2, LocalDateTime time, Integer gamePpp, Integer headCount, GameType type) {
        this.tableId = tableId;
        this.team1 = team1;
        this.team2 = team2;
        this.time = time;
        this.gamePpp = gamePpp;
        this.headCount = headCount;
        this.type = type;
    }
}
