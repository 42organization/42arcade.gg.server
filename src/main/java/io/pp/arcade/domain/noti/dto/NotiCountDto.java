package io.pp.arcade.domain.noti.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class NotiCountDto {
    Integer notiCount;

    @Override
    public String toString() {
        return "NotiCountDto{" +
                "notiCount=" + notiCount +
                '}';
    }
}
