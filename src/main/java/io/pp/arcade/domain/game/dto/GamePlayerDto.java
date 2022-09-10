package io.pp.arcade.domain.game.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GamePlayerDto {
    private String intraId;
    private String userImageUri;
    private Integer wins;
    private Integer losses;
    private Double winRate;
    private Integer pppChange;

    @Override
    public String toString() {
        return "GamePlayerDto{" +
                "intraId='" + intraId + '\'' +
                ", userImageUri='" + userImageUri + '\'' +
                ", wins=" + wins +
                ", losses=" + losses +
                ", winRate=" + winRate +
                ", pppChange=" + pppChange +
                '}';
    }
}
