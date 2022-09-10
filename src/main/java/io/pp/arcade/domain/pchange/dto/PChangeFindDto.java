package io.pp.arcade.domain.pchange.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Pageable;

@Builder
@Getter
public class PChangeFindDto {
    private Integer gameId;
    private String userId;
    private Pageable pageable;

    @Override
    public String toString() {
        return "PChangeFindDto{" +
                "gameId=" + gameId +
                ", userId='" + userId + '\'' +
                ", pageable=" + pageable +
                '}';
    }
}
