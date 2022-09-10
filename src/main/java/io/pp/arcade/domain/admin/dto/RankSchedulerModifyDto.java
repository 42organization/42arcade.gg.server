package io.pp.arcade.domain.admin.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RankSchedulerModifyDto {
    private String cron;
}
