package com.welink.web.resource;

import com.alibaba.fastjson.JSON;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.common.security.BCrypt;
import com.welink.biz.common.security.PasswordParser;
import com.welink.biz.common.security.RSAEncrypt;
import com.welink.biz.service.UserService;
import com.welink.biz.util.UserUtils;
import com.welink.commons.domain.ProfileDO;
import com.welink.commons.domain.ProfileDOExample;
import com.welink.commons.persistence.ProfileDOMapper;
import com.welink.web.common.constants.ResponseMSGConstans;
import com.welink.web.common.constants.ResponseStatusEnum;
import com.welink.web.common.filter.Profiler;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by daniel on 14-9-11.
 */
@RestController
public class FindPswd {

    @Resource
    private UserUtils userUtils;

    @Resource
    private ProfileDOMapper profileDOMapper;

    @Resource
    private UserService userService;

    @RequestMapping(value = {"/api/m/1.0/findPswd.json", "/api/h/1.0/findPswd.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        //fetch parameters
        String pswd = request.getParameter("pswd");
        String npswd = request.getParameter("npswd");

        String hp = request.getParameter("hp");
        String nhp = request.getParameter("nhp");
        String mobile = request.getParameter("mobile");

        WelinkVO welinkVO = new WelinkVO();
        boolean isH5 = false;
        Profiler.enter("password check");
        if (StringUtils.isBlank(pswd)) {
            if (StringUtils.isNotBlank(hp)) {
                pswd = hp;
                npswd = nhp;
                byte[] pswdArray = RSAEncrypt.hexStringToBytes(pswd);
                pswd = new String(pswdArray);
                byte[] npswdArray = RSAEncrypt.hexStringToBytes(npswd);
                npswd = new String(npswdArray);
                isH5 = true;
            }
        }
        String depswd = PasswordParser.parserPlanPswd(pswd, null, isH5);
        String denpswd = PasswordParser.parserPlanPswd(npswd, null, isH5);

        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        if (checkSession(session, welinkVO)) {
            welinkVO.setStatus(0);
            welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
            welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
            return JSON.toJSONString(welinkVO);
        }
        long profileId = (long) session.getAttribute("profileId");
        //判断用户是否存在
        ProfileDO profileForCheck = userService.fetchProfileById(profileId);
        if (null == profileForCheck) {
            welinkVO.setStatus(0);
            welinkVO.setCode(BizErrorEnum.NO_SUCH_MEMBER.getCode());
            welinkVO.setMsg(BizErrorEnum.NO_SUCH_MEMBER.getMsg());
            return JSON.toJSONString(welinkVO);
        }
        mobile = profileForCheck.getMobile();
        //check password
        boolean pass = userUtils.checkPswdByMobile(mobile, depswd);
        if (pass) {
            updatePassword(mobile, denpswd, welinkVO);
            return JSON.toJSONString(welinkVO);
        } else {
            welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
            welinkVO.setCode(BizErrorEnum.PARAMS_ERROR.getCode());
            welinkVO.setMsg(ResponseMSGConstans.PASSWORD_ERROR);
            return JSON.toJSONString(welinkVO);
        }
    }

    /**
     * update password
     *
     * @param mobile
     * @param denpswd
     * @param welinkVO
     */
    private void updatePassword(String mobile, String denpswd, WelinkVO welinkVO) {
        String tostorePswd = BCrypt.hashpw(denpswd, BCrypt.gensalt());
        ProfileDO profileDO = new ProfileDO();
        profileDO.setMobile(mobile);
        profileDO.setPassword(tostorePswd);
        ProfileDOExample profileDOExample = new ProfileDOExample();
        profileDOExample.createCriteria().andMobileEqualTo(mobile);
        int updatePswd = profileDOMapper.updateByExampleSelective(profileDO, profileDOExample);
        if (updatePswd > 0) {
            welinkVO.setStatus(1);
            welinkVO.setMsg(ResponseMSGConstans.CHANGE_PASSWORD_SUCCESS);
        } else {
            welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
            welinkVO.setCode(BizErrorEnum.SYSTEM_BUSY.getCode());
            welinkVO.setMsg(ResponseMSGConstans.CHANGE_PASSWORD_FAILED);
        }
    }

    private boolean checkSession(Session session, WelinkVO welinkVO) {
        if (null == session) {
            welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
            welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
            welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
            return true;
        }
        if (null != session.getAttribute("profileId")) {
            return false;
        } else {
            welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
            welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
            welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
            return true;
        }
    }

    public void setUserUtils(UserUtils userUtils) {
        this.userUtils = userUtils;
    }

    public void setProfileDOMapper(ProfileDOMapper profileDOMapper) {
        this.profileDOMapper = profileDOMapper;
    }
}
