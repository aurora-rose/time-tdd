package com.time.tdd.di.container;

import java.util.List;

/**
 * @author XuJian
 * @date 2023-03-06 21:21
 **/
class SingletonProvider<T> implements ComponentProvider<T> {
    private final ComponentProvider<T> provider;
    private T singleton;

    public SingletonProvider(ComponentProvider<T> provider) {
        this.provider = provider;
    }

    @Override
    public T get(Context context) {
        if (singleton == null) {
            singleton = provider.get(context);
        }
        return singleton;
    }

    @Override
    public List<ComponentRef<?>> getDependencies() {
        return provider.getDependencies();
    }
}

