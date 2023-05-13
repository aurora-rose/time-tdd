package com.time.tdd.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * rootResource
 *
 * @author XuJian
 * @date 2023-04-23 20:15
 **/
public class RootResourceTest {
    private ResourceContext resourceContext;
    private Messages rootResource;

    @BeforeEach
    public void before() {
        rootResource = new Messages();
        resourceContext = mock(ResourceContext.class);
        when(resourceContext.getResource(eq(Messages.class))).thenReturn(rootResource);
    }


    @Test
    public void should_get_uri_template_from_path_annotation() {
        ResourceRouter.Resource resource = new ResourceHandler(Messages.class);

        UriTemplate template = resource.getUriTemplate();

        assertTrue(template.match("/messages/hello").isPresent());
    }


    @ParameterizedTest(name = "{3}")
    @CsvSource(textBlock = """
                GET,    /messages,              Messages.get,           Map to resource method
                GET,    /messages/1/content,    Message.content,        Map to sub-resource method
                GET,    /messages/1/body,       MessageBody.get,        Map to sub-sub-resource method
            """)
    public void should_match_resource_method_in_root_resource(String httpMethod, String path, String resourceMethod, String context) {
        UriInfoBuilder uriInfoBuilder = new StubUriInfoBuilder();
        ResourceRouter.Resource resource = new ResourceHandler(Messages.class);
        UriTemplate.MatchResult result = resource.getUriTemplate().match(path).get();
        ResourceRouter.ResourceMethod method =
                resource.match(result, httpMethod, new String[]{MediaType.TEXT_PLAIN}, resourceContext, uriInfoBuilder).get();

        assertEquals(resourceMethod, method.toString());
    }


    @Test
    public void should_match_resource_method_in_sub_resource() {
        ResourceRouter.Resource resource = new ResourceHandler(new Message(), null);
        UriTemplate.MatchResult result = mock(UriTemplate.MatchResult.class);
        when(result.getRemaining()).thenReturn("/content");

        assertTrue(
                resource.match(result, "GET", new String[]{MediaType.TEXT_PLAIN}, resourceContext, mock(UriInfoBuilder.class)).isPresent());
    }


    @ParameterizedTest(name = "{2}")
    @CsvSource(textBlock = """
            GET,    /messages/hello,            No matched resource method
            GET,    /messages/1/header,         No matched sub-resource method
            """)
    public void should_return_empty_if_not_matched(String httpMethod, String uri, String context) {
        UriInfoBuilder uriInfoBuilder = new StubUriInfoBuilder();
        uriInfoBuilder.addMatchedResource(new Messages());
        ResourceHandler resource = new ResourceHandler(Messages.class);
        UriTemplate.MatchResult result = resource.getUriTemplate().match(uri).get();

        assertTrue(resource.match(result, httpMethod, new String[]{MediaType.TEXT_PLAIN}, resourceContext, uriInfoBuilder).isEmpty());
    }


    @Test
    public void should_add_last_match_resource_to_uri_info_builder() {
        StubUriInfoBuilder uriInfoBuilder = new StubUriInfoBuilder();

        ResourceRouter.Resource resource = new ResourceHandler(Messages.class);
        UriTemplate.MatchResult result = resource.getUriTemplate().match("/messages").get();

        resource.match(result, "GET", new String[]{MediaType.TEXT_PLAIN}, resourceContext, uriInfoBuilder);

        assertTrue(uriInfoBuilder.getLastMatchedResource() instanceof Messages);
    }

    @Test
    public void should_add_last_match_path_parameters_uri_info_builder() {
        StubUriInfoBuilder uriInfoBuilder = new StubUriInfoBuilder();

        ResourceRouter.Resource resource = new ResourceHandler(Messages.class);
        UriTemplate.MatchResult result = resource.getUriTemplate().match("/messages/1").get();

        resource.match(result, "GET", new String[]{MediaType.TEXT_PLAIN}, resourceContext, uriInfoBuilder);

        assertTrue(uriInfoBuilder.getLastMatchedResource() instanceof Message);
        assertEquals(List.of("1"), uriInfoBuilder.getPathParameters().get("id"));
        
        UriInfo uriInfo = uriInfoBuilder.createUriInfo();
        assertEquals(List.of("1"), uriInfo.getPathParameters().get("id"));
    }


    @Test
    public void should_throw_illegal_argument_exception_if_root_resource_not_have_path_annotation() {
        assertThrows(IllegalArgumentException.class, () -> new ResourceHandler(Message.class));
    }


    @Path("/messages")
    static class Messages {
        @GET
        @Produces(MediaType.TEXT_PLAIN)
        public String get() {
            return "messages";
        }


        @Path("/{id:[0-9]+}")
        public Message getById() {
            return new Message();
        }
    }

    static class Message {
        @GET
        @Path("/content")
        @Produces(MediaType.TEXT_PLAIN)
        public String content() {
            return "content";
        }

        @Path("/body")
        public MessageBody body() {
            return new MessageBody();
        }
    }

    static class MessageBody {
        @GET
        @Produces(MediaType.TEXT_PLAIN)
        public String get() {
            return "body";
        }
    }

}

