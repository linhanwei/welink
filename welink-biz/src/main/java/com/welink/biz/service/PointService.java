package com.welink.biz.service;

import com.welink.buy.utils.BaseResult;
import com.welink.commons.domain.*;
import com.welink.commons.persistence.PointAccountDOMapper;
import com.welink.commons.persistence.PointRecordDOMapper;
import com.welink.promotion.PromotionType;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户积分服务
 * 基础积分服务，不涉及具体业务逻辑
 * 需要加事务
 */
@Service
public class PointService {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(PointService.class);

    @Resource
    private PointAccountDOMapper pointAccountDOMapper;

    @Resource
    private PointRecordDOMapper pointRecordDOMapper;

    //根据userId获取其可用积分个数
    public long findAvailablePointByUserId(long userId) {
        PointAccountDOExample pointAccountDOExample = new PointAccountDOExample();
        pointAccountDOExample.createCriteria().andUserIdEqualTo(userId).andStatusEqualTo((byte) 1);
        List<PointAccountDO> pointAccountDOs = pointAccountDOMapper.selectByExample(pointAccountDOExample);
        if (pointAccountDOs != null && pointAccountDOs.size() > 0) {
            return pointAccountDOs.get(0).getAvailableBalance();
        }
        return 0;
    }

    //根据userId获取积分记录
    public List<PointRecordDO> findPointRecordDOsByUserId(Long userId) {
        PointRecordDOExample pointRecordDOExample = new PointRecordDOExample();
        pointRecordDOExample.createCriteria().andUserIdEqualTo(userId);
        List<PointRecordDO> pointRecordDOs = pointRecordDOMapper.selectByExample(pointRecordDOExample);
        if (pointRecordDOs != null && pointRecordDOs.size() > 0) {
            return pointRecordDOs;
        }
        return null;
    }

    //根据userId查询积分记录
    public BaseResult<List<PointRecordDO>> findPointList(long userId, int offset, int limit) {
        List<Integer> frozenStatus = new ArrayList<Integer>();
        frozenStatus.add(PromotionType.POINT_USE_IN_TRADE_FROZEN_ELIMINATE.getCode());
        PointRecordDOExample pointRecordDOExample = new PointRecordDOExample();
        pointRecordDOExample.setOrderByClause("id DESC");
        PointRecordDOExample.Criteria criteria = pointRecordDOExample.createCriteria();
        criteria.andUserIdEqualTo(userId);
        criteria.andTypeNotIn(frozenStatus);
        pointRecordDOExample.setOffset(offset);
        pointRecordDOExample.setLimit(limit);
        List<PointRecordDO> pointRecordDOList = pointRecordDOMapper.selectByExample(pointRecordDOExample);

        return new BaseResult<>(pointRecordDOList).putExternal("total", pointRecordDOList.size());
    }


}
