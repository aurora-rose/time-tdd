package com.time.tdd.rest;

import jakarta.ws.rs.core.UriInfo;

import java.util.Map;

/**
 * @author XuJian
 * @date 2023-04-19 22:04
 **/
interface UriInfoBuilder {

    Object getLastMatchedResource();

    void addMatchedResource(Object resource);

    void addMatchedPathParameters(Map<String, String> pathParameters);

    UriInfo createUriInfo();
}

