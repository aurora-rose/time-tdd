package com.time.tdd.rest;

import java.lang.annotation.Annotation;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.Response;

/**
 * @author XuJian
 * @date 2023-04-14 22:58
 **/
abstract class OutboundResponse extends Response {
    abstract GenericEntity getGenericEntity();

    abstract Annotation[] getAnnotations();
}

