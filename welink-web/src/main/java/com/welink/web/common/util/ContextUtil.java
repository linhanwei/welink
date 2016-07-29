/**
 * Project Name:welink-web
 * File Name:ContextUtil.java
 * Package Name:com.welink.web.common.util
 * Date:2015年12月10日下午5:08:58
 * Copyright (c) 2015, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.web.common.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

/**
 * ClassName:ContextUtil <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2015年12月10日 下午5:08:58 <br/>
 * @author   LuoGuangChun
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
@Service
public class ContextUtil implements ApplicationContextAware{

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        context = applicationContext;
    }

    public static ApplicationContext getContext(){
        return context;
    }

    public static Object getBean(String beanName) {
		return context.getBean(beanName);
    }

}

