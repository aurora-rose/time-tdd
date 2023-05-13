package com.time.tdd.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

enum Converter {
    Primitive, Constructor, Factory
}

interface someServiceInContext {
}

/**
 * default resource method
 *
 * @author XuJian
 * @date 2023-05-09 21:24
 **/
public class DefaultResourceMethodTest extends InjectableCallerTest {

    @Override
    protected Object initResource() {
        return Proxy.newProxyInstance(this.getClass().getClassLoader(), new
                Class[]{CallableResourceMethods.class}, (proxy, method, args) -> {
            String name = method.getName();
            lastCall =
                    new LastCall(getMethodName(name, Arrays.stream(method.getParameters()).map(Parameter::getType).toList()),
                            args != null ? List.of(args) : List.of());
            if (method.getName().equals("throwWebApplicationException")) {
                throw new WebApplicationException(300);
            }
            return "getList".equals(method.getName()) ? new ArrayList<>() : null;
        });
    }

    @Test
    public void should_call_resource_method() throws NoSuchMethodException {
        DefaultResourceMethod resourceMethod = getResourceMethod("get");
        resourceMethod.call(context, builder);

        assertEquals("get()", lastCall.name());
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

    @Test
    public void should_not_wrap_around_web_application_exception() {
        parameters.put("param", List.of("param"));
        try {
            callInjectable("throwWebApplicationException", String.class);
        } catch (WebApplicationException e) {
            assertEquals(300, e.getResponse().getStatus());
        } catch (Exception e) {
            fail();
        }
    }

    @Override
    protected void callInjectable(String method, Class<?> type) throws NoSuchMethodException {
        DefaultResourceMethod resourceMethod = getResourceMethod(method, type);
        resourceMethod.call(context, builder);
    }


    private DefaultResourceMethod getResourceMethod(String methodName, Class<?>... types) throws NoSuchMethodException {
        return new DefaultResourceMethod(CallableResourceMethods.class.getMethod(methodName, types));
    }


    // TODO: 2023/5/11 using default converters for path, matrix, query,(uri) from ,header, cookie (request)
    // TODO: 2023/5/11 default converters for List, Set, SortSet

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
        String getPathParam(@PathParam("param") short value);

        @GET
        String getPathParam(@PathParam("param") float value);

        @GET
        String getPathParam(@PathParam("param") byte value);

        @GET
        String getPathParam(@PathParam("param") boolean value);

        @GET
        String getPathParam(@PathParam("param") BigDecimal value);

        @GET
        String getPathParam(@PathParam("param") Converter value);


        @GET
        String getQueryParam(@QueryParam("param") String value);

        @GET
        String getQueryParam(@QueryParam("param") int value);

        @GET
        String getQueryParam(@QueryParam("param") double value);

        @GET
        String getQueryParam(@QueryParam("param") short value);

        @GET
        String getQueryParam(@QueryParam("param") float value);

        @GET
        String getQueryParam(@QueryParam("param") byte value);

        @GET
        String getQueryParam(@QueryParam("param") boolean value);

        @GET
        String getQueryParam(@QueryParam("param") BigDecimal value);

        @GET
        String getQueryParam(@QueryParam("param") Converter value);


        @GET
        String getContext(@Context someServiceInContext service);

        @GET
        String getContext(@Context ResourceContext context);

        @GET
        String getContext(@Context UriInfo uriInfo);

        @GET
        String throwWebApplicationException(@PathParam("param") String path);
    }

}

