package com.time.tdd.args.london;

/**
 * @author mickey
 */
public interface LdOptionClass<T> {

    String[] getOptionNames();

    T create(Object[] value);

    Class getOptionType(String type);


}
