package com.time.tdd.args.custom;

import java.util.List;

/**
 * @author XuJian
 * @date 2023-02-19 21:46
 **/
interface OptionParser<T> {
    T parse(List<String> arguments, CustomOption option);
}

