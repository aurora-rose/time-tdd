package com.time.tdd.args.london;

import java.util.Arrays;

/**
 * @author XuJian
 * @date 2023-02-22 22:52
 **/
public class ReflectionBasedOptionClass<T> implements LdOptionClass {
    private Class<T> optionClass;

    public ReflectionBasedOptionClass(Class<T> optionClass) {
        this.optionClass = optionClass;
    }

    @Override
    public String[] getOptionNames() {
        return Arrays.stream(optionClass.getDeclaredConstructors()[0].getParameters())
            .map(parameter -> parameter.getAnnotation(LdOption.class).value())
            .toArray(String[]::new);
    }

    @Override
    public Object create(Object[] value) {
        return null;
    }

    @Override
    public Class getOptionType(String type) {
        return null;
    }
}

