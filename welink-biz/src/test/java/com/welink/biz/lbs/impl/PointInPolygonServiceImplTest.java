package com.welink.biz.lbs.impl;

import com.welink.biz.lbs.PointInPolygonService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class PointInPolygonServiceImplTest {

    private PointInPolygonService pointInPolygonService = new PointInPolygonServiceImpl();

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testIsIn() throws Exception {
        boolean ret = pointInPolygonService.isIn("120.13006,30.259543", "");
        assertThat(ret, is(true));
    }
}