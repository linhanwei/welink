package com.welink.biz.service;

import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.ComplainDO;
import com.welink.commons.domain.ComplainDOExample;
import com.welink.commons.domain.ComplainNoteDO;
import com.welink.commons.domain.ComplainNoteDOExample;
import com.welink.commons.persistence.ComplainDOMapper;
import com.welink.commons.persistence.ComplainNoteDOMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.SQLSyntaxErrorException;
import java.util.Date;
import java.util.List;

/**
 * Created by daniel on 14-10-14.
 */
@Service
public class ComplainService {

    private static Logger log = LoggerFactory.getLogger(ComplainService.class);

    @Resource
    private ComplainDOMapper complainDOMapper;

    @Resource
    private ComplainNoteDOMapper complainNoteDOMapper;

    /**
     * 添加一条投诉建议
     *
     * @param communityId
     * @param buildingId
     * @param profileId
     * @param content
     * @param picUrls
     * @return
     */
    public long addComplain(long communityId, long buildingId, long profileId, String content, String picUrls, String title) throws SQLSyntaxErrorException {
        ComplainDO complainDO = new ComplainDO();
        complainDO.setCommunityId(communityId);
        complainDO.setBuildingId(buildingId);
        complainDO.setProfileId(profileId);
        complainDO.setContent(content);
        complainDO.setDateCreated(new Date());
        complainDO.setLastUpdated(new Date());
        complainDO.setPicUrls(picUrls);
        complainDO.setTitle(title);
        complainDO.setStatus(BizConstants.ComplainStatus.NEW_CREATE.getStatus());
        complainDO.setVersion(1l);
        if (complainDOMapper.insertSelective(complainDO) > 0) {
            return complainDO.getId();
        } else {
            return -1l;
        }
    }

    /**
     * 查询投诉
     *
     * @param complainId
     * @return
     */
    public List<ComplainDO> fetchComplain(long complainId) {
        ComplainDOExample cExample = new ComplainDOExample();
        cExample.createCriteria().andIdEqualTo(complainId);
        List<ComplainDO> complainDOs = complainDOMapper.selectByExample(cExample);
        return complainDOs;
    }

    /**
     * 查询某人的投诉列表--考虑状态因素
     *
     * @param communityId
     * @param buildingId
     * @param profileId
     * @param status
     * @return
     */
    public List<ComplainDO> fetchComplain(long communityId, long buildingId, long profileId, byte status) {
        ComplainDOExample cExample = new ComplainDOExample();
        cExample.createCriteria().andCommunityIdEqualTo(communityId).andBuildingIdEqualTo(buildingId)
                .andProfileIdEqualTo(profileId).andStatusEqualTo(status);
        List<ComplainDO> complainDOs = complainDOMapper.selectByExample(cExample);
        return complainDOs;
    }

    /**
     * 查询某人的投诉列表 -- 不考虑状态因素
     *
     * @param communityId
     * @param buildingId
     * @param profileId
     * @return
     */
    public List<ComplainDO> fetchComplain(long communityId, long buildingId, long profileId) {
        ComplainDOExample cExample = new ComplainDOExample();
        cExample.createCriteria().andCommunityIdEqualTo(communityId).andBuildingIdEqualTo(buildingId)
                .andProfileIdEqualTo(profileId).andStatusNotEqualTo(BizConstants.ComplainStatus.DELETE.getStatus());
        cExample.setOrderByClause("date_created DESC");
        List<ComplainDO> complainDOs = complainDOMapper.selectByExample(cExample);
        return complainDOs;
    }

    /**
     * 根据投诉ID查询投诉信息列表
     *
     * @param complainId
     * @return
     */
    public List<ComplainNoteDO> fetchComplainNotes(long complainId) {
        ComplainNoteDOExample cExample = new ComplainNoteDOExample();
        cExample.createCriteria().andComplainIdEqualTo(complainId);
        cExample.setOrderByClause("date_create ASC");
        List<ComplainNoteDO> complainNoteDOs = complainNoteDOMapper.selectByExample(cExample);
        return complainNoteDOs;
    }

    /**
     * 添加投诉建议内容
     *
     * @param communityId
     * @param buildingId
     * @param profileId
     * @param complainId
     * @param content
     * @return
     */
    public boolean addComplainNote(long communityId, long buildingId, long profileId, long complainId, String content) {
        ComplainNoteDO complainNoteDO = new ComplainNoteDO();
        complainNoteDO.setVersion(1l);
        complainNoteDO.setStatus(BizConstants.ComplainStatus.NEW_CREATE.getStatus());
        complainNoteDO.setComplainId(complainId);
        complainNoteDO.setDateCreate(new Date());
        complainNoteDO.setDealContent(content);
        complainNoteDO.setReplyerId(profileId);
        complainNoteDO.setReplyerType(BizConstants.ComplainReplyType.COMPLAINER.getType());
        if (complainNoteDOMapper.insertSelective(complainNoteDO) > 0) {
            //update complain time
            if (updateComplainTime(new Date(), complainId)) {
                log.warn("update complain last updated time failed. complainId:" + complainId);
            }
            return true;
        }
        return false;
    }

    /**
     * 更新投诉建议的最后更新时间(这里指最新处理时间)
     *
     * @param date
     * @param complainId
     * @return
     */
    public boolean updateComplainTime(Date date, long complainId) {
        ComplainDO complainDO = new ComplainDO();
        complainDO.setLastUpdated(date);
        ComplainDOExample cExample = new ComplainDOExample();
        cExample.createCriteria().andIdEqualTo(complainId);
        if (complainDOMapper.updateByExampleSelective(complainDO, cExample) > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 关闭投诉建议
     *
     * @param complainId
     * @return
     */
    public boolean doneComplain(long complainId) {
        ComplainDO complainDO = new ComplainDO();
        complainDO.setStatus(BizConstants.ComplainStatus.DONE.getStatus());
        ComplainDOExample cExample = new ComplainDOExample();
        cExample.createCriteria().andIdEqualTo(complainId);
        int done = complainDOMapper.updateByExampleSelective(complainDO, cExample);
        if (done > 0) {
            return true;
        }
        return false;
    }
}
