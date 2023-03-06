package com.time.tdd.di.container;

import java.lang.annotation.Annotation;

/**
 * @author XuJian
 * @date 2023-03-04 16:48
 **/
public record Component(Class<?> type, Annotation qualifier) {
}

