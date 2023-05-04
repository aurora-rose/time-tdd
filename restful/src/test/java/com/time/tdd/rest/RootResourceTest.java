package com.time.tdd.rest;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * rootResource
 *
 * @author XuJian
 * @date 2023-04-23 20:15
 **/
public class RootResourceTest {


    // TODO: 2023/4/23 get uri template from path annotation
    @Test
    public void should_get_uri_template_from_path_annotation() {
        ResourceRouter.RootResource resource = new RootResourceClass(Messages.class);

        UriTemplate template = resource.getUriTemplate();

        assertTrue(template.match("/messages/hello").isPresent());
    }


    // TODO: 2023/4/23 find resource method,matches the http request and http method


    @ParameterizedTest(name = "{3}")
    @CsvSource(textBlock = """
            GET,    /messages/hello,        Messages.hello,         GET and URI match
            GET,    /messages/ah,           Messages.ah,            GET and URI match
            POST,   /messages/hello,        Messages.postHello,     POST and URI match
            PUT,    /messages/hello,        Messages.putHello,      PUT and URI match
            DELETE, /messages/hello,        Messages.deleteHello,   DELETE and URI match
            PATCH,  /messages/hello,        Messages.patchHello,    PATCH and URI match
            HEAD,   /messages/hello,        Messages.headHello,     HEAD and URI match
            OPTIONS,/messages/hello,        Messages.optionsHello,  OPTIONS and URI match
            GET,    /messages/topics/1234,  Messages.topic1234,     GET with multiply choices
            GET,    /messages,              Messages.get,           GET with resource method without Path
        """)
    public void should_match_resource_method_in_root_resource(String httpMethod, String path, String resourceMethod, String context) {
        ResourceRouter.RootResource resource = new RootResourceClass(Messages.class);
        UriTemplate.MatchResult result = resource.getUriTemplate().match(path).get();
        ResourceRouter.ResourceMethod method =
            resource.match(result, httpMethod, new String[] {MediaType.TEXT_PLAIN}, mock(UriInfoBuilder.class)).get();

        assertEquals(resourceMethod, method.toString());
    }


    @Test
    public void should_match_resource_method_in_sub_resource() {
        ResourceRouter.Resource resource = new SubResource(new Message());
        UriTemplate.MatchResult result = mock(UriTemplate.MatchResult.class);
        when(result.getRemaining()).thenReturn("/content");

        assertTrue(resource.match(result, "GET", new String[] {MediaType.TEXT_PLAIN}, mock(UriInfoBuilder.class)).isPresent());
    }


    // TODO: 2023/4/23 if sub resource locator matches uri,using it to follow up matching
    @ParameterizedTest(name = "{2}")
    @CsvSource(textBlock = """
        GET,    /missing-messages/1,            URI not matched
        POST,   /missing-messages,            HttpMethod not matched
        """)
    public void should_return_empty_if_not_matched(String httpMethod, String uri, String context) {
        RootResourceClass resource = new RootResourceClass(MissingMessages.class);
        UriTemplate.MatchResult result = resource.getUriTemplate().match(uri).get();

        assertTrue(resource.match(result, httpMethod, new String[] {MediaType.TEXT_PLAIN}, mock(UriInfoBuilder.class)).isEmpty());
    }


    // TODO: 2023/4/23 if no method / sub resource locator matches ,return 404
    // TODO: 2023/4/23 if resource class does not have a path annotation, throw illegal argument exception
    // TODO: 2023/5/4 Head and Options special case


    @Path("/missing-messages")
    static class MissingMessages {
        @GET
        @Produces(MediaType.TEXT_PLAIN)
        public String get() {
            return "messages";
        }
    }

    @Path("/messages")
    static class Messages {
        @GET
        @Produces(MediaType.TEXT_PLAIN)
        public String get() {
            return "messages";
        }


        @GET
        @Path("/ah")
        @Produces(MediaType.TEXT_PLAIN)
        public String ah() {
            return "ah";
        }

        @GET
        @Path("/hello")
        @Produces(MediaType.TEXT_PLAIN)
        public String hello() {
            return "hello";
        }

        @POST
        @Path("/hello")
        @Produces(MediaType.TEXT_PLAIN)
        public String postHello() {
            return "hello";
        }


        @PUT
        @Path("/hello")
        @Produces(MediaType.TEXT_PLAIN)
        public String putHello() {
            return "hello";
        }

        @DELETE
        @Path("/hello")
        @Produces(MediaType.TEXT_PLAIN)
        public String deleteHello() {
            return "hello";
        }

        @PATCH
        @Path("/hello")
        @Produces(MediaType.TEXT_PLAIN)
        public String patchHello() {
            return "hello";
        }

        @HEAD
        @Path("/hello")
        @Produces(MediaType.TEXT_PLAIN)
        public String headHello() {
            return "hello";
        }

        @OPTIONS
        @Path("/hello")
        @Produces(MediaType.TEXT_PLAIN)
        public String optionsHello() {
            return "hello";
        }

        @GET
        @Path("/topics/{id}")
        @Produces(MediaType.TEXT_PLAIN)
        public String topicId() {
            return "topicId";
        }


        @GET
        @Path("/topics/1234")
        @Produces(MediaType.TEXT_PLAIN)
        public String topic1234() {
            return "topic1234";
        }

        @Path("/{id}")
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
    }

}
