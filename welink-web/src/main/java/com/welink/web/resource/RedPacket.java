/**
 * Project Name:welink-web
 * File Name:RedPacket.java
 * Package Name:com.welink.web.resource
 * Date:2015年11月17日下午12:43:18
 * Copyright (c) 2015, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.web.resource;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.daniel.weixin.mp.api.WxMpService;
import com.daniel.weixin.mp.bean.result.WxMpUser;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.service.MikuUserAgencyService;
import com.welink.biz.service.UserInteractionService;
import com.welink.biz.util.TimeUtils;
import com.welink.buy.utils.PhenixUserHander;
import com.welink.commons.domain.ProfileDO;
import com.welink.commons.domain.ProfileDOExample;
import com.welink.commons.domain.ProfileTempDO;
import com.welink.commons.domain.ProfileTempDOExample;
import com.welink.commons.persistence.ProfileDOMapper;
import com.welink.commons.persistence.ProfileTempDOMapper;
import com.welink.web.common.constants.ResponseStatusEnum;

/**
 * ClassName:RedPacket <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2015年11月17日 下午12:43:18 <br/>
 * @author   LuoGuangChun
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
@RestController
public class RedPacket {
	private static org.slf4j.Logger log = LoggerFactory.getLogger(RedPacket.class);

    @Resource
    private ProfileDOMapper profileDOMapper;
    
    @Resource
    private ProfileTempDOMapper profileTempDOMapper;
    
    @Resource
    private WxMpService wxMpService;
    
    @Resource
    private UserInteractionService userInteractionService;
    
    @Resource
    private MikuUserAgencyService mikuUserAgencyService;
	
	@RequestMapping(value = {"/api/m/1.0/receiveRedPacket.json", "/api/h/1.0/receiveRedPacket.json"}, produces = "application/json;charset=utf-8")
    public String receiveRedPacket(HttpServletRequest request, HttpServletResponse response,
    		@RequestParam String mobile, @RequestParam(value="parentId", required = false, defaultValue="0") Long parentId,
    		@RequestParam(value="openid", required = false) String openid) throws Exception {
		WelinkVO welinkVO = new WelinkVO();
        Map resultMap = new HashMap();
    	//根据电话查出profile信息
        ProfileDOExample profileDOExample = new ProfileDOExample();
        profileDOExample.createCriteria().andMobileEqualTo(mobile).andStatusEqualTo((byte) 1);
        List<ProfileDO> thisNOprofiles = profileDOMapper.selectByExample(profileDOExample);
        welinkVO.setStatus(1);
        if (null != thisNOprofiles && thisNOprofiles.size() > 0) {
        	resultMap.put("status", 1);		//已注册
        }else{	//未注册
        	ProfileTempDOExample profileTempDOExample = new ProfileTempDOExample();
        	profileTempDOExample.createCriteria().andMobileEqualTo(mobile);
        	List<ProfileTempDO> profileTempDOList = profileTempDOMapper.selectByExample(profileTempDOExample);
        	if(!profileTempDOList.isEmpty()){
        		resultMap.put("status", 1);		//已注册
        		welinkVO.setResult(resultMap);
                return JSON.toJSONString(welinkVO);
        	}
        	
        	try {
                //profileDO = addProfile(-1, mobileNum, toStorePassword, deplom);
            	//注册用户和设置代理关系
        		/*ProfileDO profileDO = mikuUserAgencyService.addProfileAndAgency(-1, mobile, null, (byte)0, parentId.toString(), (byte)0);
                if (null != profileDO) {
                    resultMap.put("uid", PhenixUserHander.encodeUserId(profileDO.getId()));
                    doLogin(mobile, null, profileDO.getId(), response);
                    //给新用户发积分和优惠券
                    userInteractionService.sendCouponsToNewPerson(profileDO.getId());
                } else {
                    welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
                    welinkVO.setCode(BizErrorEnum.SYSTEM_BUSY.getCode());
                    welinkVO.setMsg(BizErrorEnum.SYSTEM_BUSY.getMsg());
                    return JSON.toJSONString(welinkVO);
                }*/
        		ProfileTempDO profileTempDO = new ProfileTempDO();
        		profileTempDO.setMobile(mobile);
        		profileTempDO.setPid(parentId);
        		profileTempDO.setStatus((byte)1);
        		profileTempDO.setDateCreated(new Date());
        		if(profileTempDOMapper.insertSelective(profileTempDO) != 1){
        			welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
        			welinkVO.setCode(BizErrorEnum.SYSTEM_BUSY.getCode());
        			welinkVO.setMsg(BizErrorEnum.SYSTEM_BUSY.getMsg());
        			return JSON.toJSONString(welinkVO);
        		}
                
                String ua = ((HttpServletRequest) request).getHeader("user-agent").toLowerCase();
    	        if (ua.indexOf("micromessenger") > 0) {// 是微信浏览器
    	        	String lang = "zh_CN"; //语言
    	    		WxMpUser user = wxMpService.userInfo(openid, lang);
    	    		if(null != user){
    	    			resultMap.put("nickName", user.getNickname());
    	    			if(user.getSubscribe()){
    	    				resultMap.put("status", 2);		//已关注公众号
    	    			}else{
    	    				resultMap.put("status", 3);		//未关注公众号
    	    			}
    	    		}
    	        }else{
    	        	resultMap.put("status", 4);		//不是微信领取
    	        }
                
            } catch (Exception e) {
                log.error("register failed. mobile:" + mobile + "," + e.getMessage());
                welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
                welinkVO.setCode(BizErrorEnum.SYSTEM_BUSY.getCode());
                welinkVO.setMsg(BizErrorEnum.SYSTEM_BUSY.getMsg());
                return JSON.toJSONString(welinkVO);
            }
        	
        }
        welinkVO.setResult(resultMap);
        return JSON.toJSONString(welinkVO);
	}
	
	@RequestMapping(value = {"/api/m/1.0/receiveRedPacket2.json", "/api/h/1.0/receiveRedPacket2.json"}, produces = "application/json;charset=utf-8")
    public String receiveRedPacket2(HttpServletRequest request, HttpServletResponse response,
    		@RequestParam String mobile, @RequestParam(value="parentId", required = false, defaultValue="0") Long parentId,
    		@RequestParam(value="openid", required = false) String openid) throws Exception {
		WelinkVO welinkVO = new WelinkVO();
        Map resultMap = new HashMap();
    	//根据电话查出profile信息
        ProfileDOExample profileDOExample = new ProfileDOExample();
        profileDOExample.createCriteria().andMobileEqualTo(mobile).andStatusEqualTo((byte) 1);
        List<ProfileDO> thisNOprofiles = profileDOMapper.selectByExample(profileDOExample);
        welinkVO.setStatus(1);
        if (null != thisNOprofiles && thisNOprofiles.size() > 0) {
        	resultMap.put("status", 1);		//已注册
        }else{	//未注册
        	
        	try {
                //profileDO = addProfile(-1, mobileNum, toStorePassword, deplom);
            	//注册用户和设置代理关系
            	//profileDO = mikuUserAgencyService.addProfileAndAgency(-1, mobile, toStorePassword, 0, parentUserId, isAgency);
        		ProfileDO profileDO = mikuUserAgencyService.addProfileAndAgency(-1, mobile, null, (byte)0, parentId.toString(), (byte)0);
                if (null != profileDO) {
                    resultMap.put("uid", PhenixUserHander.encodeUserId(profileDO.getId()));
                    doLogin(mobile, null, profileDO.getId(), response);
                    //给新用户发积分和优惠券
                    userInteractionService.sendCouponsToNewPerson(profileDO.getId());
                } else {
                    welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
                    welinkVO.setCode(BizErrorEnum.SYSTEM_BUSY.getCode());
                    welinkVO.setMsg(BizErrorEnum.SYSTEM_BUSY.getMsg());
                    return JSON.toJSONString(welinkVO);
                }
                
                String ua = ((HttpServletRequest) request).getHeader("user-agent").toLowerCase();
    	        if (ua.indexOf("micromessenger") > 0) {// 是微信浏览器
    	        	String lang = "zh_CN"; //语言
    	    		WxMpUser user = wxMpService.userInfo(openid, lang);
    	    		if(null != user){
    	    			resultMap.put("nickName", user.getNickname());
    	    			if(user.getSubscribe()){
    	    				resultMap.put("status", 2);		//已关注公众号
    	    			}else{
    	    				resultMap.put("status", 3);		//未关注公众号
    	    			}
    	    		}
    	        }else{
    	        	resultMap.put("status", 4);		//不是微信领取
    	        }
                
            } catch (Exception e) {
                log.error("register failed. mobile:" + mobile + "," + e.getMessage());
                welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
                welinkVO.setCode(BizErrorEnum.SYSTEM_BUSY.getCode());
                welinkVO.setMsg(BizErrorEnum.SYSTEM_BUSY.getMsg());
                return JSON.toJSONString(welinkVO);
            }
        	
        }
        welinkVO.setResult(resultMap);
        return JSON.toJSONString(welinkVO);
	}
	
	/**
     * 执行登录
     *
     * @param mobileNum
     * @param toStorePassword
     * @param profileId
     */
    private void doLogin(String mobileNum, String toStorePassword, long profileId, HttpServletResponse response) {
        //注册成功后做登陆 --- 以mobile为name登陆
        UsernamePasswordToken token = new UsernamePasswordToken(mobileNum, toStorePassword);
        token.setRememberMe(true);
        //2. 获取当前Subject：
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();//boolean create
        session.setTimeout(TimeUtils.TIME_1_MONTH_MILi);//millis
        session.setAttribute("profileId", profileId);
        session.setAttribute("mobile", mobileNum);
        //session.setAttribute("isAgency", "1");	//(0=非代理；1=代理)
        Cookie cookieU = new Cookie("JSESSIONID", session.getId().toString());
        cookieU.setMaxAge(60 * 60 * 24 * 15);
        cookieU.setPath("/");
        response.addCookie(cookieU);
    }
	
}

