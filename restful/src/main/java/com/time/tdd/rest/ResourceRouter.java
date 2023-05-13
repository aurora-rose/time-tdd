package com.time.tdd.rest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.stream;

/**
 * @author XuJian
 * @date 2023-04-14 22:58
 **/
interface ResourceRouter {
    OutboundResponse dispatch(HttpServletRequest request, ResourceContext resourceContext);

    /**
     * @author XuJian
     * @date 2023-04-19 22:14
     **/
    interface Resource extends UriHandler {
        Optional<ResourceMethod> match(UriTemplate.MatchResult result, String httpMethod, String[] mediaTypes,
                                       ResourceContext resourceContext, UriInfoBuilder builder);
    }

    /**
     * @author XuJian
     * @date 2023-04-19 22:14
     **/
    interface ResourceMethod extends UriHandler {
        String getHttpMethod();

        GenericEntity<?> call(ResourceContext resourceContext, UriInfoBuilder builder);
    }

}


class DefaultResourceRouter implements ResourceRouter {

    private Runtime runtime;
    private List<Resource> resources;

    public DefaultResourceRouter(Runtime runtime, List<Resource> resources) {
        this.runtime = runtime;
        this.resources = resources;
    }

    private static Optional<ResourceMethod> findResourceMethod(HttpServletRequest request, ResourceContext resourceContext,
                                                               UriInfoBuilder uri, Optional<UriTemplate.MatchResult> matched,
                                                               Resource handler) {
        return handler.match(matched.get(),
                request.getMethod(), Collections.list(request.getHeaders(HttpHeaders.ACCEPT)).toArray(String[]::new), resourceContext, uri);
    }

    @Override
    public OutboundResponse dispatch(HttpServletRequest request, ResourceContext resourceContext) {
        String path = request.getServletPath();
        UriInfoBuilder uri = runtime.createUriInfoBuilder(request);

        Optional<ResourceMethod> method = UriHandlers.mapMatched(path, resources,
                (result, resource) -> findResourceMethod(request, resourceContext, uri, result, resource));

        if (method.isEmpty()) {
            return (OutboundResponse) Response.status(Response.Status.NOT_FOUND).build();
        }

        return (OutboundResponse) method.map(m -> m.call(resourceContext, uri))
                .map(entity -> entity.getEntity() instanceof OutboundResponse
                        ? (OutboundResponse) entity.getEntity()
                        : Response.ok(entity).build())
                .orElseGet(() -> Response.noContent().build());
    }

}


class DefaultResourceMethod implements ResourceRouter.ResourceMethod {

    private final Method method;
    private final PathTemplate uriTemplate;
    private final String httpMethod;


    public DefaultResourceMethod(Method method) {
        this.method = method;
        this.uriTemplate = new PathTemplate(Optional.ofNullable(method.getAnnotation(Path.class)).map(Path::value).orElse(""));
        this.httpMethod = stream(method.getAnnotations()).filter(m -> m.annotationType().isAnnotationPresent(HttpMethod.class))
                .findFirst().get().annotationType().getAnnotation(HttpMethod.class).value();
    }

    @Override
    public String getHttpMethod() {
        return httpMethod;
    }

    @Override
    public PathTemplate getUriTemplate() {
        return uriTemplate;
    }

    @Override
    public GenericEntity<?> call(ResourceContext resourceContext, UriInfoBuilder builder) {
        Object result = MethodInvoker.invoke(method, resourceContext, builder);
        return result != null ? new GenericEntity<>(result, method.getGenericReturnType()) : null;
    }

    @Override
    public String toString() {
        return method.getDeclaringClass().getSimpleName() + "." + method.getName();
    }


}


class ResourceMethods {
    private Map<String, List<ResourceRouter.ResourceMethod>> resourceMethods;

    public ResourceMethods(Method[] methods) {
        this.resourceMethods = getResourceMethods(methods);
    }

    private static Map<String, List<ResourceRouter.ResourceMethod>> getResourceMethods(Method[] methods) {
        return stream(methods)
                .filter(m -> stream(m.getAnnotations()).anyMatch(a -> a.annotationType().isAnnotationPresent(HttpMethod.class)))
                .map(DefaultResourceMethod::new)
                .collect(Collectors.groupingBy(ResourceRouter.ResourceMethod::getHttpMethod));
    }

    private Optional<ResourceRouter.ResourceMethod> findMethod(String path, String method) {
        return Optional.ofNullable(resourceMethods.get(method))
                .flatMap(methods -> UriHandlers.match(path, methods, r -> r.getRemaining() == null));
    }

    public Optional<ResourceRouter.ResourceMethod> findResourceMethods(String path, String method) {
        return findMethod(path, method).or(() -> findAlternative(path, method));
    }

    private Optional<ResourceRouter.ResourceMethod> findAlternative(String path, String method) {
        if (HttpMethod.HEAD.equals(method)) {
            return findMethod(path, HttpMethod.GET).map(HeadResourceMethod::new);
        }
        if (HttpMethod.OPTIONS.equals(method)) {
            return Optional.of(new OptionResourceMethod(path));
        }
        return Optional.empty();
    }

    class OptionResourceMethod implements ResourceRouter.ResourceMethod {

        private String path;

        public OptionResourceMethod(String path) {
            this.path = path;
        }

        @Override
        public String getHttpMethod() {
            return null;
        }

        @Override
        public GenericEntity<?> call(ResourceContext resourceContext, UriInfoBuilder builder) {
            return new GenericEntity<>(Response.noContent().allow(findAllowedMethods()).build(), Response.class);
        }

