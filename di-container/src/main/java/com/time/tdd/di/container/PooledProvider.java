package com.time.tdd.di.container;

import java.util.ArrayList;
import java.util.List;

/**
 * @author XuJian
 * @date 2023-03-06 21:21
 **/
class PooledProvider<T> implements ComponentProvider<T> {
    static int MAX = 2;
    private final List<T> pool = new ArrayList<>();
    private final ComponentProvider<T> provider;
    int current;

    public PooledProvider(ComponentProvider<T> provider) {
        this.provider = provider;
    }

    @Override
    public T get(Context context) {
        if (pool.size() < MAX) {
            pool.add(provider.get(context));
        }
        return pool.get(current++ % MAX);
    }

    @Override
    public List<ComponentRef<?>> getDependencies() {
        return provider.getDependencies();
    }
}

