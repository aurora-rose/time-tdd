package com.time.tdd.rest;

/**
 * @author XuJian
 * @date 2023-04-19 22:04
 **/
interface UriInfoBuilder {
    void pushMatchedPath(String path);

    void addParameter(String name, String value);

    String getUnmatchedPath();
}

