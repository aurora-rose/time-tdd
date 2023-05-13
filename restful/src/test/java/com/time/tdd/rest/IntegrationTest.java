package com.time.tdd.rest;

import jakarta.servlet.Servlet;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Providers;
import jakarta.ws.rs.ext.RuntimeDelegate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Integration
 *
 * @author XuJian
 * @date 2023-05-13 12:02
 **/
public class IntegrationTest extends ServletTest {
    private Runtime runtime;
    private ResourceRouter router;
    private ResourceContext resourceContext;
    private Providers providers;
    private RuntimeDelegate delegate;
    private UriInfo uriInfo;

    @Override
    protected Servlet getServlet() {
        this.runtime = mock(Runtime.class);
        this.router = new DefaultResourceRouter(runtime, List.of(new ResourceHandler(UsersApi.class)));
        this.resourceContext = mock(ResourceContext.class);
        this.providers = mock(Providers.class);
        this.uriInfo = mock(UriInfo.class);

        when(runtime.getResourceRouter()).thenReturn(router);
        when(runtime.createUriInfoBuilder(any())).thenReturn(new StubUriInfoBuilder(uriInfo));
        when(runtime.createResourceContext(any(), any())).thenReturn(resourceContext);
        when(runtime.getProviders()).thenReturn(providers);
        when(resourceContext.getResource(eq(UsersApi.class))).thenReturn(new UsersApi());

        return new ResourceServlet(runtime);
    }

    // TODO: 2023/5/13 supporting utils runtime, uriInfo, response builder, exception mapper, message body writer
    // TODO: 2023/5/13 Resource Context (DI)

    @BeforeEach
    public void before() {
        delegate = mock(RuntimeDelegate.class);
        RuntimeDelegate.setInstance(delegate);
        when(delegate.createResponseBuilder()).thenReturn(new StubResponseBuilder());
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
        when(providers.getExceptionMapper(any())).thenReturn(exception -> {
            exception.printStackTrace();
            return new StubResponseBuilder().status(500).build();
        });
        when(providers.getMessageBodyWriter(eq(String.class), eq(String.class), any(), any())).thenReturn(new MessageBodyWriter<String>() {
            @Override
            public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
                return true;
            }

            @Override
            public void writeTo(String s, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
                PrintWriter writer = new PrintWriter(entityStream);
                writer.write(s);
                writer.flush();
            }
        });
    }

    @Test
    public void should_return_404_if_url_in_exist() {
        HttpResponse<String> response = get("/customers");
        assertEquals(404, response.statusCode());
    }

    @Test
    public void should_return_404_if_user_not_exist() {
        HttpResponse<String> response = get("/users/john");
        assertEquals(404, response.statusCode());
    }

    @Test
    public void should_return_to_string_of_user_if_user_exist() {
        HttpResponse<String> response = get("/users/john-smith");

        assertEquals(200, response.statusCode());
        assertEquals(new User("john-smith", new UserData("John Smith", "John.smith@email.com")).toString(),
                response.body());
    }

}

record UserData(String name, String email) {
}

class User {
    private String id;
    private UserData data;

    public User(String id, UserData data) {
        this.id = id;
        this.data = data;
    }

    public String getId() {
        return id;
    }

    public UserData getData() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}


@Path("/users")
class UsersApi {
    List<User> users;

    public UsersApi() {
        this.users = List.of(new User("john-smith", new UserData("John Smith", "John.smith@email.com")));
    }

    @Path("/{id}")
    public UserApi finUserById(@PathParam("id") String id) {
        return users.stream().filter(user -> id.equals(user.getId())).findFirst()
                .map(UserApi::new).orElseThrow(() -> new WebApplicationException(404));
    }
}


class UserApi {
    private User user;

    public UserApi(User user) {
        this.user = user;
    }

    @GET
    public String get() {
        return user.toString();
    }
}

