package com.time.tdd.di.container;

import com.time.tdd.di.container.exceptions.CyclicDependenciesFoundException;
import com.time.tdd.di.container.exceptions.DependencyNotFoundException;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import jakarta.inject.Provider;
import static java.util.List.of;

/**
 * @author XuJian
 * @date 2023-02-24 21:49
 **/
public class ContextConfig {

    private final Map<Class<?>, ComponentProvider<?>> providers = new HashMap<>();

    public <Type> void bind(Class<Type> type, Type instance) {
        providers.put(type, (ComponentProvider<Type>) context -> instance);
    }

    public <Type, Implementation extends Type> void bind(Class<Type> type, Class<Implementation> implementation) {
        providers.put(type, new InjectionProvider<>(implementation));
    }

    public Context getContext() {
        // check dependencies
        providers.keySet().forEach(component -> checkDependencies(component, new Stack<>()));

        return new Context() {
            @Override
            public <Type> Optional<Type> get(Class<Type> type) {
                return Optional.ofNullable(providers.get(type)).map(provider -> (Type) provider.get(this));
            }

            @Override
            public Optional get(ParameterizedType type) {
                if (type.getRawType() != Provider.class) {
                    return Optional.empty();
                }
                Class<?> component = (Class<?>) type.getActualTypeArguments()[0];
                return Optional.ofNullable(providers.get(component)).map(provider -> (Provider<Object>) () -> provider.get(this));
            }
        };
    }

    private void checkDependencies(Class<?> component, Stack<Class<?>> visiting) {
        for (Class<?> dependency : providers.get(component).getDependencies()) {
            if (!providers.containsKey(dependency)) {
                throw new DependencyNotFoundException(component, dependency);
            }
            if (visiting.contains(dependency)) {
                throw new CyclicDependenciesFoundException(visiting);
            }
            visiting.push(dependency);
            checkDependencies(dependency, visiting);
            visiting.pop();
        }
    }


    interface ComponentProvider<T> {
        T get(Context context);

        default List<Class<?>> getDependencies() {
            return of();
        }
    }

}

