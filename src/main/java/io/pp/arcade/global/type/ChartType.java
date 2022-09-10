package io.pp.arcade.global.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChartType implements Constant{
    RANK("RANK");

    private final String code;
}
