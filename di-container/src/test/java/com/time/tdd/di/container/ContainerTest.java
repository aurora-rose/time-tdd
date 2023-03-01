package com.time.tdd.di.container;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;

interface Component {
}

interface Dependency {
}

interface AnotherDependency {
}

/**
 * test
 *
 * @author XuJian
 * @date 2023-02-24 21:38
 **/
public class ContainerTest {

    ContextConfig config;

    @BeforeEach
    public void setup() {
        config = new ContextConfig();
    }


    @Nested
    public class DependenciesSelection {

        @Nested
        class ProviderType {


        }

        @Nested
        class Qualifier {

        }

    }

    @Nested
    public class LifecycleManagement {

    }

}












