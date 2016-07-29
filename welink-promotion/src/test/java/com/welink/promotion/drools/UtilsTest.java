package com.welink.promotion.drools;

import org.hamcrest.MatcherAssert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

public class UtilsTest {

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testLucky() throws Exception {
        MatcherAssert.assertThat(new BigDecimal("0.30").scale(), is(2));
        MatcherAssert.assertThat(new BigDecimal("0.03").scale(), is(2));
        MatcherAssert.assertThat(new BigDecimal("0.003").scale(), is(3));

        MatcherAssert.assertThat(Utils.lucky("0.01"), equalTo(false));
        MatcherAssert.assertThat(Utils.lucky("0.99"), equalTo(true));
    }
}