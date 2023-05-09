package com.time.tdd.rest;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.container.ResourceContext;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * head resource
 *
 * @author XuJian
 * @date 2023-05-09 20:10
 **/
public class HeadResourceMethodTest {


    @Test
    public void should_call_method_and_ignore_return_value() {
        ResourceRouter.ResourceMethod method = mock(ResourceRouter.ResourceMethod.class);
        ResourceContext context = mock(ResourceContext.class);
        UriInfoBuilder builder = mock(UriInfoBuilder.class);

        HeadResourceMethod headResourceMethod = new HeadResourceMethod(method);

        assertNull(headResourceMethod.call(context, builder));
        verify(method).call(context, builder);
    }


    @Test
    public void should_delegate_to_method_for_uri_template() {
        ResourceRouter.ResourceMethod method = mock(ResourceRouter.ResourceMethod.class);
        HeadResourceMethod headResourceMethod = new HeadResourceMethod(method);

        UriTemplate uriTemplate = mock(UriTemplate.class);
        when(method.getUriTemplate()).thenReturn(uriTemplate);

        assertEquals(uriTemplate, headResourceMethod.getUriTemplate());

    }


    @Test
    public void should_delegate_to_method_for_http_method() {
        ResourceRouter.ResourceMethod method = mock(ResourceRouter.ResourceMethod.class);
        HeadResourceMethod headResourceMethod = new HeadResourceMethod(method);

        when(method.getHttpMethod()).thenReturn("GET");
        assertEquals(HttpMethod.HEAD, headResourceMethod.getHttpMethod());

    }
}

