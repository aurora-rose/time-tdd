package com.time.tdd.rest;

/**
 * @author XuJian
 * @date 2023-04-19 22:04
 **/
interface UriInfoBuilder {

    Object getLastMatchedResource();

    void addMatchedResource(Object resource);
}

