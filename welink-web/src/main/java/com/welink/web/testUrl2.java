/**
 * Project Name:welink-web
 * File Name:test.java
 * Package Name:com.welink.web
 * Date:2015年10月30日下午9:11:54
 * Copyright (c) 2015, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.web;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;

import com.welink.biz.wx.tenpay.util.ConstantUtil;

/**
 * ClassName:test <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2015年10月30日 下午9:11:54 <br/>
 * @author   LuoGuangChun
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
public class testUrl2 {

	public static void main(String[] args) throws UnsupportedEncodingException {

		//String url = "http://lgc.unesmall.com/api/h/1.0/indexPage.htm";
		String url = "http://miku.unesmall.com/api/h/1.0/getWxIsSubscribe.htm";
		
		url = "http://wx.qlogo.cn/mmopen/rQRHolqGLykiaiaIiaMup7NmW6umj3jwN5Dia6gje8IX6vfewgM1uz698T7nk5pbRUY8HfEhBiajRB4Vly3RcreaGqXV4cwUCIwHa/0";
		String enurl = URLEncoder.encode(url, "UTF-8");
		System.out.println(enurl);
		String ll = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="+ConstantUtil.GUOGEGE_H5_APP_ID+"&redirect_uri=http%3A%2F%2Flgc.unesmall.com%2Fapi%2Fh%2F1.0%2FindexPage.htm&response_type=code&scope=snsapi_base&state=1#wechat_redirect";
		//https://open.weixin.qq.com/connect/oauth2/authorize?appid=wx520c15f417810387&redirect_uri=https%3A%2F%2Fchong.qq.com%2Fphp%2Findex.php%3Fd%3D%26c%3DwxAdapter%26m%3DmobileDeal%26showwxpaytitle%3D1%26vb2ctag%3D4_2030_5_1194_60&response_type=code&scope=snsapi_base&state=123#wechat_redirect
		
		url = "http://test.unesmall.com/api/h/1.0/hActive.htm?page=scratchCard";
		String mikuEnurl = URLEncoder.encode(url, "UTF-8");
		System.out.println("------------------1111");
		System.out.println("------------------22222222222222");
		System.out.println(URLEncoder.encode("localhost:8181", "UTF-8"));
		System.out.println("------------------333333333333");
		System.out.println("------------------44444444444444444");
		String miku = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="+"wx82d4b04a531ac1a3"+"&redirect_uri="+mikuEnurl+"&response_type=code&scope=snsapi_base&state=2#wechat_redirect";
		System.out.println(miku);
	
	}
	

}

