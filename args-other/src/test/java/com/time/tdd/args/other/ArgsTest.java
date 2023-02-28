package com.time.tdd.args.other;

import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * test
 *
 * @author XuJian
 * @date 2023-02-21 23:02
 **/
class ArgsTest {

    private static boolean parseBool(String[] values) {
        checkSize(values, 0);
        return values != null;
    }


    private static int parseInt(String[] values) {
        checkSize(values, 1);
        return Integer.parseInt(values[0]);
    }


    private static void checkSize(String[] values, int size) {
        if (values != null && values.length != size) {
            throw new RuntimeException();
        }
    }

    @Test
    void should_parse_bool_option() {

        Args<BoolOption> args = new Args<>(BoolOption.class, Map.of(boolean.class, ArgsTest::parseBool));

        BoolOption option = args.parse("-l");
        Assertions.assertTrue(option.logging());

    }

    @Test
    void should_parse_int_option() {
        Args<IntOption> args = new Args<>(IntOption.class, Map.of(int.class, ArgsTest::parseInt));

        IntOption option = args.parse("-p", "8080");

        Assertions.assertEquals(8080, option.port());
    }

    record BoolOption(@Option("l") boolean logging) {
    }

    record IntOption(@Option("p") int port) {
    }

}

