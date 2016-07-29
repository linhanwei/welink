package com.welink.web.resource.multishop;

import com.alibaba.fastjson.JSON;
import com.welink.commons.commons.BizConstants;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by daniel on 15-4-20.
 */
@RestController
public class ClearSession {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(ClearSession.class);

    @RequestMapping(value = {"/api/m/1.0/clearSession.json", "/api/h/1.0/clearSession.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        if (null != session) {
            log.warn("------------session. shopId:" + session.getAttribute(BizConstants.SHOP_ID));
            log.warn("------------session. addr:" + session.getAttribute(BizConstants.LBS_LAST_ADDRESS));
            log.warn("------------session. pid:" + session.getAttribute(BizConstants.PROFILE_ID));
            session.setAttribute(BizConstants.SHOP_ID, -1L);
            session.setAttribute(BizConstants.LBS_LAST_ADDRESS, null);
            session.setAttribute(BizConstants.PROFILE_ID, -1L);
            session.setAttribute(BizConstants.OPENID, null);
        }
        log.warn("------------session is null");
        return JSON.toJSONString("");
    }
}
