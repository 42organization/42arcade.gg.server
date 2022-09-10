package io.pp.arcade.domain.currentmatch.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class CurrentMatchResponseDto {
    private LocalDateTime time;
    private Integer slotId;
    private List<String> myTeam;
    private List<String> enemyTeam;
    private Boolean isMatched;

    @Override
    public String toString() {
        return "CurrentMatchResponseDto{" +
                "time=" + time +
                ", slotId=" + slotId +
                ", myTeam=" + myTeam +
                ", enemyTeam=" + enemyTeam +
                ", isMatched=" + isMatched +
                '}';
    }
}
