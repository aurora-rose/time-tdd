package com.time.tdd.args.london;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * london
 *
 * @author XuJian
 * @date 2023-02-22 22:21
 **/
class LdArgsTest {


    @Test
    void should_parse_int_option() {
        LdValueRetriever retriever = mock(LdValueRetriever.class);
        LdOptionClass<IntOption> optionClass = mock(LdOptionClass.class);
        LdOptionParser parser = mock(LdOptionParser.class);


        when(optionClass.getOptionNames()).thenReturn(new String[] {"p"});
        when(optionClass.getOptionType(eq("p"))).thenReturn(int.class);
        when(retriever.getValue(eq("p"), eq(new String[] {"-p", "8080"}))).thenReturn(new String[] {"8080"});
        when(parser.parse(eq(int.class), eq(new String[] {"8080"}))).thenReturn(8080);
        when(optionClass.create(eq(new Object[] {8080}))).thenReturn(new IntOption(8080));

        LdArgs<IntOption> args = new LdArgs<>(retriever, parser, optionClass);
        IntOption option = args.parse("-p", "8080");

        assertEquals(8080, option.port);

    }


    record IntOption(@LdOption("p") int port) {
    }


}

