package com.welink.web.resource.hpage;

import com.welink.biz.service.UserService;
import com.welink.biz.util.UserUtils;
import com.welink.buy.utils.PhenixUserHander;
import com.welink.commons.Env;
import com.welink.commons.commons.BizConstants;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * Created by daniel on 15-1-4.
 */
@Controller
public class ListPage {

    @Resource
    private Env env;

    @Resource
    private UserService userService;

    @RequestMapping(value = {"/api/h/1.0/listPage.htm"}, produces = "text/html;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
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
            model.addAttribute(BizConstants.SHOP_ID, session.getAttribute(BizConstants.SHOP_ID));
            //设置默认站点信息
            long profileId = -1l;
            userService.setCommunity(model, session, response, profileId);
            model.addAttribute(BizConstants.P_USER_ID, PhenixUserHander.encodeUserId((Long) session.getAttribute(BizConstants.PROFILE_ID)));
        }
        if (UserUtils.redirectLbsShop(session)) {
            response.sendRedirect("/api/h/1.0/indexPage.htm?session=expire");
            return null;
        }
        if (!UserUtils.redirect(session)) {
        	model.addAttribute(BizConstants.PROFILE_ID, (long) session.getAttribute(BizConstants.PROFILE_ID));
        }
        return "listPage";
    }
}
