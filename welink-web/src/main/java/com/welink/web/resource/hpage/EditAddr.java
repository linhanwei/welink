package com.welink.web.resource.hpage;

import com.welink.biz.service.UserService;
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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * Created by daniel on 15-1-4.
 */
@Controller
public class EditAddr {

    @Resource
    private Env env;

    @Resource
    private UserService userService;

    @RequestMapping(value = {"/api/h/1.0/editAddr.htm"}, produces = "text/html;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
        String op = ParameterUtil.getParameter(request, "op");
        if (StringUtils.equalsIgnoreCase(op, "edit")) {
            model.addAttribute("showTitle", "编辑收货地址");
        } else {
            model.addAttribute("showTitle", "添加收货地址");
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
            //设置默认站点信息
            long profileId = -1l;
            userService.setCommunity(model, session, response, profileId);
            model.addAttribute(BizConstants.P_USER_ID, PhenixUserHander.encodeUserId((Long) session.getAttribute(BizConstants.PROFILE_ID)));
        }
        if (UserUtils.redirect(session)) {
            response.sendRedirect("/api/h/1.0/vLoginPage.htm?refurl=editAddr.htm");
            return null;
        }
        return "editAddr";
    }
}
