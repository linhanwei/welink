/**
 * Project Name:welink-web
 * File Name:Ally.java
 * Package Name:com.welink.web.resource.hpage
 * Date:2015年11月2日下午6:20:20
 * Copyright (c) 2015, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.web.resource.hpage;

import java.net.URLEncoder;
import java.util.Date;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.welink.biz.service.UserService;
import com.welink.biz.util.UserUtils;
import com.welink.biz.wx.tenpay.util.ConstantUtil;
import com.welink.buy.utils.PhenixUserHander;
import com.welink.commons.Env;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.ProfileDO;

/**
 * ClassName:Ally <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2015年11月2日 下午6:20:20 <br/>
 * @author   LuoGuangChun
 */
@Controller
public class AllyPage {
	@Resource
    private Env env;

    @Resource
    private UserService userService;

    /**
     * 
     * inviteAlly:(邀请盟友). <br/>
     *
     * @author LuoGuangChun
     * @param request
     * @param response
     * @param model
     */
    @RequestMapping(value = {"/api/h/1.0/inviteAlly.htm"}, produces = "text/html;charset=utf-8")
    public String inviteAlly(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
        model.addAttribute("ver", new Date().getTime() / (1000 * 60 * 60 * 24));
        String state = "1";
        if (env.isProd()) {
        	state = ConstantUtil.GUOGEGE_WX_CONF_KEY;
            model.addAttribute(BizConstants.HOST, BizConstants.H5_SEVEN_NB_HOST);
        } else {
        	state = ConstantUtil.XIAOWEILINJU_WX_CONF_KEY;
            model.addAttribute(BizConstants.HOST, BizConstants.H5_WEILINJIA_HOST);
        }
        model.addAttribute("appid", ConstantUtil.mcMap.get(state).getAppId());
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
        if (UserUtils.redirect(session)) {
            response.sendRedirect("/api/h/1.0/vLoginPage.htm?refurl=inviteAlly.htm");
            return null;
        }
        
        Integer isAgency = 0;
        try {
        	Long profileId = (Long) session.getAttribute("profileId");
        	if(profileId > 0){
        		ProfileDO profileDO = userService.fetchProfileById(profileId);
        		if(null != profileDO){
        			isAgency = (null == profileDO.getIsAgency() ? 0 :  Integer.valueOf(profileDO.getIsAgency()));
        		}
        	}
        } catch (Exception e) {
        	isAgency = 0;
        }
        
        if(isAgency != 1){
        	return "redirect:/api/h/1.0/makeMoneyPage.htm";
        }
        
        return "inviteAlly";
    }
    
