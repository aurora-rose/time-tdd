package com.time.tdd.args.exceptions;

/**
 * @author XuJian
 * @date 2023-02-21 21:39
 **/
public class UnsupportedOptionTypeException extends RuntimeException {

    private String option;
    private Class<?> aClass;


    public UnsupportedOptionTypeException(String option, Class<?> aClass) {
        this.option = option;
        this.aClass = aClass;
    }

    public String getOption() {
        return option;
    }

    public Class<?> getaClass() {
        return aClass;
    }
}

