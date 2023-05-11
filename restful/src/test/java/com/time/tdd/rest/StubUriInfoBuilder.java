package com.time.tdd.rest;

import java.util.ArrayList;
import java.util.List;
import jakarta.ws.rs.core.UriInfo;

/**
 * @author XuJian
 * @date 2023-05-07 20:58
 **/
class StubUriInfoBuilder implements UriInfoBuilder {

    private List<Object> matchedResult = new ArrayList<>();

    public StubUriInfoBuilder() {
    }

    @Override
    public Object getLastMatchedResource() {
        return matchedResult.get(matchedResult.size() - 1);
    }

    @Override
    public void addMatchedResource(Object resource) {
        matchedResult.add(resource);
    }

    @Override
    public UriInfo createUriInfo() {
        return null;
    }
}

