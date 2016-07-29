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
public class MikuCommentsPage {
	@Resource
    private Env env;

    @Resource
    private UserService userService;
    
    /**
     * 
     * wordOfMouthPage:(口碑页面). <br/>
     *
     * @author LuoGuangChun
     * @param request
     * @param response
     * @param model
     * @return
     */
    @RequestMapping(value = {"/api/h/1.0/wordOfMouthPage.htm"}, produces = "text/html;charset=utf-8")
    public String wordOfMouthPage(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
        model.addAttribute("ver", new Date().getTime() / (1000 * 60 * 60 * 24));
        long profileId = -1;
        if (env.isProd()) {
            model.addAttribute(BizConstants.HOST, BizConstants.H5_SEVEN_NB_HOST);
        } else {
            model.addAttribute(BizConstants.HOST, BizConstants.H5_WEILINJIA_HOST);
        }
        
        return "wordOfMouthPage";
    }
    
    @RequestMapping(value = {"/api/h/1.0/itemCommentsPage.htm"}, produces = "text/html;charset=utf-8")
    public String itemCommentsPage(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
        model.addAttribute("ver", new Date().getTime() / (1000 * 60 * 60 * 24));
        long profileId = -1;
        if (env.isProd()) {
            model.addAttribute(BizConstants.HOST, BizConstants.H5_SEVEN_NB_HOST);
        } else {
            model.addAttribute(BizConstants.HOST, BizConstants.H5_WEILINJIA_HOST);
        }
        
        return "itemCommentsPage";
    }
    
}

