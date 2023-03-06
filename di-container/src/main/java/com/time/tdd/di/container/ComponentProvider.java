package com.time.tdd.di.container;

import java.util.List;

/**
 * @author XuJian
 * @date 2023-03-06 21:21
 **/
interface ComponentProvider<T> {
    T get(Context context);


    default List<ComponentRef<?>> getDependencies() {
        return List.of();
    }
}

