package com.time.tdd.args;

import java.util.List;

/**
 * @author XuJian
 * @date 2023-02-18 15:35
 **/
interface OptionParser<T> {
    T parse(List<String> arguments, Option option);
}

