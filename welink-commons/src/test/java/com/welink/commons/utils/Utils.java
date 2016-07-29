package com.welink.commons.utils;

import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.Random;

/**
 * Created by saarixx on 10/3/15.
 */
public final class Utils {

    public static Random DEFAULT_RANDOM_SEED = new Random(System.currentTimeMillis());

    public static boolean lucky(String probability) {
    	if(null != probability && !"".equals(probability.trim())){
    		BigDecimal p = new BigDecimal(probability);
    		BigDecimal bigDecimal = newRandomBigDecimal(DEFAULT_RANDOM_SEED, p.scale());
    		return bigDecimal.compareTo(p) != 1;
    	}
    	return false;
    }

    public static BigDecimal newRandomBigDecimal(Random r, int precision) {
        BigInteger n = BigInteger.TEN.pow(precision);
        return new BigDecimal(newRandomBigInteger(n, r), precision);
    }

    public static BigInteger newRandomBigInteger(BigInteger n, Random rnd) {
        BigInteger r;
        do {
            r = new BigInteger(n.bitLength(), rnd);
        } while (r.compareTo(n) >= 0);
        return r;
    }

    public static boolean isToday(Date date) {
        return new DateTime(date).withTimeAtStartOfDay().equals(DateTime.now().withTimeAtStartOfDay());
    }


    public static boolean isYesterday(Date date) {
        return new DateTime(date).withTimeAtStartOfDay().equals(DateTime.now().minusDays(1).withTimeAtStartOfDay());
    }
}

