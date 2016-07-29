/**
 * Project Name:welink-web
 * File Name:WxMpUserInfo.java
 * Package Name:com.welink.web.resource
 * Date:2015年11月9日下午5:40:15
 * Copyright (c) 2015, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.web.resource;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.daniel.weixin.mp.api.WxMpService;
import com.daniel.weixin.mp.bean.result.WxMpOAuth2AccessToken;
import com.daniel.weixin.mp.bean.result.WxMpUser;
import com.google.common.base.Preconditions;
import com.welink.biz.common.model.WelinkVO;
import com.welink.commons.commons.BizConstants;
import com.welink.web.common.util.ParameterUtil;
import com.welink.web.resource.hpage.IndexPage;

/**
 * ClassName:WxMpUserInfo <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2015年11月9日 下午5:40:15 <br/>
 * @author   LuoGuangChun
 */
@RestController
public class WxMpUserInfo {
	private static org.slf4j.Logger log = LoggerFactory.getLogger(WxMpUserInfo.class);
	
	@Resource
    private WxMpService wxMpService;
	
	@RequestMapping(value = {"/api/m/1.0/getWxUserByOpenid.json", "/api/h/1.0/getWxUserByOpenid.json"}, produces = "application/json;charset=utf-8")
	public String getWxUserByOpenid(HttpServletRequest request, HttpServletResponse response,
			@RequestParam String openId) throws Exception {
		WelinkVO welinkVO = new WelinkVO();
		Map resultMap = new HashMap();
		welinkVO.setStatus(1);
		String lang = "zh_CN"; //语言
		WxMpUser user = wxMpService.userInfo(openId, lang);
		if(null != user){
			resultMap.put("openId", openId);
			resultMap.put("nickName", user.getNickname());
			if(user.getSubscribe()){
				resultMap.put("subscribe", 1);		//已订阅
			}else{
				resultMap.put("subscribe", 0);		//未订阅
			}
			welinkVO.setResult(resultMap);
		}else{
			welinkVO.setStatus(0);
			welinkVO.setMsg("阿哦，没有此用户");
		}
		return JSON.toJSONString(welinkVO);
	}
	
	//https://open.weixin.qq.com/connect/oauth2/authorize?appid=wx82d4b04a531ac1a3&redirect_uri=http%3A%2F%2Fmiku.unesmall.com%2Fapi%2Fh%2F1.0%2FgetWxIsSubscribe.htm&response_type=code&scope=snsapi_base&state=1#wechat_redirect
	//是否关注订阅米酷公众号
	@RequestMapping(value = {"/api/m/1.0/getWxIsSubscribe.json", "/api/h/1.0/getWxIsSubscribe.json"}, produces = "application/json;charset=utf-8")
	public String getWxIsSubscribe(HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.setCharacterEncoding("UTF-8");
        log.info("query string : " + request.getQueryString());
        String code = ParameterUtil.getParameter(request, BizConstants.WX_PARAM_CODE);
        String state = ParameterUtil.getParameter(request, BizConstants.WX_PARAM_STATE);
        String openId = null;
        WelinkVO welinkVO = new WelinkVO();
        Map resultMap = new HashMap();
        if (StringUtils.isNotBlank(code)) {
        	WxMpOAuth2AccessToken wxMpOAuth2AccessToken = wxMpService.oauth2getAccessToken(code, state);
        	if (null != wxMpOAuth2AccessToken) {
        		WxMpUser user = null;
        		openId = wxMpOAuth2AccessToken.getOpenId();
        		Preconditions.checkArgument(StringUtils.isNotBlank(openId), "只能在微信环境下使用，无法获得openId");
        		if (StringUtils.isNotEmpty(openId)) {
        			welinkVO.setStatus(1);
                    log.info("user enter fetch wechat page open id is not null  :"+ "openid:" + openId);
                    user = wxMpService.userInfo(openId, BizConstants.WX_LANG, state);
                    String unionId = null;
                    if (null != user) {
                        unionId = user.getUnionId();
                        if(user.getSubscribe()){
                        	resultMap.put("nickName", user.getNickname());
                        	resultMap.put("subscribe", 1);		//已订阅
                        	welinkVO.setResult(resultMap);
                        }else{
                        	resultMap.put("subscribe", 0);			//未订阅
                        }
                    }else{
                    	resultMap.put("subscribe", 0);			//未订阅
                    }
        		}else{
        			welinkVO.setStatus(0);
        			welinkVO.setMsg("只能在微信环境下使用");
        		}
        	}
        }
        response.setHeader("Access-Control-Allow-Origin", "*");
    	return JSON.toJSONString(welinkVO);
	}
}

