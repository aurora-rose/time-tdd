package com.time.tdd.di.container;

import com.time.tdd.di.container.exceptions.IllegalComponentException;
import java.lang.reflect.ParameterizedType;
import java.util.Optional;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author XuJian
 * @date 2023-02-28 21:10
 **/
@Nested
public class InjectionTest {
    private Dependency dependency = mock(Dependency.class);
    private Provider<Dependency> dependencyProvider = mock(Provider.class);
    private Context context = mock(Context.class);
    private ParameterizedType dependencyProviderType;

    @BeforeEach
    public void setup() throws NoSuchFieldException {
        dependencyProviderType = (ParameterizedType) InjectionTest.class.getDeclaredField("dependencyProvider").getGenericType();
        when(context.get(eq(Context.Ref.of(Dependency.class)))).thenReturn(Optional.of(dependency));
        when(context.get(eq(Context.Ref.of(dependencyProviderType)))).thenReturn(Optional.of(dependencyProvider));

    }


    @Nested
    class ConstructorInjection {
        @Nested
        class IllegalInjectConstructors {
            @Test
            void should_throw_exception_if_component_is_abstract() {
                assertThrows(IllegalComponentException.class, () -> new InjectionProvider<>(AbstractComponent.class));
            }

            @Test
            void should_throw_exception_if_component_is_interface() {
                assertThrows(IllegalComponentException.class, () -> new InjectionProvider<>(Component.class));
            }

            @Test
            void should_throw_exception_if_multi_inject_constructors_provided() {
                assertThrows(IllegalComponentException.class, () -> new InjectionProvider<>(
                    ComponentWithMultiInjectConstructors.class));

            }

            @Test
            void should_throw_exception_if_no_inject_nor_default_constructor_provided() {
                assertThrows(IllegalComponentException.class, () -> new InjectionProvider<>(
                    ComponentWithNoInjectNorDefaultConstructors.class));
            }


            static class ComponentWithMultiInjectConstructors implements Component {
                @Inject
                public ComponentWithMultiInjectConstructors(String name, Double value) {
                }

                @Inject
                public ComponentWithMultiInjectConstructors(String name) {

                }
            }

            static class ComponentWithNoInjectNorDefaultConstructors implements Component {
                public ComponentWithNoInjectNorDefaultConstructors(String namea) {
                }
            }

            abstract class AbstractComponent implements Component {
                @Inject
                public AbstractComponent() {
                }
            }

        }


        @Nested
        class Injection {
            @Test
            void should_call_default_constructor_if_no_inject_constructor() {
                ComponentWithDefaultConstructor instance =
                    new InjectionProvider<>(ComponentWithDefaultConstructor.class).get(context);

                assertNotNull(instance);
            }

            @Test
            void should_inject_dependency_via_inject_constructor() {
                InjectConstructor instance =
                    new InjectionProvider<>(InjectConstructor.class).get(context);

                assertNotNull(instance);
                assertSame(dependency, instance.dependency);

            }

            @Test
            void should_include_dependency_from_inject_constructor() {
                InjectionProvider<InjectConstructor> provider =
                    new InjectionProvider<>(InjectConstructor.class);

                assertArrayEquals(new Context.Ref[] {Context.Ref.of(Dependency.class)},
                    provider.getDependencies().toArray(Context.Ref[]::new));
            }

            @Test
            void should_inject_provider_via_inject_constructor() {
                ProviderInjectConstructor instance = new InjectionProvider<>(ProviderInjectConstructor.class).get(context);

                assertSame(dependencyProvider, instance.dependency);
            }

            @Test
            void should_include_provider_type_from_inject_constructor() {
                InjectionProvider<ProviderInjectConstructor> provider = new InjectionProvider<>(ProviderInjectConstructor.class);
                assertArrayEquals(new Context.Ref[] {Context.Ref.of(dependencyProviderType)},
                    provider.getDependencies().toArray(Context.Ref[]::new));
            }

            static class InjectConstructor {
                Dependency dependency;

                @Inject
                public InjectConstructor(Dependency dependency) {
                    this.dependency = dependency;
                }
            }


            static class ComponentWithDefaultConstructor implements Component {
                public ComponentWithDefaultConstructor() {
                }
            }

            static class ProviderInjectConstructor {
                Provider<Dependency> dependency;

                @Inject
                public ProviderInjectConstructor(Provider<Dependency> dependency) {
                    this.dependency = dependency;
                }
            }
        }


    }

    @Nested
    class FieldInjection {
        @Nested
        class IllegalInjectFields {
            @Test
            void should_throw_exception_if_inject_field_is_final() {
                assertThrows(IllegalComponentException.class,
                    () -> new InjectionProvider<>(FinalInjectField.class));
            }


            static class FinalInjectField {
                @Inject
                final Dependency dependency = null;
            }
        }


        @Nested
        class Injection {
            @Test
            void should_inject_dependency_via_field() {
                ComponentWithFieldInjection
                    component = new InjectionProvider<>(ComponentWithFieldInjection.class).get(context);

                assertSame(dependency, component.dependency);
            }

            @Test
            void should_inject_dependency_via_superclass_inject_field() {
                SubClassWithFieldInjection
                    component = new InjectionProvider<>(SubClassWithFieldInjection.class).get(context);

                assertSame(dependency, component.dependency);
            }

