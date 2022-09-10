package io.pp.arcade.domain.season;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class Season {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @Column(name = "season_name")
    private String seasonName;

    @NotNull
    @Column(name = "start_time")
    private LocalDateTime startTime;

    @NotNull
    @Setter
    @Column(name = "end_time")
    private LocalDateTime endTime;

    @NotNull
    @Setter
    @Column(name = "start_ppp")
    private Integer startPpp;

    @NotNull
    @Setter
    @Column(name = "ppp_gap")
    private Integer pppGap;

    @Builder
    public Season(String seasonName, LocalDateTime startTime, LocalDateTime endTime, Integer startPpp, Integer pppGap) {
        this.seasonName = seasonName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.startPpp = startPpp;
        this.pppGap = pppGap;
    }
}
