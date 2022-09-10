package io.pp.arcade.global.exception.entity;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ExceptionCode {
    /* 기본 에러 처리 */
    RUNTIME(HttpStatus.BAD_REQUEST, "E0001"),
    ACCESS_DENIED(HttpStatus.UNAUTHORIZED, "E0002"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "E0003"),

    /* 슬롯 에러 처리 */
    // 슬롯 생성 에러
    SLOT_SC_CLOSED(HttpStatus.BAD_REQUEST, "SC001"),
    SLOT_SC_DUPLICATED(HttpStatus.BAD_REQUEST, "SC002"),
    SLOT_SC_PENALTY(HttpStatus.BAD_REQUEST, "SC003"),

    // 슬롯 삭제 에러
    SLOT_SD_NOSLOT(HttpStatus.BAD_REQUEST, "SD001"),
    SLOT_SD_IMMINENT(HttpStatus.BAD_REQUEST, "SD002"),

    /* 잘못된 타입 입력 */
    ENTITY_NOT_FOUND(HttpStatus.BAD_REQUEST, "E1000", "잘못된 입력입니다."),


    SECURITY_01(HttpStatus.UNAUTHORIZED, "S0001", "권한이 없습니다.");

    private final HttpStatus status;
    private final String code;
    private String message;

    ExceptionCode(HttpStatus status, String code) {
        this.status = status;
        this.code = code;
        this.message = "잘못된 요청입니다.";
    }

    ExceptionCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
