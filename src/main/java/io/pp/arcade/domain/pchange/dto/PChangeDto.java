package io.pp.arcade.domain.pchange.dto;

import io.pp.arcade.domain.game.dto.GameDto;
import io.pp.arcade.domain.pchange.PChange;
import io.pp.arcade.domain.user.dto.UserDto;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PChangeDto {
    private Integer id;
    private GameDto game;
    private UserDto user;
    private Integer pppChange;
    private Integer pppResult;

    public static PChangeDto from(PChange pChange){
        return PChangeDto.builder()
                .id(pChange.getId())
                .game(GameDto.from(pChange.getGame()))
                .user(UserDto.from(pChange.getUser()))
                .pppChange(pChange.getPppChange())
                .pppResult(pChange.getPppResult())
                .build();
    }

    @Override
    public String toString() {
        return "PChangeDto{" +
                "id=" + id +
                ", game=" + game +
                ", user=" + user +
                ", pppChange=" + pppChange +
                ", pppResult=" + pppResult +
                '}';
    }
}
