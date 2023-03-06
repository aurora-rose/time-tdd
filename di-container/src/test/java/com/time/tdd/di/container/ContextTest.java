package com.time.tdd.di.container;

import com.time.tdd.di.container.InjectionTest.ConstructorInjection.Injection.InjectConstructor;
import com.time.tdd.di.container.exceptions.CyclicDependenciesFoundException;
import com.time.tdd.di.container.exceptions.DependencyNotFoundException;
import com.time.tdd.di.container.exceptions.IllegalComponentException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.internal.util.collections.Sets;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author XuJian
 * @date 2023-03-01 22:20
 **/
public class ContextTest {

    ContextConfig config;
    TestComponent instance = new TestComponent() {
    };

    Dependency dependency = new Dependency() {
    };


    @BeforeEach
    public void setup() {
        config = new ContextConfig();
    }


    @Nested
    class TypeBinding {
        static Stream<Arguments> should_bind_type_to_an_injectable_component() {
            return Stream.of(Arguments.of(Named.of("Constructor Injection", TypeBinding.ConstructorInjection.class)),
                Arguments.of(Named.of("Field Injection", TypeBinding.FieldInjection.class)),
                Arguments.of(Named.of("Method Injection", TypeBinding.MethodInjection.class)));
        }

        @Test
        void should_bind_type_to_a_specific_instance() {
            config.bind(TestComponent.class, instance);

            Context context = config.getContext();
            assertSame(instance, context.get(ComponentRef.of(TestComponent.class)).get());

        }

        @ParameterizedTest(name = "supporting {0}")
        @MethodSource
        void should_bind_type_to_an_injectable_component(Class<? extends TestComponent> componentType) {
            config.bind(Dependency.class, dependency);
            config.bind(TestComponent.class, componentType);

            Optional<TestComponent> component = config.getContext().get(ComponentRef.of(TestComponent.class));

            assertTrue(component.isPresent());
            assertSame(dependency, component.get().dependency());
        }

        @Test
        void should_return_empty_if_component_not_defined() {
            Context context = config.getContext();
            Optional component = context.get(ComponentRef.of(TestComponent.class));
            assertTrue(component.isEmpty());
        }

        @Test
        void should_retrieve_bind_type_as_provider() {
            config.bind(TestComponent.class, instance);
            Context context = config.getContext();
            Provider<TestComponent> provider = context.get(new ComponentRef<Provider<TestComponent>>() {
            }).get();
            assertSame(instance, provider.get());
        }

        @Test
        void should_not_retrieve_bind_type_as_unsupported_container() {
            config.bind(TestComponent.class, instance);
            Context context = config.getContext();

            assertFalse(context.get(new ComponentRef<List<TestComponent>>() {
            }).isPresent());
        }


        static class ConstructorInjection implements TestComponent {

            private Dependency dependency;

            @Inject
            public ConstructorInjection(Dependency dependency) {
                this.dependency = dependency;
            }

            @Override
            public Dependency dependency() {
                return dependency;
            }
        }

        static class FieldInjection implements TestComponent {
            @Inject
            Dependency dependency;

            @Override
            public Dependency dependency() {
                return dependency;
            }
        }

        static class MethodInjection implements TestComponent {
            private Dependency dependency;

            @Inject
            void install(Dependency dependency) {
                this.dependency = dependency;
            }

            @Override
            public Dependency dependency() {
                return dependency;
            }
        }

        @Nested
        class WithQualifier {
            @Test
            void should_bind_instance_with_multi_qualifiers() {
                config.bind(TestComponent.class, instance, new NamedLiteral("ChoseOne"), new SkywalkerLiteral());

                Context context = config.getContext();
                TestComponent choseOne = context.get(ComponentRef.of(TestComponent.class, new NamedLiteral("ChoseOne"))).get();
                TestComponent skywalker = context.get(ComponentRef.of(TestComponent.class, new SkywalkerLiteral())).get();

                assertSame(instance, choseOne);
                assertSame(instance, skywalker);
            }

            @Test
            void should_bind_component_with_multi_qualifiers() {
                config.bind(Dependency.class, dependency);
                config.bind(InjectConstructor.class, InjectConstructor.class, new NamedLiteral("ChoseOne"), new SkywalkerLiteral());

                Context context = config.getContext();
                InjectConstructor choseOne = context.get(ComponentRef.of(InjectConstructor.class, new NamedLiteral("ChoseOne"))).get();
                InjectConstructor skywalker = context.get(ComponentRef.of(InjectConstructor.class, new SkywalkerLiteral())).get();

                assertSame(dependency, choseOne.dependency);
                assertSame(dependency, skywalker.dependency);
            }


