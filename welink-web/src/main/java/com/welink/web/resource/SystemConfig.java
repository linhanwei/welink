/**
 * Project Name:welink-web
 * File Name:SystemConfig.java
 * Package Name:com.welink.web.resource
 * Date:2015年12月10日下午2:07:59
 * Copyright (c) 2015, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.web.resource;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.spy.memcached.MemcachedClient;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.service.UserService;
import com.welink.commons.domain.ProfileDO;
import com.welink.web.common.thread.MaxVisitCountThread;
import com.welink.web.common.util.ContextUtil;
import com.welink.web.common.util.PropertiesUtils;

/**
 * ClassName:SystemConfig <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2015年12月10日 下午2:07:59 <br/>
 * @author   LuoGuangChun
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
@RestController
public class SystemConfig {
	@Resource
    private MemcachedClient memcachedClient;

	@RequestMapping(value = {"/api/m/1.0/getSystemTimes.json", "/api/h/1.0/getSystemTimes.json"}, produces = "application/json;charset=utf-8")
	public String fetchItemTagList(HttpServletRequest request, HttpServletResponse response) throws Exception {
		WelinkVO welinkVO = new WelinkVO();
		Map resultMap = new HashMap();
		resultMap.put("now", new Date());
		welinkVO.setStatus(1);
		welinkVO.setResult(resultMap);
		return JSON.toJSONString(welinkVO);
	}
	
	@RequestMapping(value = {"/api/m/1.0/getPropertiesMax_visit.json", "/api/h/1.0/getPropertiesMax_visit.json"}, produces = "application/json;charset=utf-8")
	public String getPropertiesMax_visit(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map resultMap = new HashMap();
		PropertiesUtils propertiesUtils = PropertiesUtils.getInstance();
		propertiesUtils.init();
		String max_visit_count = propertiesUtils.getProperty("max_visit_count");
		resultMap.put("max_visit_count", max_visit_count);
		resultMap.put("now", new Date());
		return JSON.toJSONString(resultMap);
	}
	
	@RequestMapping(value = {"/api/m/1.0/getMemcachedMax_visit2.json", "/api/h/1.0/getMemcachedMax_visit2.json"}, produces = "application/json;charset=utf-8")
	public String getMemcachedMax_visit(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map resultMap = new HashMap();
		String  max_visit_count2 = (String)memcachedClient.get(MaxVisitCountThread.MAX_VISIT_COUNT);
		int max_visit_count = (Integer) memcachedClient.get("max_visit_count");
		/*if(null != memcachedClient && null != memcachedClient.get(MaxVisitCountThread.MAX_VISIT_COUNT)){
    		int max_visit_count = (Integer)memcachedClient.get(MaxVisitCountThread.MAX_VISIT_COUNT);
    		if(max_visit_count > 0){
    			memcachedClient.decr(MaxVisitCountThread.MAX_VISIT_COUNT, 1);
    		}else{
    			//memcachedClient.set(MaxVisitCountThread.MAX_VISIT_COUNT, exp, o)
    			welinkVO.setCode(BizErrorEnum.SYSTEM_BUSY.getCode());
    			welinkVO.setMsg(BizErrorEnum.SYSTEM_BUSY.getMsg());
    		}
    	}*/
		resultMap.put("max_visit_count", max_visit_count2);
		resultMap.put("now", new Date());
		return JSON.toJSONString(resultMap);
	}
	
	@RequestMapping(value = {"/api/m/1.0/getMemcachedMax_visit3.json", "/api/h/1.0/getMemcachedMax_visit3.json"}, produces = "application/json;charset=utf-8")
	public String getMemcachedMax_visit3(HttpServletRequest request, HttpServletResponse response) throws Exception {
		UserService userService = (UserService) ContextUtil.getBean("userService");
		ProfileDO fetchProfileById = userService.fetchProfileById(69374L);
		MemcachedClient MemcachedClient2 = (MemcachedClient) ContextUtil.getBean("memcachedClient");
		memcachedClient.set("testmax2", 10, "2000");
		memcachedClient.set("testmax3", 300, "2000");
		
		Map resultMap = new HashMap();
		//resultMap.put("profileDO", fetchProfileById);
		resultMap.put("testmax", memcachedClient.get("testmax"));
		return JSON.toJSONString(resultMap);
	}
	
	@RequestMapping(value = {"/api/m/1.0/getMemcachedMax_visit4.json", "/api/h/1.0/getMemcachedMax_visit4.json"}, produces = "application/json;charset=utf-8")
	public String getMemcachedMax_visit4(HttpServletRequest request, HttpServletResponse response) throws Exception {
		UserService userService = (UserService) ContextUtil.getBean("userService");
		ProfileDO fetchProfileById = userService.fetchProfileById(69374L);
		MemcachedClient MemcachedClient2 = (MemcachedClient) ContextUtil.getBean("memcachedClient");
		Map resultMap = new HashMap();
		//resultMap.put("profileDO", fetchProfileById);
		memcachedClient.incr("testmax2", 5);
		memcachedClient.decr("testmax3", 5);
		memcachedClient.decr("max_visit_count", 5);
		resultMap.put("testmax2", memcachedClient.get("testmax2"));
		resultMap.put("testmax3", memcachedClient.get("testmax3"));
		resultMap.put("max_visit_count", memcachedClient.get("max_visit_count"));
		return JSON.toJSONString(resultMap);
	}
	
	
}

