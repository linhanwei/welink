package com.welink.biz.service;

import com.welink.commons.commons.BizConstants;

/**
 * Created by daniel on 14-9-28.
 */
public interface PushService {

    /**
     * 推送消息给某个用户 (根据userId)
     *
     * @param action
     * @param content
     * @param url
     * @param profileId
     * @return
     */
    public String pushMsg(BizConstants.PushActionEnum action, String content, String url, long profileId, long bizType, long tradeId);

    /**
     * 推送消息给邻里消息发布者
     *
     * @param action
     * @param profileId
     * @param bizType
     * @param linkMsgCnt
     * @return
     */
    public String pushLinkMsg(BizConstants.PushActionEnum action, long profileId, String content, long bizType, long linkMsgCnt);

    /**
     * 推送消息不弹出横幅
     *
     * @param action
     * @param profileId
     * @param linkMsgCnt
     * @return
     */
    public String pushLinkMsgWithoutAlert(BizConstants.PushActionEnum action, long profileId, long linkMsgCnt);


    /**
     * 保存详细消息并推送消息
     *
     * @param action
     * @param bizType
     * @param content
     * @param url
     * @param profileId
     * @param communityId
     * @param buildingId
     * @param contentDetail
     * @return
     */
    public String pushMsgAndStoreMsgDetail(BizConstants.PushActionEnum action, long bizType, String content, String url, long profileId, long communityId, long buildingId, String contentDetail, long tradeId);

    /**
     * 推送消息给某个channel的用户 （主要用于推送消息给特定小区用户）
     *
     * @param action
     * @param content
     * @param url
     * @param communityId
     * @param title
     * @return
     */
    public String pushMsg2CommunityMembers(BizConstants.PushActionEnum action, String content, String url, long communityId, String title, long bizType);

    /**
     * 根据community属性推送给特定小区的人
     *
     * @param action
     * @param content
     * @param url
     * @param communityId
     * @param title
     * @return
     */
    public String pushMsg2Channel(BizConstants.PushActionEnum action, String content, String url, String communityId, String title, long bizType);

    /**
     * @param action
     * @param bizType
     * @param content
     * @param url
     * @param communityId
     * @param buildingId
     * @param contentDetail
     * @return
     */
    public String pushMsg2ChannelAndStoreMsg(BizConstants.PushActionEnum action, long bizType, String content, String url, long communityId, long buildingId, String contentDetail, long tradeId);

    boolean sendSMS(String msg, String mobile);

}
