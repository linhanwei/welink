/**
 * Project Name:welink-web
 * File Name:MikuReturnGoodsPage.java
 * Package Name:com.welink.web.resource.hpage
 * Date:2016年1月21日上午10:27:00
 * Copyright (c) 2016, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.web.resource.hpage;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
import com.welink.buy.utils.Constants;
import com.welink.buy.utils.PhenixUserHander;
import com.welink.commons.Env;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.persistence.TradeMapper;
import com.welink.commons.utils.BigDecimalUtils;

/**
 * ClassName:MikuReturnGoodsPage <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2016年1月21日 上午10:27:00 <br/>
 * @author   LuoGuangChun
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
@Controller
public class MikuReturnGoodsPage {
	@Resource
    private Env env;

    @Resource
    private UserService userService;
    
    /**
     * 
     * reqReturnGoodPage:(申请退款页面). <br/>
     *
     * @author LuoGuangChun
     * @param request
     * @param response
     * @param model
     * @return
     */
    @RequestMapping(value = {"/api/h/1.0/reqReturnGoodPage.htm"}, produces = "text/html;charset=utf-8")
    public String reqReturnGoodPage(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
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
            response.sendRedirect("/api/h/1.0/vLoginPage.htm?refurl=reqReturnGoodPage.htm");
            return null;
        }
        
        return "reqReturnGoodPage";
    }
    
    /**
     * 
     * returnGoodFlowPage:(退款流程页面). <br/>
     *
     * @author LuoGuangChun
     * @param request
     * @param response
     * @param model
     */
    @RequestMapping(value = {"/api/h/1.0/returnGoodFlowPage.htm"}, produces = "text/html;charset=utf-8")
    public String returnGoodFlowPage(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
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
            response.sendRedirect("/api/h/1.0/vLoginPage.htm?refurl=returnGoodFlowPage.htm");
            return null;
        }
        
        return "returnGoodFlowPage";
    }
    
}

