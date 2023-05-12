package com.time.tdd.rest;

import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import static com.time.tdd.rest.Converter.Factory;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * constructor converter
 *
 * @author XuJian
 * @date 2023-05-12 21:17
 **/
public class ConstructorConverterTest {

    @Test
    public void should_convert_via_converter_constructor() {
        assertEquals(Optional.of(new BigDecimal(12345)), ConverterConstructor.convert(BigDecimal.class, "12345"));
    }

    @Test
    public void should_not_convert_if_no_converter_constructor() {
        assertEquals(Optional.empty(), ConverterConstructor.convert(NoConverter.class, "12345"));
    }


    @Test
    public void should_convert_via_converter_factory() {
        assertEquals(Optional.of(Factory), ConverterFactory.convert(Converter.class, "Factory"));
    }

    @Test
    public void should_not_convert_if_no_converter_factory() {
        assertEquals(Optional.empty(), ConverterFactory.convert(NoConverter.class, "Factory"));
    }


}

class NoConverter {

    NoConverter valueOf(String value) {
        return new NoConverter();
    }
}


