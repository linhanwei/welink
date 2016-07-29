package com.welink.web.resource;

import com.alibaba.fastjson.JSON;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.WelinkVO;
import com.welink.commons.domain.ProfileDO;
import com.welink.commons.domain.ProfileDOExample;
import com.welink.commons.persistence.ProfileDOMapper;
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

/**
 * Created by daniel on 14-10-10.
 */
@RestController
public class ChangeInfo {

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(ChangeInfo.class);

    @Resource
    private ProfileDOMapper profileDOMapper;

    @RequestMapping(value = {"/api/m/1.0/changeInfo.json", "/api/h/1.0/changeInfo.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        //para
        long profileId = -1;
        String nick = ParameterUtil.getParameter(request, "nick");
        String head_pic = ParameterUtil.getParameter(request, "pic");

        WelinkVO welinkVO = new WelinkVO();
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();//boolean create
        if (checkSession(welinkVO, session)) {
            return JSON.toJSONString(welinkVO);
        }
        if (StringUtils.isBlank(nick) && StringUtils.isBlank(head_pic)) {
            welinkVO.setCode(BizErrorEnum.PARAMS_ERROR.getCode());
            welinkVO.setMsg(BizErrorEnum.PARAMS_ERROR.getMsg());
            welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
            return JSON.toJSONString(welinkVO);
        }

        profileId = (long) session.getAttribute("profileId");

        //change profile
        ProfileDOExample pExample = new ProfileDOExample();
        pExample.createCriteria().andIdEqualTo(profileId).andStatusEqualTo((byte) 1);
        ProfileDO profileDO = new ProfileDO();
        if (StringUtils.isNotBlank(nick)) {
            profileDO.setNickname(nick);
            profileDO.setLemonName(nick);
        }
        if (StringUtils.isNotBlank(head_pic)) {
            profileDO.setProfilePic(head_pic);
        }
        if (profileDOMapper.updateByExampleSelective(profileDO, pExample) > 0) {
            welinkVO.setStatus(ResponseStatusEnum.SUCCESS.getCode());
        } else {
            welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
            welinkVO.setCode(BizErrorEnum.UPDATE_PROFILE_FAILED.getCode());
            welinkVO.setMsg(BizErrorEnum.UPDATE_PROFILE_FAILED.getMsg());
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
