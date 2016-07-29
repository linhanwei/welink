package com.welink.web.resource;

import com.alibaba.fastjson.JSON;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.WelinkAgent;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.common.security.PasswordParser;
import com.welink.biz.common.security.RSAEncrypt;
import com.welink.biz.util.TimeUtils;
import com.welink.biz.util.UserUtils;
import com.welink.buy.utils.PhenixUserHander;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.ProfileDO;
import com.welink.commons.domain.ProfileDOExample;
import com.welink.commons.persistence.ProfileDOMapper;
import com.welink.commons.tacker.EventTracker;
import com.welink.web.common.constants.ResponseStatusEnum;
import com.welink.web.common.filter.Profiler;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by daniel on 14-9-10.
 */
@RestController
public class Login {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(Login.class);

    @Resource
    private ProfileDOMapper profileDOMapper;

    @Resource
    private UserUtils userUtils;

    @RequestMapping(value = {"/api/m/1.0/login.json", "/api/h/1.0/login.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        //fetch parameters
        String mobile = request.getParameter("m");
        String pswd = request.getParameter("p");
        String hpswd = request.getParameter("hp");
        String ip = request.getParameter("ip");
        String deviceId = request.getParameter("deviceId");
        WelinkAgent welinkAgent = new WelinkAgent();
        WelinkVO welinkVO = new WelinkVO();
        Map resultMap = new HashMap();
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        Profiler.enter("user login, fetch mobile " + mobile + " profile");
        ProfileDO profileDO = null;
        //check has profile
        ProfileDOExample pExample = new ProfileDOExample();
        pExample.createCriteria().andMobileEqualTo(mobile);
        List<ProfileDO> profileDOs = profileDOMapper.selectByExample(pExample);
        Profiler.release();
        EventTracker.track(BizConstants.LOGIN, "login", "login", "pre", 1L);
        if (null == profileDOs) {
            welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
            welinkVO.setCode(BizErrorEnum.NO_SUCH_MEMBER.getCode());
            welinkVO.setMsg(BizErrorEnum.NO_SUCH_MEMBER.getMsg());
            return JSON.toJSONString(welinkVO);
        }
        if (null != profileDOs && profileDOs.size() < 1) {
            welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
            welinkVO.setCode(BizErrorEnum.NO_SUCH_MEMBER.getCode());
            welinkVO.setMsg(BizErrorEnum.NO_SUCH_MEMBER.getMsg());
            return JSON.toJSONString(welinkVO);
        }
        profileDO = profileDOs.get(0);
        //1. check password
        boolean isH5 = false;
        Profiler.enter("password check");
        if (StringUtils.isBlank(pswd)) {
            if (StringUtils.isNotBlank(hpswd)) {
                pswd = hpswd;
                byte[] pswdArray = RSAEncrypt.hexStringToBytes(pswd);
                pswd = new String(pswdArray);
                isH5 = true;
            }
        }
        pswd = PasswordParser.parserPlanPswd(pswd, null, isH5);
        if (org.apache.commons.lang.StringUtils.isBlank(pswd)) {
            log.error("登录失败 密码错误  Login failed. mobile:" + mobile + ",pswd:" + pswd + ",sessionId:" + session.getId().toString());
            EventTracker.track(profileDO.getMobile(), "login", "check-password", "failure", 1L);
            welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
            welinkVO.setCode(BizErrorEnum.PASSWORD_ERROR.getCode());
            welinkVO.setMsg(BizErrorEnum.PASSWORD_ERROR.getMsg());
            return JSON.toJSONString(welinkVO);
        }
        Profiler.release();
        EventTracker.track(BizConstants.LOGIN, "login", "password", "success", 1L);

        boolean pass = userUtils.checkPswdByMobile(profileDO, pswd);
        //password valid
        if (pass) {
            //获取用户设备信息
            String userAgent = request.getHeader(BizConstants.USER_AGENT);
            resultMap.put("uid", PhenixUserHander.encodeUserId(profileDO.getId()));
            //recode ext info
            try {
                if (org.apache.commons.lang.StringUtils.isNotBlank(userAgent)) {
                    welinkAgent = JSON.parseObject(userAgent, WelinkAgent.class);
                }
                if (null != welinkAgent) {
                    if (StringUtils.equals(BizConstants.PRE_CLIENT_FLAG, welinkAgent.getAppbundle())) {
                        ProfileDO profileDO1 = new ProfileDO();
                        profileDO1.setDiploma((byte) 1);
                        ProfileDOExample profileDOExample = new ProfileDOExample();
                        profileDOExample.createCriteria().andIdEqualTo(profileDOs.get(0).getId());
                        if (profileDOMapper.updateByExampleSelective(profileDO1, profileDOExample) < 1) {
                            log.error("record client diploma failed. profileId:" + profileDOs.get(0).getId() + ",sessionId:" + session.getId().toString());
                        }
                    }
                }
            } catch (Exception e) {
                log.error("record client diploma failed. profileId:" + profileDOs.get(0).getId() + ",sessionId:" + session.getId().toString());
            }
            //执行登录
            UsernamePasswordToken token = new UsernamePasswordToken(mobile, profileDO.getPassword());
            token.setRememberMe(true);
            currentUser.login(token);
            if (currentUser.isAuthenticated()) {
                session.setTimeout(TimeUtils.TIME_1_MONTH_MILi);//millis
                session.setAttribute("profileId", profileDO.getId());
                session.setAttribute("mobile", mobile);
                session.setAttribute("isAgency", null == profileDO.getIsAgency() ? "0" : profileDO.getIsAgency());	//(0=非代理；1=代理)
                Cookie cookieU = new Cookie(BizConstants.JSESSION_ID, session.getId().toString());
                cookieU.setMaxAge(TimeUtils.TIME_1_MONTH_SEC);
                cookieU.setPath("/");
                response.addCookie(cookieU);
            }
        } else {//password invalid
            EventTracker.track(mobile, "login", "login-action", "login-pswd-error", 1L);
            log.error("-登录失败 密码错误  Login failed. mobile:" + mobile + ",pswd:" + pswd + ",sessionId:" + session.getId().toString());
            welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
            welinkVO.setCode(BizErrorEnum.PASSWORD_ERROR.getCode());
            welinkVO.setMsg(BizErrorEnum.PASSWORD_ERROR.getMsg());
            return JSON.toJSONString(welinkVO);
        }
        welinkVO.setStatus(ResponseStatusEnum.SUCCESS.getCode());
        resultMap.put("profilePic", profileDO.getProfilePic());
        resultMap.put("lemonName", profileDO.getNickname());
        resultMap.put("uid", PhenixUserHander.encodeUserId(profileDO.getId()));
        resultMap.put("uid", profileDO.getId());
        resultMap.put("isAgency", null == profileDO.getIsAgency() ? "0" : profileDO.getIsAgency());
        resultMap.put("isExpert", (null == profileDO.getIsExpert() ? (byte)0 : profileDO.getIsExpert()));	//是否专家(0=不是;1=是)
        resultMap.put("sex", profileDO.getSex());
        resultMap.put("ageGroup", profileDO.getAgeGroup());
        EventTracker.track(BizConstants.LOGIN, "login", "login", "success", 1L);
        welinkVO.setResult(resultMap);
        return JSON.toJSONString(welinkVO);
    }

    public void setUserUtils(UserUtils userUtils) {
        this.userUtils = userUtils;
    }

    public void setProfileDOMapper(ProfileDOMapper profileDOMapper) {
        this.profileDOMapper = profileDOMapper;
    }
}
