package io.pp.arcade.domain.admin.dto.create;

import lombok.Getter;

@Getter
public class CurrentMatchCreateRequestDto {
    private Integer userId;
    private Integer slotId;
    private Integer gameId;
    private Boolean matchImminent;
    private Boolean isMatched;
}
