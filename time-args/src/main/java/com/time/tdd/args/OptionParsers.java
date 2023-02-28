package com.time.tdd.args;

import com.time.tdd.args.exceptions.IllegalValueException;
import com.time.tdd.args.exceptions.InsufficientArgumentsException;
import com.time.tdd.args.exceptions.TooManyArgumentsException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

/**
 * @author XuJian
 * @date 2023-02-18 15:36
 **/
class OptionParsers {

    public static OptionParser<Boolean> bool() {
        return ((arguments, option) -> values(arguments, option, 0).isPresent());
    }

    public static <T> OptionParser<Object> unary(T defaultValue, Function<String, T> valueParser) {
        return ((arguments, option) -> values(arguments, option, 1).map(it -> parseValue(option, it.get(0), valueParser)).orElse(
            defaultValue));
    }


    public static <T> OptionParser<T[]> list(Function<String, T> valueParser, IntFunction<T[]> generator) {
        return ((arguments, option) -> values(arguments, option)
            .map(it -> it.stream().map(val -> parseValue(option, val, valueParser))
                .toArray(generator)).orElse(generator.apply(0)));
    }

    private static List<String> values(List<String> arguments, int index) {

        return arguments.subList(index + 1, IntStream.range(index + 1, arguments.size())
            .filter(it -> arguments.get(it).matches("^-[a-zA-Z-]+$"))
            .findFirst().orElse(arguments.size()));
    }

    private static Optional<List<String>> values(List<String> arguments, Option option, int exceptedSize) {
        return values(arguments, option).map(it -> checkSize(option, exceptedSize, it));
    }

    private static List<String> checkSize(Option option, int exceptedSize, List<String> values) {
        if (values.size() < exceptedSize) {
            throw new InsufficientArgumentsException(option.value());
        }
        if (values.size() > exceptedSize) {
            throw new TooManyArgumentsException(option.value());
        }
        return values;
    }


    private static Optional<List<String>> values(List<String> arguments, Option option) {
        int index = arguments.indexOf("-" + option.value());
        return Optional.ofNullable(index == -1 ? null : values(arguments, index));
    }

    private static <T> T parseValue(Option option, String value, Function<String, T> valueParser) {
        try {
            return valueParser.apply(value);
        } catch (Exception e) {
            throw new IllegalValueException(option.value(), value);
        }
    }
}

