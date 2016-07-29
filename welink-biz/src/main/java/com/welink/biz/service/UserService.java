package com.welink.biz.service;

import com.alibaba.fastjson.JSON;
import com.daniel.weixin.mp.bean.result.WxMpOAuth2AccessToken;
import com.daniel.weixin.mp.bean.result.WxMpUser;
import com.welink.biz.common.cache.CheckNOGenerator;
import com.welink.biz.common.model.AlipayInfoModel;
import com.welink.biz.common.model.UserInfo;
import com.welink.biz.common.model.WxInfoModel;
import com.welink.biz.util.TimeUtils;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.*;
import com.welink.commons.persistence.*;
import com.welink.commons.utils.EmojiFilter;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.session.Session;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Created by daniel on 14-10-7.
 */
@Service
public class UserService {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(UserService.class);

    @Resource
    private NotifyConfigDOMapper notifyConfigDOMapper;

    @Resource
    private ProfileDOMapper profileDOMapper;

    @Resource
    private MessageSummaryDOMapper messageSummaryDOMapper;

    @Resource
    private ProfileWeChatDOMapper profileWeChatDOMapper;

    @Resource
    private WeChatProfileDOMapper weChatProfileDOMapper;

    @Resource
    private ProfileCoopDOMapper profileCoopDOMapper;

    @Resource
    private CoopProfileDOMapper coopProfileDOMapper;

    @Resource
    private ProfileExtService profileExtService;

    @Resource
    private AddressService addressService;
    
    @Resource
    private MikuUserAgencyService mikuUserAgencyService;
    
    @Resource
    private ProfileTempDOMapper profileTempDOMapper;

    /**
     * 获取用户最近登录的站点并设置session
     *
     * @param model
     * @param session
     * @param response
     * @param profileId
     */
    public void setCommunity(ModelMap model, Session session, HttpServletResponse response, long profileId) {
        profileId = -1l;
        if (null != session.getAttribute(BizConstants.PROFILE_ID)) {
            profileId = (long) session.getAttribute(BizConstants.PROFILE_ID);
        }
        CommunityDO communityDO = addressService.fetchLastLoginCommunity(profileId);
        if (null != communityDO) {
            session.setAttribute(BizConstants.SHOP_ID, communityDO.getId());
            model.addAttribute(BizConstants.SHOP_ID, communityDO.getId());
            Cookie cookie = new Cookie(BizConstants.JSESSION_ID, session.getId().toString());
            cookie.setMaxAge(TimeUtils.TIME_1_MONTH_SEC);
            cookie.setPath("/");
            response.addCookie(cookie);
        }
    }

    /**
     * 更新最近登录的站点
     *
     * @param communityId
     * @param profileId
     * @return
     */
    public boolean updateLastLoginCommunity(long communityId, long profileId) {
        ProfileDO profileDO = new ProfileDO();
        profileDO.setLastCommunity(communityId);
        profileDO.setLastUpdated(new Date());
        ProfileDOExample pExample = new ProfileDOExample();
        pExample.createCriteria().andIdEqualTo(profileId);
        if (profileDOMapper.updateByExampleSelective(profileDO, pExample) < 1) {
            return true;
        }
        return true;
    }

    /**
     * 根据openid获取存储的profile_wechat id 如果没有存储则返回-1l
     *
     * @param openid
     * @return
     */
    public long fetchWechatIdByOpenid(String openid) {
        long wechatId = -1l;
        if (StringUtils.isNotBlank(openid)) {
            ProfileWeChatDOExample pExample = new ProfileWeChatDOExample();
            pExample.createCriteria().andOpenidEqualTo(openid);
            List<ProfileWeChatDO> profileWeChatDOs = profileWeChatDOMapper.selectByExample(pExample);
            if (null != profileWeChatDOs && profileWeChatDOs.size() > 0) {
                wechatId = profileWeChatDOs.get(0).getId();
            }
        }
        return wechatId;
    }

    /**
     * 添加wechat info
     *
     * @param weChatUserInfo
     * @return
     */
    public boolean addWechatInfo(WxMpUser weChatUserInfo, ProfileWeChatDO profileWeChatDO) {
        ProfileWeChatDO pweChatDO = new ProfileWeChatDO();
        pweChatDO.setCity(weChatUserInfo.getCity());
        pweChatDO.setCountry(weChatUserInfo.getCountry());
        pweChatDO.setDateCreated(new Date());
        pweChatDO.setLastUpdated(new Date());
        pweChatDO.setHeadimgurl(weChatUserInfo.getHeadImgUrl());
        pweChatDO.setNickname(EmojiFilter.filterEmoji(weChatUserInfo.getNickname()));
        pweChatDO.setOpenid(weChatUserInfo.getOpenId());
        pweChatDO.setStatus(1);
        pweChatDO.setVersion(1l);
        pweChatDO.setSex(weChatUserInfo.getSex());
        pweChatDO.setProvince(weChatUserInfo.getProvince());
        pweChatDO.setAccessToken(profileWeChatDO.getAccessToken());
        pweChatDO.setExpiresIn(profileWeChatDO.getExpiresIn());
        pweChatDO.setRefreshToken(profileWeChatDO.getRefreshToken());
        pweChatDO.setOpenid(profileWeChatDO.getOpenid());
        pweChatDO.setScope(profileWeChatDO.getScope());
        pweChatDO.setUnionId(weChatUserInfo.getUnionId());
        pweChatDO.setSubscribe(weChatUserInfo.isSubscribe());
        pweChatDO.setLanguage(weChatUserInfo.getLanguage());
        pweChatDO.setSubscribeTime(weChatUserInfo.getSubscribeTime());
        //1. add wechat info
        if (profileWeChatDOMapper.insertSelective(pweChatDO) < 0) {
            log.error("insert wechat user info failed. nick:" + weChatUserInfo.getNickname() + ",openid:" + weChatUserInfo.getOpenId());
            return false;
        }
        return true;
    }

