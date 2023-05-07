package com.time.tdd.rest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.RuntimeDelegate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Vector;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * dispatcher
 *
 * @author XuJian
 * @date 2023-04-19 21:58
 **/
public class ResourceDispatcherTest {
    private RuntimeDelegate delegate;
    private Runtime runtime;
    private HttpServletRequest request;
    private ResourceContext context;
    private UriInfoBuilder builder;


    @BeforeEach
    public void before() {
        runtime = mock(Runtime.class);
        delegate = mock(RuntimeDelegate.class);
        RuntimeDelegate.setInstance(delegate);
        when(delegate.createResponseBuilder()).thenReturn(new StubResponseBuilder());

        request = mock(HttpServletRequest.class);
        context = mock(ResourceContext.class);
        when(request.getServletPath()).thenReturn("/users/1");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeaders(eq(HttpHeaders.ACCEPT))).thenReturn(new Vector<>(List.of(MediaType.WILDCARD)).elements());

        builder = mock(UriInfoBuilder.class);
        when(runtime.createUriInfoBuilder(same(request))).thenReturn(builder);
    }


    // TODO: 2023/4/19 根据与Path匹配的结果，降序排列RootResource，选择第一个的RootResource
    // TODO: 2023/4/19 R1，R2，R1 matched，R2 none R1
    // TODO: 2023/4/19 R1,R2,R1,R2,matched,R1 result < R2 result R1
    @Test
    void should_use_matched_root_resource() {
        GenericEntity entity = new GenericEntity("matched", String.class);

        ResourceRouter router = new DefaultResourceRouter(runtime, List.of(
                rootResource(matched("/users/1", result("/1")), returns(entity)),
                rootResource(unmatched("/users/1"))));

        OutboundResponse response = router.dispatch(request, context);
        assertSame(entity, response.getGenericEntity());
        assertEquals(200, response.getStatus());
    }


    @Test
    void should_sort_matched_root_resource_descending_order() {
        GenericEntity entity1 = new GenericEntity("1", String.class);
        GenericEntity entity2 = new GenericEntity("2", String.class);

        ResourceRouter router = new DefaultResourceRouter(runtime, List.of(
                rootResource(matched("/users/1", result("/1", 2)), returns(entity2)),
                rootResource(matched("/users/1", result("/1", 1)), returns(entity1))));

        OutboundResponse response = router.dispatch(request, context);
        assertSame(entity1, response.getGenericEntity());
        assertEquals(200, response.getStatus());
    }

    // TODO: 2023/4/19 如果没有匹配的RootResource，则构造404的Response
    @Test
    public void should_return_404_if_no_root_resource_matched() {
        ResourceRouter router = new DefaultResourceRouter(runtime, List.of(
                rootResource(unmatched("/users/1"))));

        OutboundResponse response = router.dispatch(request, context);
        assertNull(response.getGenericEntity());
        assertEquals(404, response.getStatus());


    }

    // TODO: 2023/4/19 如果返回的RootResource中无法匹配剩余的Path，则构造404的Response
    @Test
    public void should_return_404_if_no_resource_method_found() {
        ResourceRouter router = new DefaultResourceRouter(runtime, List.of(
                rootResource(matched("/users/1", result("/1", 2)))));

        OutboundResponse response = router.dispatch(request, context);
        assertNull(response.getGenericEntity());
        assertEquals(404, response.getStatus());
    }


    // TODO: 2023/4/19 如果ResourceMethod返回null，则构造204的Response

    @Test
    public void should_return_204_if_method_return_null() {
        ResourceRouter router = new DefaultResourceRouter(runtime, List.of(
                rootResource(matched("/users/1", result("/1", 2)), returns(null))));

        OutboundResponse response = router.dispatch(request, context);
        assertNull(response.getGenericEntity());
        assertEquals(204, response.getStatus());
    }


    private StubUriTemplate matched(String path, UriTemplate.MatchResult result) {
        UriTemplate matchedUriTemplate = mock(UriTemplate.class);
        when(matchedUriTemplate.match(eq(path))).thenReturn(Optional.of(result));
        return new StubUriTemplate(matchedUriTemplate, result);
    }

    private StubUriTemplate unmatched(String path) {
        UriTemplate unmatchedUriTemplate = mock(UriTemplate.class);
        when(unmatchedUriTemplate.match(eq(path))).thenReturn(Optional.empty());
        return new StubUriTemplate(unmatchedUriTemplate, null);
    }

    private UriTemplate.MatchResult result(String path) {
        return new FakeMatchResult(path, 0);
    }

    private UriTemplate.MatchResult result(String path, int order) {
        return new FakeMatchResult(path, order);
    }

    private ResourceRouter.Resource rootResource(StubUriTemplate stub) {
        ResourceRouter.Resource unmatched = mock(ResourceRouter.Resource.class);
        when(unmatched.getUriTemplate()).thenReturn(stub.uriTemplate);
        when(unmatched.match(same(stub.result), eq("GET"), eq(new String[]{MediaType.WILDCARD}), same(context), eq(builder))).thenReturn(
                Optional.empty());
        return unmatched;
    }


    private ResourceRouter.Resource rootResource(StubUriTemplate stub, ResourceRouter.ResourceMethod method) {
        ResourceRouter.Resource matched = mock(ResourceRouter.Resource.class);
        when(matched.getUriTemplate()).thenReturn(stub.uriTemplate);
        when(matched.match(same(stub.result), eq("GET"), eq(new String[]{MediaType.WILDCARD}), same(context), eq(builder))).thenReturn(
                Optional.of(method));
        return matched;
    }

    private ResourceRouter.ResourceMethod returns(GenericEntity entity) {
        ResourceRouter.ResourceMethod method = mock(ResourceRouter.ResourceMethod.class);
        when(method.call(same(context), same(builder))).thenReturn(entity);
        return method;
    }

    record StubUriTemplate(UriTemplate uriTemplate, UriTemplate.MatchResult result) {

    }

    class FakeMatchResult implements UriTemplate.MatchResult {
        private String remaning;
        private Integer order;

        public FakeMatchResult(String remaning, Integer order) {
            this.remaning = remaning;
            this.order = order;
        }


        @Override
        public String getMatched() {
            return null;
        }

        @Override
        public String getRemaining() {
            return remaning;
        }

        @Override
        public Map<String, String> getMatchedPathParameters() {
            return null;
        }

        @Override
        public int compareTo(UriTemplate.MatchResult o) {
            return order.compareTo(((FakeMatchResult) o).order);
        }
    }


}

