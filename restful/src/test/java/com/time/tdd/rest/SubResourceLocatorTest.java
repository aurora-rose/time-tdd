package com.time.tdd.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.RuntimeDelegate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

interface SubResourceMethods {
    @Path("/message/{param}")
    Message getPathParam(@PathParam("param") String value);


    @Path("/message/{param}")
    Message getPathParam(@PathParam("param") int value);

    @Path("/message/{param}")
    Message getPathParam(@PathParam("param") double value);

    @Path("/message/{param}")
    Message getPathParam(@PathParam("param") short value);

    @Path("/message/{param}")
    Message getPathParam(@PathParam("param") float value);

    @Path("/message/{param}")
    Message getPathParam(@PathParam("param") byte value);

    @Path("/message/{param}")
    Message getPathParam(@PathParam("param") boolean value);

    @Path("/message/{param}")
    Message getPathParam(@PathParam("param") BigDecimal value);

    @Path("/message/{param}")
    Message getPathParam(@PathParam("param") Converter value);


    @Path("/message")
    Message getQueryParam(@QueryParam("param") String value);

    @Path("/message")
    Message getQueryParam(@QueryParam("param") int value);

    @Path("/message")
    Message getQueryParam(@QueryParam("param") double value);

    @Path("/message")
    Message getQueryParam(@QueryParam("param") short value);

    @Path("/message")
    Message getQueryParam(@QueryParam("param") float value);

    @Path("/message")
    Message getQueryParam(@QueryParam("param") byte value);

    @Path("/message")
    Message getQueryParam(@QueryParam("param") boolean value);

    @Path("/message")
    Message getQueryParam(@QueryParam("param") BigDecimal value);

    @Path("/message")
    Message getQueryParam(@QueryParam("param") Converter value);


    @Path("/message")
    Message getContext(@Context someServiceInContext service);

    @Path("/message")
    Message getContext(@Context ResourceContext context);

    @Path("/message")
    Message getContext(@Context UriInfo uriInfo);

    @Path("/message/{param}")
    Message throwWebApplicationException(@PathParam("param") String path);
}

class Message {
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String content() {
        return "content";
    }
}

/**
 * SubResourceLocator
 *
 * @author XuJian
 * @date 2023-05-13 09:48
 **/
public class SubResourceLocatorTest extends InjectableCallerTest {

    private final Map<String, String> matchedPathParameters = Map.of("param", "param");
    private UriTemplate.MatchResult result;
    private RuntimeDelegate delegate;


    @BeforeEach
    public void before() {
        super.before();
        result = mock(UriTemplate.MatchResult.class);
        when(result.getMatchedPathParameters()).thenReturn(matchedPathParameters);

        delegate = mock(RuntimeDelegate.class);
        RuntimeDelegate.setInstance(delegate);
        when(delegate.createResponseBuilder()).thenReturn(new StubResponseBuilder());
    }

    @Override
    protected Object initResource() {
        return Proxy.newProxyInstance(this.getClass().getClassLoader(), new
                Class[]{SubResourceMethods.class}, (proxy, method, args) -> {
            String name = method.getName();
            lastCall = new LastCall(getMethodName(name, Arrays.stream(method.getParameters())
                    .map(Parameter::getType).toList()), args != null ? List.of(args) : List.of());
            if (method.getName().equals("throwWebApplicationException")) {
                throw new WebApplicationException(300);
            }
            return new Message();
        });
    }


    @Override
    protected void callInjectable(String method, Class<?> type) throws NoSuchMethodException {
        SubResourceLocators.SubResourceLocator locator = new SubResourceLocators.SubResourceLocator(SubResourceMethods.class.getMethod(method, type));
        locator.match(result, "GET", new String[0], context, builder);
    }

    @Test
    public void should_add_matched_path_parameter_to_builder() throws NoSuchMethodException {
        parameters.put("param", List.of("param"));
        callInjectable("getPathParam", String.class);

        verify(builder).addMatchedPathParameters(matchedPathParameters);
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
}

