/**
 * Project Name:welink-web
 * File Name:MikuWalletAction.java
 * Package Name:com.welink.web.resource
 * Date:2016年2月27日下午3:43:32
 * Copyright (c) 2016, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.web.resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.WelinkVO;
import com.welink.commons.domain.MikuActiveTopicDO;
import com.welink.commons.domain.MikuActiveTopicDOExample;
import com.welink.commons.persistence.MikuActiveTopicDOMapper;
import com.welink.commons.persistence.ProfileDOMapper;

/**
 * 
 * ClassName: MikuActiveTopicAction <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason: TODO ADD REASON(可选). <br/>
 * date: 2016年4月8日 下午2:42:22 <br/>
 *
 * @author LuoGuangChun
 * @version 
 * @since JDK 1.6
 */
@RestController
public class MikuActiveTopicAction {
	
	@Resource
	private MikuActiveTopicDOMapper mikuActiveTopicDOMapper;
	
	@Resource
	private ProfileDOMapper profileDOMapper;
	
	@RequestMapping(value = {"/api/m/1.0/getActiveTopics.json", "/api/h/1.0/getActiveTopics.json"}, produces = "application/json;charset=utf-8")
	public String getActiveTopics(HttpServletRequest request, HttpServletResponse response,
    		@RequestParam(value="pg", required = false, defaultValue = "0") Integer pg,
    		@RequestParam(value="sz", required = false, defaultValue = "7") Integer sz) throws Exception {
		int page = pg;
        int size = sz;
        if (size < 1) {
            size = 7;
        }
        if (page < 0) {
            page = 0;
        }
        int startRow = 0;
        startRow = (page) * size;
        
		Long profileId = -1l;
		org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
		Session session = currentUser.getSession();
		WelinkVO welinkVO = new WelinkVO();
		welinkVO.setStatus(1);
		Map resultMap = new HashMap();
		profileId = (Long) session.getAttribute("profileId");
		if (null == profileId ||profileId < 0) {
			welinkVO.setStatus(0);
			welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
			welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
			return JSON.toJSONString(welinkVO);
		}
		MikuActiveTopicDOExample mikuActiveTopicDOExample = new MikuActiveTopicDOExample();
		mikuActiveTopicDOExample.createCriteria().andStatusEqualTo((byte)1); 
		mikuActiveTopicDOExample.setOffset(startRow);
		mikuActiveTopicDOExample.setLimit(page);
		
		List<MikuActiveTopicDO> activeTopicList = mikuActiveTopicDOMapper.selectByExample(mikuActiveTopicDOExample);
		
		boolean hasNext = true;
		if (null != activeTopicList && activeTopicList.size() < size) {
			hasNext = false;
		}else if(null == activeTopicList){
			hasNext = false;
		}else{
			hasNext = true;
		}
		welinkVO.setStatus(1);
		resultMap.put("vo", activeTopicList);
		resultMap.put("hasNext", hasNext);
		welinkVO.setResult(resultMap);
		return JSON.toJSONString(welinkVO);
	}
	
	@RequestMapping(value = {"/api/m/1.0/getItemsByTopic.json", "/api/h/1.0/getItemsByTopic.json"}, produces = "application/json;charset=utf-8")
	public String getItemsByTopic(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value="topicId", required = false, defaultValue = "-1") Long topicId,
    		@RequestParam(value="pg", required = false, defaultValue = "0") Integer pg,
    		@RequestParam(value="sz", required = false, defaultValue = "7") Integer sz) throws Exception {
		int page = pg;
        int size = sz;
        if (size < 1) {
            size = 7;
        }
        if (page < 0) {
            page = 0;
        }
        int startRow = 0;
        startRow = (page) * size;
        
		Long profileId = -1l;
		org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
		Session session = currentUser.getSession();
		WelinkVO welinkVO = new WelinkVO();
		welinkVO.setStatus(1);
		Map resultMap = new HashMap();
		profileId = (Long) session.getAttribute("profileId");
		if (null == profileId ||profileId < 0) {
			welinkVO.setStatus(0);
			welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
			welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
			return JSON.toJSONString(welinkVO);
		}
		MikuActiveTopicDOExample mikuActiveTopicDOExample = new MikuActiveTopicDOExample();
		mikuActiveTopicDOExample.createCriteria().andStatusEqualTo((byte)1); 
		mikuActiveTopicDOExample.setOffset(startRow);
		mikuActiveTopicDOExample.setLimit(page);
		
		List<MikuActiveTopicDO> activeTopicList = mikuActiveTopicDOMapper.selectByExample(mikuActiveTopicDOExample);
		
		boolean hasNext = true;
		if (null != activeTopicList && activeTopicList.size() < size) {
			hasNext = false;
		}else if(null == activeTopicList){
			hasNext = false;
		}else{
			hasNext = true;
		}
		welinkVO.setStatus(1);
		resultMap.put("vo", activeTopicList);
		resultMap.put("hasNext", hasNext);
		welinkVO.setResult(resultMap);
		return JSON.toJSONString(welinkVO);
	}
	
}

