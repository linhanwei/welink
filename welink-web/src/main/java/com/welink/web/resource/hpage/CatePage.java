/**
 * Project Name:welink-web
 * File Name:FetchCatesPage.java
 * Package Name:com.welink.web.resource.hpage
 * Date:2015年11月2日上午11:30:19
 * Copyright (c) 2015, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.web.resource.hpage;

import java.util.Date;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.welink.biz.common.security.NeedShop;
import com.welink.biz.service.UserService;
import com.welink.biz.util.UserUtils;
import com.welink.buy.utils.PhenixUserHander;
import com.welink.commons.Env;
import com.welink.commons.commons.BizConstants;

/**
 * ClassName:FetchCatesPage <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2015年11月2日 上午11:30:19 <br/>
 * @author   LuoGuangChun
 */
@Controller
public class CatePage {
	private static org.slf4j.Logger log = LoggerFactory.getLogger(CatePage.class);
	
	@Resource
    private Env env;
	
	@Resource
    private UserService userService;

    //@NeedShop
    @RequestMapping(value = {"/api/h/1.0/catePage.htm"}, produces = "text/html;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
    	model.addAttribute("ver", new Date().getTime() / (1000 * 60 * 60 * 24));
        if (env.isProd()) {
            model.addAttribute(BizConstants.HOST, BizConstants.H5_SEVEN_NB_HOST);
        } else {
            model.addAttribute(BizConstants.HOST, BizConstants.H5_WEILINJIA_HOST);
        }
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        /*if (null != session) {
            model.addAttribute(BizConstants.OPENID, (String) session.getAttribute(BizConstants.OPENID));
            model.addAttribute(BizConstants.PROFILE_ID, session.getAttribute(BizConstants.PROFILE_ID));
            model.addAttribute(BizConstants.P_USER_ID, PhenixUserHander.encodeUserId((Long) session.getAttribute(BizConstants.PROFILE_ID)));
            //设置默认站点信息
            long profileId = -1l;
            userService.setCommunity(model, session, response, profileId);
        }*/
        if (!UserUtils.redirect(session)) {
        	model.addAttribute(BizConstants.PROFILE_ID, (long) session.getAttribute(BizConstants.PROFILE_ID));
        }
        return "catePage";
    }
    
    @RequestMapping(value = {"/api/h/1.0/cateTwoPage.htm"}, produces = "text/html;charset=utf-8")
    public String cateTwoPage(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
    	model.addAttribute("ver", new Date().getTime() / (1000 * 60 * 60 * 24));
        if (env.isProd()) {
            model.addAttribute(BizConstants.HOST, BizConstants.H5_SEVEN_NB_HOST);
        } else {
            model.addAttribute(BizConstants.HOST, BizConstants.H5_WEILINJIA_HOST);
        }
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        /*if (null != session) {
            model.addAttribute(BizConstants.OPENID, (String) session.getAttribute(BizConstants.OPENID));
            model.addAttribute(BizConstants.PROFILE_ID, session.getAttribute(BizConstants.PROFILE_ID));
            model.addAttribute(BizConstants.P_USER_ID, PhenixUserHander.encodeUserId((Long) session.getAttribute(BizConstants.PROFILE_ID)));
            //设置默认站点信息
            long profileId = -1l;
            userService.setCommunity(model, session, response, profileId);
        }*/
        if (!UserUtils.redirect(session)) {
        	model.addAttribute(BizConstants.PROFILE_ID, (long) session.getAttribute(BizConstants.PROFILE_ID));
        }
        return "cateTwoPage";
    }
    
}

