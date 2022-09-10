package io.pp.arcade.domain.team;

import io.pp.arcade.domain.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Getter
@NoArgsConstructor
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Setter
    @OneToOne
    @JoinColumn(name = "user1_id", referencedColumnName = "intra_id")
    private User user1;

    @Setter
    @OneToOne
    @JoinColumn(name = "user2_id", referencedColumnName = "intra_id")
    private User user2;

    @NotNull
    @Setter
    @Column(name = "team_ppp")
    private Integer teamPpp;

    @NotNull
    @Setter
    @Column(name = "head_count")
    private Integer headCount;

    @NotNull
    @Setter
    @Column(name = "score")
    private Integer score;

    @Setter
    @Column(name = "win")
    private Boolean win;

    @Builder
    public Team(User user1, User user2, Integer teamPpp, Integer headCount, Integer score, Boolean win) {
        this.user1 = user1;
        this.user2 = user2;
        this.teamPpp = teamPpp;
        this.headCount = headCount;
        this.score = score;
        this.win = win;
    }
}
