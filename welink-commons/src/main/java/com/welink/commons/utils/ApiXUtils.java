/**
 * Project Name:welink-commons
 * File Name:ApiXUtils.java
 * Package Name:com.welink.commons.utils
 * Date:2016年3月1日下午7:11:41
 * Copyright (c) 2016, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.commons.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.welink.commons.vo.ApixIdCardMsg;
import com.welink.commons.vo.ApixIdCardMsgData;

/**
 * ClassName:ApiXUtils <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2016年3月1日 下午7:11:41 <br/>
 * @author   LuoGuangChun
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
public class ApiXUtils {
	
	//service:6b51a07161364cde66ec8e13d097309a		c37243bb6c8d41316bd4f02085477300
	private static String apix_key = "6b51a07161364cde66ec8e13d097309a";
	private static String apix_key_test = "32959d779e8a4b757011c2514b39526e";
	
	public static String requestGet(String httpUrl, String httpArg) {
	    BufferedReader reader = null;
	    HttpURLConnection connection = null;
	    InputStream is = null;
	    String result = null;
	    StringBuffer sbf = new StringBuffer();
	    httpUrl = httpUrl + "?" + httpArg;

	    try {
	        URL url = new URL(httpUrl);
	        connection = (HttpURLConnection) url
	                .openConnection();
	        connection.setRequestMethod("GET");
			
	        // 填入apix-key到HTTP header
	        connection.setRequestProperty("apix-key", apix_key_test);
	        connection.connect();
	        is = connection.getInputStream();
	        reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
	        String strRead = null;
	        while ((strRead = reader.readLine()) != null) {
	            sbf.append(strRead);
	            sbf.append("\r\n");
	        }
	        reader.close();
	        result = sbf.toString();
	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	    	if(null != connection){
	    		connection.disconnect();
	    	}
			if(null != is){
				try {
					is.close();
				} catch (IOException e) {
				}	
			}
	    }
	    return result;
	}
	
	public static ApixIdCardMsg requestGetApixIdCardMsg(String name, String cardno) { 
	    BufferedReader reader = null;
	    HttpURLConnection connection = null;
	    InputStream is = null;
	    String result = null;
	    ApixIdCardMsg apixIdCardMsg = null;
	    StringBuffer sbf = new StringBuffer();
	    String httpUrl = "http://v.apix.cn/apixcredit/idcheck/idcard" + "?type=idcard&name=" + name + "&cardno=" + cardno;
	    //String httpUrl = "http://a.apix.cn/apixcredit/idcheck/test/idcard" + "?type=idcard&name=" + name + "&cardno=" + cardno;
	    try {
	        URL url = new URL(httpUrl);
	        connection = (HttpURLConnection) url
	                .openConnection();
	        connection.setRequestMethod("GET");
			
	        // 填入apix-key到HTTP header
	        connection.setRequestProperty("apix-key", apix_key);
	        connection.connect();
	        is = connection.getInputStream();
	        reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
	        String strRead = null;
	        while ((strRead = reader.readLine()) != null) {
	            sbf.append(strRead);
	            sbf.append("\r\n");
	        }
	        reader.close();
	        result = sbf.toString();
	        apixIdCardMsg = JSONObject.parseObject(result, ApixIdCardMsg.class);
	    } catch (Exception e) {
	        e.printStackTrace();
	        apixIdCardMsg = null;
	    } finally {
	    	if(null != connection){
	    		connection.disconnect();
	    	}
			if(null != is){
				try {
					is.close();
				} catch (IOException e) {
				}	
			}
	    }
	    return apixIdCardMsg;
	}
	
	public static void main(String[] args) throws UnsupportedEncodingException {
	    //发送 GET 请求
		
		String httpUrl = " http://v.apix.cn/apixcredit/idcheck/idcard";
		String httpArg = "type=idcard&name=罗光春&cardno=440223199001250318";
		//String jsonResult2 = requestGet(httpUrl, httpArg);
		//String jsonResult2 = requestGetApixIdCardMsg("罗光春", "440223199001250318");
		String jsonResult = "{";
					jsonResult += "\"msg\": \"成功：验证信息一致\",";
					jsonResult += "\"code\": 0,";
					jsonResult += "\"data\": {";
						jsonResult += "\"moible_prov\": \"北京\",";
						jsonResult += "\"sex\": \"M\",";
						jsonResult += "\"birthday\": \"1983-11-12\",";
						jsonResult += "\"address\": \"江苏省苏州市吴中区\",";
						jsonResult += "\"mobile_operator\": \"联通185卡\",";
						jsonResult += "\"mobile_city\": \"北京\"";
					jsonResult += "}";
			jsonResult += "}";		
		//System.out.println("----------------------------------"+jsonResult2);
		/*JSONObject jsonObject = new JSONObject();
        Object parse = jsonObject.parse(jsonResult);
        System.out.println("-------------------------------");
        System.out.println(jsonObject.toString());
        System.out.println("-------------------------------");
        System.out.println("111111........................................."+jsonObject.get("msg"));*/
        //ApixIdCardMsg apixIdCardMsg = JSONObject.parseObject(jsonResult2, ApixIdCardMsg.class);
		ApixIdCardMsg apixIdCardMsg = requestGetApixIdCardMsg("王烁", "130490198002227815");
		System.out.println(JSON.toJSONString(apixIdCardMsg));
        System.out.println("2222222........................................."+apixIdCardMsg.getMsg());
        if(null != apixIdCardMsg.getData()){
        	ApixIdCardMsgData data = apixIdCardMsg.getData();
        	System.out.println("333333333333..........."+data.getMobile_city());
        	System.out.println("44444444444444..........."+data.getBirthday());
        }
        
        if(IdcardUtils.validateCard("6228480602557438515")){
        	System.out.println("5555555555555555555");
        }else{
        	System.out.println("6666666666666666666666666");
        }
	}
	
}

