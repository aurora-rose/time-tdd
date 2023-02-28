package com.time.tdd.args.custom;

import com.time.tdd.args.exceptions.IllegalOptionException;
import com.time.tdd.args.exceptions.UnsupportedOptionTypeException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import static com.time.tdd.args.custom.OptionParsers.bool;
import static com.time.tdd.args.custom.OptionParsers.unary;

/**
 * @author XuJian
 * @date 2023-02-19 20:44
 **/
public class CustomArgs {
    private static final Map<Class<?>, OptionParser> PARSERS = Map.of(
        boolean.class, bool(),
        int.class, unary(0, Integer::parseInt),
        String.class, unary("", String::valueOf)
    );

    public static <T> T parse(Class<T> optionClass, String... args) {
        return getT(PARSERS, optionClass, args);
    }


    private static Object parseOption(List<String> arguments, Parameter parameter, Map<Class<?>, OptionParser> parser) {
        if (!parameter.isAnnotationPresent(CustomOption.class)) {
            throw new IllegalOptionException(parameter.getName());
        }
        CustomOption option = parameter.getAnnotation(CustomOption.class);
        if (!parser.containsKey(parameter.getType())) {
            throw new UnsupportedOptionTypeException(option.value(), parameter.getType());
        }
        return parser.get(parameter.getType()).parse(arguments, option);
    }

    public static <T> T getT(Map<Class<?>, OptionParser> parser, Class<T> optionsClass, String[] args) {
        List<String> arguments = List.of(args);
        Constructor<?> constructor = optionsClass.getDeclaredConstructors()[0];
        Object[] values = Arrays.stream(constructor.getParameters()).map(it -> parseOption(arguments, it, parser)).toArray();

        try {
            return (T) constructor.newInstance(values);

        } catch (IllegalOptionException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

