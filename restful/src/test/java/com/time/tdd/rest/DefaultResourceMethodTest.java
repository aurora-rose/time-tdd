package com.time.tdd.rest;

import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * default resource method
 *
 * @author XuJian
 * @date 2023-05-09 21:24
 **/
public class DefaultResourceMethodTest {
    private CallableResourceMethods resource;
    private ResourceContext context;
    private UriInfoBuilder builder;
    private UriInfo uriInfo;
    private MultivaluedHashMap<String, String> parameters;
    private LastCall lastCall;

    private static String getMethodName(String name, List<? extends Class<?>> classList) {
        return name + "(" + classList.stream()
            .map(Class::getSimpleName).collect(Collectors.joining(",")) + ")";
    }

    @BeforeEach
    public void before() {
        lastCall = null;
        resource = (CallableResourceMethods) Proxy.newProxyInstance(this.getClass().getClassLoader(), new
            Class[] {CallableResourceMethods.class}, (proxy, method, args) -> {
            String name = method.getName();
            lastCall =
                new LastCall(getMethodName(name, Arrays.stream(method.getParameters()).map(Parameter::getType).toList()),
                    args != null ? List.of(args) : List.of());
            return "getList".equals(method.getName()) ? new ArrayList<>() : null;
        });


        context = mock(ResourceContext.class);
        builder = mock(UriInfoBuilder.class);
        uriInfo = mock(UriInfo.class);
        parameters = new MultivaluedHashMap<>();
        when(builder.getLastMatchedResource()).thenReturn(resource);
        when(uriInfo.getPathParameters()).thenReturn(parameters);
        when(uriInfo.getQueryParameters()).thenReturn(parameters);
        when(builder.createUriInfo()).thenReturn(uriInfo);
    }

    @Test
    public void should_call_resource_method() throws NoSuchMethodException {
        DefaultResourceMethod resourceMethod = getResourceMethod("get");
        resourceMethod.call(context, builder);

        assertEquals("get()", lastCall.name);
    }

    // TODO: 2023/5/9 return type ,List<String>
    @Test
    public void should_use_response_method_generic_return_type() throws NoSuchMethodException {
        DefaultResourceMethod resourceMethod = getResourceMethod("getList");
        assertEquals(new
                GenericEntity<>(List.of(), CallableResourceMethods.class.getMethod("getList").getGenericReturnType()),
            resourceMethod.call(context, builder));
    }

    @Test
    public void should_call_resource_method_with_void_return_type() throws NoSuchMethodException {
        DefaultResourceMethod resourceMethod = getResourceMethod("post");
        assertNull(resourceMethod.call(context, builder));
    }

    private void verifyResourceMethodCalled(String method, Class<?> type, String paramStr, Object paramValue)
        throws NoSuchMethodException {
        DefaultResourceMethod resourceMethod = getResourceMethod(method, type);
        parameters.put("param", List.of(paramStr));

        resourceMethod.call(context, builder);

        assertEquals(getMethodName(method, List.of(type)), lastCall.name);
        assertEquals(List.of(paramValue), lastCall.arguments);
    }


    @TestFactory
    public List<DynamicTest> injectableTypes() {
        List<DynamicTest> tests = new ArrayList<>();

        List<InjectableTypeTestCase> typeCases = List.of(
            new InjectableTypeTestCase(String.class, "string", "string"),
            new InjectableTypeTestCase(int.class, "1", 1),
            new InjectableTypeTestCase(double.class, "3.25", 3.25)
        );

        List<String> paramTypes = List.of("getPathParam", "getQueryParam");

        for (String type : paramTypes) {
            for (InjectableTypeTestCase testCase : typeCases) {
                tests.add(DynamicTest.dynamicTest("should inject " + testCase.type.getSimpleName() + " to " + type, () -> {
                    verifyResourceMethodCalled(type, testCase.type, testCase.string, testCase.value);
                }));
            }
        }
        return tests;
    }

    private DefaultResourceMethod getResourceMethod(String methodName, Class<?>... types) throws NoSuchMethodException {
        return new DefaultResourceMethod(CallableResourceMethods.class.getMethod(methodName, types));
    }


    interface CallableResourceMethods {

        @POST
        void post();

        @GET
        String get();

        @GET
        List<String> getList();

        @GET
        String getPathParam(@PathParam("param") String value);

        @GET
        String getPathParam(@PathParam("param") int value);

        @GET
        String getPathParam(@PathParam("param") double value);

        @GET
        String getQueryParam(@QueryParam("param") String value);

        @GET
        String getQueryParam(@QueryParam("param") int value);

        @GET
        String getQueryParam(@QueryParam("param") double value);
    }


    // TODO: 2023/5/11 using default converters for path, matrix, query,(uri) from ,header, cookie (request)
    // TODO: 2023/5/11 default converters for int, short, float, double, byte, char, String, and boolean
    // TODO: 2023/5/11 default converters for class with converter constructor
    // TODO: 2023/5/11 default converters for List, Set, SortSet
    // TODO: 2023/5/11 injection -get injection from resource context
    // TODO: 2023/5/11 injection - can inject resource context itself
    // TODO: 2023/5/11 injection -can inject uri info built from uri info builder

    record InjectableTypeTestCase(Class<?> type, String string, Object value) {
    }

    record LastCall(String name, List<Object> arguments) {
    }
}

