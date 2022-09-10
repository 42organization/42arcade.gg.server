package io.pp.arcade.global.exception.entity;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ExceptionEntity {
    private String code;
    private String message;

    @Builder
    public ExceptionEntity(String code, String message){
        this.code = code;
        this.message = message;
    }
}
