package com.time.tdd.args.custom;

import com.time.tdd.args.exceptions.IllegalValueException;
import com.time.tdd.args.exceptions.InsufficientArgumentsException;
import com.time.tdd.args.exceptions.TooManyArgumentsException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * @author XuJian
 * @date 2023-02-19 21:47
 **/
class OptionParsers {

    private static List<String> values(List<String> arguments, int index) {
        int followingFlag = IntStream.range(index + 1, arguments.size())
            .filter(it -> arguments.get(it).startsWith("-"))
            .findFirst().orElse(arguments.size());

        return arguments.subList(index + 1, followingFlag);
    }

    private static Optional<List<String>> values(List<String> arguments, CustomOption option, int expectedSize) {
        int index = arguments.indexOf("-" + option.value());
        if (index == -1) {
            return Optional.empty();
        }
        List<String> values = values(arguments, index);

        if (values.size() < expectedSize) {
            throw new InsufficientArgumentsException(option.value());
        }

        if (values.size() > expectedSize) {
            throw new TooManyArgumentsException(option.value());
        }
        return Optional.of(values);
    }

    static OptionParser<Boolean> bool() {
        return ((arguments, option) -> values(arguments, option, 0).isPresent());
    }

    static <T> OptionParser<Object> unary(T defaultValue, Function<String, T> valueParser) {
        return ((arguments, option) -> values(arguments, option, 1)
            .map(it -> parseValue(option, it.get(0), valueParser))
            .orElse(defaultValue));
    }

    private static <T> T parseValue(CustomOption option, String value, Function<String, T> valueParser) {
        try {
            return valueParser.apply(value);
        } catch (Exception e) {
            throw new IllegalValueException(option.value(), value);
        }
    }

}

