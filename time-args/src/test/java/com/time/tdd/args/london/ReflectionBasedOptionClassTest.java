package com.time.tdd.args.london;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author XuJian
 * @date 2023-02-22 22:50
 **/
public class ReflectionBasedOptionClassTest {


    @Test
    void should_treat_parameter_with_option_annotation_as_option() {
        ReflectionBasedOptionClass<IntOption> optionClass = new ReflectionBasedOptionClass(IntOption.class);

        Assertions.assertArrayEquals(new String[] {"p"}, optionClass.getOptionNames());
    }


    record IntOption(@LdOption("p") int port) {
    }

}

