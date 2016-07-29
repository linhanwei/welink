package com.welink.biz.common.cache;

import java.util.Random;

/**
 * Created by daniel on 14-9-10.
 */
public class CheckNOGenerator {
    /*
 * 返回长度为【strLength】的随机数，在前面补0
 */
    public static String getFixLenthString(int strLength) {

        Random rm = new Random();

        // 获得随机数
        double pross = (1 + rm.nextDouble()) * Math.pow(10, strLength);

        // 将获得的获得随机数转化为字符串
        String fixLenthString = String.valueOf(pross);

        // 返回固定的长度的随机数
        return fixLenthString.substring(1, strLength + 1);
    }
    
    public static void main(String[] args) {
    	Random rand = new Random();
    	int MIN = 10, MAX = 12;
    	for(int i=0; i<20; i++){
    	int randNumber = rand.nextInt(MAX - MIN + 1) + MIN;		//设置什么区间内的随机数
    		System.out.print("num:"+randNumber+"  ");
    	}
    	
    	System.out.println("----------------------------------------");
    	for(int i=0; i<20; i++){
    		int randNumber2 = rand.nextInt(2);		//设置什么区间内的随机数
    		System.out.print("num:"+randNumber2+"  ");
    	}
	}
}
