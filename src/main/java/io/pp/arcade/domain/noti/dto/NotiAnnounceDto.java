package io.pp.arcade.domain.noti.dto;

import io.pp.arcade.global.type.NotiType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class NotiAnnounceDto {
    private Integer id;
    private String type;
    private Boolean isChecked;
    private String message;
    private LocalDateTime createdAt;

    @Builder
    public NotiAnnounceDto(Integer id, NotiType type, Boolean isChecked, String message, LocalDateTime createdAt) {
        this.id = id;
        this.type = type.getCode();
        this.isChecked = isChecked;
        this.message = message;
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "NotiAnnounceDto{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", isChecked=" + isChecked +
                ", message='" + message + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
