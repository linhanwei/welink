package com.welink.biz.service;

import com.daniel.weixin.common.util.StringUtils;
import com.google.common.collect.Lists;
import com.welink.commons.domain.*;
import com.welink.commons.persistence.ProfileExtDOMapper;
import com.welink.commons.persistence.ProfileWeChatDOMapper;
import com.welink.commons.persistence.WeChatProfileDOMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * Created by daniel on 14-9-24.
 */
@Service
public class ProfileExtService {

    private static Logger log = LoggerFactory.getLogger(ProfileExtService.class);

    @Resource
    private ProfileExtDOMapper profileExtDOMapper;

    @Resource
    private WeChatProfileDOMapper weChatProfileDOMapper;

    @Resource
    private ProfileWeChatDOMapper profileWeChatDOMapper;

    /**
     * 更新最后登录时间
     *
     * @param profileId
     * @return
     */
    public boolean updateLastLogin(long profileId) {
        ProfileExtDO profileExtDO = new ProfileExtDO();
        profileExtDO.setLoginTime(new Date());
        ProfileExtDOExample uExample = new ProfileExtDOExample();
        uExample.createCriteria().andProfileIdEqualTo(profileId);
        profileExtDOMapper.updateByExampleSelective(profileExtDO, uExample);
        //更新微信公众好用户登录时间
        WeChatProfileDOExample qExample = new WeChatProfileDOExample();
        qExample.createCriteria().andProfileIdEqualTo(profileId);
        List<WeChatProfileDO> weChatProfileDOs = weChatProfileDOMapper.selectByExample(qExample);
        if (null != weChatProfileDOs && weChatProfileDOs.size() > 0) {
            long wechatId = weChatProfileDOs.get(0).getWechatId();
            ProfileWeChatDOExample qpExample = new ProfileWeChatDOExample();
            qpExample.createCriteria().andIdEqualTo(wechatId);
            ProfileWeChatDO uProfileWechatDO = new ProfileWeChatDO();
            uProfileWechatDO.setLastLoginTime(new Date());
            profileWeChatDOMapper.updateByExampleSelective(uProfileWechatDO, qpExample);
        }
        return true;
    }

    /**
     * 更新最后登录时间
     *
     * @param profileId
     * @return
     */
    public boolean updateLastLoginProfile(long profileId) {
        ProfileExtDO profileExtDO = new ProfileExtDO();
        profileExtDO.setLoginTime(new Date());
        ProfileExtDOExample uExample = new ProfileExtDOExample();
        uExample.createCriteria().andProfileIdEqualTo(profileId);
        profileExtDOMapper.updateByExampleSelective(profileExtDO, uExample);
        return true;
    }

    /**
     * asyncly update users device info
     *
     * @param profileExtDO
     * @param profileExtDOExample
     * @return
     */
    @Async
    public void updateProfileExt(ProfileExtDO profileExtDO, ProfileExtDOExample profileExtDOExample) {
        List<ProfileExtDO> profileExtDOs = profileExtDOMapper.selectByExample(profileExtDOExample);
        if (null != profileExtDOs && profileExtDOs.size() > 0) {
            if (profileExtDOs.get(0).getUseTimes() != null) {
                profileExtDO.setUseTimes(profileExtDOs.get(0).getUseTimes() + 1);
            } else {
                profileExtDO.setUseTimes(1L);
            }
            profileExtDOMapper.updateByExampleSelective(profileExtDO, profileExtDOExample);
        } else {//no profileExt info insert
            profileExtDO.setUseTimes(1L);
            profileExtDOMapper.insert(profileExtDO);
        }
    }

    public void updateOpenId(String openId, long profileId) {
        WeChatProfileDOExample qExample = new WeChatProfileDOExample();
        qExample.createCriteria().andProfileIdEqualTo(profileId);
        List<WeChatProfileDO> profileDOs = weChatProfileDOMapper.selectByExample(qExample);
        if (null != profileDOs && profileDOs.size() > 0) {
            List<Long> ids = Lists.newArrayList();
            for (WeChatProfileDO weChatProfileDO : profileDOs) {
                ids.add(weChatProfileDO.getWechatId());
            }
            if (ids.size() > 0 && StringUtils.isNotBlank(openId)) {
                for (Long id : ids) {
                    ProfileWeChatDO profileWeChatDO = new ProfileWeChatDO();
                    profileWeChatDO.setOpenid(openId);
                    profileWeChatDO.setLastUpdated(new Date());
                    ProfileWeChatDOExample uExample = new ProfileWeChatDOExample();
                    uExample.createCriteria().andIdEqualTo(id);
                    profileWeChatDOMapper.updateByExampleSelective(profileWeChatDO, uExample);
                }
            }
        }
    }
}
