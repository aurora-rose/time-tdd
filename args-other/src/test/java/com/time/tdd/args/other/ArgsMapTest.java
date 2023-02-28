package com.time.tdd.args.other;

import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * map test
 *
 * @author XuJian
 * @date 2023-02-21 22:28
 **/
class ArgsMapTest {
    @Test
    void should_split_args_to_map() {
        Map<String, String[]> args = Args.toMap("-b", "-p", "8080", "-d", "/usr/logs");

        Assertions.assertEquals(3, args.size());
        Assertions.assertArrayEquals(new String[] {}, args.get("b"));
        Assertions.assertArrayEquals(new String[] {"8080"}, args.get("p"));
        Assertions.assertArrayEquals(new String[] {"/usr/logs"}, args.get("d"));
    }


    @Test
    void should_split_args_list_to_map() {
        Map<String, String[]> args = Args.toMap("-g", "this", "is", "a", "list", "-d", "1", "2", "-3", "5");

        Assertions.assertEquals(2, args.size());
        Assertions.assertArrayEquals(new String[] {"this", "is", "a", "list"}, args.get("g"));
        Assertions.assertArrayEquals(new String[] {"1", "2", "-3", "5"}, args.get("d"));
    }

    // option without value -b
    @Test
    void should_split_option_without_value() {
        Map<String, String[]> args = Args.toMap("-b");

        Assertions.assertEquals(1, args.size());
        Assertions.assertArrayEquals(new String[] {}, args.get("b"));
    }

    // option with value -p 8080
    @Test
    void should_split_option_with_value() {
        Map<String, String[]> args = Args.toMap("-p", "8080");

        Assertions.assertEquals(1, args.size());
        Assertions.assertArrayEquals(new String[] {"8080"}, args.get("p"));
    }

    // option with value -g this is a list

    @Test
    void should_split_option_with_values() {
        Map<String, String[]> args = Args.toMap("-g", "this", "is", "a", "list");

        Assertions.assertEquals(1, args.size());
        Assertions.assertArrayEquals(new String[] {"this", "is", "a", "list"}, args.get("g"));
    }
    // multi options


}

