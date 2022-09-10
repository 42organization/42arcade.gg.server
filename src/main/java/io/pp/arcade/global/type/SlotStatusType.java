package io.pp.arcade.global.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Locale;

@Getter
@RequiredArgsConstructor
public enum SlotStatusType implements Constant {
    OPEN("open"),
    CLOSE("close"),
    MYTABLE("mytable");

    private final String code;

    @JsonCreator
    public static SlotStatusType getEnumFromValue(String value) {
        for(SlotStatusType e : values()) {
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
