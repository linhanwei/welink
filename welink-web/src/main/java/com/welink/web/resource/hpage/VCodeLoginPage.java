package com.welink.web.resource.hpage;

import com.welink.biz.util.TimeUtils;
import com.welink.commons.Env;
import com.welink.commons.commons.BizConstants;
import com.welink.web.common.util.ParameterUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * Created by daniel on 15-3-9.
 */
@Controller
public class VCodeLoginPage {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(VCodeLoginPage.class);

    @Resource
    private Env env;

    @RequestMapping(value = {"/api/h/1.0/vLoginPage.htm"}, produces = "text/html;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
        String oauth = ParameterUtil.getParameter(request, "refurl");
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        log.info("not in mp weixin inv ......   mobile validate code login page ......");
        model.addAttribute("ver", new Date().getTime() / (1000 * 60 * 60 * 24));
        if (null != session) {
            Cookie cookie = new Cookie(BizConstants.JSESSION_ID, session.getId().toString());
            cookie.setMaxAge(TimeUtils.TIME_1_MONTH_SEC);
            cookie.setPath("/");
            response.addCookie(cookie);
        }
        if (env.isProd()) {
            model.addAttribute(BizConstants.HOST, BizConstants.H5_SEVEN_NB_HOST);
        } else {
            model.addAttribute(BizConstants.HOST, BizConstants.H5_WEILINJIA_HOST);
        }
        return "vCodeLoginPage";
    }
    
    
    @RequestMapping(value = {"/api/h/1.0/registerPage.htm"}, produces = "text/html;charset=utf-8")
    public String registerPage(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
        String oauth = ParameterUtil.getParameter(request, "refurl");
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        log.info("not in mp weixin inv ......   mobile validate code login page ......");
        model.addAttribute("ver", new Date().getTime() / (1000 * 60 * 60 * 24));
        if (null != session) {
            Cookie cookie = new Cookie(BizConstants.JSESSION_ID, session.getId().toString());
            cookie.setMaxAge(TimeUtils.TIME_1_MONTH_SEC);
            cookie.setPath("/");
            response.addCookie(cookie);
        }
        if (env.isProd()) {
            model.addAttribute(BizConstants.HOST, BizConstants.H5_SEVEN_NB_HOST);
        } else {
            model.addAttribute(BizConstants.HOST, BizConstants.H5_WEILINJIA_HOST);
        }
        return "registerPage";
    }
    
    @RequestMapping(value = {"/api/h/1.0/loginYzmPicPage.htm"}, produces = "text/html;charset=utf-8")
    public String loginYzmPicPage(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
        String oauth = ParameterUtil.getParameter(request, "refurl");
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        log.info("not in mp weixin inv ......   mobile validate code login page ......");
        model.addAttribute("ver", new Date().getTime() / (1000 * 60 * 60 * 24));
        if (null != session) {
            Cookie cookie = new Cookie(BizConstants.JSESSION_ID, session.getId().toString());
            cookie.setMaxAge(TimeUtils.TIME_1_MONTH_SEC);
            cookie.setPath("/");
            response.addCookie(cookie);
        }
        if (env.isProd()) {
            model.addAttribute(BizConstants.HOST, BizConstants.H5_SEVEN_NB_HOST);
        } else {
            model.addAttribute(BizConstants.HOST, BizConstants.H5_WEILINJIA_HOST);
        }
        return "loginYzmPicPage";
    }
    
}
