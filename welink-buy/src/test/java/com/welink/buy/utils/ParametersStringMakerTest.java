package com.welink.buy.utils;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ParametersStringMakerTest {

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testParametersMake() throws Exception {
        assertThat(ParametersStringMaker.parametersMake(null), is(ParametersStringMaker.NON_PARAMETER));
        assertThat(ParametersStringMaker.parametersMake("a"), is("a"));
        assertThat(ParametersStringMaker.parametersMake("a", "b"), is("a$$b"));
    }
}