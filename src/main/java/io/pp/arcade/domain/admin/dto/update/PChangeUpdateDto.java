package io.pp.arcade.domain.admin.dto.update;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PChangeUpdateDto {
    private Integer gameId;
    private Integer userId;
    private Integer pppChange;
    private Integer pppResult;
}
