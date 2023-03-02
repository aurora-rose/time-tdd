package com.time.tdd.di.container;

import com.time.tdd.di.container.exceptions.CyclicDependenciesFoundException;
import com.time.tdd.di.container.exceptions.DependencyNotFoundException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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


    @Nested
    class TypeBinding {
        @Test
        void should_bind_type_to_a_specific_instance() {
            Component instance = new Component() {
            };
            config.bind(Component.class, instance);

            Context context = config.getContext();
            assertSame(instance, context.get(Context.Ref.of(Component.class)).get());

        }

        @Test
        void should_return_empty_if_component_not_defined() {
            Context context = config.getContext();
            Optional component = context.get(Context.Ref.of(Component.class));
            assertTrue(component.isEmpty());
        }

        @Test
        void should_retrieve_bind_type_as_provider() {
            Component instance = new Component() {
            };
            config.bind(Component.class, instance);
            Context context = config.getContext();

            ParameterizedType type = new TypeLiteral<Provider<Component>>() {
            }.getType();

            Provider<Component> provider = (Provider<Component>) context.get(Context.Ref.of(type)).get();
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

            assertFalse(context.get(Context.Ref.of(type)).isPresent());
        }

        static abstract class TypeLiteral<T> {
            public ParameterizedType getType() {
                return (ParameterizedType) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
            }
        }
    }


    @Nested
    class DependencyCheck {
        public static Stream<Arguments> should_throw_exception_if_dependency_not_found() {
            return Stream.of(Arguments.of(Named.of("Inject Constructor", DependencyCheck.MissingDependencyConstructor.class)),
                Arguments.of(Named.of("Inject Field", DependencyCheck.MissingDependencyField.class)),
                Arguments.of(Named.of("Inject Method", DependencyCheck.MissingDependencyMethod.class)),
                Arguments.of(Named.of("Provider in Inject Constructor", DependencyCheck.MissingDependencyProviderConstructor.class)),
                Arguments.of(Named.of("Provider in Inject Field", DependencyCheck.MissingDependencyField.class)),
                Arguments.of(Named.of("Provider in Inject Method", DependencyCheck.MissingDependencyMethod.class))
            );
        }

        static Stream<Arguments> should_throw_exception_if_cyclic_dependencies_found() {
            List<Arguments> arguments = new ArrayList<>();
            for (Named<? extends Class<? extends Component>> component : List.of(
                Named.of("Inject Constructor", CyclicComponentInjectConstructor.class),
                Named.of("Inject Field", CyclicComponentInjectField.class),
                Named.of("Inject Method", CyclicComponentInjectMethod.class))) {
                for (Named<? extends Class<? extends Dependency>> dependency : List.of(
                    Named.of("Inject Constructor", CyclicDependencyInjectConstructor.class),
                    Named.of("Inject Field", CyclicDependencyInjectField.class),
                    Named.of("Inject Method", CyclicDependencyInjectMethod.class))) {
                    arguments.add(Arguments.of(component, dependency));
                }
            }
            return arguments.stream();
        }

        static Stream<Arguments> should_throw_exception_if_transitive_cyclic_dependencies_found() {
            List<Arguments> arguments = new ArrayList<>();
            for (Named<? extends Class<? extends Component>> component : List.of(
                Named.of("Inject Constructor", CyclicComponentInjectConstructor.class),
                Named.of("Inject Field", CyclicComponentInjectField.class),
                Named.of("Inject Method", CyclicComponentInjectMethod.class))) {
                for (Named<? extends Class<? extends Dependency>> dependency : List.of(
                    Named.of("Inject Constructor", IndirectCyclicDependencyInjectConstructor.class),
                    Named.of("Inject Field", IndirectCyclicDependencyInjectField.class),
                    Named.of("Inject Method", IndirectCyclicDependencyInjectMethod.class))) {
                    for (Named<? extends Class<? extends AnotherDependency>> anotherDependency : List.of(
                        Named.of("Inject Constructor", IndirectCyclicAnotherDependencyInjectConstructor.class),
                        Named.of("Inject Field", IndirectCyclicAnotherDependencyInjectField.class),
                        Named.of("Inject Method", IndirectCyclicAnotherDependencyInjectMethod.class))) {
                        arguments.add(Arguments.of(component, dependency, anotherDependency));
                    }
                }
            }
            return arguments.stream();
        }

        @ParameterizedTest
        @MethodSource
        void should_throw_exception_if_dependency_not_found(Class<? extends Component> component) {
            config.bind(Component.class, component);

            DependencyNotFoundException exception =
                assertThrows(DependencyNotFoundException.class, () -> config.getContext());

            assertSame(Dependency.class, exception.getDependency());
            assertSame(Component.class, exception.getComponent());
        }

        @ParameterizedTest(name = "cyclic dependency between {0} and {1}")
        @MethodSource
        void should_throw_exception_if_cyclic_dependencies_found(Class<? extends Component> component,
                                                                 Class<? extends Dependency> dependency) {
            config.bind(Component.class, component);
            config.bind(Dependency.class, dependency);

            CyclicDependenciesFoundException e =
                assertThrows(CyclicDependenciesFoundException.class, () -> config.getContext());

            Set<Class<?>> classes = Sets.newSet(e.getComponents());
            assertEquals(2, classes.size());
            assertTrue(classes.contains(Component.class));
            assertTrue(classes.contains(Dependency.class));
        }

        // A-> B -> C -> A
        @ParameterizedTest(name = "indirect cyclic dependency between {0}, {1} and {2}")
        @MethodSource
        void should_throw_exception_if_transitive_cyclic_dependencies_found(Class<? extends Component> component,
                                                                            Class<? extends Dependency> dependency,
                                                                            Class<? extends AnotherDependency> anotherDependency) {
            config.bind(Component.class, component);
            config.bind(Dependency.class, dependency);
            config.bind(AnotherDependency.class, anotherDependency);

            CyclicDependenciesFoundException e =
                assertThrows(CyclicDependenciesFoundException.class, () -> config.getContext());

            List<Class<?>> components = Arrays.asList(e.getComponents());

            assertEquals(3, components.size());
            assertTrue(components.contains(Component.class));
            assertTrue(components.contains(Dependency.class));
            assertTrue(components.contains(AnotherDependency.class));
        }

        @Test
        void should_not_throw_exception_if_cyclic_dependency_via_provider() {
            config.bind(Component.class, CyclicComponentInjectConstructor.class);
            config.bind(Dependency.class, CyclicDependencyProviderConstructor.class);

            Context context = config.getContext();
            assertTrue(context.get(Context.Ref.of(Component.class)).isPresent());
        }

        static class CyclicComponentInjectConstructor implements Component {
            @Inject
            public CyclicComponentInjectConstructor(Dependency dependency) {
            }
        }

        static class CyclicComponentInjectField implements Component {
            @Inject
            Dependency dependency;
        }

        static class CyclicComponentInjectMethod implements Component {
            @Inject
            void install(Dependency dependency) {
            }
        }

        static class CyclicDependencyInjectConstructor implements Dependency {
            @Inject
            public CyclicDependencyInjectConstructor(Component component) {
            }
        }

        static class CyclicDependencyInjectField implements Dependency {
            @Inject
            Component component;
        }

        static class CyclicDependencyInjectMethod implements Dependency {
            @Inject
            void install(Component component) {
            }
        }

        static class IndirectCyclicDependencyInjectConstructor implements Dependency {
            @Inject
            public IndirectCyclicDependencyInjectConstructor(AnotherDependency anotherDependency) {
            }
        }

        static class IndirectCyclicDependencyInjectField implements Dependency {
            @Inject
            AnotherDependency anotherDependency;
        }

        static class IndirectCyclicDependencyInjectMethod implements Dependency {
            @Inject
            void install(AnotherDependency anotherDependency) {
            }
        }

        static class IndirectCyclicAnotherDependencyInjectConstructor implements AnotherDependency {
            @Inject
            public IndirectCyclicAnotherDependencyInjectConstructor(Component component) {
            }
        }

        static class IndirectCyclicAnotherDependencyInjectField implements AnotherDependency {
            @Inject
            Component component;
        }

        static class IndirectCyclicAnotherDependencyInjectMethod implements AnotherDependency {
            @Inject
            void install(Component component) {
            }
        }

        static class MissingDependencyProviderField implements Component {
            @Inject
            Provider<Dependency> dependency;
        }

        static class MissingDependencyProviderMethod implements Component {
            @Inject
            public void install(Provider<Dependency> dependency) {
            }
        }

        static class MissingDependencyProviderConstructor implements Component {
            @Inject
            public MissingDependencyProviderConstructor(Provider<Dependency> dependency) {
            }
        }

        static class MissingDependencyConstructor implements Component {
            @Inject
            public MissingDependencyConstructor(Dependency dependency) {
            }
        }

        static class MissingDependencyField implements Component {
            @Inject
            Dependency dependency;
        }

        static class MissingDependencyMethod implements Component {
            @Inject
            void install(Dependency dependency) {
            }
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

        static class CyclicDependencyProviderConstructor implements Dependency {
            @Inject
            public CyclicDependencyProviderConstructor(Provider<Component> component) {
            }
        }

    }
}

