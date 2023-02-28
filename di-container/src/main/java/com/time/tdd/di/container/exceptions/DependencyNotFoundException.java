package com.time.tdd.di.container.exceptions;

/**
 * @author XuJian
 * @date 2023-02-25 18:54
 **/
public class DependencyNotFoundException extends RuntimeException {
    private Class<?> component;
    private Class<?> dependency;

    public DependencyNotFoundException(Class<?> component, Class<?> dependency) {
        this.component = component;
        this.dependency = dependency;
    }

    public Class<?> getDependency() {
        return dependency;
    }

    public Class<?> getComponent() {
        return component;
    }
}

