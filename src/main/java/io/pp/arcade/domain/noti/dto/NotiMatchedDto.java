package io.pp.arcade.domain.noti.dto;

import io.pp.arcade.global.type.NotiType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Getter
public class NotiMatchedDto {
    private Integer id;
    private String type;
    private LocalDateTime time;
    private Boolean isChecked;
    private LocalDateTime createdAt;

    @Builder
    public NotiMatchedDto(Integer id, NotiType type, LocalDateTime time, Boolean isChecked, LocalDateTime createdAt) {
        this.id = id;
        this.type = type.getCode().toLowerCase(Locale.ROOT);
        this.time = time;
        this.isChecked = isChecked;
        this.createdAt = createdAt;
    }
}