            @Test
            void should_include_dependency_from_field_dependency() {
                InjectionProvider<ComponentWithFieldInjection> provider =
                    new InjectionProvider<>(ComponentWithFieldInjection.class);
                assertArrayEquals(new Context.Ref[] {Context.Ref.of(Dependency.class)},
                    provider.getDependencies().toArray(Context.Ref[]::new));
            }

            @Test
            void should_inject_provider_via_inject_field() {
                ProviderInjectField instance = new InjectionProvider<>(ProviderInjectField.class).get(context);

                assertSame(dependencyProvider, instance.dependency);
            }

            @Test
            void should_include_provider_type_from_inject_field() {
                InjectionProvider<ProviderInjectField> provider = new InjectionProvider<>(ProviderInjectField.class);
                assertArrayEquals(new Context.Ref[] {Context.Ref.of(dependencyProviderType)},
                    provider.getDependencies().toArray(Context.Ref[]::new));
            }


            static class ComponentWithFieldInjection {
                @Inject
                Dependency dependency;
            }

            static class SubClassWithFieldInjection extends ComponentWithFieldInjection {

            }

            static class ProviderInjectField {
                @Inject
                Provider<Dependency> dependency;
            }

        }


    }

    @Nested
    class MethodInjection {
        @Nested
        class IllegalInjectMethods {
            @Test
            void should_throw_exception_if_inject_method_has_type_parameter() {
                assertThrows(IllegalComponentException.class,
                    () -> new InjectionProvider<>(InjectMethodWithTypeParameter.class));
            }

            static class InjectMethodWithTypeParameter {
                @Inject
                <T> void install() {

                }
            }
        }

        @Nested
        class Injection {
            @Test
            void should_call_inject_method_even_if_no_dependency_declared() {
                InjectMethodWithNoDependency component =
                    new InjectionProvider<>(InjectMethodWithNoDependency.class).get(context);

                assertTrue(component.called);
            }

            @Test
            void should_inject_dependency_via_inject_method() {
                InjectMethodWithDependency
                    component = new InjectionProvider<>(InjectMethodWithDependency.class).get(context);

                assertSame(dependency, component.dependency);
            }

            @Test
            void should_include_dependencies_from_inject_method() {
                InjectionProvider<InjectMethodWithDependency> provider =
                    new InjectionProvider<>(InjectMethodWithDependency.class);

                assertArrayEquals(new Context.Ref[] {Context.Ref.of(Dependency.class)},
                    provider.getDependencies().toArray(Context.Ref[]::new));
            }

            @Test
            void should_inject_dependencies_via_inject_method_from_superclass() {
                SubClassInjectMethod component =
                    new InjectionProvider<>(SubClassInjectMethod.class).get(context);

                assertEquals(1, component.superCalled);
                assertEquals(2, component.subCalled);
            }

            @Test
            void should_only_call_once_if_subclass_override_inject_method_with_inject() {
                SubClassOverrideSuperClassWithInjectMethod component =
                    new InjectionProvider<>(SubClassOverrideSuperClassWithInjectMethod.class).get(context);

                assertEquals(1, component.superCalled);
            }

            @Test
            void should_not_call_inject_method_if_override_with_no_inject() {
                SubClassOverrideSuperClassWithNoInject component =
                    new InjectionProvider<>(SubClassOverrideSuperClassWithNoInject.class).get(context);

                assertEquals(0, component.superCalled);
            }

            @Test
            void should_inject_provider_via_inject_method() {
                ProviderInjectMethod instance = new InjectionProvider<>(ProviderInjectMethod.class).get(context);

                assertSame(dependencyProvider, instance.dependency);
            }

            @Test
            void should_include_provider_type_from_inject_method() {
                InjectionProvider<ProviderInjectMethod> provider = new InjectionProvider<>(ProviderInjectMethod.class);
                assertArrayEquals(new Context.Ref[] {Context.Ref.of(dependencyProviderType)},
                    provider.getDependencies().toArray(Context.Ref[]::new));
            }

            static class SubClassOverrideSuperClassWithNoInject extends SuperClassInjectMethod {

                void install() {
                    super.install();
                }
            }

            static class SubClassOverrideSuperClassWithInjectMethod extends SuperClassInjectMethod {

                @Inject
                void install() {
                    super.install();
                }
            }

            static class InjectMethodWithNoDependency {
                boolean called = false;

                @Inject
                void install() {
                    this.called = true;
                }
            }

            static class SuperClassInjectMethod {
                int superCalled = 0;

                @Inject
                void install() {
                    superCalled++;
                }
            }

            static class SubClassInjectMethod extends SuperClassInjectMethod {
                int subCalled = 0;

                @Inject
                void installAnother() {
                    subCalled = superCalled + 1;
                }
            }

            static class InjectMethodWithDependency {
                Dependency dependency;

                @Inject
                void install(Dependency dependency) {
                    this.dependency = dependency;
                }
            }

            static class ProviderInjectMethod {
                Provider<Dependency> dependency;

                @Inject
                void install(Provider<Dependency> dependency) {
                    this.dependency = dependency;
                }
            }
        }


    }

}

