package com.time.tdd.di.container;

import java.util.Optional;

public interface Context {
    <Type> Optional<Type> get(Class<Type> type);
}
