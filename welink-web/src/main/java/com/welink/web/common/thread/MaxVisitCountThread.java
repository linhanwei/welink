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

import net.spy.memcached.MemcachedClient;

import org.springframework.stereotype.Service;

import com.welink.web.common.util.ContextUtil;
import com.welink.web.common.util.PropertiesUtils;

/**
 * ClassName:MaxVisitCount <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2015年12月10日 下午2:41:21 <br/>
 * @author   LuoGuangChun
 * @version  
 */
@Service
public class MaxVisitCountThread implements Runnable {
	public static final String MAX_VISIT_COUNT = "max_visit_count";
	
    //private MemcachedClient memcachedClient = (MemcachedClient) ContextUtil.getBean("memcachedClient");
	private MemcachedClient memcachedClient = null;
    
	@Override
	public void run() {
		try {
			if(null == memcachedClient){
				memcachedClient = (MemcachedClient) ContextUtil.getBean("memcachedClient");
			}
			PropertiesUtils propertiesUtils = PropertiesUtils.getInstance();
			while(true){
				Integer max_visit_count = null;
				Integer max_visit_times = null;
				try {
					propertiesUtils.init();
					max_visit_count = Integer.valueOf(propertiesUtils.getProperty("max_visit_count"));
					max_visit_times = Integer.valueOf(propertiesUtils.getProperty("max_visit_times"));
				} catch (Exception e) {
					max_visit_count = 2000;
					max_visit_times = 5;
				}
				if(null == max_visit_count){
					max_visit_count = 2000;
				}
				if(null == max_visit_times){
					max_visit_times = 5;
				}
				if(null != memcachedClient && null == memcachedClient.get(MAX_VISIT_COUNT)){
					memcachedClient.set(MAX_VISIT_COUNT, max_visit_times, String.valueOf(max_visit_count));
				}
				Thread.sleep(max_visit_times*1000);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/*@PostConstruct
    public void init() {
		MaxVisitCountThread myThread = new MaxVisitCountThread();  
		Thread thread = new Thread(myThread);  
		thread.start();  
	}*/
	
}

