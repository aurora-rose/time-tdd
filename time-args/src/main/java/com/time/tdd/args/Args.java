package com.time.tdd.args;

import com.time.tdd.args.exceptions.IllegalOptionException;
import com.time.tdd.args.exceptions.UnsupportedOptionTypeException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import static com.time.tdd.args.OptionParsers.list;
import static com.time.tdd.args.OptionParsers.unary;

/**
 * @author XuJian
 * @date 2023-02-18 13:51
 **/
public class Args<T> {


    private static final Map<Class<?>, OptionParser> PARSERS = Map.of(
        boolean.class, OptionParsers.bool(),
        int.class, unary(0, Integer::parseInt),
        String.class, unary("", String::valueOf),
        String[].class, list(String::valueOf, String[]::new),
        Integer[].class, list(Integer::parseInt, Integer[]::new));
    private Class<T> optionsClass;
    private Map<Class<?>, OptionParser> parsers;

    public Args(Class<T> optionsClass, Map<Class<?>, OptionParser> parsers) {
        this.optionsClass = optionsClass;
        this.parsers = parsers;
    }

    public static <T> T parse(Class<T> optionsClass, String... args) {
        return new Args<>(optionsClass, PARSERS).parse(args);
    }

    private Object parseOption(List<String> arguments, Parameter parameter) {
        if (!parameter.isAnnotationPresent(Option.class)) {
            throw new IllegalOptionException(parameter.getName());
        }
        Option option = parameter.getAnnotation(Option.class);
        if (!parsers.containsKey(parameter.getType())) {
            throw new UnsupportedOptionTypeException(option.value(), parameter.getType());
        }
        return parsers.get(parameter.getType()).parse(arguments, option);
    }

    public T parse(String... args) {
        try {
            List<String> arguments = List.of(args);
            Constructor<?> constructor = optionsClass.getDeclaredConstructors()[0];

            Object[] values = Arrays.stream(constructor.getParameters())
                .map(it -> parseOption(arguments, it)).toArray();

            return (T) constructor.newInstance(values);
        } catch (IllegalOptionException | UnsupportedOptionTypeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}

