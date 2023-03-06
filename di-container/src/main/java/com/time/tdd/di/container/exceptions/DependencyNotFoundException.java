package com.time.tdd.di.container.exceptions;

import com.time.tdd.di.container.Component;

/**
 * @author XuJian
 * @date 2023-02-25 18:54
 **/
public class DependencyNotFoundException extends RuntimeException {
    private Component component;
    private Component dependency;

    public DependencyNotFoundException(Component component, Component dependency) {
        this.component = component;
        this.dependency = dependency;
    }


    public Component getDependency() {
        return dependency;
    }

    public Component getComponent() {
        return component;
    }
}

