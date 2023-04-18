package com.time.tdd.rest;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import jakarta.servlet.Servlet;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Providers;
import jakarta.ws.rs.ext.RuntimeDelegate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author XuJian
 * @date 2023-04-14 23:19
 **/
public class ResourceServletTest extends ServletTest {
    private Runtime runtime;
    private ResourceRouter router;
    private ResourceContext resourceContext;
    private Providers providers;
    private OutboundResponseBuilder response;
    private RuntimeDelegate delegate;

    @Override
    protected Servlet getServlet() {
        this.runtime = mock(Runtime.class);
        this.router = mock(ResourceRouter.class);
        this.resourceContext = mock(ResourceContext.class);
        this.providers = mock(Providers.class);

        when(runtime.getResourceRouter()).thenReturn(router);
        when(runtime.createResourceContext(any(), any())).thenReturn(resourceContext);
        when(runtime.getProviders()).thenReturn(providers);

        return new ResourceServlet(runtime);
    }

    @BeforeEach
    public void before() {
        response = response();
        delegate = mock(RuntimeDelegate.class);
        RuntimeDelegate.setInstance(delegate);
        when(delegate.createHeaderDelegate(eq(NewCookie.class))).thenReturn(new RuntimeDelegate.HeaderDelegate<>() {
            @Override
            public NewCookie fromString(String value) {
                return null;
            }

            @Override
            public String toString(NewCookie value) {
                return value.getName() + "=" + value.getValue();
            }
        });
    }

    private OutboundResponseBuilder response() {
        return new OutboundResponseBuilder();
    }


