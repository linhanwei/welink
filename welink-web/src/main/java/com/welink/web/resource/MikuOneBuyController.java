/**
 * Project Name:welink-web
 * File Name:MikuSalesRecord.java
 * Package Name:com.welink.web.resource
 * Date:2015年11月2日下午7:07:42
 * Copyright (c) 2015, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.web.resource;

import java.util.HashMap;
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
import com.welink.commons.persistence.MikuAgencyShareAccountDOMapper;
import com.welink.commons.persistence.MikuGetpayDOMapper;
import com.welink.commons.persistence.MikuSalesRecordDOMapper;

/**
 * ClassName:MikuSalesRecord <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2015年11月2日 下午7:07:42 <br/>
 * @author   LuoGuangChun
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
@RestController
public class MikuOneBuyController {
	
	@Resource
	private MikuSalesRecordDOMapper mikuSalesRecordDOMapper;
	
	@Resource
	private MikuAgencyShareAccountDOMapper mikuAgencyShareAccountDOMapper;
	
	@Resource
	private MikuGetpayDOMapper mikuGetpayDOMapper;
	
	@RequestMapping(value = {"/api/m/1.0/oneBuyList.json", "/api/h/1.0/oneBuyList.json"}, produces = "application/json;charset=utf-8")
	public String oneBuyList(HttpServletRequest request, HttpServletResponse response,
    		@RequestParam(value="isGetpays", required = false) String isGetpays,
    		@RequestParam(value="pg", required = false, defaultValue = "0") Integer pg,
    		@RequestParam(value="sz", required = false, defaultValue = "7") Integer sz) throws Exception {
		Long profileId = -1l;
		org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
		Session session = currentUser.getSession();
		WelinkVO welinkVO = new WelinkVO();
		Map resultMap = new HashMap();
		profileId = (Long) session.getAttribute("profileId");
		if (null == profileId ||profileId < 0) {
			welinkVO.setStatus(0);
			welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
			welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
			return JSON.toJSONString(welinkVO);
		}
		
        return JSON.toJSONString(welinkVO);
	}
	
	
}

