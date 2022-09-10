package io.pp.arcade.domain.rank.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RankModifyStatusMessageDto {
    private String intraId;
    private String statusMessage;

    @Override
    public String toString() {
        return "RankModifyStatusMessageDto{" +
                "intraId='" + intraId + '\'' +
                ", statusMessage='" + statusMessage + '\'' +
                '}';
    }
}