    /**
     * 更新profile_wechat data
     *
     * @param user
     */
    public void updateProfileWechatInfo(WxMpUser user) {
        if (null == user || StringUtils.isBlank(user.getOpenId())) {
            return;
        }
        ProfileWeChatDO profileWeChatDO = new ProfileWeChatDO();
        profileWeChatDO.setLastLoginTime(new Date());
        if (StringUtils.isNotBlank(user.getNickname())) {
        	profileWeChatDO.setNickname(EmojiFilter.filterEmoji(user.getNickname()));
            //profileWeChatDO.setNickname(user.getNickname());
        }
        if (StringUtils.isNotBlank(user.getCity())) {
            profileWeChatDO.setCity(user.getCity());
        }
        if (StringUtils.isNotBlank(user.getCountry())) {
            profileWeChatDO.setCountry(user.getCountry());
        }
        if (StringUtils.isNotBlank(user.getProvince())) {
            profileWeChatDO.setProvince(user.getProvince());
        }
        if (StringUtils.isNotBlank(user.getHeadImgUrl())) {
            profileWeChatDO.setHeadimgurl(user.getHeadImgUrl());
        }
        if (StringUtils.isNotBlank(user.getUnionId())) {
            profileWeChatDO.setUnionId(user.getUnionId());
        }
        if (null != user.getSubscribe() && user.getSubscribe()) {
            profileWeChatDO.setSubscribe(user.getSubscribe());
        }
        if (null != user.getSubscribeTime() && user.getSubscribeTime() > 0) {
            profileWeChatDO.setSubscribeTime(user.getSubscribeTime());
        }

        ProfileWeChatDOExample qpExample = new ProfileWeChatDOExample();
        qpExample.createCriteria().andOpenidEqualTo(user.getOpenId());
        List<ProfileWeChatDO> profileWeChatDOs = profileWeChatDOMapper.selectByExample(qpExample);
        if (null != profileWeChatDO && profileWeChatDOs.size() > 0) {
            ProfileWeChatDOExample pExample = new ProfileWeChatDOExample();
            pExample.createCriteria().andIdEqualTo(profileWeChatDOs.get(0).getId());
            if (profileWeChatDOMapper.updateByExampleSelective(profileWeChatDO, pExample) < 1) {
                log.warn("update profileWechatDO failed. openId:" + user.getOpenId());
            }
        }
    }

    public boolean addWechatInfoByToken(WxMpUser weChatUserInfo, WxMpOAuth2AccessToken wxMpOAuth2AccessToken) {
        ProfileWeChatDO pweChatDO = new ProfileWeChatDO();
        pweChatDO.setCity(weChatUserInfo.getCity());
        pweChatDO.setCountry(weChatUserInfo.getCountry());
        pweChatDO.setDateCreated(new Date());
        pweChatDO.setLastUpdated(new Date());
        pweChatDO.setHeadimgurl(weChatUserInfo.getHeadImgUrl());
        pweChatDO.setNickname(EmojiFilter.filterEmoji(weChatUserInfo.getNickname()));
        pweChatDO.setOpenid(weChatUserInfo.getOpenId());
        pweChatDO.setStatus(1);
        pweChatDO.setVersion(1l);
        pweChatDO.setSex(weChatUserInfo.getSex());
        pweChatDO.setProvince(weChatUserInfo.getProvince());
        if (null != wxMpOAuth2AccessToken) {
            pweChatDO.setAccessToken(wxMpOAuth2AccessToken.getAccessToken());
            pweChatDO.setExpiresIn(wxMpOAuth2AccessToken.getExpiresIn());
            pweChatDO.setRefreshToken(wxMpOAuth2AccessToken.getRefreshToken());
            pweChatDO.setOpenid(wxMpOAuth2AccessToken.getOpenId());
            pweChatDO.setScope(wxMpOAuth2AccessToken.getScope());
        } else {
            pweChatDO.setExpiresIn(wxMpOAuth2AccessToken.getExpiresIn());
            pweChatDO.setRefreshToken(wxMpOAuth2AccessToken.getRefreshToken());
            pweChatDO.setOpenid(wxMpOAuth2AccessToken.getOpenId());
            pweChatDO.setScope(wxMpOAuth2AccessToken.getScope());
        }

        pweChatDO.setUnionId(weChatUserInfo.getUnionId());
        pweChatDO.setSubscribe(weChatUserInfo.isSubscribe());
        pweChatDO.setLanguage(weChatUserInfo.getLanguage());
        pweChatDO.setSubscribeTime(weChatUserInfo.getSubscribeTime());
        //1. add wechat info
        if (profileWeChatDOMapper.insertSelective(pweChatDO) < 0) {
            log.error("insert wechat user info failed. nick:" + weChatUserInfo.getNickname() + ",openid:" + weChatUserInfo.getOpenId());
            return false;
        }
        return true;
    }

