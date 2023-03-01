package com.time.tdd.di.container;

import com.time.tdd.di.container.exceptions.CyclicDependenciesFoundException;
import com.time.tdd.di.container.exceptions.DependencyNotFoundException;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.internal.util.collections.Sets;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author XuJian
 * @date 2023-03-01 22:20
 **/
public class ContextTest {

    ContextConfig config;


    @BeforeEach
    public void setup() {
        config = new ContextConfig();
    }

    @Test
    void should_bind_type_to_a_specific_instance() {
        Component instance = new Component() {
        };
        config.bind(Component.class, instance);

        assertSame(instance, config.getContext().get(Component.class).get());

    }

    @Test
    void should_return_empty_if_component_not_defined() {
        Optional<Component> component = config.getContext().get(Component.class);
        assertTrue(component.isEmpty());
    }

    // TODO: 2023/3/1 could get Provider<T> from context
    @Test
    void should_retrieve_bind_type_as_provider() {
        Component instance = new Component() {
        };
        config.bind(Component.class, instance);
        Context context = config.getContext();

        ParameterizedType type = new TypeLiteral<Provider<Component>>() {
        }.getType();

        Provider<Component> provider = (Provider<Component>) context.get(type).get();
        assertSame(instance, provider.get());
    }

    @Test
    void should_not_retrieve_bind_type_as_unsupported_container() {
        Component instance = new Component() {
        };

        config.bind(Component.class, instance);
        Context context = config.getContext();

        ParameterizedType type = new TypeLiteral<List<Component>>() {
        }.getType();

        assertFalse(context.get(type).isPresent());
    }

    static abstract class TypeLiteral<T> {
        public ParameterizedType getType() {
            return (ParameterizedType) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        }
    }

    @Nested
    class DependencyCheck {
        @Test
        void should_throw_exception_if_dependency_not_found() {
            config.bind(Component.class, ComponentWithInjectConstructor.class);

            DependencyNotFoundException exception =
                assertThrows(DependencyNotFoundException.class, () -> config.getContext());

            assertSame(Dependency.class, exception.getDependency());
            assertSame(Component.class, exception.getComponent());
        }

        @Test
        void should_throw_exception_if_cyclic_dependencies_found() {
            config.bind(Component.class, ComponentWithInjectConstructor.class);
            config.bind(Dependency.class, DependencyDependedOnComponent.class);

            CyclicDependenciesFoundException e =
                assertThrows(CyclicDependenciesFoundException.class, () -> config.getContext());

            Set<Class<?>> classes = Sets.newSet(e.getComponents());
            assertEquals(2, classes.size());
            assertTrue(classes.contains(Component.class));
            assertTrue(classes.contains(Dependency.class));

        }

        @Test
            // A-> B -> C -> A
        void should_throw_exception_if_transitive_cyclic_dependencies_found() {
            config.bind(Component.class, DependencyCheck.ComponentWithInjectConstructor.class);
            config.bind(Dependency.class, DependencyCheck.DependencyDependedOnAnotherDependency.class);
            config.bind(AnotherDependency.class, DependencyCheck.AnotherDependencyDependedOnComponent.class);

            CyclicDependenciesFoundException e =
                assertThrows(CyclicDependenciesFoundException.class, () -> config.getContext());

            List<Class<?>> components = Arrays.asList(e.getComponents());

            assertEquals(3, components.size());
            assertTrue(components.contains(Component.class));
            assertTrue(components.contains(Dependency.class));
            assertTrue(components.contains(AnotherDependency.class));
        }

        static class DependencyDependedOnAnotherDependency implements Dependency {
            private AnotherDependency anotherDependency;

            @Inject
            public DependencyDependedOnAnotherDependency(AnotherDependency anotherDependency) {
                this.anotherDependency = anotherDependency;
            }
        }

        static class AnotherDependencyDependedOnComponent implements AnotherDependency {

            private Component component;

            @Inject
            public AnotherDependencyDependedOnComponent(Component component) {
                this.component = component;
            }
        }

        static class DependencyDependedOnComponent implements Dependency {
            private Component component;

            @Inject
            public DependencyDependedOnComponent(Component component) {
                this.component = component;
            }
        }

        static class ComponentWithInjectConstructor implements Component {

            private Dependency dependency;

            @Inject
            public ComponentWithInjectConstructor(Dependency dependency) {
                this.dependency = dependency;
            }

            public Dependency getDependency() {
                return dependency;
            }
        }

    }
}

