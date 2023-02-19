package com.time.tdd.args.exceptions;

/**
 * @author XuJian
 * @date 2023-02-18 22:46
 **/
public class InsufficientArgumentsException extends RuntimeException {
    private String option;

    public InsufficientArgumentsException(String option) {
        this.option = option;
    }


    public String getOption() {
        return this.option;
    }

}