        private Set<String> findAllowedMethods() {
            Set<String> allowed = Stream.of(HttpMethod.GET, HttpMethod.HEAD, HttpMethod.OPTIONS, HttpMethod.POST,
                            HttpMethod.DELETE, HttpMethod.PUT, HttpMethod.PATCH)
                    .filter(method -> findMethod(path, method).isPresent())
                    .collect(Collectors.toSet());

            allowed.add(HttpMethod.OPTIONS);
            if (allowed.contains(HttpMethod.GET)) {
                allowed.add(HttpMethod.HEAD);
            }
            return allowed;
        }

        @Override
        public UriTemplate getUriTemplate() {
            return null;
        }
    }


}

class HeadResourceMethod implements ResourceRouter.ResourceMethod {
    private ResourceRouter.ResourceMethod method;

    public HeadResourceMethod(ResourceRouter.ResourceMethod method) {
        this.method = method;
    }

    @Override
    public String getHttpMethod() {
        return HttpMethod.HEAD;
    }

    @Override
    public GenericEntity<?> call(ResourceContext resourceContext, UriInfoBuilder builder) {
        method.call(resourceContext, builder);
        return null;
    }

    @Override
    public UriTemplate getUriTemplate() {
        return method.getUriTemplate();
    }

}


class SubResourceLocators {

    private final List<ResourceRouter.Resource> subResourceLocators;

    public SubResourceLocators(Method[] methods) {
        this.subResourceLocators = stream(methods).filter(m -> m.isAnnotationPresent(Path.class)
                        && stream(m.getAnnotations()).noneMatch(a -> a.annotationType().isAnnotationPresent(HttpMethod.class)))
                .map((Function<Method, ResourceRouter.Resource>) SubResourceLocator::new).toList();
    }

    public Optional<ResourceRouter.ResourceMethod> findSubResourceMethods(String path, String method, String[] mediaTypes,
                                                                          ResourceContext resourceContext, UriInfoBuilder uriInfoBuilder) {
        return UriHandlers.mapMatched(path, subResourceLocators, (result, locator) ->
                locator.match(result.get(), method, mediaTypes, resourceContext, uriInfoBuilder));
    }


    static class SubResourceLocator implements ResourceRouter.Resource {
        private PathTemplate uriTemplate;
        private Method method;

        public SubResourceLocator(Method method) {
            this.method = method;
            this.uriTemplate = new PathTemplate(method.getAnnotation(Path.class).value());
        }

        private static UriTemplate.MatchResult excludePathParameters(UriTemplate.MatchResult result) {
            return new UriTemplate.MatchResult() {
                @Override
                public String getMatched() {
                    return result.getMatched();
                }

                @Override
                public String getRemaining() {
                    return result.getRemaining();
                }

                @Override
                public Map<String, String> getMatchedPathParameters() {
                    return new HashMap<>();
                }

                @Override
                public int compareTo(UriTemplate.MatchResult o) {
                    return result.compareTo(o);
                }
            };
        }

        @Override
        public UriTemplate getUriTemplate() {
            return uriTemplate;
        }

        @Override
        public String toString() {
            return method.getDeclaringClass().getSimpleName() + "." + method.getName();
        }

        @Override
        public Optional<ResourceRouter.ResourceMethod> match(UriTemplate.MatchResult result, String httpMethod, String[] mediaTypes,
                                                             ResourceContext resourceContext, UriInfoBuilder builder) {
            try {
                builder.addMatchedPathParameters(result.getMatchedPathParameters());
                Object subResource = MethodInvoker.invoke(method, resourceContext, builder);
                return new ResourceHandler(subResource, uriTemplate).match(excludePathParameters(result), httpMethod, mediaTypes, resourceContext, builder);
            } catch (WebApplicationException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }
}

class ResourceHandler implements ResourceRouter.Resource {

    private UriTemplate uriTemplate;
    private ResourceMethods resourceMethods;
    private SubResourceLocators subResourceLocators;
    private Function<ResourceContext, Object> resource;


    public ResourceHandler(Class<?> resourceClass) {
        this(resourceClass, new PathTemplate(getTemplate(resourceClass)), rc -> rc.getResource(resourceClass));
    }

    public ResourceHandler(Object resource, UriTemplate uriTemplate) {
        this(resource.getClass(), uriTemplate, rc -> resource);
    }

    private ResourceHandler(Class<?> resourceClass, UriTemplate uriTemplate, Function<ResourceContext, Object> resource) {
        this.uriTemplate = uriTemplate;
        this.resourceMethods = new ResourceMethods(resourceClass.getMethods());
        this.subResourceLocators = new SubResourceLocators(resourceClass.getMethods());
        this.resource = resource;
    }

    private static String getTemplate(Class<?> resourceClass) {
        if (!resourceClass.isAnnotationPresent(Path.class)) {
            throw new IllegalArgumentException();
        }
        return resourceClass.getAnnotation(Path.class).value();
    }

    @Override
    public Optional<ResourceRouter.ResourceMethod> match(UriTemplate.MatchResult result, String httpMethod, String[] mediaTypes,
                                                         ResourceContext resourceContext, UriInfoBuilder builder) {
        builder.addMatchedResource(resource.apply(resourceContext));
        builder.addMatchedPathParameters(result.getMatchedPathParameters());
        String remaining = Optional.ofNullable(result.getRemaining()).orElse("");
        return resourceMethods.findResourceMethods(remaining, httpMethod)
                .or(() -> subResourceLocators.findSubResourceMethods(remaining, httpMethod, mediaTypes, resourceContext, builder));
    }


    @Override
    public UriTemplate getUriTemplate() {
        return uriTemplate;
    }

}



