/**
 * Project Name:welink-web
 * File Name:RedPacket.java
 * Package Name:com.welink.web.resource
 * Date:2015年11月9日下午6:05:43
 * Copyright (c) 2015, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.web.resource.hpage;

import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.daniel.weixin.mp.api.WxMpService;
import com.daniel.weixin.mp.bean.result.WxMpOAuth2AccessToken;
import com.daniel.weixin.mp.bean.result.WxMpUser;
import com.google.common.base.Preconditions;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.service.MikuUserAgencyService;
import com.welink.biz.service.UserInteractionService;
import com.welink.biz.service.UserService;
import com.welink.biz.util.TimeUtils;
import com.welink.buy.utils.PhenixUserHander;
import com.welink.commons.Env;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.MikuMobileAreaDO;
import com.welink.commons.domain.MikuMobileAreaDOExample;
import com.welink.commons.domain.ProfileDO;
import com.welink.commons.domain.ProfileDOExample;
import com.welink.commons.domain.ProfileTempDO;
import com.welink.commons.domain.ProfileTempDOExample;
import com.welink.commons.persistence.MikuMobileAreaDOMapper;
import com.welink.commons.persistence.ProfileDOMapper;
import com.welink.commons.persistence.ProfileTempDOMapper;
import com.welink.commons.utils.MobileUtils;
import com.welink.web.common.util.ParameterUtil;

/**
 * ClassName:RedPacket <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2015年11月9日 下午6:05:43 <br/>
 * @author   LuoGuangChun
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
@Controller
public class RedPacketPage {
	private static org.slf4j.Logger log = LoggerFactory.getLogger(RedPacketPage.class);
	
	@Resource
    private Env env;

    @Resource
    private UserService userService;
    
    @Resource
    private ProfileTempDOMapper profileTempDOMapper;
    
	@Resource
    private WxMpService wxMpService;
	
	@Resource
    private ProfileDOMapper profileDOMapper;
    
    @Resource
    private UserInteractionService userInteractionService;
    
    @Resource
    private MikuUserAgencyService mikuUserAgencyService;
    
    @Resource
    private MikuMobileAreaDOMapper mikuMobileAreaDOMapper;

    @RequestMapping(value = {"/api/h/1.0/redPacket.htm"}, produces = "text/html;charset=utf-8")
    public String redPacket(HttpServletRequest request, HttpServletResponse response, ModelMap model,
    		@RequestParam(value="pid", required = false, defaultValue="0") Long pid) throws Exception {
    	response.setCharacterEncoding("UTF-8");
    	log.info("query string : " + request.getQueryString());
    	
        model.addAttribute("ver", new Date().getTime() / (1000 * 60 * 60 * 24));
        if (env.isProd()) {
            model.addAttribute(BizConstants.HOST, BizConstants.H5_SEVEN_NB_HOST);
        } else {
            model.addAttribute(BizConstants.HOST, BizConstants.H5_WEILINJIA_HOST);
        }
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        if (null != session) {
            model.addAttribute(BizConstants.OPENID, (String) session.getAttribute(BizConstants.OPENID));
            model.addAttribute(BizConstants.PROFILE_ID, session.getAttribute(BizConstants.PROFILE_ID));
            model.addAttribute(BizConstants.P_USER_ID, PhenixUserHander.encodeUserId((Long) session.getAttribute(BizConstants.PROFILE_ID)));
            //设置默认站点信息
            long profileId = -1l;
            userService.setCommunity(model, session, response, profileId);
        }
        model.addAttribute("parentId", pid);		//分享人用户id
        return "redPacket";
    }
    
    /**
     * 
     * receiveRedPacketPageByArea:(领取红包根据电话号码区域邀请成为下级). <br/>
     * TODO(这里描述这个方法适用条件 – 可选).<br/>
     *
     * @author LuoGuangChun
     * @param request
     * @param response
     * @param model
     * @param mobile
     * @param parentId
     * @return
     */
    @RequestMapping(value = {"/api/h/1.0/receiveRedPacketPageByArea.htm"}, produces = "text/html;charset=utf-8")
    public String receiveRedPacketPageByArea(HttpServletRequest request, HttpServletResponse response, ModelMap model,
    		@RequestParam String mobile, @RequestParam(value="parentId", required = false, defaultValue="0") Long parentId) throws Exception {
    	response.setCharacterEncoding("UTF-8");
    	log.info("query string : " + request.getQueryString());
    	
    	if(!MobileUtils.isMobile(mobile)){
    		model.addAttribute("status", 0);	//异常
        	model.addAttribute("msg", BizErrorEnum.IS_MOBILE.getMsg());
        	return "receiveRedPacketPage";
    	}
    	
        model.addAttribute("ver", new Date().getTime() / (1000 * 60 * 60 * 24));
        if (env.isProd()) {
            model.addAttribute(BizConstants.HOST, BizConstants.H5_SEVEN_NB_HOST);
        } else {
            model.addAttribute(BizConstants.HOST, BizConstants.H5_WEILINJIA_HOST);
        }
        
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        if (null != session) {
            model.addAttribute(BizConstants.OPENID, (String) session.getAttribute(BizConstants.OPENID));
            model.addAttribute(BizConstants.PROFILE_ID, session.getAttribute(BizConstants.PROFILE_ID));
            model.addAttribute(BizConstants.P_USER_ID, PhenixUserHander.encodeUserId((Long) session.getAttribute(BizConstants.PROFILE_ID)));
            //设置默认站点信息
            long profileId = -1l;
            userService.setCommunity(model, session, response, profileId);
        }
        
        model.addAttribute("mobile", mobile);
        
        ProfileDO parentProfileDO = profileDOMapper.selectByPrimaryKey(parentId);
        if(null == parentProfileDO){
        	parentId = 0L;
        	//model.addAttribute("parentIsAgency", "0");
        }else if(null != parentProfileDO && !parentProfileDO.getIsAgency().equals((byte)1)){
        	parentId = 0L;
        }
        /*if(null != parentProfileDO && !parentProfileDO.getIsAgency().equals(1)){
        	model.put("status", 5);		//邀请人不是代理
        }*/
        
        //根据电话查出profile信息
        ProfileDOExample profileDOExample = new ProfileDOExample();
        profileDOExample.createCriteria().andMobileEqualTo(mobile).andStatusEqualTo((byte) 1);
        List<ProfileDO> thisNOprofiles = profileDOMapper.selectByExample(profileDOExample);
        if (null != thisNOprofiles && thisNOprofiles.size() > 0) {
        	model.addAttribute("status", 1);		//已注册
        }else{	//未注册
        	ProfileTempDOExample profileTempDOExample = new ProfileTempDOExample();
        	profileTempDOExample.createCriteria().andMobileEqualTo(mobile);
        	List<ProfileTempDO> profileTempDOList = profileTempDOMapper.selectByExample(profileTempDOExample);
        	if(!profileTempDOList.isEmpty()){
        		model.put("status", 1);		//已注册
        		return "receiveRedPacketPage";
        	}
        	
        	try {
            	//注册用户和设置代理关系
        		/*ProfileDO profileDO = mikuUserAgencyService.addProfileAndAgency(-1, mobile, null, (byte)0, parentId.toString(), (byte)0);
                if (null != profileDO) {
                    //doLogin(mobile, null, profileDO.getId(), response);
                    //给新用户发积分和优惠券
                    userInteractionService.sendCouponsToNewPerson(profileDO.getId());
                } else {
                	model.addAttribute("status", 0);
                	model.addAttribute("msg", BizErrorEnum.SYSTEM_BUSY.getMsg());
                }*/
        		
        		//查询电话号码所属地域
        		MikuMobileAreaDOExample mikuMobileAreaDOExample = new MikuMobileAreaDOExample();
        		mikuMobileAreaDOExample.createCriteria().andMobileEqualTo(mobile.substring(0, 7));
        		List<MikuMobileAreaDO> mikuMobileAreaDOList = mikuMobileAreaDOMapper.selectByExample(mikuMobileAreaDOExample);
        		ProfileTempDO profileTempDO = new ProfileTempDO();
        		profileTempDO.setMobile(mobile);
        		//profileTempDO.setPid(parentId);
        		profileTempDO.setInviteId(parentId);;
        		profileTempDO.setPid(0L);
        		profileTempDO.setStatus((byte)1);
        		profileTempDO.setDateCreated(new Date());
        		/*if (!mikuMobileAreaDOList.isEmpty()) {
        			String city = mikuMobileAreaDOList.get(0).getCity();
        			ProfileDOExample profileDOExampleArea = new ProfileDOExample();
        			profileDOExampleArea.createCriteria().andCityEqualTo(city).andIsAgencyEqualTo((byte)1);
        			profileDOExampleArea.setLimit(1);
        			profileDOExampleArea.setOffset(0);
        			List<ProfileDO> profiles = profileDOMapper.selectByExample(profileDOExampleArea);	//查询电话号码所属区域的所有代理
        			if(!profiles.isEmpty() && profiles.get(0).getCity().equals(city)){
        				profileTempDO.setPid(parentId);		//设置上级代理
        			}else{
        				if(!profiles.isEmpty()){	//设置随机上级代理
        					Random rand = new Random();
        			    	//int randNumber = rand.nextInt(MAX - MIN + 1) + MIN;
        					int randNum = rand.nextInt(profiles.size() - 0 + 1) + 0;
        					ProfileDO profileDO = profiles.get(randNum);
        					if(null != profileDO){
        						profileTempDO.setPid(profileDO.getId());		//设置随机上级代理
        					}
        				}
        			}
				}*/
        		
        		if (!mikuMobileAreaDOList.isEmpty()) {
					MikuMobileAreaDO mikuMobileAreaDO = mikuMobileAreaDOList.get(0);
					String city = mikuMobileAreaDOList.get(0).getCity();	//电话号码所属城市
					if(null != parentProfileDO && city.equals(parentProfileDO.getCity())){	
						//当上级代理的地区与当前电话所属地区相等时
						profileTempDO.setPid(parentProfileDO.getId());
					}else{	//没有上级代理时,或当上级代理的地区与当前电话所属地区不相等时
						ProfileDOExample profileDOExampleArea = new ProfileDOExample();
						profileDOExampleArea.createCriteria().andCityEqualTo(city).andIsAgencyEqualTo((byte)1);
						/*profileDOExampleArea.setLimit(1);
        				profileDOExampleArea.setOffset(0);*/
						int countProfile = profileDOMapper.countByExample(profileDOExampleArea);
						Random rand = new Random();
						if(countProfile > 0){
							int randNumAll = rand.nextInt(countProfile);
							profileDOExampleArea.setLimit(randNumAll);
		    				profileDOExampleArea.setOffset(5);
							List<ProfileDO> profiles = profileDOMapper.selectByExample(profileDOExampleArea);	//查询电话号码所属区域的所有代理
							if(!profiles.isEmpty() && profiles.get(0).getCity().equals(city)){
								//int randNumber = rand.nextInt(MAX - MIN + 1) + MIN;	//获取特定区间的随机数
								int randNum = rand.nextInt(profiles.size()-1 - 0 + 1) + 0;
								ProfileDO profileDORand = profiles.get(randNum);
								if(null != profileDORand){
									profileTempDO.setPid(profileDORand.getId());	//设置随机上级代理
								}
							}
						}
					}
				}else{	//当根据电话号码未查到所属区域时，设为未知
					ProfileDOExample profileDOExampleArea = new ProfileDOExample();
					profileDOExampleArea.createCriteria().andCityEqualTo("未知").andIsAgencyEqualTo((byte)1);
					/*profileDOExampleArea.setLimit(1);
    				profileDOExampleArea.setOffset(0);*/
					int countProfile = profileDOMapper.countByExample(profileDOExampleArea);
					Random rand = new Random();
					if(countProfile > 0){
						int randNumAll = rand.nextInt(countProfile);
						profileDOExampleArea.setLimit(5);
	    				profileDOExampleArea.setOffset(randNumAll);
						List<ProfileDO> profiles = profileDOMapper.selectByExample(profileDOExampleArea);	//查询电话号码所属区域的所有代理
						if(!profiles.isEmpty()){
							//int randNumber = rand.nextInt(MAX - MIN + 1) + MIN;	//获取特定区间的随机数
							int randNum = rand.nextInt(profiles.size()-1 - 0 + 1) + 0;
							ProfileDO profileDORand = profiles.get(randNum);
							if(null != profileDORand){
								profileTempDO.setPid(profileDORand.getId());	//设置随机上级代理
							}
						}
					}
					
				}
        		
        		if(profileTempDOMapper.insertSelective(profileTempDO) != 1){
        			model.addAttribute("status", 0);	//异常
                	model.addAttribute("msg", BizErrorEnum.SYSTEM_BUSY.getMsg());
                	return "receiveRedPacketPage";
        		}
                
                String ua = ((HttpServletRequest) request).getHeader("user-agent").toLowerCase();
    	        if (ua.indexOf("micromessenger") > 0) {// 是微信浏览器
    	        	
    	        	 String code = ParameterUtil.getParameter(request, BizConstants.WX_PARAM_CODE);
    	             String state = ParameterUtil.getParameter(request, BizConstants.WX_PARAM_STATE);
    	             String openId = null;
    	             if (StringUtils.isNotBlank(code)) {
    	             	WxMpOAuth2AccessToken wxMpOAuth2AccessToken = wxMpService.oauth2getAccessToken(code, state);
    	             	if (null != wxMpOAuth2AccessToken) {
    	             		WxMpUser user = null;
    	             		openId = wxMpOAuth2AccessToken.getOpenId();
    	             		Preconditions.checkArgument(StringUtils.isNotBlank(openId), "只能在微信环境下使用，无法获得openId");
    	             		if (StringUtils.isNotEmpty(openId)) {
    	             			//model.addAttribute("openid2", openId);
    	                         log.info("user enter fetch wechat page open id is not null  :"+ "openid:" + openId);
    	                         user = wxMpService.userInfo(openId, BizConstants.WX_LANG, state);
    	                         if (null != user) {
    	                        	 model.addAttribute("nickName", user.getNickname());
    	                        	 //model.addAttribute("status", 1);
    	                        	 if(user.getSubscribe()){
    	                         		model.addAttribute("status", 2);	//已订阅
    	                         	}else{
    	                         		model.addAttribute("status", 3);	//未订阅
    	                         	}
    	                         }else{
    	                        	 model.addAttribute("status", 0);
    	                         	model.addAttribute("msg", "阿哦，没有此微信用户");
    	                         }
    	             		}else{
    	            			model.addAttribute("status", 0);
    	            			model.addAttribute("msg", "阿哦，只能在微信环境下使用");
    	            		}
    	             	}
    	             }
    	        }else{
    	        	model.addAttribute("status", 4);		//不是微信领取
    	        }
                
            } catch (Exception e) {
            	model.addAttribute("status", 0);	//异常
            	model.addAttribute("msg", BizErrorEnum.SYSTEM_BUSY.getMsg());
            }
        	
        }
        
        return "receiveRedPacketPage";
    }
    
    @RequestMapping(value = {"/api/h/1.0/receiveRedPacketPage.htm"}, produces = "text/html;charset=utf-8")
    public String receiveRedPacketPage(HttpServletRequest request, HttpServletResponse response, ModelMap model,
    		@RequestParam String mobile, @RequestParam(value="parentId", required = false, defaultValue="0") Long parentId) throws Exception {
    	response.setCharacterEncoding("UTF-8");
    	log.info("query string : " + request.getQueryString());
    	Long invitId = parentId;
    	
    	if(!MobileUtils.isMobile(mobile)){
    		model.addAttribute("status", 0);	//异常
        	model.addAttribute("msg", BizErrorEnum.IS_MOBILE.getMsg());
        	return "receiveRedPacketPage";
    	}
    	
        model.addAttribute("ver", new Date().getTime() / (1000 * 60 * 60 * 24));
        if (env.isProd()) {
            model.addAttribute(BizConstants.HOST, BizConstants.H5_SEVEN_NB_HOST);
        } else {
            model.addAttribute(BizConstants.HOST, BizConstants.H5_WEILINJIA_HOST);
        }
        
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        if (null != session) {
            model.addAttribute(BizConstants.OPENID, (String) session.getAttribute(BizConstants.OPENID));
            model.addAttribute(BizConstants.PROFILE_ID, session.getAttribute(BizConstants.PROFILE_ID));
            model.addAttribute(BizConstants.P_USER_ID, PhenixUserHander.encodeUserId((Long) session.getAttribute(BizConstants.PROFILE_ID)));
            //设置默认站点信息
            long profileId = -1l;
            userService.setCommunity(model, session, response, profileId);
        }
        
        model.addAttribute("mobile", mobile);
        
        ProfileDO parentProfileDO = profileDOMapper.selectByPrimaryKey(parentId);
        if(null == parentProfileDO){
        	parentId = 0L;
        	model.addAttribute("parentIsAgency", "0");
        }else if(null != parentProfileDO && !parentProfileDO.getIsAgency().equals((byte)1)){
        	parentId = 0L;
        }
        /*if(null != parentProfileDO && !parentProfileDO.getIsAgency().equals(1)){
        	model.put("status", 5);		//邀请人不是代理
        }*/
        
        //根据电话查出profile信息
        ProfileDOExample profileDOExample = new ProfileDOExample();
        profileDOExample.createCriteria().andMobileEqualTo(mobile).andStatusEqualTo((byte) 1);
        List<ProfileDO> thisNOprofiles = profileDOMapper.selectByExample(profileDOExample);
        if (null != thisNOprofiles && thisNOprofiles.size() > 0) {
        	model.addAttribute("status", 1);		//已注册
        }else{	//未注册
        	ProfileTempDOExample profileTempDOExample = new ProfileTempDOExample();
        	profileTempDOExample.createCriteria().andMobileEqualTo(mobile);
        	List<ProfileTempDO> profileTempDOList = profileTempDOMapper.selectByExample(profileTempDOExample);
        	if(!profileTempDOList.isEmpty()){
        		model.put("status", 1);		//已注册
        		return "receiveRedPacketPage";
        	}
        	
        	try {
        		ProfileTempDO profileTempDO = new ProfileTempDO();
        		profileTempDO.setMobile(mobile);
        		profileTempDO.setPid(parentId);
        		profileTempDO.setInviteId(invitId);;
        		profileTempDO.setStatus((byte)1);
        		profileTempDO.setDateCreated(new Date());
        		
        		if(profileTempDOMapper.insertSelective(profileTempDO) != 1){
        			model.addAttribute("status", 0);	//异常
                	model.addAttribute("msg", BizErrorEnum.SYSTEM_BUSY.getMsg());
                	return "receiveRedPacketPage";
        		}
                
                String ua = ((HttpServletRequest) request).getHeader("user-agent").toLowerCase();
    	        if (ua.indexOf("micromessenger") > 0) {// 是微信浏览器
    	        	
    	        	 String code = ParameterUtil.getParameter(request, BizConstants.WX_PARAM_CODE);
    	             String state = ParameterUtil.getParameter(request, BizConstants.WX_PARAM_STATE);
    	             String openId = null;
    	             if (StringUtils.isNotBlank(code)) {
    	             	WxMpOAuth2AccessToken wxMpOAuth2AccessToken = wxMpService.oauth2getAccessToken(code, state);
    	             	if (null != wxMpOAuth2AccessToken) {
    	             		WxMpUser user = null;
    	             		openId = wxMpOAuth2AccessToken.getOpenId();
    	             		Preconditions.checkArgument(StringUtils.isNotBlank(openId), "只能在微信环境下使用，无法获得openId");
    	             		if (StringUtils.isNotEmpty(openId)) {
    	             			//model.addAttribute("openid2", openId);
    	                         log.info("user enter fetch wechat page open id is not null  :"+ "openid:" + openId);
    	                         user = wxMpService.userInfo(openId, BizConstants.WX_LANG, state);
    	                         if (null != user) {
    	                        	 model.addAttribute("nickName", user.getNickname());
    	                        	 //model.addAttribute("status", 1);
    	                        	 if(user.getSubscribe()){
    	                         		model.addAttribute("status", 2);	//已订阅
    	                         	}else{
    	                         		model.addAttribute("status", 3);	//未订阅
    	                         	}
    	                         }else{
    	                        	 model.addAttribute("status", 0);
    	                         	model.addAttribute("msg", "阿哦，没有此微信用户");
    	                         }
    	             		}else{
    	            			model.addAttribute("status", 0);
    	            			model.addAttribute("msg", "阿哦，只能在微信环境下使用");
    	            		}
    	             	}
    	             }
    	        }else{
    	        	model.addAttribute("status", 4);		//不是微信领取
    	        }
                
            } catch (Exception e) {
            	model.addAttribute("status", 0);	//异常
            	model.addAttribute("msg", BizErrorEnum.SYSTEM_BUSY.getMsg());
            }
        	
        }
        
        return "receiveRedPacketPage";
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

