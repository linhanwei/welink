package com.welink.web.resource.hpage;

import com.welink.biz.service.UserService;
import com.welink.biz.util.TimeUtils;
import com.welink.biz.util.UserUtils;
import com.welink.commons.Env;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.utils.PhenixUserHander;
import com.welink.web.common.util.ParameterUtil;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;

/**
 * Created by daniel on 15-1-4.
 */
@Controller
public class CartPage {

    @Resource
    private Env env;

    @Resource
    private UserService userService;

    @RequestMapping(value = {"/api/h/1.0/cartPage.htm"}, produces = "text/html;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
        model.addAttribute("ver", new Date().getTime() / (1000 * 60 * 60 * 24));
        if (env.isProd()) {
            model.addAttribute(BizConstants.HOST, BizConstants.H5_SEVEN_NB_HOST);
        } else {
            model.addAttribute(BizConstants.HOST, BizConstants.H5_WEILINJIA_HOST);
        }
        
        String pUserId = ParameterUtil.getParameter(request, "pUserId");	//上级用户id
        if(null == pUserId || !StringUtils.isNumeric(pUserId) || StringUtils.isBlank(pUserId)){
        	pUserId = "0";
        }
        model.addAttribute("pUserId", pUserId);	//上级userId
        
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        if (null != session) {
            model.addAttribute(BizConstants.OPENID, (String) session.getAttribute(BizConstants.OPENID));
            model.addAttribute(BizConstants.PROFILE_ID, session.getAttribute(BizConstants.PROFILE_ID));
            //设置默认站点信息
            long profileId = -1l;
            userService.setCommunity(model, session, response, profileId);
            model.addAttribute(BizConstants.P_USER_ID, PhenixUserHander.encodeUserId((Long) session.getAttribute(BizConstants.PROFILE_ID)));
            Cookie cookie = new Cookie(BizConstants.JSESSION_ID, session.getId().toString());
            cookie.setMaxAge(TimeUtils.TIME_1_MONTH_SEC);
            cookie.setPath("/");
            response.addCookie(cookie);
        }
        
        if (UserUtils.redirect(session)) {
        	response.sendRedirect("/api/h/1.0/vLoginPage.htm?refurl=" + 
        				URLEncoder.encode("cartPage.htm"+(null==request.getQueryString() ? "" : "?"+request.getQueryString()), "utf-8"));
            return null;
        }
        /*if (UserUtils.redirectLbsShop(session)) {
            response.sendRedirect("/api/h/1.0/indexPage.htm?session=expire" + "&pUserId="+pUserId);
            return null;
        }*/
        return "cartPage";
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        System.out.println(new Date().getTime());
        long d = TimeUtils.getStartTime();
        System.out.println(new Date().getTime() / (1000 * 60 * 60 * 24));
        System.out.println(d / (1000 * 60 * 60 * 24));
    }
}
