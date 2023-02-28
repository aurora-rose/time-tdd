package com.time.tdd.di.container.exceptions;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author XuJian
 * @date 2023-02-25 21:10
 **/
public class CyclicDependenciesFoundException extends RuntimeException {

    private Set<Class<?>> components = new HashSet<>();


    public CyclicDependenciesFoundException(List<Class<?>> visiting) {
        components.addAll(visiting);
    }

    public Class<?>[] getComponents() {
        return components.toArray(Class<?>[]::new);
    }
}

