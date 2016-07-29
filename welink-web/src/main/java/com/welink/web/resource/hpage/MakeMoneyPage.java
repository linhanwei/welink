package com.welink.web.resource.hpage;

import com.welink.biz.service.UserService;
import com.welink.biz.util.UserUtils;
import com.welink.buy.utils.Constants;
import com.welink.buy.utils.PhenixUserHander;
import com.welink.commons.Env;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.Trade;
import com.welink.commons.domain.TradeExample;
import com.welink.commons.persistence.TradeMapper;
import com.welink.commons.utils.BigDecimalUtils;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by daniel on 15-1-13.
 */
@Controller
public class MakeMoneyPage {

    @Resource
    private Env env;

    @Resource
    private UserService userService;
    
    @Resource
    private TradeMapper tradeMapper;

    @RequestMapping(value = {"/api/h/1.0/makeMoneyPage.htm"}, produces = "text/html;charset=utf-8")
    public String makeMoneyPage(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
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
            response.sendRedirect("/api/h/1.0/vLoginPage.htm?refurl=makeMoneyPage.htm");
            return null;
        }
        
        Map paramsMap = new HashMap();
        paramsMap.put("profileId", profileId);
        //paramsMap.put("status", Constants.TradeStatus.TRADE_SUCCESSED.getTradeStatusId());	//一完成的订单
        paramsMap.put("type", Constants.TradeType.join_agency.getTradeTypeId());	//成为代理的订单
        Map<String, Object> resultMap = tradeMapper.sumByBuyer(paramsMap);
        if(null == resultMap){
        	model.addAttribute("price", "0.00");
        	model.addAttribute("totalFee", "0.00");
        }else{
        	model.addAttribute("price", BigDecimalUtils.divFee100(resultMap.get("price").toString()));		//分转元
        	model.addAttribute("totalFee", BigDecimalUtils.divFee100(resultMap.get("totalFee").toString())); //分转元
        }
        model.addAttribute("canJoinAgencyFee",  BigDecimalUtils.divFee100(BizConstants.CAN_JOIN_AGENCY_FEE));
        
        return "makeMoneyPage";
    }
    
    /**
     * 
     * buyForJoinAgencyPage:(购买成为代理页面). <br/>
     *
     * @author LuoGuangChun
     * @param request
     * @param response
     * @param model
     * @return
     */
    @RequestMapping(value = {"/api/h/1.0/buyForJoinAgencyPage.htm"}, produces = "text/html;charset=utf-8")
    public String buyForJoinAgencyPage(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
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
        return "buyForJoinAgencyPage";
    }
    
    /**
     * 
     * joinAgencySuccessPage:(成功成为代理页面). <br/>
     * TODO(这里描述这个方法适用条件 – 可选).<br/>
     *
     * @author LuoGuangChun
     * @param request
     * @param response
     * @param model
     * @return
     */
    @RequestMapping(value = {"/api/h/1.0/joinAgencySuccessPage.htm"}, produces = "text/html;charset=utf-8")
    public String joinAgencySuccessPage(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
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
            response.sendRedirect("/api/h/1.0/vLoginPage.htm?refurl=joinAgencySuccessPage.htm");
            return null;
        }
        
        return "joinAgencySuccessPage";
    }
}
