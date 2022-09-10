package io.pp.arcade.domain.pchange;

import io.pp.arcade.domain.game.Game;
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
public class PChange extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "game_id")
    private Game game;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "intra_id")
    private User user;

    @Setter
    @NotNull
    @Column(name = "ppp_change")
    private  Integer pppChange;

    @Setter
    @NotNull
    @Column(name = "ppp_result")
    private  Integer pppResult;

    @Builder
    public PChange(Game game, User user, Integer pppChange, Integer pppResult) {
        this.game = game;
        this.user = user;
        this.pppChange = pppChange;
        this.pppResult = pppResult;
    }
}