    /**
     * 通过手机号进行同步
     *
     * @param weChatUserInfo
     * @param session
     * @return
     */
    public boolean synchromProfileByMobile(WxMpUser weChatUserInfo, ProfileWeChatDO profileWeChatDO, Session session, ProfileDO profileDO) {
        if (synchroned(weChatUserInfo.getOpenId())) {
            session.setAttribute(BizConstants.PROFILE_ID, profileDO.getId());
            return true;
        }
        //是否已经关联
        long relaProfileId = fetchProfileIdByOpenid(weChatUserInfo.getOpenId());
        if (relaProfileId > 0) {
            session.setAttribute(BizConstants.PROFILE_ID, profileDO.getId());
            session.setAttribute(BizConstants.UNION_ID, weChatUserInfo.getUnionId());
            return true;
        } else {
            //直接进行关联
            WeChatProfileDO weChatProfileDO = new WeChatProfileDO();
            weChatProfileDO.setStatus((byte) 1);
            weChatProfileDO.setVersion(1l);
            weChatProfileDO.setDateCreated(new Date());
            weChatProfileDO.setProfileId(profileDO.getId());
            weChatProfileDO.setWechatId(profileWeChatDO.getId());
            weChatProfileDO.setLastUpdated(new Date());
            weChatProfileDO.setUnionId(weChatUserInfo.getUnionId());
            if (weChatProfileDOMapper.insertSelective(weChatProfileDO) < 0) {
                log.error("insert we chat profile relationship table failed.  nick:" + weChatUserInfo.getNickname() + ",openid:" + weChatUserInfo.getOpenId() + " sessionid:" + session.getId());
                profileDOMapper.deleteByPrimaryKey(profileDO.getId());
                return false;
            }
            session.setAttribute(BizConstants.PROFILE_ID, profileDO.getId());
            session.setAttribute(BizConstants.UNION_ID, weChatUserInfo.getUnionId());
            ProfileWeChatDO uProfileWechatDO = new ProfileWeChatDO();
            uProfileWechatDO.setSynchron((byte) 1);
            uProfileWechatDO.setId(profileWeChatDO.getId());
            uProfileWechatDO.setLastLoginTime(new Date());
            if (StringUtils.isNotBlank(weChatUserInfo.getUnionId())) {
                uProfileWechatDO.setUnionId(weChatUserInfo.getUnionId());
            }
            profileWeChatDOMapper.updateByPrimaryKeySelective(uProfileWechatDO);
        }
        return true;
    }

