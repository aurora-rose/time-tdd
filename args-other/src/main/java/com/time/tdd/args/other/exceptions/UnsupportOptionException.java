package com.time.tdd.args.other.exceptions;

/**
 * @author XuJian
 * @date 2023-02-21 22:58
 **/
public class UnsupportOptionException extends RuntimeException {
    private String option;
    private Class<?> aClass;

    public UnsupportOptionException(String option, Class<?> aClass) {
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

