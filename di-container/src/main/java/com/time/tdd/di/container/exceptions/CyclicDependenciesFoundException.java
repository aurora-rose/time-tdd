package com.time.tdd.di.container.exceptions;

import com.time.tdd.di.container.Component;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author XuJian
 * @date 2023-02-25 21:10
 **/
public class CyclicDependenciesFoundException extends RuntimeException {

    private Set<Component> components = new HashSet<>();


    public CyclicDependenciesFoundException(List<Component> visiting) {
        components.addAll(visiting);
    }

    public Class<?>[] getComponents() {
        return components.stream().map(Component::type).toArray(Class<?>[]::new);
    }
}

