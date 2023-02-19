package com.time.tdd.args.exceptions;

/**
 * @author XuJian
 * @date 2023-02-18 23:38
 **/
public class IllegalOptionException extends RuntimeException {
    private String parameter;

    public IllegalOptionException(String parameter) {
        this.parameter = parameter;
    }

    public String getParameter() {
        return this.parameter;
    }
}

