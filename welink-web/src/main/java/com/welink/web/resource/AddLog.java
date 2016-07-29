package com.welink.web.resource;

import com.alibaba.fastjson.JSON;
import com.welink.commons.commons.BizConstants;
import com.welink.web.common.util.ParameterUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by daniel on 14-12-31.
 */
@RestController
public class AddLog {

    protected final Logger log = LoggerFactory.getLogger("backLog");

    @RequestMapping(value = {"/api/m/1.0/addLog.json", "/api/h/1.0/addLog.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String data = ParameterUtil.getParameter(request, "dt");
        String action = ParameterUtil.getParameter(request, "act");
        String status = ParameterUtil.getParameter(request, "status");
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();

        long profileId = -1l;
        if (null != session.getAttribute(BizConstants.PROFILE_ID)) {
            profileId = (long) session.getAttribute(BizConstants.PROFILE_ID);
        }
        log.error("client json failed. sessionId:" + session.getId() + ",profileId:" + profileId + ",status:" + status + ",action:" + action + ",data:" + data);
        return JSON.toJSONString("");
    }
}
