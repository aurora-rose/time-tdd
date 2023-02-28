package com.time.tdd.args.other.exceptions;

/**
 * @author XuJian
 * @date 2023-02-21 22:56
 **/
public class IllegalOptionException extends RuntimeException {

    private String parameter;

    public IllegalOptionException(String parameter) {
        this.parameter = parameter;
    }

    public String getParameter() {
        return parameter;
    }
}

