package io.pp.arcade.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class BusinessException extends RuntimeException {
    private String code;
    private String message;

    public BusinessException(String message) {
        super(message);
        this.message = message;
    }
}

