package io.pp.arcade.domain.admin.dto.update;

import lombok.Getter;

@Getter
public class PChangeUpdateRequestDto {
    private Integer pchangeId;
    private Integer gameId;
    private Integer userId;
    private Integer pppChange;
    private Integer pppResult;
}
