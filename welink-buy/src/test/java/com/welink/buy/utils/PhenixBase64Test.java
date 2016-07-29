package com.welink.buy.utils;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

public class PhenixBase64Test {

    static Logger logger = LoggerFactory.getLogger(PhenixBase64.class);

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    static final char base64EncodeChars[] = {'N', '9', 'j', 'm', 'H', 'h', /* 索引 0 ~ 5*/
            'z', 'e', 'I', '-', 'K', '2', 'v', 'M', 'O', 'o', 'Q', 'P', 'X',  /* 索引6 ~ 18*/
            'F', '8', 'R', 'w', '_', 'x', 'Z', 'a', '1', 'c', '7', 'E', 'f',  /* 索引 19 ~ 31*/
            'g', '*', 'i', 'C', 'k', 'l', 'D', 'n', 'A', 'p', '5', 'r', 's',  /* 索引 32 ~ 44*/
            't', '3', 'U', 'W', 'Y', 'y', 'G', '0', 'b', 'L', 'u', '4', 'S',  /* 索引 45 ~ 57*/
            '6', 'd', 'V', 'B', 'q', 'J'}; 								 	/* 索引58 ~ 63*/


    static final byte base64DecodeChars[] = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 33, -1, -1, 9, -1, -1, 52, 27, 11, 46, 56, 42,
            58, 29, 20, 1, -1, -1, -1, -1, -1, -1, -1, 40, 61, 35, 38, 30, 19, 51, 4, 8, 63, 10, 54,
            13, 0, 14, 17, 16, 21, 57, -1, 47, 60, 48, 18, 49, 25, -1, -1, -1, -1, 23, -1, 26, 53,
            28, 59, 7, 31, 32, 5, 34, 2, 36, 37, 3, 39, 15, 41, 62, 43, 44, 45, 55, 12, 22, 24, 50, 6};

    @Test
    public void testAccuracy() throws Exception {
        String keyStr = new String(base64EncodeChars);
        char[] chars = keyStr.toCharArray();
        logger.info(ToStringBuilder.reflectionToString(keyStr.getBytes()));
    }
}