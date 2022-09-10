package io.pp.arcade.domain.noti.dto;

import io.pp.arcade.global.type.NotiType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

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
        this.type = type.getCode();
        this.time = time;
        this.isChecked = isChecked;
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "NotiMatchedDto{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", time=" + time +
                ", isChecked=" + isChecked +
                ", createdAt=" + createdAt +
                '}';
    }
}
