package com.welink.biz.service;

import com.welink.biz.common.MSG.PushApiService;
import com.welink.biz.common.MSG.SMSUtils;
import com.welink.buy.utils.PhenixUserHander;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.ProfileDO;
import com.welink.commons.domain.ProfileDOExample;
import com.welink.commons.persistence.ProfileDOMapper;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by daniel on 14-9-29.
 */
@Service(value = "pushService")
public class PushServiceImpl implements PushService {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(PushServiceImpl.class);

    @Resource
    private MessageService messageService;

    @Resource
    private UserService userService;

    @Resource
    private PushApiService pushApiService;

    @Resource
    private ProfileDOMapper profileDOMapper;

    @Override
    public String pushMsg(BizConstants.PushActionEnum action, String alertContent, String url, long profileId, long bizType, long tradeId) {
        ProfileDOExample profileDOExample = new ProfileDOExample();
        profileDOExample.createCriteria().andIdEqualTo(profileId);
        ProfileDO profileDO = profileDOMapper.selectByPrimaryKey(profileId);
        boolean isPre = false;
        if (null != profileDO && profileDO.getDiploma() == 1) {
            isPre = true;
        }
        String userId = fetchBase64Id(profileId);
        return pushApiService.pushMsg(String.valueOf(action.getAction()), alertContent, url, userId, null, bizType, isPre, tradeId);
    }

    @Override
    public String pushLinkMsg(BizConstants.PushActionEnum action, long profileId, String alertContent, long bizType, long linkMsgCnt) {
        String userId = fetchBase64Id(profileId);
        ProfileDOExample profileDOExample = new ProfileDOExample();
        profileDOExample.createCriteria().andIdEqualTo(profileId);
        ProfileDO profileDO = profileDOMapper.selectByPrimaryKey(profileId);
        boolean isPre = false;
        if (null != profileDO && profileDO.getDiploma() == 1) {
            isPre = true;
        }
        return pushApiService.pushLinkMsg(String.valueOf(action.getAction()), userId, alertContent, bizType, linkMsgCnt, isPre);
    }

    @Override
    public String pushLinkMsgWithoutAlert(BizConstants.PushActionEnum action, long profileId, long linkMsgCnt) {
        String userId = fetchBase64Id(profileId);
        ProfileDOExample profileDOExample = new ProfileDOExample();
        profileDOExample.createCriteria().andIdEqualTo(profileId);
        ProfileDO profileDO = profileDOMapper.selectByPrimaryKey(profileId);
        boolean isPre = false;
        if (null != profileDO && profileDO.getDiploma() == 1) {
            isPre = true;
        }
        return pushApiService.pushMsgWithoutAlert(String.valueOf(action.getAction()), userId, linkMsgCnt, isPre);
    }

    @Override
    public String pushMsg2CommunityMembers(BizConstants.PushActionEnum action, String content, String url, long communityId, String title, long bizType) {
        String userId = fetchBase64Id(communityId);
        return pushApiService.pushMsg2CommunityMembers(String.valueOf(action.getAction()), content, url, userId, title, bizType, -1, false);
    }

    @Override
    public String pushMsg2Channel(BizConstants.PushActionEnum action, String contentDetail, String url, String communityId, String title, long bizType) {
        return pushApiService.pushMsg2Channel(String.valueOf(action.getAction()), contentDetail, url, communityId, title, bizType, false);
    }

    /*----------------------------------------------------------------------------------------------------------------*/
    //推送消息并记录消息内容

    @Override
    public String pushMsgAndStoreMsgDetail(BizConstants.PushActionEnum action, long bizType, String content, String url, long profileId, long communityId, long buildingId, String contentDetail, long tradeId) {
        //1. 记录content
        messageService.storeMsg(profileId, buildingId, communityId, bizType, contentDetail, tradeId);
        // boolean config = checkMsgConfig(profileId,communityId,buildingId,bizType);
//        if (!config){
//            return "no config for push service with this biz type ";
//        }
        //2. 推送消息
        String userId = fetchBase64Id(profileId);
        ProfileDOExample profileDOExample = new ProfileDOExample();
        profileDOExample.createCriteria().andIdEqualTo(profileId);
        ProfileDO profileDO = profileDOMapper.selectByPrimaryKey(profileId);
        boolean isPre = false;
        if (null != profileDO && profileDO.getDiploma() == 1) {
            isPre = true;
        }
        return pushApiService.pushMsg(String.valueOf(action.getAction()), content, url, userId, null, bizType, isPre, tradeId);
    }

    @Override
    public String pushMsg2ChannelAndStoreMsg(BizConstants.PushActionEnum action, long bizType, String content, String url, long communityId, long buildingId, String contentDetail, long tradeId) {
        //TODO:目前业务没有这种存储推送，保留
        return null;
    }

    private boolean checkMsgConfig(long profileId, long communityId, long buildingId, long bizType) {
        BizConstants.UserNotifyTagEnum userNotifyTagEnum = BizConstants.UserNotifyTagEnum.getUserNotifyTag(bizType);
        if (null != userNotifyTagEnum) {
            return userService.checkUserNotifyConf(profileId, communityId, buildingId, userNotifyTagEnum);
        }
        return true;
    }

    @Override
    public boolean sendSMS(String msg, String mobile) {
        return SMSUtils.sendSms(msg, mobile);
    }

    private String fetchBase64Id(long id) {
        return PhenixUserHander.encodeUserId(id);
    }

    public static void main(String[] args) {
        String userId = PhenixUserHander.encodeUserId(38L);
        System.out.println(userId);
//        String userId = fetchBase64Id(3l);
//        System.out.println(userId);
    }

}
