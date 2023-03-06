package com.time.tdd.di.container;

import java.util.Optional;

/**
 * @author mickey
 */
public interface Context {

    <ComponentType> Optional<ComponentType> get(ComponentRef<ComponentType> ref);

}
