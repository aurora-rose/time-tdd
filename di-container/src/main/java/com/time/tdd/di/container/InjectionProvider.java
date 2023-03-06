package com.time.tdd.di.container;

import com.time.tdd.di.container.exceptions.IllegalComponentException;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import jakarta.inject.Inject;
import jakarta.inject.Qualifier;
import static java.util.Arrays.stream;
import static java.util.stream.Stream.concat;

/**
 * @author XuJian
 * @date 2023-02-26 16:28
 **/
class InjectionProvider<T> implements ComponentProvider<T> {

    private final Injectable<Constructor<T>> injectConstructor;
    private final List<Injectable<Method>> injectMethods;
    private final List<Injectable<Field>> injectFields;

    public InjectionProvider(Class<T> component) {
        if (Modifier.isAbstract(component.getModifiers())) {
            throw new IllegalComponentException();
        }


        this.injectConstructor = getInjectConstructor(component);
        this.injectMethods = getInjectMethods(component);
        this.injectFields = getInjectFields(component);


        if (injectFields.stream().map(Injectable::element).anyMatch(f -> Modifier.isFinal(f.getModifiers()))) {
            throw new IllegalComponentException();
        }
        if (injectMethods.stream().map(Injectable::element).anyMatch(m -> m.getTypeParameters().length != 0)) {
            throw new IllegalComponentException();
        }
    }


    private static <T> boolean isOverrideByNoInjectMethod(Class<T> component, Method m) {
        return stream(component.getDeclaredMethods()).filter(cm -> !cm.isAnnotationPresent(Inject.class))
            .noneMatch(o -> isOverride(m, o));
    }

    private static boolean isOverrideByInjectMethod(List<Method> injectMethods, Method m) {
        return injectMethods.stream().noneMatch(o -> isOverride(m, o));
    }

    private static boolean isOverride(Method m, Method o) {
        return o.getName().equals(m.getName()) &&
            Arrays.equals(o.getParameterTypes(), m.getParameterTypes());
    }

    private static <T> List<T> traverse(Class<?> component, BiFunction<List<T>, Class<?>, List<T>> finder) {
        List<T> members = new ArrayList<>();
        Class<?> current = component;
        while (current != Object.class) {
            members.addAll(finder.apply(members, current));
            current = current.getSuperclass();
        }
        return members;
    }

    private static <Type> Constructor<Type> defaultConstructor(Class<Type> implementation) {
        try {
            return implementation.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new IllegalComponentException();
        }
    }

    private static <T extends AnnotatedElement> Stream<T> injectable(T[] declaredFields) {
        return stream(declaredFields).filter(f -> f.isAnnotationPresent(Inject.class));
    }

    private static List<Injectable<Field>> getInjectFields(Class<?> component) {
        List<Field> injectFields = traverse(component, (fields, current) -> injectable(current.getDeclaredFields()).toList());
        return injectFields.stream().map(Injectable::of).toList();
    }

    private static List<Injectable<Method>> getInjectMethods(Class<?> component) {
        List<Method> injectMethods = traverse(component, (methods, current) -> injectable(current.getDeclaredMethods())
            .filter(m -> isOverrideByInjectMethod(methods, m))
            .filter(m -> isOverrideByNoInjectMethod(component, m))
            .toList());
        Collections.reverse(injectMethods);
        return injectMethods.stream().map(Injectable::of).toList();
    }

    private static <T> Injectable<Constructor<T>> getInjectConstructor(Class<T> component) {
        List<Constructor<?>> injectConstructors = injectable(component.getConstructors()).toList();
        if (injectConstructors.size() > 1) {
            throw new IllegalComponentException();
        }

        return Injectable.of((Constructor<T>) injectConstructors.stream().findFirst().orElseGet(() -> defaultConstructor(component)));
    }

    @Override
    public T get(Context context) {
        try {
            T instance = injectConstructor.element().newInstance(injectConstructor.toDependencies(context));
            for (Injectable<Field> field : injectFields) {
                field.element().set(instance, field.toDependencies(context)[0]);
            }
            for (Injectable<Method> method : injectMethods) {
                method.element().invoke(instance, method.toDependencies(context));
            }
            return instance;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ComponentRef<?>> getDependencies() {
        return concat(concat(Stream.of(injectConstructor), injectFields.stream()), injectMethods.stream())
            .flatMap(i -> stream(i.required())).toList();
    }

    record Injectable<Element extends AccessibleObject>(Element element, ComponentRef<?>[] required) {

        static <Element extends Executable> Injectable<Element> of(Element constructor) {
            return new Injectable<>(constructor,
                stream(constructor.getParameters()).map(Injectable::toComponentRef).toArray(ComponentRef<?>[]::new));
        }

        static Injectable<Field> of(Field field) {
            return new Injectable<>(field, new ComponentRef<?>[] {toComponentRef(field)});
        }

        private static ComponentRef toComponentRef(Field field) {
            return ComponentRef.of(field.getGenericType(), getQualifier(field));
        }

        private static Annotation getQualifier(AnnotatedElement element) {
            List<Annotation> qualifiers =
                stream(element.getAnnotations()).filter(a -> a.annotationType().isAnnotationPresent(Qualifier.class)).toList();
            if (qualifiers.size() > 1) {
                throw new IllegalComponentException();
            }
            return qualifiers.stream().findFirst().orElse(null);
        }

        private static ComponentRef<?> toComponentRef(Parameter parameter) {
            return ComponentRef.of(parameter.getParameterizedType(), getQualifier(parameter));
        }

        Object[] toDependencies(Context context) {
            return stream(required).map(context::get).map(Optional::get).toArray();
        }
    }
}

