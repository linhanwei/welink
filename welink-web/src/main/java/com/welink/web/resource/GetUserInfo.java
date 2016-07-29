package com.welink.web.resource;

import com.alibaba.fastjson.JSON;
import com.opensymphony.xwork2.ActionContext;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.service.UserService;
import com.welink.commons.domain.ProfileDO;
import com.welink.web.common.constants.ResponseStatusEnum;
import com.welink.web.common.model.ResponseResult;
import com.welink.web.common.util.ParameterUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by daniel on 15-1-13.
 */
@RestController
public class GetUserInfo {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(GetUserInfo.class);

    @Resource
    private UserService userService;

    @RequestMapping(value = {"/api/h/1.0/getUserInfo.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        long profileId = -1l;
        String from = ParameterUtil.getParameter(request, "from");
        ActionContext context = ActionContext.getContext();
        ResponseResult result = new ResponseResult();
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        WelinkVO welinkVO = new WelinkVO();
        ProfileDO profileDO;

        try {
            if (session == null || !sessionObject(session, "profileId")) {
                result.setStatus(ResponseStatusEnum.FAILED.getCode());
                result.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
                result.setErrorCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
                context.put("result", result);
                welinkVO.setStatus(0);
                welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
                welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
                return JSON.toJSONString(welinkVO);
            }
            profileId = (long) session.getAttribute("profileId");
        } catch (Exception e) {
            log.error("fetch paras from session failed . exp:" + e.getMessage());
            result.setStatus(ResponseStatusEnum.FAILED.getCode());
            result.setErrorCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
            result.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
            context.put("result", result);
            welinkVO.setStatus(0);
            welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
            welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
            return JSON.toJSONString(welinkVO);
        }
        if (profileId == -1) {
            result.setStatus(ResponseStatusEnum.FAILED.getCode());
            result.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
            result.setErrorCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
            context.put("result", result);
            welinkVO.setStatus(0);
            welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
            welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
            return JSON.toJSONString(welinkVO);
        }

        profileDO = userService.fetchProfileById(profileId);
        welinkVO.setStatus(1);
        Map resultMap = new HashMap();
        if (profileDO != null) {
            resultMap.put("nickname", profileDO.getNickname());
            resultMap.put("profile_pic", profileDO.getProfilePic());
        }
        welinkVO.setResult(resultMap);
        return JSON.toJSONString(welinkVO);
    }

    private boolean sessionObject(Session session, String key) {
        if (null == session) {
            return false;
        }
        return null != session.getAttribute(key);
    }
}
