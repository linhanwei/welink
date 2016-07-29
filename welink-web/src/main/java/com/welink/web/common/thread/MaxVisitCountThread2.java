/**
 * Project Name:welink-web
 * File Name:MaxVisitCount.java
 * Package Name:com.welink.web.common.tread
 * Date:2015年12月10日下午2:41:21
 * Copyright (c) 2015, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.web.common.thread;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import net.spy.memcached.MemcachedClient;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import com.welink.web.common.util.ContextUtil;

/**
 * ClassName:MaxVisitCount <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2015年12月10日 下午2:41:21 <br/>
 * @author   LuoGuangChun
 * @version  
 */
//public class MaxVisitCountThread2 implements InitializingBean {
public class MaxVisitCountThread2{
 
    public void close(){
    }
    public void afterPropertiesSet() throws Exception {
        //在这里启动你的线程 
        //方式1 利用构造方法把bean传递过去
        //new Thread(commandTextService);
        //方式2 在thread 内部使用我之前说的获取bean的方式 
       // new Thread();
    }
}

