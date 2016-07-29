package com.welink.biz.util;

import com.alibaba.fastjson.JSON;
import com.daniel.weixin.common.util.StringUtils;
import com.welink.biz.common.constants.ProfileEnum;
import com.welink.biz.common.model.UserBaseProfile;
import com.welink.biz.common.model.WelinkAgent;
import com.welink.biz.common.security.BCrypt;
import com.welink.buy.utils.Constants;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.ProfileDO;
import com.welink.commons.domain.ProfileDOExample;
import com.welink.commons.domain.TenantDO;
import com.welink.commons.domain.TenantDOExample;
import com.welink.commons.persistence.ProfileDOMapper;
import com.welink.commons.persistence.TenantDOMapper;
import org.apache.shiro.session.Session;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created by daniel on 14-9-11.
 */
@Service
public class UserUtils {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(UserUtils.class);

    @Resource
    private ProfileDOMapper profileDOMapper;

    @Resource
    private TenantDOMapper tenantDOMapper;

    public boolean isProprietor(long buildingId, ProfileDO profileDO) {
        //根据profile获取其与房屋关系
        if (null != profileDO) {//profle表有记录
            long profileId = profileDO.getId();
            TenantDOExample tenantDOExample = new TenantDOExample();
            tenantDOExample.createCriteria().andProfileIdEqualTo(profileId).andBuildingIdEqualTo(buildingId).andStatusEqualTo((byte) 1);
            List<TenantDO> tenants = tenantDOMapper.selectByExample(tenantDOExample);
            for (TenantDO t : tenants) {
                if (Long.compare(t.getProfileId(), profileId) == 0) {
                    //是业主
                    if (Byte.compare(ProfileEnum.proprietor.getCode(), t.getIdentity()) == 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public ProfileEnum fetchUserRole(long profileId, long buildingId) {
        UserBaseProfile ubProfile = new UserBaseProfile();
        ProfileDOExample profileDOExample = new ProfileDOExample();
        profileDOExample.createCriteria().andIdEqualTo(profileId).andStatusEqualTo((byte) 1);
        List<ProfileDO> profiles = profileDOMapper.selectByExample(profileDOExample);
        //根据profile获取其与房屋关系
        if (null != profiles && profiles.size() > 0) {//profle表有记录
            ubProfile.setProfileId(profileId);
            TenantDOExample tenantDOExample = new TenantDOExample();
            tenantDOExample.createCriteria().andProfileIdEqualTo(profileId).andBuildingIdEqualTo(buildingId);
            List<TenantDO> tenants = tenantDOMapper.selectByExample(tenantDOExample);
            boolean isPro = false;
            if (null != tenants && tenants.size() > 0) {
                for (TenantDO t : tenants) {
                    if (Long.compare(t.getProfileId(), profileId) == 0) {
                        return ProfileEnum.getEnumByCode(t.getIdentity());
                    }
                }
            }
        }
        return null;
    }


    /**
     * 判断用户是否是业主
     *
     * @param profileId
     * @return
     */
    public UserBaseProfile isProprietor(long profileId, long buildingId) {
        UserBaseProfile ubProfile = new UserBaseProfile();
        ProfileDOExample profileDOExample = new ProfileDOExample();
        profileDOExample.createCriteria().andIdEqualTo(profileId).andStatusEqualTo((byte) 1);
        List<ProfileDO> thisNOprofiles = profileDOMapper.selectByExample(profileDOExample);
        //根据profile获取其与房屋关系
        if (null != thisNOprofiles && thisNOprofiles.size() > 0) {//profle表有记录
            ubProfile.setProfileId(profileId);
            TenantDOExample tenantDOExample = new TenantDOExample();
            tenantDOExample.createCriteria().andProfileIdEqualTo(profileId).andBuildingIdEqualTo(buildingId);
            List<TenantDO> tenants = tenantDOMapper.selectByExample(tenantDOExample);
            boolean isPro = false;
            for (TenantDO t : tenants) {
                if (Long.compare(t.getProfileId(), profileId) == 0) {
                    //是业主
                    if (Byte.compare(ProfileEnum.proprietor.getCode(), t.getIdentity()) == 0) {
                        isPro = true;
                    }
                    ubProfile.setProprietor(isPro);
                }
            }
            ubProfile.setHasProfile(true);
        } else {//profile表中没有记录
            ubProfile.setProprietor(false);
            ubProfile.setHasProfile(false);
        }
        return ubProfile;
    }

    /**
     * 是否需要跳转判断
     *
     * @param session
     * @return
     */
    public static boolean redirect(Session session) {
        if (null == session) {
            return true;
        }
        if (null == session.getAttribute(BizConstants.PROFILE_ID)) {
            return true;
        }
        long profileId = (long) session.getAttribute(BizConstants.PROFILE_ID);
        if (profileId < 0) {
            return true;
        }
        return false;
    }

    /**
     * 是否需要跳转判断
     *
     * @param session
     * @return
     */
    public static boolean redirectLbsShop(Session session) {
        if (null == session) {
            return true;
        }
        if (null == session.getAttribute(BizConstants.SHOP_ID)) {
            return true;
        }
        long shopId = (long) session.getAttribute(BizConstants.SHOP_ID);
        if (shopId < 0) {
            return true;
        }
        return false;
    }


    /**
     * 是否需要跳转微信进行授权
     *
     * @param session
     * @return
     */
    public static boolean oauthRedirect(Session session) {

        if (StringUtils.isBlank((String) session.getAttribute(BizConstants.OPENID)) || org.apache.commons.lang.StringUtils.equals("null", (String) session.getAttribute(BizConstants.OPENID))) {
            return true;
        }
        return false;
    }


    /**
     * 匹配密码
     *
     * @param mobile
     * @param pswd
     * @return
     */
    public boolean checkPswdByMobile(String mobile, String pswd) {
        boolean pass = false;
        ProfileDOExample profileDOExample = new ProfileDOExample();
        profileDOExample.createCriteria().andMobileEqualTo(mobile).andStatusEqualTo((byte) 1);
        List<ProfileDO> profileDOs = profileDOMapper.selectByExample(profileDOExample);
        if (null != profileDOs && profileDOs.size() > 0) {
            String storedPswd = profileDOs.get(0).getPassword();
            if (StringUtils.isBlank(storedPswd)) {
                return false;
            }
            //密码对比
            if (BCrypt.checkpw(pswd, storedPswd)) {
                pass = true;
            }
        }

        return pass;
    }

    /**
     * 匹配密码
     *
     * @param profileDO
     * @param pswd
     * @return
     */
    public boolean checkPswdByMobile(ProfileDO profileDO, String pswd) {
        if (null == profileDO) {
            return false;
        }
        boolean pass = false;
        String storedPswd = profileDO.getPassword();
        if (org.apache.commons.lang.StringUtils.isBlank(storedPswd)) {
            return false;
        }
        //密码对比
        if (BCrypt.checkpw(pswd, storedPswd)) {
            pass = true;
        } else {
            log.error("密码校验失败 input pswd:" + pswd + "profileId:" + profileDO.getId());
        }
        return pass;
    }

    public static Constants.TradeFrom differentiateOS(HttpServletRequest request) {
        //获取用户设备信息
        WelinkAgent welinkAgent = new WelinkAgent();
        String userAgent = request.getHeader(BizConstants.USER_AGENT);
        //recode ext info
        try {
            if (org.apache.commons.lang.StringUtils.isNotBlank(userAgent)) {
                welinkAgent = JSON.parseObject(userAgent, WelinkAgent.class);
            }
            if (null != welinkAgent) {
                if (org.apache.commons.lang.StringUtils.equals(welinkAgent.getMode(), "iPhone")) {
                    return Constants.TradeFrom.APP_IOS;
                }
                if (org.apache.commons.lang.StringUtils.equals(welinkAgent.getMode(), "android")) {
                    return Constants.TradeFrom.APP_ANDROID;
                }
            }
        } catch (Exception e) {
            log.error("fetch agent ios/android/h5 failed.");
        }
        return Constants.TradeFrom.UNKNOWN;
    }
}
