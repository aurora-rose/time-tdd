package com.time.tdd.args.london;

import java.util.Arrays;

/**
 * london
 *
 * @author XuJian
 * @date 2023-02-22 22:19
 **/
public class LdArgs<T> {
    private LdValueRetriever retriever;
    private LdOptionParser parser;
    private LdOptionClass<T> optionClass;

    public LdArgs(LdValueRetriever retriever, LdOptionParser parser,
                  LdOptionClass<T> optionClass) {

        this.retriever = retriever;
        this.parser = parser;
        this.optionClass = optionClass;
    }

    public T parse(String... args) {
        return optionClass.create(Arrays.stream(optionClass.getOptionNames())
            .map(name -> parser.parse(optionClass.getOptionType(name), retriever.getValue(name, args))
            ).toArray(Object[]::new));
    }
}

