package com.time.tdd.args.custom;

import com.time.tdd.args.exceptions.InsufficientArgumentsException;
import com.time.tdd.args.exceptions.TooManyArgumentsException;
import java.lang.annotation.Annotation;
import java.util.function.Function;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * single
 *
 * @author XuJian
 * @date 2023-02-19 22:03
 **/
class OptionParsersTest {


    static CustomOption option(String value) {
        return new CustomOption() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return CustomOption.class;
            }

            @Override
            public String value() {
                return value;
            }
        };
    }

    @Nested
    class UnaryOptionParser {


        @Test
            //sad path
        void should_not_accept_extra_argument_for_string_option_value() {
            TooManyArgumentsException e = assertThrows(TooManyArgumentsException.class, () -> {
                (OptionParsers.unary(0, Integer::parseInt)).parse(asList("-p", "8080", "8081"),
                    option("p"));
            });

            assertEquals("p", e.getOption());
        }

        @ParameterizedTest
        @ValueSource(strings = {"-p -l", "-p"})
            // sad path
        void should_not_accept_extra_insufficient_argument_for_single_value_option(String arguments) {
            InsufficientArgumentsException e = assertThrows(InsufficientArgumentsException.class, () -> {
                (OptionParsers.unary(0, Integer::parseInt)).parse(asList(arguments.split(" ")),
                    option("p"));
            });

            assertEquals("p", e.getOption());
        }

        @Test
            // default value
        void should_set_default_value_to_0_for_int_option() {
            Function<String, Object> whatever = (it) -> null;
            Object defaultValue = new Object();

            assertSame(defaultValue,
                (OptionParsers.unary(defaultValue, whatever)).parse(asList(), option("p")));
        }

        @Test
            // happy path
        void should_parse_value_if_flag_present() {
            Object parsed = new Object();
            Function<String, Object> parse = (it) -> parsed;
            Object whatever = new Object();
            assertEquals(parsed, OptionParsers.unary(whatever, parse).parse(asList("-p", "8080"), option("p")));
        }
    }

    @Nested
    class BooleanOptionParser {


        @Test
            //sad path
        void should_not_accept_extra_argument_for_boolean_option() {
            TooManyArgumentsException e = assertThrows(TooManyArgumentsException.class, () -> {
                OptionParsers.bool().parse(asList("-l", "t"), option("l"));
            });

            assertEquals("l", e.getOption());
        }


        @Test
            //default value
        void should_set_boolean_option_to_false_if_flag_not_present() {

            Assertions.assertFalse((OptionParsers.bool().parse(asList(), option("l"))));
        }


        @Test
            //happy path
        void should_set_value_true_if_option_present() {
            assertTrue((OptionParsers.bool().parse(asList("-l"), option("l"))));
        }
    }
}

