package com.time.tdd.args.exceptions;

/**
 * @author XuJian
 * @date 2023-02-19 15:13
 **/
public class IllegalValueException extends RuntimeException {

    private String option;
    private Object value;

    public IllegalValueException(String option, Object value) {
        this.option = option;
        this.value = value;
    }


    public String getOption() {
        return this.option;
    }

    public Object getValue() {
        return this.value;
    }
}

