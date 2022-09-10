package io.pp.arcade.domain.currentmatch;

import io.pp.arcade.domain.game.Game;
import io.pp.arcade.domain.slot.Slot;
import io.pp.arcade.domain.user.User;
import io.pp.arcade.global.util.BaseTimeEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Getter
@NoArgsConstructor
public class CurrentMatch extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "intra_id")
    private User user;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "slot_id")
    private Slot slot;

    @Setter
    @ManyToOne
    @JoinColumn(name = "game_id")
    private Game game;

    @Setter
    @Column(name = "match_imminent")
    private Boolean matchImminent;

    @Setter
    @Column(name = "is_matched")
    private Boolean isMatched;

    @Builder
    public CurrentMatch(User user, Slot slot, Game game, Boolean matchImminent, Boolean isMatched) {
        this.user = user;
        this.slot = slot;
        this.game = game;
        this.matchImminent = matchImminent;
        this.isMatched = isMatched;
    }
}
