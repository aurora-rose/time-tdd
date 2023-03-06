package com.time.tdd.di.container;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * @author XuJian
 * @date 2023-03-03 00:16
 **/
public class ComponentRef<ComponentType> {
    private Type container;
    private Component component;


    ComponentRef(Type type, Annotation qualifier) {
        init(type, qualifier);
    }


    ComponentRef(Class<ComponentType> componentType) {
        init(componentType, null);
    }

    protected ComponentRef() {
        Type type = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        init(type, null);
    }

    public static <ComponentType> ComponentRef<ComponentType> of(Class<ComponentType> component) {
        return new ComponentRef<>(component);
    }

    public static <ComponentType> ComponentRef<ComponentType> of(Class<ComponentType> component, Annotation qualifier) {
        return new ComponentRef<>(component, qualifier);
    }

    static ComponentRef of(Type type) {
        return new ComponentRef(type, null);
    }

    static ComponentRef of(Type type, Annotation qualifier) {
        return new ComponentRef(type, qualifier);
    }

    private void init(Type type, Annotation qualifier) {
        if (type instanceof ParameterizedType container) {
            this.container = container.getRawType();
            Class<ComponentType> componentType = (Class<ComponentType>) container.getActualTypeArguments()[0];
            this.component = new Component(componentType, qualifier);
        } else {
            Class<ComponentType> componentType = (Class<ComponentType>) type;
            this.component = new Component(componentType, qualifier);
        }
    }

    public Component component() {
        return component;
    }

    public Type getContainer() {
        return container;
    }

    public boolean isContainer() {
        return container != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ComponentRef<?> that = (ComponentRef<?>) o;
        return Objects.equals(container, that.container) && component.equals(that.component);
    }

    @Override
    public int hashCode() {
        return Objects.hash(container, component);
    }
}

