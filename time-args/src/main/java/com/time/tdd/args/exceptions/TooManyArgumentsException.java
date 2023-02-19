package com.time.tdd.args.exceptions;

/**
 * @author XuJian
 * @date 2023-02-18 22:09
 **/
public class TooManyArgumentsException extends RuntimeException {


    private String option;


    public TooManyArgumentsException(String option) {
        super();
        this.option = option;
    }

    public String getOption() {
        return this.option;
    }
}

