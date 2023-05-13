package com.time.tdd.rest;

import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.RuntimeDelegate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author XuJian
 * @date 2023-05-13 11:03
 **/
public abstract class InjectableCallerTest {
    protected ResourceContext context;
    protected UriInfoBuilder builder;
    protected UriInfo uriInfo;
    protected MultivaluedHashMap<String, String> parameters;
    protected LastCall lastCall;
    protected someServiceInContext service;
    private Object resource;
    private RuntimeDelegate delegate;


    protected static String getMethodName(String name, List<? extends Class<?>> classList) {
        return name + "(" + classList.stream()
                .map(Class::getSimpleName).collect(Collectors.joining(",")) + ")";
    }

    @BeforeEach
    public void before() {
        lastCall = null;
        resource = initResource();


        context = mock(ResourceContext.class);
        builder = mock(UriInfoBuilder.class);
        uriInfo = mock(UriInfo.class);
        service = mock(someServiceInContext.class);

        parameters = new MultivaluedHashMap<>();
        when(builder.getLastMatchedResource()).thenReturn(resource);
        when(uriInfo.getPathParameters()).thenReturn(parameters);
        when(uriInfo.getQueryParameters()).thenReturn(parameters);
        when(builder.createUriInfo()).thenReturn(uriInfo);
        when(context.getResource(eq(someServiceInContext.class))).thenReturn(service);

        delegate = mock(RuntimeDelegate.class);
        RuntimeDelegate.setInstance(delegate);
        when(delegate.createResponseBuilder()).thenReturn(new StubResponseBuilder());
    }

    protected abstract Object initResource();

    private void verifyResourceMethodCalled(String method, Class<?> type, String paramStr, Object paramValue)
            throws NoSuchMethodException {
        parameters.put("param", List.of(paramStr));

        callInjectable(method, type);

        assertEquals(InjectableCallerTest.getMethodName(method, List.of(type)), lastCall.name());
        assertEquals(List.of(paramValue), lastCall.arguments());
    }

    protected abstract void callInjectable(String method, Class<?> type) throws NoSuchMethodException;

    @TestFactory
    public List<DynamicTest> inject_convertible_types() {
        List<DynamicTest> tests = new ArrayList<>();

        List<InjectableTypeTestCase> typeCases = List.of(
                new InjectableTypeTestCase(String.class, "string", "string"),
                new InjectableTypeTestCase(int.class, "1", 1),
                new InjectableTypeTestCase(double.class, "3.25", 3.25),
                new InjectableTypeTestCase(short.class, "128", (short) 128),
                new InjectableTypeTestCase(float.class, "3.25", 3.25f),
                new InjectableTypeTestCase(byte.class, "42", (byte) 42),
                new InjectableTypeTestCase(boolean.class, "true", true),
                new InjectableTypeTestCase(BigDecimal.class, "1234", new BigDecimal(1234)),
                new InjectableTypeTestCase(Converter.class, "Factory", Converter.Factory)
        );

        List<String> paramTypes = List.of("getPathParam", "getQueryParam");

        for (String type : paramTypes) {
            for (InjectableTypeTestCase testCase : typeCases) {
                tests.add(DynamicTest.dynamicTest("should inject " + testCase.type().getSimpleName() + " to " + type, () -> {
                    verifyResourceMethodCalled(type, testCase.type(), testCase.string(), testCase.value());
                }));
            }
        }
        return tests;
    }

    @TestFactory
    public List<DynamicTest> inject_context_object() {
        List<DynamicTest> tests = new ArrayList<>();

        List<InjectableTypeTestCase> typeTestCases = List.of(
                new InjectableTypeTestCase(someServiceInContext.class, "N/A", service),
                new InjectableTypeTestCase(ResourceContext.class, "N/A", context),
                new InjectableTypeTestCase(UriInfo.class, "N/A", uriInfo)
        );

        for (InjectableTypeTestCase testCase : typeTestCases) {
            tests.add(DynamicTest.dynamicTest("should inject " + testCase.type().getSimpleName() + " to getContext ", () -> {
                verifyResourceMethodCalled("getContext", testCase.type(), testCase.string(), testCase.value());
            }));
        }

        return tests;
    }

    record LastCall(String name, List<Object> arguments) {
    }

    record InjectableTypeTestCase(Class<?> type, String string, Object value) {
    }
}

