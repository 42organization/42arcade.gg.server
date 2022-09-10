package io.pp.arcade.domain.game.dto;

import lombok.Getter;
import org.springframework.lang.Nullable;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Getter
public class GameFindRequestDto {
    @Max(100)
    @Min(1)
    @Nullable
    Integer count;

    @Max(Integer.MAX_VALUE)
    @Min(1)
    @Nullable
    Integer gameId;
    
    String status;

    @Override
    public String toString() {
        return "GameFindRequestDto{" +
                "count=" + count +
                ", gameId=" + gameId +
                ", status='" + status + '\'' +
                '}';
    }
}
