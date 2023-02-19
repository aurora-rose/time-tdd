package com.time.tdd.args.custom;

import com.time.tdd.args.exceptions.IllegalOptionException;
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
    private static final Map<Class<?>, OptionParser> PASSER = Map.of(
        boolean.class, bool(),
        int.class, unary(0, Integer::parseInt),
        String.class, unary("", String::valueOf)
    );

    public static <T> T parse(Class<T> optionClass, String... args) {
        List<String> arguments = List.of(args);
        Constructor<?> constructor = optionClass.getDeclaredConstructors()[0];
        Object[] values = Arrays.stream(constructor.getParameters()).map(it -> getOptionParser(arguments, it)).toArray();


        try {
            return (T) constructor.newInstance(values);

        } catch (IllegalOptionException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Object getOptionParser(List<String> arguments, Parameter parameter) {
        if (!parameter.isAnnotationPresent(CustomOption.class)) {
            throw new IllegalOptionException(parameter.getName());
        }
        return PASSER.get(parameter.getType()).parse(arguments, parameter.getAnnotation(CustomOption.class));
    }

}

