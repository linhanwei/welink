/**
 * Project Name:welink-web
 * File Name:AliKeys.java
 * Package Name:com.welink.web.ons.config
 * Date:2015年11月11日下午12:19:43
 * Copyright (c) 2015, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.web.ons.config;
/**
 * ClassName:AliKeys <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2015年11月11日 下午12:19:43 <br/>
 * @author   LuoGuangChun
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
public enum AliKeys {
	ACCESS_KEY("aHg4n4dESlLleVJz"),					//生产环境
	SECRET_KEY("L2iNoTs1UyRStJgUtkTtqmHUS22Thd"),	//生产环境
	
	ACCESS_KEY_TEST("GDheOkyZuLg7VALU"),			//开发环境
    SECRET_KEY_TEST("7sQA8nMHZkB3CNgspOWnpzrl5B7tx0"),//开发环境
    
    ;
	
	private String value;
	
	AliKeys(String value) {
		this.value = value;
	}

	@Override
    public String toString() {
        return value;
    }
}

