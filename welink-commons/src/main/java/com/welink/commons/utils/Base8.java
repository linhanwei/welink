package com.welink.commons.utils;

/**
 * Created by saarixx on 23/1/15.
 */
public class Base8 {

    public static String encoding(long numbers) {
        return Long.toString(numbers, 8);
    }

    public static long decoding(String octal) {
        return Long.valueOf(octal, 8);
    }
}
