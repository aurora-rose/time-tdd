package com.time.tdd.args.other;

import com.time.tdd.args.other.exceptions.IllegalOptionException;
import com.time.tdd.args.other.exceptions.UnsupportOptionException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author XuJian
 * @date 2023-02-21 22:29
 **/
public class Args<T> {
    private final Class<T> optionClass;
    private final Map<Class<?>, OptionParser> parsers;

    public Args(Class<T> optionClass, Map<Class<?>, OptionParser> parsers) {
        this.optionClass = optionClass;
        this.parsers = parsers;
    }

    public static Map<String, String[]> toMap(String... args) {
        Map<String, String[]> result = new HashMap<>();
        String option = null;
        List<String> values = new ArrayList<>();
        for (String arg : args) {
            if (arg.matches("^-[a-zA-Z-]+$")) {
                if (option != null) {
                    result.put(option.substring(1), values.toArray(String[]::new));
                }
                option = arg;
                values = new ArrayList<>();
            } else {
                values.add(arg);
            }
        }
        result.put(option.substring(1), values.toArray(String[]::new));

        return result;
    }

    public T parse(String... args) {
        try {
            Map<String, String[]> options = toMap(args);
            Constructor<?> constructor = optionClass.getDeclaredConstructors()[0];

            Object[] values = Arrays.stream(constructor.getParameters()).map(it -> parseOption(options, it)).toArray();

            return (T) constructor.newInstance(values);
        } catch (IllegalOptionException | UnsupportOptionException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private Object parseOption(Map<String, String[]> options, Parameter parameter) {
        if (!parameter.isAnnotationPresent(Option.class)) {
            throw new IllegalOptionException(parameter.getName());
        }
        Option option = parameter.getAnnotation(Option.class);
        if (!parsers.containsKey(parameter.getType())) {
            throw new UnsupportOptionException(option.value(), parameter.getType());
        }

        return parsers.get(parameter.getType()).parse(options.get(option.value()));

    }
}

