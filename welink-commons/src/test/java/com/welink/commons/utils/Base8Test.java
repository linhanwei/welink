package com.welink.commons.utils;

import org.junit.Test;

public class Base8Test {

    @Test
    public void testEncoding() throws Exception {
        for (int i = 0; i < 10000000; i++) {
            //assertThat(i, equalTo(Base8.decoding(Base8.encoding(i))));
        }
    }
}