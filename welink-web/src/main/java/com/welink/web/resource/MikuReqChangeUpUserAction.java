/**
 * Project Name:welink-web
 * File Name:MikuReqChangeUpUserAction.java
 * Package Name:com.welink.web.resource
 * Date:2016年3月14日下午6:06:17
 * Copyright (c) 2016, chenzhou1025@126.com All Rights Reserved.
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
import com.welink.biz.common.security.NeedProfile;
import com.welink.commons.persistence.ProfileDOMapper;

/**
 * ClassName:MikuReqChangeUpUserAction <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2016年3月14日 下午6:06:17 <br/>
 * @author   LuoGuangChun
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
@RestController
public class MikuReqChangeUpUserAction {
	@Resource
	private ProfileDOMapper profileDOMapper;
	
	@NeedProfile
	@RequestMapping(value = {"/api/m/1.0/reqChangeUpUser.json", "/api/h/1.0/reqChangeUpUser.json"}, produces = "application/json;charset=utf-8")
	public String addCommentsReply(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value="upMobile", required = false, defaultValue = "") String upMobile,
			@RequestParam(value="seqMobiles", required = false, defaultValue = "") String seqMobiles) throws Exception {
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
		
		welinkVO.setStatus(1);
		welinkVO.setResult(resultMap);
        return JSON.toJSONString(welinkVO);
	}
	
}