            @Test
            void should_throw_exception_if_illegal_qualifier_given_to_instance() {
                assertThrows(IllegalComponentException.class, () -> config.bind(TestComponent.class, instance, new TestLiteral()));
            }

            @Test
            void should_throw_exception_if_illegal_qualifier_given_to_component() {
                assertThrows(IllegalComponentException.class,
                    () -> config.bind(InjectConstructor.class, InjectConstructor.class, new TestLiteral()));
            }

            // TODO: 2023/3/4 Provider
        }

        @Nested
        class WithScope {
            @Test
            void should_not_be_singleton_scope_by_default() {
                config.bind(NotSingleton.class, NotSingleton.class);
                Context context = config.getContext();
                assertNotSame(context.get(ComponentRef.of(NotSingleton.class)), context.get(ComponentRef.of(NotSingleton.class)));
            }

            @Test
            void should_bind_component_as_singleton_scoped() {
                config.bind(NotSingleton.class, NotSingleton.class, new SingletonLiteral());

                Context context = config.getContext();

                assertSame(context.get(ComponentRef.of(NotSingleton.class)).get(), context.get(ComponentRef.of(NotSingleton.class)).get());
            }

            @Test
            void should_retrieve_scope_annotation_from_component() {
                config.bind(Dependency.class, SingletonAnnotated.class);

                Context context = config.getContext();
                assertSame(context.get(ComponentRef.of(Dependency.class)).get(),
                    context.get(ComponentRef.of(Dependency.class)).get());
            }

            @Test
            void should_bind_component_as_customize_scope() {
                config.scope(Pooled.class, PooledProvider::new);
                config.bind(NotSingleton.class, NotSingleton.class, new PooledLiteral());

                Context context = config.getContext();

                List<NotSingleton> instances =
                    IntStream.range(0, 5).mapToObj(i -> context.get(ComponentRef.of(NotSingleton.class)).get()).toList();

                assertEquals(PooledProvider.MAX, new HashSet<>(instances).size());
            }

            @Test
            void should_throw_exception_if_multi_scope_provider() {
                assertThrows(IllegalComponentException.class,
                    () -> config.bind(NotSingleton.class, NotSingleton.class, new SingletonLiteral(), new PooledLiteral()));
            }

            @Test
            void should_throw_exception_if_multi_scope_annotated() {

                assertThrows(IllegalComponentException.class, () -> config.bind(MultiScopeAnnotated.class, MultiScopeAnnotated.class));
            }


            @Test
            void should_throw_exception_if_scope_undefined() {
                assertThrows(IllegalComponentException.class,
                    () -> config.bind(NotSingleton.class, NotSingleton.class, new PooledLiteral()));
            }


            @Singleton
            @Pooled
            static class MultiScopeAnnotated {
            }

            static class NotSingleton {

            }

            @Singleton
            static class SingletonAnnotated implements Dependency {

            }

            @Nested
            class WithQualifier {
                @Test
                void should_not_be_singleton_scope_by_default() {
                    config.bind(NotSingleton.class, NotSingleton.class, new SkywalkerLiteral());
                    Context context = config.getContext();
                    assertNotSame(context.get(ComponentRef.of(NotSingleton.class, new SkywalkerLiteral())),
                        context.get(ComponentRef.of(
                            NotSingleton.class, new SkywalkerLiteral())));
                }

                @Test
                void should_bind_component_as_singleton_scoped() {
                    config.bind(NotSingleton.class, NotSingleton.class, new SingletonLiteral(), new SkywalkerLiteral());

                    Context context = config.getContext();

                    assertSame(context.get(ComponentRef.of(NotSingleton.class, new SkywalkerLiteral())).get(),
                        context.get(ComponentRef.of(NotSingleton.class, new SkywalkerLiteral())).get());
                }

                @Test
                void should_retrieve_scope_annotation_from_component() {
                    config.bind(Dependency.class, SingletonAnnotated.class, new SkywalkerLiteral());

                    Context context = config.getContext();
                    assertSame(context.get(ComponentRef.of(Dependency.class, new SkywalkerLiteral())).get(),
                        context.get(ComponentRef.of(Dependency.class, new SkywalkerLiteral())).get());
                }

            }
        }

    }

