package com.time.tdd.args.custom;

import com.time.tdd.args.exceptions.IllegalOptionException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * test
 *
 * @author XuJian
 * @date 2023-02-19 20:33
 **/
class CustomTest {


    // single option:


    @Test
    @Disabled
    void should_example_1() {
        Option option = CustomArgs.parse(Option.class, "-l", "-p", "8080", "-d", "/usr/logs");

        Assertions.assertTrue(option.logging());
        Assertions.assertEquals(8080, option.port());
        Assertions.assertEquals("/usr/logs", option.directory());
    }

    ;


    @Test
    @Disabled
    void should_example_2() {
        ListOption option = CustomArgs.parse(ListOption.class, "-g", "this", "is", "a", "list", "-d", "1", "2", "-3", "5");
        Assertions.assertEquals(new String[] {"this", "is", "a", "list"}, option.group());
        Assertions.assertEquals(new Integer[] {1, 2, -3, 4}, option.decimals());
    }


    @Test
    void should_parse_multi_options() {
        MultiOption option = CustomArgs.parse(MultiOption.class, "-l", "-p", "8080", "-d", "/usr/logs");

        Assertions.assertTrue(option.logging());
        Assertions.assertEquals(8080, option.port());
        Assertions.assertEquals("/usr/logs", option.directory());
    }

    @Test
    void should_throw_illegal_option_exception_if_annotation_not_present() {
        IllegalOptionException e = Assertions.assertThrows(IllegalOptionException.class, () -> {
            CustomArgs.parse(OptionWithoutAnnotation.class, "-l", "-p", "8080", "-d", "/usr/logs");
        });

        Assertions.assertEquals("port", e.getParameter());
    }

    record OptionWithoutAnnotation(@CustomOption("l") boolean logging, int port, @CustomOption("d") String directory) {
    }

    record MultiOption(@CustomOption("l") boolean logging, @CustomOption("p") int port, @CustomOption("d") String directory) {
    }

    // sad path:
    // -bool   -l / -l t
    // -int    -p / -p 8080 8081
    // -string -d / -d /usr/logs /usr/vars
    // default value:
    // -bool false
    // -int 0
    // -string ""

    record Option(@CustomOption("l") boolean logging, @CustomOption("p") int port, @CustomOption("d") String directory) {
    }


    record ListOption(@CustomOption("g") String[] group, @CustomOption("d") Integer[] decimals) {
    }


}

