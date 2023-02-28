package com.time.tdd.args;

import com.time.tdd.args.exceptions.IllegalOptionException;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author XuJian
 * @date 2023-02-18 13:20
 **/

class ArgsTest {
    // -l -p 8080-d /usr/logs

    // simple Option:
    // TODO: - Bool: -l
    // TODO: -Integer: -p 8080
    // TODO: -string: -d /usr/logs
    // TODO: multi options: -l -p 8080 -d /usr/logs

    // sad path:
    // TODO: -bool: -l t / -l t f
    // TODO: -int: -p / -p 8080 8081
    // TODO: -string: -d / -d /usr/logs /usr/vars
    // default value:
    // TODO: -bool: false
    // TODO: -int: 0
    // TODO: -string: ""

    // BooleanOptionParserTest:
    // sad path:
    // TODO: -bool -l t / -l t f
    //default:
    // TODO: -bool false

    // SingleValueOptionParserTest:
    // sad path:
    // TODO: -int -p / -p 8080 8081
    // TODO: -string: -d / -d /usr/logs /usr/vars
    // default:
    // TODO: -int 0
    // TODO: -string ""

    @Test
    void should_example_1() {
        Options options = Args.parse(Options.class, "-l", "-p", "8080", "-d", "/usr/logs");
        assertTrue(options.logging);
        assertEquals(8080, options.port());
        assertEquals("/usr/logs", options.directory());

    }


    @Test
    void should_example_2() {
        ListOptions options = Args.parse(ListOptions.class, "-g", "this", "is", "a", "list", "-d", "1", "2", "-3", "5");

        assertArrayEquals(new String[] {"this", "is", "a", "list"}, options.group());
        assertArrayEquals(new Integer[] {1, 2, -3, 5}, options.decimals());
    }


    @Test
    void should_parse_multi_options() {

        MultiOptions options = Args.parse(MultiOptions.class, "-l", "-p", "8080", "-d", "/usr/logs");

        assertTrue(options.logging());
        assertEquals(8080, options.port());
        assertEquals("/usr/logs", options.directory());
    }


    @Test
    void should_throw_illegal_option_exception_for_annotation_not_present() {
        IllegalOptionException e = assertThrows(IllegalOptionException.class,
            () -> Args.parse(OptionWithoutAnnotation.class, "-l", "-p", "8080", "-d", "/usr/logs"));

        assertEquals("port", e.getParameter());
    }

    @Test
    void should_parse_options_if_option_parser_provided() {
        OptionParser boolParser = mock(OptionParser.class);
        OptionParser intParser = mock(OptionParser.class);
        OptionParser stringParser = mock(OptionParser.class);

        when(boolParser.parse(any(), any())).thenReturn(true);
        when(intParser.parse(any(), any())).thenReturn(1000);
        when(stringParser.parse(any(), any())).thenReturn("parsed");

        Args<MultiOptions> args = new Args<>(MultiOptions.class, Map.of(
            boolean.class, boolParser, int.class, intParser, String.class, stringParser));

        MultiOptions options = args.parse("-l", "-p", "8080", "-d", "/usr/logs");
        assertTrue(options.logging());
        assertEquals(1000, options.port());
        assertEquals("parsed", options.directory());
    }

    record OptionWithoutAnnotation(@Option("l") boolean logging, int port, @Option("d") String directory) {
    }

    record MultiOptions(@Option("l") boolean logging, @Option("p") int port, @Option("d") String directory) {
    }

    record Options(@Option("l") boolean logging, @Option("p") int port, @Option("d") String directory) {
    }

    record ListOptions(@Option("g") String[] group, @Option("d") Integer[] decimals) {
    }

}

