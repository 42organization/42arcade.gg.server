package io.pp.arcade.domain.game.dto;

import io.pp.arcade.global.type.StatusType;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Pageable;

@Builder
@Getter
public class GameFindDto {
    Integer id;
    StatusType status;
    Pageable pageable;

    @Override
    public String toString() {
        return "GameFindDto{" +
                "id=" + id +
                ", status=" + status +
                ", pageable=" + pageable +
                '}';
    }
}
