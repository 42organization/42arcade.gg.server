package io.pp.arcade.domain.pchange.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PChangeAddDto {
    private Integer gameId;
    private Integer userId;
    private Integer pppChange;
    private Integer pppResult;

    @Override
    public String toString() {
        return "PChangeAddDto{" +
                "gameId=" + gameId +
                ", userId=" + userId +
                ", pppChange=" + pppChange +
                ", pppResult=" + pppResult +
                '}';
    }
}
