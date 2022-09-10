package io.pp.arcade.domain.admin.dto.update;

import lombok.Getter;

@Getter
public class CurrentMatchUpdateRequestDto {
    private Integer currentMatchId;
    private Integer userId;
    private Integer slotId;
    private Integer gameId;
    private Boolean matchImminent;
    private Boolean isMatched;
}
