package io.pp.arcade.global.type;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

import java.util.Arrays;
import java.util.Locale;

public class CodeToEnumConverterFactory implements ConverterFactory<String, Enum<? extends Constant>> {

    @Override
    public <T extends Enum<? extends Constant>> Converter<String, T> getConverter(Class<T> targetType) {
        return new StringToEnumsConverter<>(targetType);
    }

    private static final class StringToEnumsConverter<T extends Enum<? extends Constant>> implements Converter<String, T> {

        private final Class<T> enumType;
        private final boolean constantEnum;

        public StringToEnumsConverter(Class<T> enumType) {
            this.enumType = enumType;
            this.constantEnum = Arrays.stream(enumType.getInterfaces()).anyMatch(i -> i == Constant.class);
        }

        @Override
        public T convert(String source) {
            if (source.isEmpty()) {
                return null;
            }

            T[] constants = enumType.getEnumConstants();
            for (T c : constants) {
                if (constantEnum) {
                    if (((Constant) c).getCode().toUpperCase(Locale.ROOT).equals(source.trim().toUpperCase(Locale.ROOT))) {
                        return c;
                    }
                } else {
                    if (c.name().equals(source.trim())) {
                        return c;
                    }
                }
            }
            return null;
        }
    }
}
