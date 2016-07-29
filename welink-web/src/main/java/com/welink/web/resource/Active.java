/**
 * Project Name:welink-web
 * File Name:Active.java
 * Package Name:com.welink.web.resource
 * Date:2015年12月1日上午9:54:50
 * Copyright (c) 2015, chenzhou1025@126.com All Rights Reserved.
 *
 */

package com.welink.web.resource;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.WelinkVO;

/**
 * ClassName:Active <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason: TODO ADD REASON. <br/>
 * Date: 2015年12月1日 上午9:54:50 <br/>
 * 
 * @author LuoGuangChun
 * @version
 * @since JDK 1.6
 * @see
 */
@RestController
public class Active {

	@RequestMapping(value = { "/api/m/1.0/active.json",
			"/api/h/1.0/active.json" }, produces = "application/json;charset=utf-8")
	public String execute(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		long profileId = -1l;
		long point = 0;
		long couponCount = 0;

		org.apache.shiro.subject.Subject currentUser = SecurityUtils
				.getSubject();
		Session session = currentUser.getSession();
		WelinkVO welinkVO = new WelinkVO();
		profileId = (long) session.getAttribute("profileId");
		if (profileId < 0) {
			welinkVO.setStatus(0);
			welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
			welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
			return JSON.toJSONString(welinkVO);
		}

		Map resultMap = new HashMap();

		return null;
	}

}
