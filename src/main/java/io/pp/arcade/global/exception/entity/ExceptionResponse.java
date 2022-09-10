package io.pp.arcade.global.exception.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Setter
@Getter
public class ExceptionResponse {
    private String code;
    private String message;

    public static ExceptionResponse from(String code, BindingResult bindingResult){
        FieldError fieldError = bindingResult.getFieldError();
        return ExceptionResponse.builder()
                .code(code)
                .message(fieldError.getDefaultMessage())
                .build();
    }

    public static ExceptionResponse from(String code, MethodArgumentTypeMismatchException ex){
        return ExceptionResponse.builder()
                .code(code)
                .message(ex.getMessage())
                .build();
    }

    public static ExceptionResponse from(String code){
        return ExceptionResponse.builder()
                .code(code)
                .build();
    }

    public static ExceptionResponse from(String code, String Message){
        return ExceptionResponse.builder()
                .code(code)
                .message(Message)
                .build();
    }
    @Builder
    private ExceptionResponse(String code, String message){
        this.code = code;
        this.message = "잘못된 요청입니다.";
    }
}
