package com.time.tdd.rest;

import com.time.tdd.di.container.Context;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.ext.Providers;

public interface Runtime {

    Providers getProviders();

    ResourceContext createResourceContext(HttpServletRequest request, HttpServletResponse response);

    Context getApplicationContext();

    ResourceRouter getResourceRouter();

    UriInfoBuilder createUriInfoBuilder(HttpServletRequest request);
}
