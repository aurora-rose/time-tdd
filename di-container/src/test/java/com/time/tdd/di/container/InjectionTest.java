package com.time.tdd.di.container;

import com.time.tdd.di.container.exceptions.IllegalComponentException;
import java.util.Optional;
import jakarta.inject.Inject;
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
    private Context context = mock(Context.class);

    @BeforeEach
    public void setup() {
        when(context.get(eq(Dependency.class))).thenReturn(Optional.of(dependency));

    }
    

    @Nested
    class ConstructorInjection {
        @Nested
        class IllegalInjectConstructors {
            @Test
            void should_throw_exception_if_component_is_abstract() {
                assertThrows(IllegalComponentException.class, () -> new ConstructorInjectionProvider<>(AbstractComponent.class));
            }

            @Test
            void should_throw_exception_if_component_is_interface() {
                assertThrows(IllegalComponentException.class, () -> new ConstructorInjectionProvider<>(Component.class));
            }

            @Test
            void should_throw_exception_if_multi_inject_constructors_provided() {
                assertThrows(IllegalComponentException.class, () -> new ConstructorInjectionProvider<>(
                    ComponentWithMultiInjectConstructors.class));

            }

            @Test
            void should_throw_exception_if_no_inject_nor_default_constructor_provided() {
                assertThrows(IllegalComponentException.class, () -> new ConstructorInjectionProvider<>(
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

        }


        @Nested
        class Injection {
            @Test
            void should_call_default_constructor_if_no_inject_constructor() {
                ComponentWithDefaultConstructor instance =
                    new ConstructorInjectionProvider<>(ComponentWithDefaultConstructor.class).get(context);

                assertNotNull(instance);
            }

            // TODO: with dependencies
            @Test
            void should_inject_dependency_via_inject_constructor() {
                InjectConstructor instance =
                    new ConstructorInjectionProvider<>(InjectConstructor.class).get(context);

                assertNotNull(instance);
                assertSame(dependency, instance.dependency);

            }

            @Test
            void should_include_dependency_from_inject_constructor() {
                ConstructorInjectionProvider<InjectConstructor> provider =
                    new ConstructorInjectionProvider<>(InjectConstructor.class);

                assertArrayEquals(new Class<?>[] {Dependency.class}, provider.getDependencies().toArray(Class<?>[]::new));
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
        }

        abstract class AbstractComponent implements Component {
            @Inject
            public AbstractComponent() {
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
                    () -> new ConstructorInjectionProvider<>(FinalInjectField.class));
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
                    component = new ConstructorInjectionProvider<>(ComponentWithFieldInjection.class).get(context);

                assertSame(dependency, component.dependency);
            }

            @Test
            void should_inject_dependency_via_superclass_inject_field() {
                SubClassWithFieldInjection
                    component = new ConstructorInjectionProvider<>(SubClassWithFieldInjection.class).get(context);

                assertSame(dependency, component.dependency);
            }

            @Test
            void should_include_dependency_from_field_dependency() {
                ConstructorInjectionProvider<ComponentWithFieldInjection> provider =
                    new ConstructorInjectionProvider<>(ComponentWithFieldInjection.class);
                assertArrayEquals(new Class<?>[] {Dependency.class}, provider.getDependencies().toArray(Class<?>[]::new));
            }

            static class ComponentWithFieldInjection {
                @Inject
                Dependency dependency;
            }

            static class SubClassWithFieldInjection extends ComponentWithFieldInjection {

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
                    () -> new ConstructorInjectionProvider<>(InjectMethodWithTypeParameter.class));
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
                    new ConstructorInjectionProvider<>(InjectMethodWithNoDependency.class).get(context);

                assertTrue(component.called);
            }

            @Test
            void should_inject_dependency_via_inject_method() {
                InjectMethodWithDependency
                    component = new ConstructorInjectionProvider<>(InjectMethodWithDependency.class).get(context);

                assertSame(dependency, component.dependency);
            }

            @Test
            void should_include_dependencies_from_inject_method() {
                ConstructorInjectionProvider<InjectMethodWithDependency> provider =
                    new ConstructorInjectionProvider<>(InjectMethodWithDependency.class);

                assertArrayEquals(new Class<?>[] {Dependency.class}, provider.getDependencies().toArray(Class<?>[]::new));
            }

            @Test
            void should_inject_dependencies_via_inject_method_from_superclass() {
                SubClassInjectMethod component =
                    new ConstructorInjectionProvider<>(SubClassInjectMethod.class).get(context);

                assertEquals(1, component.superCalled);
                assertEquals(2, component.subCalled);
            }

            @Test
            void should_only_call_once_if_subclass_override_inject_method_with_inject() {
                SubClassOverrideSuperClassWithInjectMethod component =
                    new ConstructorInjectionProvider<>(SubClassOverrideSuperClassWithInjectMethod.class).get(context);

                assertEquals(1, component.superCalled);
            }

            @Test
            void should_not_call_inject_method_if_override_with_no_inject() {
                SubClassOverrideSuperClassWithNoInject component =
                    new ConstructorInjectionProvider<>(SubClassOverrideSuperClassWithNoInject.class).get(context);

                assertEquals(0, component.superCalled);
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
        }

    }

}

