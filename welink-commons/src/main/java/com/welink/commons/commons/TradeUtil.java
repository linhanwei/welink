package com.welink.commons.commons;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Random;

/**
 * Created by daniel on 14-11-12.
 */
public class TradeUtil {

    /**
     * 生成tradeId
     *
     * @param buyerId
     * @return
     */
    public static long genTradeNO(long buyerId) {
        long tail = buyerId % 100;
        Date d = new Date();
        long i = d.getTime();
        long di = i % 1000000000000L;
        System.out.println("i:" + i);
        System.out.println("di:" + di);
        String prefix = randomFix(9, 1);
        String diStr = String.valueOf(di);
        String tradeIdStr = prefix + diStr + String.valueOf(tail);
        return Long.valueOf(tradeIdStr);
    }

    public static void main(String[] args) {
        //1426671306910
        long timeL = new Date().getTime();
        long di = timeL % 1000000000000L;
        for (int i = 0; i < 20; i++) {
            long tradeId = genTradeNO(70);
            System.out.println(tradeId);

        }
    }

    public static String randomFix(int max, int min) {
        Random random = new Random();
        int r = random.nextInt(max) % (max - min + 1) + min;
        int r1 = random.nextInt(max) % (max - min + 1) + min;
        return String.valueOf(r) + String.valueOf(r1);
    }

    /**
     * 字符串元转分long
     *
     * @param price
     * @return
     */
    public static long getLongPrice(String price) {
        BigDecimal bd = new BigDecimal(price);
        long lPrice = bd.multiply(new BigDecimal(100)).longValue();
        return lPrice;
    }
}
