package com.time.tdd.args;

import com.time.tdd.args.exceptions.IllegalValueException;
import com.time.tdd.args.exceptions.InsufficientArgumentsException;
import com.time.tdd.args.exceptions.TooManyArgumentsException;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * single
 *
 * @author XuJian
 * @date 2023-02-18 22:00
 **/
class CustomOptionParsersTest {


    static Option option(String value) {
        return new Option() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return Option.class;
            }

            @Override
            public String value() {
                return value;
            }
        };
    }

    @Nested
    class UnaryCustomOptionParser {

        @Test
            //Sad Path
        void should_not_accept_extra_argument_for_single_valued_option() {
            TooManyArgumentsException e = assertThrows(TooManyArgumentsException.class, () -> {
                OptionParsers.unary(0, Integer::parseInt).parse(asList("-p", "8080", "8081"), option("p"));
            });

            Assertions.assertEquals("p", e.getOption());
        }


        @ParameterizedTest // Sad Path
        @ValueSource(strings = {"-p -l", "-p"})
        void should_not_accept_insufficient_argument_for_single_valued_option(String arguments) {
            InsufficientArgumentsException e = assertThrows(InsufficientArgumentsException.class, () -> {
                OptionParsers.unary(0, Integer::parseInt).parse(asList(arguments.split(" ")), option("p"));
            });

            Assertions.assertEquals("p", e.getOption());
        }


        @Test
            //Default Value
        void should_set_default_value_for_single_valued_option() {

            Function<String, Object> whatever = (it) -> null;
            Object defaultValue = new Object();
            assertSame(defaultValue,
                OptionParsers.unary(defaultValue, whatever).parse(List.of(), option("p")));
        }

        @Test
            //Happy Path
        void should_parse_value_if_flag_present() {
            Object parsed = new Object();
            Function<String, Object> parse = (it) -> parsed;
            Object whatever = new Object();
            assertSame(parsed, OptionParsers.unary(whatever, parse).parse(asList("-p", "8080"), option("p")));

        }

    }

    @Nested
    class BooleanCustomOptionParserTest {


        @Test
            // Sad Path
        void should_not_accept_extra_argument_for_boolean_option() {
            TooManyArgumentsException e = assertThrows(TooManyArgumentsException.class,
                () -> {
                    OptionParsers.bool().parse(asList("-l", "t"), option("l"));
                });

            assertEquals("l", e.getOption());
        }

        @Test
            // Defualt Value
        void should_set_default_value_to_false_if_option_not_present() {
            assertFalse(OptionParsers.bool().parse(List.of(), option("l")));
        }

        @Test
            // Happy Path
        void should_set_default_value_to_true_if_option_present() {
            assertTrue(OptionParsers.bool().parse(List.of("-l"), option("l")));
        }


    }


    @Nested
    class ListCustomOptionParser {
        // TODO: -g "this" "is" {"this","is"}

        @Test
        void should_parse_list_value() {
            String[] value = OptionParsers.list(String[]::new, String::valueOf).parse(asList("-g", "this", "is"), option("g"));
            Assertions.assertArrayEquals(new String[] {"this", "is"}, value);
        }

        @Test
        void should_not_treat_negative_int_as_flag() {
            assertArrayEquals(new Integer[] {-1, -2},
                OptionParsers.list(Integer[]::new, Integer::parseInt).parse(asList("-g", "-1", "-2"), option("g"))
            );
        }

        // TODO:default value []

        @Test
        void should_use_empty_array_as_default_value() {
            String[] value = OptionParsers.list(String[]::new, String::valueOf).parse(asList(), option("g"));
            assertEquals(0, value.length);
        }
        // TODO: -d a throw exception

        @Test
        void should_throw_exception_if_value_parser_cant_parse_value() {
            Function<String, String> parser = (it) -> {
                throw new RuntimeException();
            };

            IllegalValueException e = assertThrows(IllegalValueException.class, () ->
                OptionParsers.list(String[]::new, parser).parse(asList("-g", "this", "is"), option("g")));

            assertEquals("g", e.getOption());
            assertEquals("this", e.getValue());


        }
    }
}

