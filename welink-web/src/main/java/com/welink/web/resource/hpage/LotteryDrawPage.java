/**
 * Project Name:welink-web
 * File Name:LotteryDrawPage.java
 * Package Name:com.welink.web.resource.hpage
 * Date:2016年1月7日上午10:57:13
 * Copyright (c) 2016, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.web.resource.hpage;

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
import com.welink.buy.utils.PhenixUserHander;
import com.welink.commons.Env;
import com.welink.commons.commons.BizConstants;

/**
 * ClassName:LotteryDrawPage <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2016年1月7日 上午10:57:13 <br/>
 * @author   LuoGuangChun
 * @version  
 */
@Controller
public class LotteryDrawPage {
	@Resource
    private Env env;

    @Resource
    private UserService userService;
    
    @RequestMapping(value = {"/api/h/1.0/lotteryActive.htm"}, produces = "text/html;charset=utf-8")
    public String lottery(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
        model.addAttribute("ver", new Date().getTime() / (1000 * 60 * 60 * 24));
        long profileId = -1;
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
            userService.setCommunity(model, session, response, profileId);
        }
        if (UserUtils.redirect(session) || profileId < 0) {
            response.sendRedirect("/api/h/1.0/vLoginPage.htm?refurl=lotteryActive.htm");
            return null;
        }
        return "lotteryActive";
    }
    
    @RequestMapping(value = {"/api/h/1.0/lotteryOrder.htm"}, produces = "text/html;charset=utf-8")
    public String lotteryOrder(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
        model.addAttribute("ver", new Date().getTime() / (1000 * 60 * 60 * 24));
        long profileId = -1;
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
            userService.setCommunity(model, session, response, profileId);
        }
        if (null != session.getAttribute(BizConstants.PROFILE_ID)) {
            profileId = (long) session.getAttribute(BizConstants.PROFILE_ID);
        }
        if (UserUtils.redirect(session) || profileId < 0) {
            response.sendRedirect("/api/h/1.0/vLoginPage.htm?refurl=lotteryOrder.htm");
            return null;
        }
        
        return "lotteryOrder";
    }
}

