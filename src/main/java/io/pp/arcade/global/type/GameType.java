package io.pp.arcade.global.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.pp.arcade.global.redis.Key;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Locale;

@Getter
@RequiredArgsConstructor
public enum GameType implements Constant{
    SINGLE(Key.SINGLE), DOUBLE(Key.DOUBLE);

    private final String code;

    @JsonCreator
    public static GameType getEnumFromValue(String value) {
        for(GameType e : values()) {
            if(e.code.equals(value)) {
                return e;
            }
            else if (e.code.toUpperCase(Locale.ROOT).equals(value.toUpperCase(Locale.ROOT))) {
                return e;
            }
        }
        return null;
    }
}
