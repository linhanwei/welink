package com.welink.web.resource;

import com.alibaba.fastjson.JSON;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.service.ComplainService;
import com.welink.commons.commons.BizConstants;
import com.welink.web.common.constants.ResponseStatusEnum;
import com.welink.web.common.util.ParameterUtil;
import org.apache.commons.lang.StringUtils;
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
 * Created by daniel on 14-10-14.
 */
@RestController
public class AddCompNote {

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(AddCompNote.class);

    @Resource
    private ComplainService complainService;

    @RequestMapping(value = {"/api/m/1.0/addCompNote.json", "/api/h/1.0/addCompNote.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        WelinkVO welinkVO = new WelinkVO();
        Map resultMap = new HashMap();
        //para
        long profileId = -1;
        String content = ParameterUtil.getParameter(request, "content");
        String op = ParameterUtil.getParameter(request, "op");
        long complainId = ParameterUtil.getParameterAslong("complainId");
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();//boolean create
        if (checkSession(welinkVO, session)) {
            return JSON.toJSONString(welinkVO);
        }
        profileId = (long) session.getAttribute("profileId");
        //op
        if (StringUtils.equals("ov", op)) {
            boolean close = complainService.doneComplain(BizConstants.WELINK_ID);
            if (close) {
                welinkVO.setStatus(ResponseStatusEnum.SUCCESS.getCode());
            } else {
                welinkVO.setCode(BizErrorEnum.SYSTEM_BUSY.getCode());
                welinkVO.setMsg(BizErrorEnum.SYSTEM_BUSY.getMsg());
                welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
            }
            return JSON.toJSONString(welinkVO);
        }
        //add complain note
        boolean add = complainService.addComplainNote(BizConstants.WELINK_ID, -1, profileId, complainId, content);
        if (!add) {
            welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
            welinkVO.setCode(BizErrorEnum.SYSTEM_BUSY.getCode());
            welinkVO.setMsg(BizErrorEnum.SYSTEM_BUSY.getMsg());
            return JSON.toJSONString(welinkVO);
        } else {
            welinkVO.setStatus(ResponseStatusEnum.SUCCESS.getCode());
        }
        return JSON.toJSONString(welinkVO);
    }

    private boolean checkSession(WelinkVO welinkVO, Session session) {
        if (null == session) {
            welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
            welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
            welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
            return true;
        }
        if (null != session.getAttribute("profileId")) {
            return false;
        } else {
            welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
            welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
            welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
            return true;
        }
    }
}