    /**
     * 该微信xxOpenidxx  -->  unionId是否已经关联过数据
     *
     * @param openid
     * @return
     */
    public boolean synchroned(String openid) {
        if (StringUtils.isBlank(openid)) {
            return false;
        }
        ProfileWeChatDOExample pExample = new ProfileWeChatDOExample();
        pExample.createCriteria().andOpenidEqualTo(openid);
        List<ProfileWeChatDO> profileWeChatDOs = profileWeChatDOMapper.selectByExample(pExample);
        if (null != profileWeChatDOs && profileWeChatDOs.size() > 0) {
            String unionId = profileWeChatDOs.get(0).getUnionId();
            WeChatProfileDOExample qwExample = new WeChatProfileDOExample();
            qwExample.createCriteria().andUnionIdEqualTo(unionId);
            int wechatCount = weChatProfileDOMapper.countByExample(qwExample);
            if (wechatCount > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 该第三方登陆用户是否已经关联了Profile
     *
     * @param userId
     * @return
     */
    public boolean coopSynchroned(String userId) {
        if (StringUtils.isBlank(userId)) {
            return false;
        }
        ProfileCoopDOExample pExample = new ProfileCoopDOExample();
        pExample.createCriteria().andUserIdEqualTo(userId);
        List<ProfileCoopDO> profileCoopDOs = profileCoopDOMapper.selectByExample(pExample);
        if (null != profileCoopDOs && profileCoopDOs.size() > 0) {
            long coopId = profileCoopDOs.get(0).getId();
            CoopProfileDOExample qcExample = new CoopProfileDOExample();
            qcExample.createCriteria().andCoopIdEqualTo(coopId);
            int coopRelationCount = coopProfileDOMapper.countByExample(qcExample);
            if (coopRelationCount > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 对于公众账户进来的用户，判断是否手机号方式已经登录
     *
     * @param openid
     * @param session
     * @return
     */
    public boolean isMobileUserLogedin(String openid, Session session) {
        if (null != session && null != session.getAttribute(BizConstants.PROFILE_ID)) {
            return true;
        }
        return false;
    }
    
    /**
     * 根据openid获取关联关系
     *
     * @param openid
     * @return
     */
    public long fetchProfileIdByOpenid(String openid) {
        ProfileWeChatDOExample pwExample = new ProfileWeChatDOExample();
        pwExample.createCriteria().andOpenidEqualTo(openid);
        List<ProfileWeChatDO> profileWeChatDOs = profileWeChatDOMapper.selectByExample(pwExample);
        if (null != profileWeChatDOs && profileWeChatDOs.size() > 0) {
            String unionId = profileWeChatDOs.get(0).getUnionId();
            WeChatProfileDOExample qwpExample = new WeChatProfileDOExample();
            qwpExample.createCriteria().andUnionIdEqualTo(unionId);
            List<WeChatProfileDO> weChatProfileDOs = weChatProfileDOMapper.selectByExample(qwpExample);
            if (null != weChatProfileDOs && weChatProfileDOs.size() > 0) {
                return weChatProfileDOs.get(0).getProfileId();
            } else {
                return -1l;
            }
        } else {
            return -1l;
        }
    }

    /**
     * 根据openid从数据库中获取用户的微信信息
     *
     * @param openid
     * @return
     */
    public ProfileWeChatDO fetchProfileWechatByOpenid(String openid) {
        ProfileWeChatDOExample pwExample = new ProfileWeChatDOExample();
        pwExample.createCriteria().andOpenidEqualTo(openid);
        List<ProfileWeChatDO> profileWeChatDOs = profileWeChatDOMapper.selectByExample(pwExample);
        if (null != profileWeChatDOs && profileWeChatDOs.size() > 0) {
            return profileWeChatDOs.get(0);
        } else {
            return null;
        }
    }

    /**
     * 添加微信用户信息
     *
     * @param weChatUserInfo
     * @return
     */
    public boolean addSyncWeChatUserByMock(WxMpUser weChatUserInfo, ProfileWeChatDO profileWeChatDO, Session session, String mobile) {
        if (synchroned(weChatUserInfo.getOpenId())) {
            return true;
        }
        //判断关联关系是否存在
        WeChatProfileDOExample qwExample = new WeChatProfileDOExample();
        qwExample.createCriteria().andWechatIdEqualTo(profileWeChatDO.getId());
        List<WeChatProfileDO> wpDOs = weChatProfileDOMapper.selectByExample(qwExample);
        if (null != wpDOs && wpDOs.size() > 0) {
            //如果原先未存储unionId,存储之
            WeChatProfileDO weChatProfileDO = new WeChatProfileDO();
            weChatProfileDO.setUnionId(wpDOs.get(0).getUnionId());
            WeChatProfileDOExample wExample = new WeChatProfileDOExample();
            wExample.createCriteria().andIdEqualTo(wpDOs.get(0).getId());
            weChatProfileDOMapper.updateByExampleSelective(weChatProfileDO, wExample);

            session.setAttribute(BizConstants.PROFILE_ID, wpDOs.get(0).getProfileId());
            session.setAttribute(BizConstants.UNION_ID, weChatUserInfo.getUnionId());
            profileExtService.updateLastLogin(wpDOs.get(0).getProfileId());
            return true;
        } else {
            //添加profile并建立对应关系
            //2. add profile
            long profileWchatId = -1l;
            ProfileDO profileDO = new ProfileDO();
            profileDO.setDateCreated(new Date());
            profileDO.setLastUpdated(new Date());
            profileDO.setProfilePic(weChatUserInfo.getHeadImgUrl());
            profileDO.setType(BizConstants.LoginEnum.SERVICE.getType());
            profileDO.setStatus((byte) 1);
            profileDO.setMobile(mobile);
            String checkCode = CheckNOGenerator.getFixLenthString(BizConstants.CHECK_NO_LEN);
    		if(null != mobile && mobile.length() > 4){
    			profileDO.setNickname(mobile.substring(mobile.length()-4, mobile.length())+checkCode);
    		}else{
    			profileDO.setNickname(CheckNOGenerator.getFixLenthString(6));
    		}
            if (profileDOMapper.insertSelective(profileDO) < 0) {
                log.error("insert we chat profile table failed.  nick:" + weChatUserInfo.getNickname() + ",openid:" + weChatUserInfo.getOpenId() + " sessionid:" + session.getId());
                ProfileWeChatDOExample pExample = new ProfileWeChatDOExample();
                pExample.createCriteria().andOpenidEqualTo(weChatUserInfo.getOpenId());
                List<ProfileWeChatDO> profileWeChatDOs = profileWeChatDOMapper.selectByExample(pExample);
                if (null != profileWeChatDOs && profileWeChatDOs.size() > 0) {
                    profileWchatId = profileWeChatDOs.get(0).getId();
                    profileWeChatDOMapper.deleteByPrimaryKey(profileWchatId);
                }
                return false;
            } else {
                //3. 添加对应关系
                WeChatProfileDO weChatProfileDO = new WeChatProfileDO();
                weChatProfileDO.setStatus((byte) 1);
                weChatProfileDO.setVersion(1l);
                weChatProfileDO.setDateCreated(new Date());
                weChatProfileDO.setProfileId(profileDO.getId());
                weChatProfileDO.setWechatId(profileWeChatDO.getId());
                weChatProfileDO.setLastUpdated(new Date());
                weChatProfileDO.setUnionId(weChatUserInfo.getUnionId());
                if (weChatProfileDOMapper.insertSelective(weChatProfileDO) < 0) {
                    log.error("insert we chat profile relationship table failed.  nick:" + weChatUserInfo.getNickname() + ",openid:" + weChatUserInfo.getOpenId());
                    profileDOMapper.deleteByPrimaryKey(profileDO.getId());
                    return false;
                }
                session.setAttribute(BizConstants.PROFILE_ID, profileDO.getId());
                session.setAttribute(BizConstants.UNION_ID, weChatUserInfo.getUnionId());
                profileExtService.updateLastLogin(profileDO.getId());
                ProfileWeChatDO uProfileWechatDO = new ProfileWeChatDO();
                uProfileWechatDO.setSynchron((byte) 1);
                uProfileWechatDO.setId(profileWeChatDO.getId());
                uProfileWechatDO.setLastLoginTime(new Date());
                profileWeChatDOMapper.updateByPrimaryKeySelective(uProfileWechatDO);
            }
        }
        return true;
    }

    public ProfileDO fetchProfileById(long profileId) {
        return profileDOMapper.selectByPrimaryKey(profileId);
    }

    /**
     * 根据电话号码设置
     *
     * @param mobile
     * @return
     */
    public ProfileDO fetchProfileByMobile(String mobile) {
        ProfileDOExample pExample = new ProfileDOExample();
        pExample.createCriteria().andMobileEqualTo(mobile).andStatusEqualTo((byte) 1);
        List<ProfileDO> profileDOs = profileDOMapper.selectByExample(pExample);
        if (null != profileDOs && profileDOs.size() > 0) {
            return profileDOs.get(0);
        }
        return null;
    }

    /**
     * 手机号是否有账户
     *
     * @param mobile
     * @return
     */
    public boolean hasMobileAccount(String mobile) {
        boolean hasAccount = false;
        if (StringUtils.isNotBlank(mobile)) {
            if (null != fetchProfileByMobile(mobile)) {
                hasAccount = true;
            }
        }
        return hasAccount;
    }

    /**
     * 判断是否具有某一业务的推送消息配置
     *
     * @param profileId
     * @param communityId
     * @param buildingId
     * @param userNotifyTagEnum
     * @return
     */
    public boolean checkUserNotifyConf(long profileId, long communityId, long buildingId, BizConstants.UserNotifyTagEnum userNotifyTagEnum) {
        NotifyConfigDOExample example = new NotifyConfigDOExample();
        example.createCriteria().andBuildingIdEqualTo(buildingId).andCommunityIdEqualTo(communityId).andProfileIdEqualTo(profileId);
        List<NotifyConfigDO> configDOs = notifyConfigDOMapper.selectByExample(example);
        if (null != configDOs && configDOs.size() > 0) {
            long tag = configDOs.get(0).getNotifyTag();
            if (containsConfig(tag, userNotifyTagEnum)) {
                return true;
            } else {
                return false;
            }
        } else {//如果用户没有设置过消息，默认全部都接收
            return true;
        }
    }

    /**
     * 设置消息配置，有则更新，无则插入
     *
     * @param profileId
     * @param communityId
     * @param buildingId
     * @param msgTag
     */
    public void updateMsgTag(long profileId, long communityId, long buildingId, long msgTag) {
        NotifyConfigDO notify = new NotifyConfigDO();
        notify.setProfileId(profileId);
        notify.setBuildingId(buildingId);
        notify.setCommunityId(communityId);
        notify.setNotifyTag(msgTag);
        notify.setLastUpdated(new Date());
        //是否存在
        NotifyConfigDOExample nExample = new NotifyConfigDOExample();
        nExample.createCriteria().andProfileIdEqualTo(profileId).andBuildingIdEqualTo(buildingId)
                .andCommunityIdEqualTo(communityId);
        List<NotifyConfigDO> notifyConfigDOs = notifyConfigDOMapper.selectByExample(nExample);
        if (null != notifyConfigDOs && notifyConfigDOs.size() > 0) {
            //update
            if (notifyConfigDOMapper.updateByExampleSelective(notify, nExample) < 1) {
                log.error("update notify msg configration failed. profileId:" + profileId + ",buildingId:" + buildingId + ",communityId:" + communityId + ",tag" + msgTag);
            }
        } else {
            //insert
            notify.setDateCreated(new Date());
            if (notifyConfigDOMapper.insertSelective(notify) < 1) {
                log.error("insert notify msg configration failed. profileId:" + profileId + ",buildingId:" + buildingId + ",communityId:" + communityId + ",tag" + msgTag);
            }
        }
    }

    /**
     * 查询用户业务消息的配置数据
     *
     * @param profileId
     * @param communityId
     */
    public long fetchMsgTag(long profileId, long communityId) {
        NotifyConfigDO notify = new NotifyConfigDO();
        notify.setProfileId(profileId);
        notify.setBuildingId(profileId);
        notify.setCommunityId(communityId);
        notify.setLastUpdated(new Date());
        //是否存在
        NotifyConfigDOExample nExample = new NotifyConfigDOExample();
        nExample.createCriteria().andProfileIdEqualTo(profileId)
                .andCommunityIdEqualTo(communityId);
        List<NotifyConfigDO> notifyConfigDOs = notifyConfigDOMapper.selectByExample(nExample);
        if (null != notifyConfigDOs && notifyConfigDOs.size() > 0) {
            return notifyConfigDOs.get(0).getNotifyTag();
        }
        return 65535l;
    }

    /**
     * 获取用户未读消息数量
     *
     * @param profileId
     * @param communityId
     * @return
     */
    public int fetchMsgCount(long profileId, long communityId) {

        MessageSummaryDOExample mExample = new MessageSummaryDOExample();
        mExample.createCriteria().andProfileIdEqualTo(profileId).andCommunityIdEqualTo(communityId)
                .andBizTypeIn(BizConstants.AllBizTypes);
        List<MessageSummaryDO> messageSummaryDOs = messageSummaryDOMapper.selectByExample(mExample);
        int count = 0;
        for (MessageSummaryDO m : messageSummaryDOs) {
            count += m.getNonReadCount();
        }
        return count;
    }

    /**
     * 根据用户权限查询成员信息
     *
     * @param profileId
     * @return
     */
    public List<UserInfo> fetchValidUsersByCommunityAndProfileId(long profileId) {
        //fetch users(tenant)
        List<UserInfo> users = new ArrayList<UserInfo>();
        ProfileDOExample pExample = new ProfileDOExample();
        pExample.createCriteria().andIdEqualTo(profileId).andStatusEqualTo((byte) 1);
        List<ProfileDO> profileDOs = profileDOMapper.selectByExample(pExample);
        if (null != profileDOs && profileDOs.size() > 0) {
            UserInfo userInfo = new UserInfo();
            userInfo.setProfileDO(profileDOs.get(0));
            users.add(userInfo);
            return users;
        } else {
            return null;
        }
    }

    public String list2Json(List list) {
        if (null == list || (list != null && list.size() > 0)) {
            return null;
        }
        String jsonText = JSON.toJSONString(list, true);
        return jsonText;
    }

    private boolean containsConfig(long tag, BizConstants.UserNotifyTagEnum userNotifyTagEnum) {
        long subTag = userNotifyTagEnum.getUserNotifyTagId();
        return (tag & subTag) == subTag;
    }

    /**
     * 是否已经关联过union_id
     *
     * @param openid
     */
    public boolean isOauthed(String openid) {
        ProfileWeChatDOExample qpExample = new ProfileWeChatDOExample();
        qpExample.createCriteria().andOpenidEqualTo(openid);
        List<ProfileWeChatDO> profileWeChatDOs = profileWeChatDOMapper.selectByExample(qpExample);
        if (null != profileWeChatDOs && profileWeChatDOs.size() > 0) {
            for (ProfileWeChatDO pDO : profileWeChatDOs) {//有存储过微信信息且已经获取过union_id
                if (StringUtils.isNotBlank(pDO.getUnionId())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 根据openid获取用户微信信息
     *
     * @param openid
     * @return
     */
    public ProfileWeChatDO fetchWxInfo(String openid) {
        ProfileWeChatDOExample qpExample = new ProfileWeChatDOExample();
        qpExample.createCriteria().andOpenidEqualTo(openid);
        List<ProfileWeChatDO> profileWeChatDOs = profileWeChatDOMapper.selectByExample(qpExample);
        if (null != profileWeChatDOs && profileWeChatDOs.size() > 0) {
            return profileWeChatDOs.get(0);
        }
        return null;
    }

    /**
     * 判断联合登陆是否已经保存过用户信息
     *
     * @param loginType
     * @param openid
     * @return
     */
    public boolean isCoopInfoStored(String loginType, String openid) {
        ProfileCoopDOExample qpExample = new ProfileCoopDOExample();
        qpExample.createCriteria().andOpenidEqualTo(openid).andTypeEqualTo(Byte.valueOf(loginType));
        List<ProfileCoopDO> profileCoopDOs = profileCoopDOMapper.selectByExample(qpExample);
        if (null != profileCoopDOs && profileCoopDOs.size() > 0) {
            return true;
        }
        return false;
    }

    /**
     * 获取profile coop by union id
     *
     * @param loginType
     * @param unionId
     * @return
     */
    public ProfileCoopDO fetchProfileCoopByUnionId(String loginType, String unionId) {
        ProfileCoopDOExample qpExample = new ProfileCoopDOExample();
        qpExample.createCriteria().andUnionIdEqualTo(unionId).andTypeEqualTo(Byte.valueOf(loginType));
        List<ProfileCoopDO> profileCoopDOs = profileCoopDOMapper.selectByExample(qpExample);
        if (null != profileCoopDOs && profileCoopDOs.size() > 0) {
            return profileCoopDOs.get(0);
        }
        return null;
    }

    /**
     * 根据支付宝userID判断是否已经存储过支付宝用户信息
     *
     * @param loginType
     * @param userId
     * @return
     */
    public ProfileCoopDO fetchProfileCoopByAlipayUserId(String loginType, String userId) {
        ProfileCoopDOExample qpExample = new ProfileCoopDOExample();
        qpExample.createCriteria().andUserIdEqualTo(userId).andTypeEqualTo(Byte.valueOf(loginType));
        List<ProfileCoopDO> profileCoopDOs = profileCoopDOMapper.selectByExample(qpExample);
        if (null != profileCoopDOs && profileCoopDOs.size() > 0) {
            return profileCoopDOs.get(0);
        }
        return null;
    }


    /**
     * 添加联合登陆之用户信息
     *
     * @param loginType
     * @param wxInfoModel
     * @return
     */
    public boolean addCoopInfo(String loginType, WxInfoModel wxInfoModel, Session session) {
        ProfileCoopDO profileCoopDO = new ProfileCoopDO();
        profileCoopDO.setCity(wxInfoModel.getCity());
        profileCoopDO.setCountry(wxInfoModel.getCountry());
        profileCoopDO.setHeadimgurl(wxInfoModel.getHeadimgurl());
        profileCoopDO.setLanguage(wxInfoModel.getLanguage());
        profileCoopDO.setNickname(EmojiFilter.filterEmoji(wxInfoModel.getNickname()));
        profileCoopDO.setOpenid(wxInfoModel.getOpenid());
        profileCoopDO.setSex(wxInfoModel.getSex());
        profileCoopDO.setUnionId(wxInfoModel.getUnionid());
        profileCoopDO.setVersion(1l);
        profileCoopDO.setDateCreated(new Date());
        profileCoopDO.setLastUpdated(new Date());
        profileCoopDO.setType(Byte.valueOf(loginType));
        profileCoopDO.setStatus(1);

        if (profileCoopDOMapper.insertSelective(profileCoopDO) < 0) {
            log.error("add wx user info failed. nick:" + wxInfoModel.getNickname() + ",openid:" + wxInfoModel.getOpenid());
            return false;
        }
        return true;
    }

    /**
     * 添加联合登陆之用户信息
     *
     * @param loginType
     * @param alipayInfoModel
     * @return
     */
    public boolean addCoopInfo(String loginType, AlipayInfoModel alipayInfoModel, Session session) {
        ProfileCoopDO profileCoopDO = new ProfileCoopDO();
        profileCoopDO.setUserId(alipayInfoModel.getUserId());
        profileCoopDO.setVersion(1l);
        profileCoopDO.setDateCreated(new Date());
        profileCoopDO.setLastUpdated(new Date());
        profileCoopDO.setType(Byte.valueOf(loginType));
        profileCoopDO.setStatus(1);

        if (profileCoopDOMapper.insertSelective(profileCoopDO) < 0) {
            log.error("add alipay user info failed. userId:" + alipayInfoModel.getUserId());
            return false;
        }
        return true;
    }

    /**
     * 关联用户(如果用户对应的手机已经注册过app，则直接进行关联；否则Q用户并进行关联)
     *
     * @param mobile
     * @param profileCoopDO
     * @param loginType
     * @return
     */
    public boolean relaUser(String mobile, ProfileCoopDO profileCoopDO, String loginType, Session session) {
        ProfileDO profileDO = fetchProfileByMobile(mobile);
        //1. 存在profile 用户
        if (null != profileDO) {
            //1.1 存在用户进行关联
            return relaUser(mobile, loginType, profileDO, profileCoopDO, session);
        }
        //2. 不存在profile 用户
        else {
            //2.1 Q profile
            /*ProfileDO qProfileDO = new ProfileDO();
            qProfileDO.setMobile(mobile);
            qProfileDO.setStatus((byte) 1);
            qProfileDO.setDateCreated(new Date());
            qProfileDO.setLastUpdated(new Date());
//            qProfileDO.setLemonName(profileCoopDO.getNickname());
//            qProfileDO.setNickname(profileCoopDO.getNickname());
            qProfileDO.setType(Byte.valueOf(loginType));
            qProfileDO.setProfilePic(profileCoopDO.getHeadimgurl());
            if (profileDOMapper.insertSelective(qProfileDO) < 0) {
                log.error("add Q profile info failed. mobile:" + mobile);
                return false;
            }*/
        	boolean isProfileTemp = false;
        	String parentUserId = "0";
        	ProfileTempDOExample profileTempDOExample = new ProfileTempDOExample();
        	profileTempDOExample.createCriteria().andMobileEqualTo(mobile).andStatusEqualTo((byte)1);
        	List<ProfileTempDO> profileTempDOList = profileTempDOMapper.selectByExample(profileTempDOExample);
        	if(!profileTempDOList.isEmpty()){
        		Long pid = (null == profileTempDOList.get(0).getPid() ? 0L : profileTempDOList.get(0).getPid());
        		parentUserId = String.valueOf(pid);
        		isProfileTemp = true;	//ProfileTemp（用户临时表）是否有此电话号码数据，（true=有；false=无）
        	}
            profileDO = mikuUserAgencyService.addProfileAndAgency(-1, mobile, null, (byte)0, parentUserId, (byte)0);	//建立代理关系
            if(null == profileDO){
            	log.error("add Q profile info failed. mobile:" + mobile);
                return false;
            }else if(null != profileDO && profileDO.getId() < 1){
            	log.error("add Q profile info failed. mobile:" + mobile);
                return false;
            }
            if(isProfileTemp){		//ProfileTemp（用户临时表）有此电话数据并用户添加成功，则执行以下
             	ProfileTempDO profileTempDO = new ProfileTempDO();
             	profileTempDO.setStatus((byte)2);
             	if(profileTempDOMapper.updateByExampleSelective(profileTempDO, profileTempDOExample) < 1){
             		 log.error("更新Profile_Temp（用户临时表） failed. mobile:" + mobile);
             	}
             }
            //2.2 关联用户
            ProfileDOExample qqExample = new ProfileDOExample();
            qqExample.createCriteria().andMobileEqualTo(mobile).andStatusEqualTo((byte) 1);
            List<ProfileDO> profileDOs = profileDOMapper.selectByExample(qqExample);
            if (null != profileDOs && profileDOs.size() > 0) {
                return relaUser(mobile, loginType, profileDOs.get(0), profileCoopDO, session);
            } else {
                return false;
            }
        }
    }

    public boolean bindWxByProfileIdAndOpenId(long profileId, String openId) {
        ProfileWeChatDOExample pExample = new ProfileWeChatDOExample();
        pExample.createCriteria().andOpenidEqualTo(openId);
        List<ProfileWeChatDO> profileWeChatDOs = profileWeChatDOMapper.selectByExample(pExample);
        if (null != profileWeChatDOs && profileWeChatDOs.size() > 0) {
            WeChatProfileDO weChatProfileDO = new WeChatProfileDO();
            weChatProfileDO.setLastUpdated(new Date());
            weChatProfileDO.setUnionId(profileWeChatDOs.get(0).getUnionId());
            weChatProfileDO.setWechatId(profileWeChatDOs.get(0).getId());
            weChatProfileDO.setProfileId(profileId);
            weChatProfileDO.setDateCreated(new Date());
            weChatProfileDO.setStatus((byte) 1);
            weChatProfileDO.setVersion(1l);
            weChatProfileDOMapper.insertSelective(weChatProfileDO);
            return true;
        }
        return false;
    }

    private boolean relaUser(String mobile, String loginType, ProfileDO profileDO, ProfileCoopDO profileCoopDO, Session session) {
        CoopProfileDO coopProfileDO = new CoopProfileDO();
        coopProfileDO.setType(Byte.valueOf(loginType));
        coopProfileDO.setVersion(1l);
        coopProfileDO.setCoopId(profileCoopDO.getId());
        coopProfileDO.setProfileId(profileDO.getId());
        coopProfileDO.setDateCreated(new Date());
        coopProfileDO.setLastUpdated(new Date());
        coopProfileDO.setStatus((byte) 1);
        if (coopProfileDOMapper.insertSelective(coopProfileDO) < 0) {
            log.error("add coop profile relationship failed. mobile:" + mobile);
            return false;
        }
        session.setAttribute(BizConstants.MOBILE, mobile);
        session.setAttribute(BizConstants.PROFILE_ID, profileDO.getId());
        profileExtService.updateLastLogin(profileDO.getId());
        return true;
    }

    /**
     * wx 是否关联过profileCoop -- profile
     *
     * @return
     */
    public boolean relatedCheck(ProfileCoopDO profileCoopDO, Session session) {
        CoopProfileDOExample cExample = new CoopProfileDOExample();
        cExample.createCriteria().andCoopIdEqualTo(profileCoopDO.getId());
        List<CoopProfileDO> coopProfileDOs = coopProfileDOMapper.selectByExample(cExample);
        if (null != coopProfileDOs && coopProfileDOs.size() > 0) {
            session.setAttribute(BizConstants.PROFILE_ID, coopProfileDOs.get(0).getProfileId());
            profileExtService.updateLastLogin(coopProfileDOs.get(0).getProfileId());
            return true;
        }
        return false;
    }

    /**
     * 是否已经绑定过
     *
     * @param loginType
     * @param
     * @param profileCoopId
     * @return
     */
    public long hasBindUser(String loginType, long profileCoopId, Session session) {
        CoopProfileDOExample qcExample = new CoopProfileDOExample();
        qcExample.createCriteria().andTypeEqualTo(Byte.valueOf(loginType)).andCoopIdEqualTo(profileCoopId);
        List<CoopProfileDO> coopProfileDOs = coopProfileDOMapper.selectByExample(qcExample);
        if (null != coopProfileDOs && coopProfileDOs.size() > 0) {
            session.setAttribute(BizConstants.PROFILE_ID, coopProfileDOs.get(0).getProfileId());
            profileExtService.updateLastLogin(coopProfileDOs.get(0).getProfileId());
            return coopProfileDOs.get(0).getProfileId();
        }
        return -1;
    }

    /**
     * 判断unionid 是否已经绑定过profile
     *
     * @param unionId
     * @return profileId
     */
    public long checkWxMpBinded(String unionId) {
        WeChatProfileDOExample qExample = new WeChatProfileDOExample();
        qExample.createCriteria().andUnionIdEqualTo(unionId);
        List<WeChatProfileDO> weChatProfileDOs = weChatProfileDOMapper.selectByExample(qExample);
        if (null != weChatProfileDOs && weChatProfileDOs.size() > 0) {
            return weChatProfileDOs.get(0).getProfileId();
        }
        return -1l;
    }

    public long checkWxMpBinded(long profileId) {
        WeChatProfileDOExample qExample = new WeChatProfileDOExample();
        qExample.createCriteria().andProfileIdEqualTo(profileId);
        List<WeChatProfileDO> weChatProfileDOs = weChatProfileDOMapper.selectByExample(qExample);
        if (null != weChatProfileDOs && weChatProfileDOs.size() > 0) {
            return weChatProfileDOs.get(0).getProfileId();
        }
        return -1l;
    }

    /**
     * 添加profile
     *
     * @param mobile
     * @return
     */
    public boolean addProfile(String mobile) {
        ProfileDO profileDO = new ProfileDO();
        profileDO.setType(BizConstants.LoginEnum.H5_MOBILE_CODE.getType());
        profileDO.setDateCreated(new Date());
        profileDO.setLastUpdated(new Date());
        profileDO.setStatus((byte) 1);
        profileDO.setMobile(mobile);
        String checkCode = CheckNOGenerator.getFixLenthString(BizConstants.CHECK_NO_LEN);
		if(null != mobile && mobile.length() > 4){
			profileDO.setNickname(mobile.substring(mobile.length()-4, mobile.length())+checkCode);
		}else{
			profileDO.setNickname(CheckNOGenerator.getFixLenthString(6));
		}
        if (profileDOMapper.insertSelective(profileDO) > 0) {
            return true;
        }
        return false;
    }

    public long fetchLastLoginShop(Session session) {
        if (null != session && null != session.getAttribute(BizConstants.SHOP_ID)) {
            return (long) session.getAttribute(BizConstants.SHOP_ID);
        } else {

        }
        return 0;
    }
    
    public ProfileWeChatDO getProfileWeChatByProfileId(Long profileId){
    	try {
    		WeChatProfileDOExample weChatProfileDOExample = new WeChatProfileDOExample();
    		weChatProfileDOExample.createCriteria().andProfileIdEqualTo(profileId);
    		List<WeChatProfileDO> weChatProfileDOList = weChatProfileDOMapper.selectByExample(weChatProfileDOExample);
    		if(null != weChatProfileDOList && !weChatProfileDOList.isEmpty()){
    			return profileWeChatDOMapper.selectByPrimaryKey(weChatProfileDOList.get(0).getWechatId());
    		}
		} catch (Exception e) {
		}
    	return null;
    }
    
}
