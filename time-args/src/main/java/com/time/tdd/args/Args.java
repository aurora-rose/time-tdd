package com.time.tdd.args;

import com.time.tdd.args.exceptions.IllegalOptionException;
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
public class Args {

    private static final Map<Class<?>, OptionParser> PARSER = Map.of(
        boolean.class, OptionParsers.bool(),
        int.class, unary(0, Integer::parseInt),
        String.class, unary("", String::valueOf),
        String[].class, list(String[]::new, String::valueOf),
        Integer[].class, list(Integer[]::new, Integer::parseInt));

    public static <T> T parse(Class<T> optionsClass, String... args) {
        try {
            List<String> arguments = List.of(args);
            Constructor<?> constructor = optionsClass.getDeclaredConstructors()[0];

            Object[] values = Arrays.stream(constructor.getParameters()).map(it -> parseOption(arguments, it)).toArray();

            return (T) constructor.newInstance(values);
        } catch (IllegalOptionException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Object parseOption(List<String> arguments, Parameter parameter) {
        if (!parameter.isAnnotationPresent(Option.class)) {
            throw new IllegalOptionException(parameter.getName());
        }
        Class<?> type = parameter.getType();
        return PARSER.get(type).parse(arguments, parameter.getAnnotation(Option.class));
    }


}

