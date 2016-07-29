/**
 * Project Name:welink-commons
 * File Name:JsonUtils.java
 * Package Name:com.welink.commons.utils
 * Date:2015年11月4日下午7:15:17
 * Copyright (c) 2015, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.commons.utils;

import java.util.Date;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.SimpleDateFormatSerializer;
import com.welink.commons.domain.MikuGetpayDO;

/**
 * ClassName:JsonUtils <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2015年11月4日 下午7:15:17 <br/>
 * @author   LuoGuangChun
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
public class JsonUtils {
	private static SerializeConfig mapping = new SerializeConfig();  
    private static String dateFormat;  
    static {  
        dateFormat = "yyyy-MM-dd HH:mm:ss";  
    }  
  
    /** 
     * 默认的处理时间 
     *  
     * @param jsonText 
     * @return 
     */  
    public static String toJSON(Object jsonText) {  
        return JSON.toJSONString(jsonText,  
                SerializerFeature.WriteDateUseDateFormat);  
    }  
  
    /** 
     * 自定义时间格式 
     *  
     * @param jsonText 
     * @return 
     */  
    public static String toJSONString(String dateFormat, String jsonText) {  
        mapping.put(Date.class, new SimpleDateFormatSerializer(dateFormat));  
        return JSON.toJSONString(jsonText, mapping);  
    }
    
    public static void main(String[] args) {
    	MikuGetpayDO mikuGetpayDO = new MikuGetpayDO();
		mikuGetpayDO.setAgencyId(1L);
		mikuGetpayDO.setGetpayType(1);
		mikuGetpayDO.setGetpayAccount("ali111@163.com");
		mikuGetpayDO.setGetpayFee(10L);
		mikuGetpayDO.setGetpayUserName("用户一");
		mikuGetpayDO.setStatus(Byte.valueOf("0"));
		mikuGetpayDO.setClerkerId(1L);
		mikuGetpayDO.setClerkerName("优理氏");
		mikuGetpayDO.setClerkerUserName("优理氏");
		mikuGetpayDO.setDateCreated(new Date());
		
		System.out.println(JsonUtils.toJSON(mikuGetpayDO));
	}
}

