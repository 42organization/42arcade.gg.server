package io.pp.arcade.domain.rank.dto;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.text.DecimalFormat;

@Configuration
public class DtoSerialize {
    @Bean
    public CustomDoubleSerializer CustomDoubleSerializeCreate() {
        return new CustomDoubleSerializer();
    }

    public class CustomDoubleSerializer extends JsonSerializer<Double> {
        @Override
        public void serialize(Double value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
            if (null == value) {
                jgen.writeNull();
            } else {
                final String pattern = "0.0";
                final DecimalFormat myFormatter = new DecimalFormat(pattern);
                final String output = myFormatter.format(value);
                jgen.writeNumber(output);
            }
        }
    }
}