    @Nested
    class DependencyCheck {
        static Stream<Arguments> should_throw_exception_if_dependency_not_found() {
            return Stream.of(Arguments.of(Named.of("Inject Constructor", DependencyCheck.MissingDependencyConstructor.class)),
                Arguments.of(Named.of("Inject Field", DependencyCheck.MissingDependencyField.class)),
                Arguments.of(Named.of("Inject Method", DependencyCheck.MissingDependencyMethod.class)),
                Arguments.of(Named.of("Provider in Inject Constructor", DependencyCheck.MissingDependencyProviderConstructor.class)),
                Arguments.of(Named.of("Provider in Inject Field", DependencyCheck.MissingDependencyProviderField.class)),
                Arguments.of(Named.of("Provider in Inject Method", DependencyCheck.MissingDependencyProviderMethod.class)),
                Arguments.of(Named.of("Scoped", MissingDependencyScoped.class)),
                Arguments.of(Named.of("Provider Scoped", MissingDependencyProviderScoped.class))
            );
        }

        static Stream<Arguments> should_throw_exception_if_cyclic_dependencies_found() {
            List<Arguments> arguments = new ArrayList<>();
            for (Named<? extends Class<? extends TestComponent>> component : List.of(
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
            for (Named<? extends Class<? extends TestComponent>> component : List.of(
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
        void should_throw_exception_if_dependency_not_found(Class<? extends TestComponent> component) {
            config.bind(TestComponent.class, component);

            DependencyNotFoundException exception =
                assertThrows(DependencyNotFoundException.class, () -> config.getContext());

            assertSame(Dependency.class, exception.getDependency().type());
            assertSame(TestComponent.class, exception.getComponent().type());
        }

        @ParameterizedTest(name = "cyclic dependency between {0} and {1}")
        @MethodSource
        void should_throw_exception_if_cyclic_dependencies_found(Class<? extends TestComponent> component,
                                                                 Class<? extends Dependency> dependency) {
            config.bind(TestComponent.class, component);
            config.bind(Dependency.class, dependency);

            CyclicDependenciesFoundException e =
                assertThrows(CyclicDependenciesFoundException.class, () -> config.getContext());

            Set<Class<?>> classes = Sets.newSet(e.getComponents());
            assertEquals(2, classes.size());
            assertTrue(classes.contains(TestComponent.class));
            assertTrue(classes.contains(Dependency.class));
        }

        // A-> B -> C -> A
        @ParameterizedTest(name = "indirect cyclic dependency between {0}, {1} and {2}")
        @MethodSource
        void should_throw_exception_if_transitive_cyclic_dependencies_found(Class<? extends TestComponent> component,
                                                                            Class<? extends Dependency> dependency,
                                                                            Class<? extends AnotherDependency> anotherDependency) {
            config.bind(TestComponent.class, component);
            config.bind(Dependency.class, dependency);
            config.bind(AnotherDependency.class, anotherDependency);

            CyclicDependenciesFoundException e =
                assertThrows(CyclicDependenciesFoundException.class, () -> config.getContext());

            List<Class<?>> components = Arrays.asList(e.getComponents());

            assertEquals(3, components.size());
            assertTrue(components.contains(TestComponent.class));
            assertTrue(components.contains(Dependency.class));
            assertTrue(components.contains(AnotherDependency.class));
        }

        @Test
        void should_not_throw_exception_if_cyclic_dependency_via_provider() {
            config.bind(TestComponent.class, CyclicComponentInjectConstructor.class);
            config.bind(Dependency.class, CyclicDependencyProviderConstructor.class);

            Context context = config.getContext();
            assertTrue(context.get(ComponentRef.of(TestComponent.class)).isPresent());
        }


        // TODO: 2023/3/6 cyclic dependencies with scope

        @Singleton
        static class MissingDependencyScoped implements TestComponent {
            @Inject
            Dependency dependency;
        }

        @Singleton
        static class MissingDependencyProviderScoped implements TestComponent {
            @Inject
            Provider<Dependency> dependency;
        }

        static class CyclicComponentInjectConstructor implements TestComponent {
            @Inject
            public CyclicComponentInjectConstructor(Dependency dependency) {
            }
        }

        static class CyclicComponentInjectField implements TestComponent {
            @Inject
            Dependency dependency;
        }

        static class CyclicComponentInjectMethod implements TestComponent {
            @Inject
            void install(Dependency dependency) {
            }
        }

        static class CyclicDependencyInjectConstructor implements Dependency {
            @Inject
            public CyclicDependencyInjectConstructor(TestComponent component) {
            }
        }

        static class CyclicDependencyInjectField implements Dependency {
            @Inject
            TestComponent component;
        }

        static class CyclicDependencyInjectMethod implements Dependency {
            @Inject
            void install(TestComponent component) {
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
            public IndirectCyclicAnotherDependencyInjectConstructor(TestComponent component) {
            }
        }

        static class IndirectCyclicAnotherDependencyInjectField implements AnotherDependency {
            @Inject
            TestComponent component;
        }

        static class IndirectCyclicAnotherDependencyInjectMethod implements AnotherDependency {
            @Inject
            void install(TestComponent component) {
            }
        }

        static class MissingDependencyProviderField implements TestComponent {
            @Inject
            Provider<Dependency> dependency;
        }

        static class MissingDependencyProviderMethod implements TestComponent {
            @Inject
            public void install(Provider<Dependency> dependency) {
            }
        }

        static class MissingDependencyProviderConstructor implements TestComponent {
            @Inject
            public MissingDependencyProviderConstructor(Provider<Dependency> dependency) {
            }
        }

        static class MissingDependencyConstructor implements TestComponent {
            @Inject
            public MissingDependencyConstructor(Dependency dependency) {
            }
        }

        static class MissingDependencyField implements TestComponent {
            @Inject
            Dependency dependency;
        }

        static class MissingDependencyMethod implements TestComponent {
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

            private TestComponent component;

            @Inject
            public AnotherDependencyDependedOnComponent(TestComponent component) {
                this.component = component;
            }
        }

        static class DependencyDependedOnComponent implements Dependency {
            private TestComponent component;

            @Inject
            public DependencyDependedOnComponent(TestComponent component) {
                this.component = component;
            }
        }

        static class ComponentWithInjectConstructor implements TestComponent {

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
            public CyclicDependencyProviderConstructor(Provider<TestComponent> component) {
            }
        }

        @Nested
        class WithQualifier {
            static Stream<Arguments> should_throw_exception_if_dependency_with_qualifier_not_found() {
                return Stream.of(Named.of("Inject Constructor with Qualifier", InjectConstructor.class),
                    Named.of("Inject Field with Qualifier", InjectField.class),
                    Named.of("Inject Method with Qualifier", InjectMethod.class),
                    Named.of("Provider in Inject Constructor with Qualifier", InjectConstructorProvider.class),
                    Named.of("Provider in Inject Field with Qualifier", InjectFieldProvider.class),
                    Named.of("Provider in Inject Method with Qualifier", InjectMethodProvider.class)
                ).map(Arguments::of);
            }

            @ParameterizedTest
            @MethodSource
            void should_throw_exception_if_dependency_with_qualifier_not_found(Class<? extends TestComponent> component) {
                config.bind(Dependency.class, dependency);
                config.bind(TestComponent.class, component, new NamedLiteral("Owner"));

                DependencyNotFoundException exception =
                    assertThrows(DependencyNotFoundException.class, () -> config.getContext());

                assertEquals(new Component(TestComponent.class, new NamedLiteral("Owner")), exception.getComponent());
                assertEquals(new Component(Dependency.class, new SkywalkerLiteral()), exception.getDependency());
            }

            // A -> @skywalker A -> @Named A(instance0)
            @Test
            void should_not_throw_cyclic_exception_if_component_with_same_type_aged_with_different_qualifiers() {
                config.bind(Dependency.class, dependency, new NamedLiteral("ChoseOne"));
                config.bind(Dependency.class, SkywalkerDependency.class, new SkywalkerLiteral());
                config.bind(Dependency.class, NotCyclicDependency.class);

                assertDoesNotThrow(() -> config.getContext());
            }


            static class InjectConstructor {
                @Inject
                public InjectConstructor(@Skywalker Dependency dependency) {
                }
            }

            static class SkywalkerDependency implements Dependency {
                @Inject
                public SkywalkerDependency(@jakarta.inject.Named("ChoseOne") Dependency dependency) {
                }
            }

            static class NotCyclicDependency implements Dependency {
                @Inject
                public NotCyclicDependency(@Skywalker Dependency dependency) {
                }
            }

            static class InjectField implements TestComponent {
                @Inject
                @Skywalker
                Dependency dependency;
            }

            static class InjectMethod implements TestComponent {
                @Inject
                void install(@Skywalker Dependency dependency) {

                }
            }

            static class InjectConstructorProvider implements TestComponent {
                @Inject
                public InjectConstructorProvider(@Skywalker Provider<Dependency> dependency) {
                }
            }

            static class InjectFieldProvider implements TestComponent {
                @Inject
                @Skywalker
                Provider<Dependency> dependency;
            }

            static class InjectMethodProvider implements TestComponent {
                @Inject
                void install(@Skywalker Provider<Dependency> dependency) {

                }
            }

        }

    }
}

