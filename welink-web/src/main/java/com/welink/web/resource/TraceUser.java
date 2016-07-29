package com.welink.web.resource;

import com.alibaba.fastjson.JSON;
import com.welink.biz.common.model.WelinkAgent;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.service.ProfileExtService;
import com.welink.biz.service.UserService;
import com.welink.buy.utils.Constants;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.ProfileDO;
import com.welink.commons.domain.ProfileExtDO;
import com.welink.commons.domain.ProfileExtDOExample;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 记录/更新用户设备信息
 * Created by daniel on 15-3-23.
 */
@RestController
public class TraceUser {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(TraceUser.class);

    @Resource
    private ProfileExtService profileExtService;

    @Resource
    private UserService userService;

    @RequestMapping(value = {"/api/m/1.0/traceUser.json", "/api/h/1.0/traceUser.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        String userAgent = request.getHeader(BizConstants.USER_AGENT);
        WelinkVO welinkVO = new WelinkVO();
        WelinkAgent welinkAgent = new WelinkAgent();
        boolean needUpdate = false;
        try {
            if (StringUtils.isNotBlank(userAgent)) {
                welinkAgent = JSON.parseObject(userAgent, WelinkAgent.class);
            }
            if (null != welinkAgent) {
                long profileId = -1l;
                if (null != session && null != session.getAttribute(BizConstants.PROFILE_ID)) {
                    profileId = (long) session.getAttribute(BizConstants.PROFILE_ID);
                    if (profileId > 0) {
                        ProfileDO profileDO = userService.fetchProfileById(profileId);
                        recordProfileExtInfo(welinkAgent.getBuildVersion(), welinkAgent.getVersion(), welinkAgent.getScale(), welinkAgent.getMode(), welinkAgent.getPlatform(), null, welinkAgent.getSystemVersion(), null, profileDO);
                    }
                }
            }
            if (null != welinkAgent && welinkAgent.getBuildVersion() != null && StringUtils.equals(BizConstants.PRE_CLIENT_FLAG, welinkAgent.getAppbundle()) && Long.valueOf(welinkAgent.getBuildVersion()) < Constants.IOS_PRE_VERSION) {
                needUpdate = true;
            } else if (null != welinkAgent && welinkAgent.getBuildVersion() != null && StringUtils.equals(BizConstants.IOS_CLIENT_FLAG, welinkAgent.getAppbundle()) && Long.valueOf(welinkAgent.getBuildVersion()) < Constants.IOS_VERSION) {
                needUpdate = true;
            } else if (null != welinkAgent && welinkAgent.getBuildVersion() != null && StringUtils.equals("android", welinkAgent.getMode()) && Long.valueOf(welinkAgent.getBuildVersion()) < Constants.ANDROID_VERSION) {
                needUpdate = true;
            }
        } catch (Exception e) {
            log.error("check update failed... ");
        }
        Map resultMap = new HashMap();
        resultMap.put("update", needUpdate);
        welinkVO.setStatus(1);
        welinkVO.setResult(resultMap);
        return JSON.toJSONString(welinkVO);
    }

    private void recordProfileExtInfo(String buildVersion, String version, String scale, String os, String plateform, String ip, String osVersion, String deviceId, ProfileDO profileDO) {
        if (profileDO.getId() > 0) {
            ProfileExtDO profileExtDO = new ProfileExtDO();
            ProfileExtDOExample profileExtDOExample = new ProfileExtDOExample();
            profileExtDOExample.createCriteria().andProfileIdEqualTo(profileDO.getId());
            if (org.apache.commons.lang.StringUtils.isNotBlank(plateform)) {
                profileExtDO.setPlateform(plateform);
            }
            if (org.apache.commons.lang.StringUtils.isNotBlank(version)) {
                profileExtDO.setVersion(version);
            }
            if (org.apache.commons.lang.StringUtils.isNotBlank(buildVersion)) {
                profileExtDO.setBuildVersion(buildVersion);
            }
            if (org.apache.commons.lang.StringUtils.isNotBlank(os)) {
                profileExtDO.setOs(os);
            }
            if (org.apache.commons.lang.StringUtils.isNotBlank(osVersion)) {
                profileExtDO.setOsVersion(osVersion);
            }
            if (org.apache.commons.lang.StringUtils.isNotBlank(ip)) {
                profileExtDO.setIp(ip);
            }
            if (org.apache.commons.lang.StringUtils.isNotBlank(deviceId)) {
                profileExtDO.setDeviceId(deviceId);
            }
            profileExtDO.setProfileId(profileDO.getId());
            profileExtDO.setLoginTime(new Date());
            profileExtDO.setScale(scale);
            profileExtDO.setLastUpdated(new Date());
            profileExtDO.setDateCreated(new Date());
            try {
                profileExtService.updateProfileExt(profileExtDO, profileExtDOExample);
            } catch (Exception e) {
                log.warn("recode prifile ext info failed. profile id :" + profileDO.getId());
            }
        }
    }
}
