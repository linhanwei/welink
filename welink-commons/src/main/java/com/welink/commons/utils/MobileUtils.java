/**
 * Project Name:welink-web
 * File Name:MobileUtils.java
 * Package Name:com.welink.web.test
 * Date:2016年1月5日下午4:47:04
 * Copyright (c) 2016, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.commons.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

/**
 * ClassName:MobileUtils <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2016年1月5日 下午4:47:04 <br/>
 * @author   LuoGuangChun
 */
public class MobileUtils {
	/** 
     * 手机号验证 
     *  
     * @param  str 
     * @return 验证通过返回true 
     */  
    public static boolean isMobile(String str) {   
    	/*移动：134、135、136、137、138、139、150、151、157(TD)、158、159、187、188
	　　联通：130、131、132、152、155、156、185、186
	　　电信：133、153、180、189、（1349卫通）*/
        Pattern p = null;  
        Matcher m = null;  
        boolean b = false;   
        p = Pattern.compile("^[1][3,4,5,8][0-9]{9}$"); // 验证手机号  
        //p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");
        m = p.matcher(str);  
        b = m.matches();   
        if(null != str && str.trim().length() == 11 && !StringUtils.isBlank(str)){
        	b = true;
        }
        return b;  
    }  
    /** 
     * 电话号码验证 
     *  
     * @param  str 
     * @return 验证通过返回true 
     */  
    public static boolean isPhone(String str) {   
        Pattern p1 = null,p2 = null;  
        Matcher m = null;  
        boolean b = false;    
        p1 = Pattern.compile("^[0][1-9]{2,3}-[0-9]{5,10}$");  // 验证带区号的  
        p2 = Pattern.compile("^[1-9]{1}[0-9]{5,8}$");         // 验证没有区号的  
        if(str.length() >9)  
        {   m = p1.matcher(str);  
            b = m.matches();    
        }else{  
            m = p2.matcher(str);  
            b = m.matches();   
        }    
        return b;  
    }  
    
    public static void main(String[] args) {
		String mobile = "13345685242";
		if(isMobile(mobile)){
			System.out.println("111111111111111111111111111111111111");
		}else{
			System.out.println("22222222222222222222222222222222");
		}
	}
}