    // TODO: 2023/4/14 500 if MessageBodyWriter not found
    // TODO: 2023/4/15 500 if header delegate
    // TODO: 2023/4/15 500 if exception mapper
    @TestFactory
    public List<DynamicTest> RespondWhenExtensionMissing() {
        List<DynamicTest> tests = new ArrayList<>();
        Map<String, org.junit.jupiter.api.function.Executable> extensions = Map.of(
            "MessageBodyWriter", () -> response().entity(new GenericEntity<>(1, Integer.class), new Annotation[0]).returnFrom(router),
            "HeaderDelegate", () -> response().headers(HttpHeaders.DATE, new Date()).returnFrom(router),
            "ExceptionMapper", () -> when(router.dispatch(any(), eq(resourceContext))).thenThrow(IllegalArgumentException.class)
        );

        for (String name : extensions.keySet()) {
            tests.add(DynamicTest.dynamicTest(name + " not found ", () -> {
                extensions.get(name).execute();
                when(providers.getExceptionMapper(eq(NullPointerException.class))).thenReturn(
                    e -> response().status(Response.Status.INTERNAL_SERVER_ERROR).build());
                HttpResponse<String> httpResponse = get("/test");
                assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), httpResponse.statusCode());
            }));
        }

        return tests;
    }

    // TODO: 2023/4/15 providers gets exception mapper
    @ExceptionThrownFrom
    private void providers_getExceptionMapper(RuntimeException exception) {
        when(router.dispatch(any(), eq(resourceContext))).thenThrow(RuntimeException.class);
        when(providers.getExceptionMapper(eq(RuntimeException.class))).thenThrow(exception);
    }

    // TODO: 2023/4/15 runtime delegate
    @ExceptionThrownFrom
    private void runtimeDelegate_createHeaderDelegate(RuntimeException exception) {
        response().headers(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_TYPE).returnFrom(router);

        when(delegate.createHeaderDelegate(eq(MediaType.class))).thenThrow(exception);
    }

    // TODO: 2023/4/15 header delegate
    @ExceptionThrownFrom
    private void headerDelegate_toString(RuntimeException exception) {
        response().headers(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_TYPE).returnFrom(router);
        when(delegate.createHeaderDelegate(eq(MediaType.class))).thenReturn(new RuntimeDelegate.HeaderDelegate<>() {
            @Override
            public MediaType fromString(String value) {
                return null;
            }

            @Override
            public String toString(MediaType value) {
                throw exception;
            }
        });
    }

    // TODO: 2023/4/15 providers gets message body writer
    @ExceptionThrownFrom
    private void providers_getMessageBodyWriter(RuntimeException exception) {
        response().entity(new GenericEntity<>(2.5, Double.class), new Annotation[0]).returnFrom(router);

        when(providers.getMessageBodyWriter(eq(Double.class), eq(Double.class), eq(new Annotation[0]),
            eq(MediaType.TEXT_PLAIN_TYPE))).thenThrow(exception);
    }

    // TODO: 2023/4/15 message body writer write
    @ExceptionThrownFrom
    private void messageBodyWriter_writeTo(RuntimeException exception) {
        response().entity(new GenericEntity<>(2.5, Double.class), new Annotation[0]).returnFrom(router);

        when(providers.getMessageBodyWriter(eq(Double.class), eq(Double.class), eq(new Annotation[0]),
            eq(MediaType.TEXT_PLAIN_TYPE))).thenReturn(
            new MessageBodyWriter<>() {
                @Override
                public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
                    return false;
                }

                @Override
                public void writeTo(Double aDouble, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                                    MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws WebApplicationException {
                    throw exception;
                }
            });
    }

    @TestFactory
    public List<DynamicTest> RespondForException() {
        List<DynamicTest> tests = new ArrayList<>();

        Map<String, Consumer<Consumer<RuntimeException>>> exceptions = Map.of("Other Exception", this::otherExceptionsThrownFrom,
            "WebApplicationException", this::webApplicationExceptionThrownFrom);

        Map<String, Consumer<RuntimeException>> callers = getCallers();

        for (Map.Entry<String, Consumer<RuntimeException>> caller : callers.entrySet()) {
            for (Map.Entry<String, Consumer<Consumer<RuntimeException>>> exceptionThrownFrom : exceptions.entrySet()) {
                tests.add(DynamicTest.dynamicTest(caller.getKey() + " throws " + exceptionThrownFrom.getKey(),
                    () -> exceptionThrownFrom.getValue().accept(caller.getValue())));
            }
        }

        return tests;
    }

    private Map<String, Consumer<RuntimeException>> getCallers() {
        Map<String, Consumer<RuntimeException>> callers = new HashMap<>();

        for (Method method : Arrays.stream(this.getClass().getDeclaredMethods())
            .filter(m -> m.isAnnotationPresent(ExceptionThrownFrom.class)).toList()) {
            String name = method.getName();
            String callerName = name.substring(0, 1).toUpperCase() + name.substring(1).replace("-", ".");

            callers.put(callerName, e -> {
                try {
                    method.invoke(this, e);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            });
        }
        return callers;
    }

    private void webApplicationExceptionThrownFrom(Consumer<RuntimeException> caller) {
        RuntimeException exception = new WebApplicationException(response().status(Response.Status.FORBIDDEN).build());
        caller.accept(exception);
        HttpResponse<String> httpResponse = get("/test");
        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), httpResponse.statusCode());
    }

    private void otherExceptionsThrownFrom(Consumer<RuntimeException> caller) {
        RuntimeException exception = new IllegalArgumentException();
        caller.accept(exception);

        when(providers.getExceptionMapper(eq(IllegalArgumentException.class))).thenReturn(
            e -> response().status(Response.Status.FORBIDDEN).build());

        HttpResponse<String> httpResponse = get("/test");
        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), httpResponse.statusCode());
    }

    // TODO: 2023/4/14 throw WebApplicationException with response, use response
    // TODO: 2023/4/14 throw other exception, use ExceptionMapper build response
    @ExceptionThrownFrom
    private void resourceRouter_dispatch(RuntimeException exception) {
        when(router.dispatch(any(), eq(resourceContext))).thenThrow(exception);
    }

    // TODO: 2023/4/15 exception mapper
    @ExceptionThrownFrom
    private void exceptionMapper_toResponse(RuntimeException exception) {
        when(router.dispatch(any(), eq(resourceContext))).thenThrow(RuntimeException.class);

        when(providers.getExceptionMapper(eq(RuntimeException.class))).thenReturn(
            e -> {
                throw exception;
            });
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface ExceptionThrownFrom {

    }

    @Nested
    class RespondForOutboundResponse {
        // TODO: 2023/4/14 use status code as http status

        @Test
        public void should_use_status_from_response() {
            response.status(Response.Status.NOT_MODIFIED).returnFrom(router);

            HttpResponse<String> httpResponse = get("/test");
            assertEquals(Response.Status.NOT_MODIFIED.getStatusCode(), httpResponse.statusCode());
        }

        // TODO: 2023/4/14 use headers as http headers
        @Test
        void should_use_http_headers_from_response() {
            response
                .headers("Set-Cookie", new NewCookie.Builder("SESSION_ID").value("session").build(),
                    new NewCookie.Builder("USER_ID").value("user").build())
                .returnFrom(router);

            HttpResponse<String> httpResponse = get("/test");

            assertArrayEquals(new String[] {"SESSION_ID=session", "USER_ID=user"},
                httpResponse.headers().allValues("Set-Cookie").toArray());
        }

        // TODO: 2023/4/14 writer body using MessageBodyWriter
        @Test
        void should_writer_entity_to_http_response_using_message_body_writer() {
            response
                .entity(new GenericEntity<>("entity", String.class), new Annotation[0])
                .returnFrom(router);

            HttpResponse<String> httpResponse = get("/test");

            Assertions.assertEquals("entity", httpResponse.body());
        }

        // TODO: 2023/4/15 entity is null , ignored WriterBodMessage
        @Test
        void should_not_call_message_body_writer_if_entity_is_null() {
            response.entity(null, new Annotation[0]).returnFrom(router);

            HttpResponse<String> httpResponse = get("/test");

            assertEquals(Response.Status.OK.getStatusCode(), httpResponse.statusCode());
            assertEquals("", httpResponse.body());
        }
    }

    class OutboundResponseBuilder {
        Response.Status status = Response.Status.OK;
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        GenericEntity<Object> entity = new GenericEntity<>("entity", String.class);
        Annotation[] annotations = new Annotation[0];
        MediaType mediaType = MediaType.TEXT_PLAIN_TYPE;

        public OutboundResponseBuilder headers(String name, Object... values) {
            this.headers.addAll(name, values);
            return this;
        }

        public OutboundResponseBuilder status(Response.Status status) {
            this.status = status;
            return this;
        }

        public OutboundResponseBuilder entity(GenericEntity<Object> entity, Annotation[] annotations) {
            this.entity = entity;
            this.annotations = annotations;
            return this;
        }


        void returnFrom(ResourceRouter router) {
            build(response -> when(router.dispatch(any(), eq(resourceContext))).thenReturn(response));
        }

        void throwFrom(ResourceRouter router) {
            build(response -> {
                WebApplicationException exception = new WebApplicationException(response);
                when(router.dispatch(any(), eq(resourceContext))).thenThrow(exception);
            });
        }


        void build(Consumer<OutboundResponse> consumer) {
            OutboundResponse response = build();

            consumer.accept(response);
        }

        public OutboundResponse build() {
            OutboundResponse response = mock(OutboundResponse.class);
            when(response.getStatus()).thenReturn(status.getStatusCode());
            when(response.getStatusInfo()).thenReturn(status);
            when(response.getHeaders()).thenReturn(headers);
            when(response.getGenericEntity()).thenReturn(entity);
            when(response.getAnnotations()).thenReturn(annotations);
            when(response.getMediaType()).thenReturn(mediaType);
            stubMessageBodyWriter();
            return response;
        }

        private void stubMessageBodyWriter() {
            when(providers.getMessageBodyWriter(eq(String.class), eq(String.class), same(annotations), eq(mediaType)))
                .thenReturn(new MessageBodyWriter<>() {
                    @Override
                    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
                        return false;
                    }

                    @Override
                    public void writeTo(String s, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                                        MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
                        throws WebApplicationException {

                        PrintWriter writer = new PrintWriter(entityStream);
                        writer.write(s);
                        writer.flush();
                    }
                });
        }

    }

}

