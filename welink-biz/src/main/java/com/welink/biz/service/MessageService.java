package com.welink.biz.service;

import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.BizMessage;
import com.welink.commons.domain.BizMessageExample;
import com.welink.commons.domain.MessageSummaryDO;
import com.welink.commons.domain.MessageSummaryDOExample;
import com.welink.commons.persistence.BizMessageMapper;
import com.welink.commons.persistence.MessageSummaryDOMapper;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * Created by daniel on 14-10-9.
 */
@Service
public class MessageService {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(MessageService.class);

    @Resource
    private MessageSummaryDOMapper messageSummaryDOMapper;

    @Resource
    private BizMessageMapper bizMessageMapper;

    /**
     * 更新消息摘要
     *
     * @param profileId
     * @param buildingId
     * @param communityId
     * @param bizType
     * @param content
     * @return 是否更新成功
     */
    public boolean addMsgSummary(long profileId, long buildingId, long communityId, long bizType, String content) {
        //先查询是否存在
        MessageSummaryDOExample mmExample = new MessageSummaryDOExample();
        mmExample.createCriteria()/*.andBuildingIdEqualTo(buildingId).andCommunityIdEqualTo(communityId)*/
                .andProfileIdEqualTo(profileId).andBizTypeEqualTo(bizType);
        List<MessageSummaryDO> messageSummaryDOList = messageSummaryDOMapper.selectByExample(mmExample);
        MessageSummaryDO messageSummaryDO = new MessageSummaryDO();
        messageSummaryDO.setBizType(bizType);
        messageSummaryDO.setLastUpdated(new Date());
        messageSummaryDO.setDateCreated(new Date());
        messageSummaryDO.setBizName(BizConstants.UserNotifyTagEnum.getUserNotifyTagName(bizType));
//        messageSummaryDO.setBuildingId(buildingId);
//        messageSummaryDO.setCommunityId(communityId);
        messageSummaryDO.setProfileId(profileId);
        messageSummaryDO.setLastMessage(content);
        int reflectCnt = 0;
        if (null != messageSummaryDOList && messageSummaryDOList.size() > 0) {
            //更新
            messageSummaryDO.setNonReadCount(messageSummaryDOList.get(0).getNonReadCount() + 1);
            reflectCnt = messageSummaryDOMapper.updateByExampleSelective(messageSummaryDO, mmExample);
        } else {
            //插入
            messageSummaryDO.setNonReadCount(1);
            reflectCnt = messageSummaryDOMapper.insert(messageSummaryDO);
        }
        return reflectCnt > 0;
    }

    /**
     * 保存新消息并更新消息摘要
     *
     * @param profileId
     * @param buildingId
     * @param communityId
     * @param bizType
     * @param content
     */
    public void storeMsg(long profileId, long buildingId, long communityId, long bizType, String content, long tradeId) {
        BizMessage bizMessage = new BizMessage();
        bizMessage.setBuildingId(buildingId);
        bizMessage.setProfileId(profileId);
        bizMessage.setCommunityId(communityId);
        bizMessage.setBizType(bizType);
        bizMessage.setBizName(BizConstants.UserNotifyTagEnum.getUserNotifyTagName(bizType));
        bizMessage.setLastUpdated(new Date());
        bizMessage.setDateCreated(new Date());
        bizMessage.setStatus(BizConstants.UserNotifyMsgStatus.NON_READ.getStatus());
        bizMessage.setContent(content);
        bizMessage.setTradeId(tradeId);
        bizMessage.setBizStatus(BizConstants.UserMsgBizStatus.VALID.getStatus());
        //insert message
        int reflectCnt = bizMessageMapper.insertSelective(bizMessage);
        if (reflectCnt < 1) {
            log.warn("insert message failed. profileId:" + profileId + ",buildingId:" + buildingId + ",profileId:" + profileId + "bizType:" + bizType + ",content:" + content);
        }
        //update message summary
        addMsgSummary(profileId, buildingId, communityId, bizType, content);
    }

    @Async
    public void updateMsgs(long profileId, byte bizStatus, long bizType) {
        BizMessage bizMessage = new BizMessage();
        bizMessage.setBizStatus((int) bizStatus);
        bizMessage.setStatus(BizConstants.UserNotifyMsgStatus.READED.getStatus());
        BizMessageExample bExample = new BizMessageExample();
        bExample.createCriteria().andProfileIdEqualTo(profileId).andBizTypeEqualTo(bizType);

        if (bizMessageMapper.updateByExampleSelective(bizMessage, bExample) < 1) {
            log.warn("update biz message bizStatus failed. profileId:" + profileId);
        }
    }
}