    /**
     * 
     * inviteAlly:(邀请盟友). <br/>
     *
     * @author LuoGuangChun
     * @param request
     * @param response
     * @param model
     */
    @RequestMapping(value = {"/api/h/1.0/inviteAllyQrPage.htm"}, produces = "text/html;charset=utf-8")
    public String inviteAllyQrPage(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
        model.addAttribute("ver", new Date().getTime() / (1000 * 60 * 60 * 24));
        String state = "1";
        String domain = BizConstants.ONLINE_DOMAIN;
        if (env.isProd()) {
        	state = ConstantUtil.GUOGEGE_WX_CONF_KEY;
        	domain = BizConstants.ONLINE_DOMAIN;
            model.addAttribute(BizConstants.HOST, BizConstants.H5_SEVEN_NB_HOST);
        } else {
        	state = ConstantUtil.XIAOWEILINJU_WX_CONF_KEY;
        	domain = BizConstants.ONLINE_DOMAIN_TEST;
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
        if (UserUtils.redirect(session)) {
            response.sendRedirect("/api/h/1.0/vLoginPage.htm?refurl=inviteAllyQrPage.htm");
            return null;
        }
        
        Integer isAgency = 0;
        try {
        	Long profileId = (Long) session.getAttribute("profileId");
        	String url = "";
        	if(null != profileId && profileId > 0){
        		ProfileDO profileDO = userService.fetchProfileById(profileId);
        		if(null != profileDO){
        			isAgency = (null == profileDO.getIsAgency() ? 0 :  Integer.valueOf(profileDO.getIsAgency()));
        		}
        		String detailPageUrl = "http://"+domain+"/api/h/1.0/inviteSpreadMangerPage.htm?pUserId"+profileId+"&isShare=1";
        		String detailPageUrlEUrl = URLEncoder.encode(detailPageUrl, "UTF-8");
        		url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="+ConstantUtil.mcMap.get(state).getAppId()
        				+	"&redirect_uri=" + detailPageUrlEUrl
        				+ "&response_type=code&scope=snsapi_base&state="+state+"#wechat_redirect";
        	}else{
        		profileId = 0L;
        		url =  "http://"+domain+"/api/h/1.0/inviteSpreadMangerPage.htm?pUserId"+profileId+"&isShare=1";
        	}
        	model.addAttribute("refurl", url);
        } catch (Exception e) {
        	isAgency = 0;
        }
        
        if(isAgency != 1){
        	return "redirect:/api/h/1.0/makeMoneyPage.htm";
        }
        
        return "inviteAllyQrPage";
    }
    
    /**
     * 
     * myAlly:(我的盟友). <br/>
     *
     * @author LuoGuangChun
     * @param request
     * @param response
     * @param model
     */
    @RequestMapping(value = {"/api/h/1.0/myAlly.htm"}, produces = "text/html;charset=utf-8")
    public String myAlly(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
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
        
        if (UserUtils.redirect(session)) {
            response.sendRedirect("/api/h/1.0/vLoginPage.htm?refurl=myAlly.htm");
            return null;
        }
        
        Integer isAgency = 0;
        try {
        	Long profileId = (Long) session.getAttribute("profileId");
        	if(profileId > 0){
        		ProfileDO profileDO = userService.fetchProfileById(profileId);
        		if(null != profileDO){
        			isAgency = (null == profileDO.getIsAgency() ? 0 :  Integer.valueOf(profileDO.getIsAgency()));
        		}
        	}
        } catch (Exception e) {
        	isAgency = 0;
        }
        
        if(isAgency != 1){
        	return "redirect:/api/h/1.0/makeMoneyPage.htm";
        }
        
        return "myAlly";
    }
    
    /**
     * 
     * myInOrDirectAlly:(我的直接或间接盟友). <br/>
     *
     * @author LuoGuangChun
     * @param request
     * @param response
     * @param model
     */
    @RequestMapping(value = {"/api/h/1.0/myInOrDirectAlly.htm"}, produces = "text/html;charset=utf-8")
    public String myDirectAlly(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
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
        if (UserUtils.redirect(session)) {
            response.sendRedirect("/api/h/1.0/vLoginPage.htm?refurl=myInOrDirectAlly.htm");
            return null;
        }
        return "myInOrDirectAlly";
    }
    
    /**
     * 
     * allyOrderList:(盟友订单列表). <br/>
     *
     * @author LuoGuangChun
     * @param request
     * @param response
     * @param model
     */
    @RequestMapping(value = {"/api/h/1.0/allyOrderList.htm"}, produces = "text/html;charset=utf-8")
    public String allyOrderList(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
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
        if (UserUtils.redirect(session)) {
            response.sendRedirect("/api/h/1.0/vLoginPage.htm?refurl=allyOrderList.htm");
            return null;
        }
        return "allyOrderList";
    }
    
    /**
     * 
     * myAlly:(审核注意事项). <br/>
     *
     * @author LuoGuangChun
     * @param request
     * @param response
     * @param model
     */
    @RequestMapping(value = {"/api/h/1.0/allyExamineAttention.htm"}, produces = "text/html;charset=utf-8")
    public String allyExamineAttention(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
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
        if (UserUtils.redirect(session)) {
            response.sendRedirect("/api/h/1.0/vLoginPage.htm?refurl=allyExamineAttention.htm");
            return null;
        }

        return "allyExamineAttention";
    }
    
}

