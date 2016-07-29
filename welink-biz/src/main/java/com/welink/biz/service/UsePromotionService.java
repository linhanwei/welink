package com.welink.biz.service;

import com.google.common.collect.ImmutableMap;
import com.welink.commons.domain.*;
import com.welink.commons.persistence.*;
import com.welink.promotion.PromotionType;
import com.welink.promotion.reactive.UserInteractionEffect;
import com.welink.promotion.reactive.UserInteractionRequest;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by XUTIAN on 2015/3/10.
 */
@Service
public class UsePromotionService {
    private static org.slf4j.Logger log = LoggerFactory.getLogger(UsePromotionService.class);

    @Resource
    private UsePromotionDOMapper usePromotionDOMapper;

    @Resource
    private PointRecordDOMapper pointRecordDOMapper;

    @Resource
    private CouponDOMapper couponDOMapper;

    @Resource
    private UserCouponDOMapper userCouponDOMapper;

    @Resource
    private UserInteractionRecordsDOMapper userInteractionRecordsDOMapper;

    @Resource
    private UserInteractionEffect userInteractionEffect;

    //线上支付成功后将优惠从冻结状态转换成已使用
    public void changePromotionFrozenToUsed(long tradeId, long userId) {
        UsePromotionDOExample usePromotionDOExample = new UsePromotionDOExample();
        usePromotionDOExample.createCriteria().andTradeIdEqualTo(tradeId);
        List<UsePromotionDO> usePromotionDOList = usePromotionDOMapper.selectByExample(usePromotionDOExample);
        for (UsePromotionDO usePromotionDO : usePromotionDOList) {
            UserInteractionRequest userInteractionRequest = new UserInteractionRequest();
            userInteractionRequest.setUserId(userId);
            userInteractionRequest.setTargetId(String.valueOf(tradeId));
            if (usePromotionDO.getType() == PromotionType.POINT_USE_IN_TRADE_FROZEN.getCode()) {
                userInteractionRequest.setType(PromotionType.POINT_USE_IN_TRADE_FROZEN_ELIMINATE.getCode());
                PointRecordDO pointRecordDO = pointRecordDOMapper.selectByPrimaryKey(usePromotionDO.getPromotionId());
                if (pointRecordDO != null) {
                    userInteractionRequest.setValue(pointRecordDO.getAmount());
                }
                userInteractionEffect.interactive(userInteractionRequest);
            } else if (usePromotionDO.getType() == PromotionType.COUPON_USE_IN_TRADE_FROZEN.getCode()) {
                userInteractionRequest.setType(PromotionType.COUPON_USE_IN_TRADE_FROZEN_ELIMINATE.getCode());
                UserCouponDO userCouponDO = userCouponDOMapper.selectByPrimaryKey(usePromotionDO.getPromotionId());
                userInteractionRequest.getParams().putAll(ImmutableMap.of("user_coupon", userCouponDO));
                userInteractionEffect.interactive(userInteractionRequest);
            }
        }
    }


    //将优惠从冻结状态转换成未使用
    public void changePromotionFrozenToUnUsed(long tradeId, long userId) {
        UsePromotionDOExample usePromotionDOExample = new UsePromotionDOExample();
        usePromotionDOExample.createCriteria().andTradeIdEqualTo(tradeId);
        List<UsePromotionDO> usePromotionDOList = usePromotionDOMapper.selectByExample(usePromotionDOExample);
        for (UsePromotionDO usePromotionDO : usePromotionDOList) {
            UserInteractionRequest userInteractionRequest = new UserInteractionRequest();
            userInteractionRequest.setUserId(userId);
            userInteractionRequest.setTargetId(String.valueOf(tradeId));
            //积分
            if (usePromotionDO.getType() == PromotionType.POINT_USE_IN_TRADE_FROZEN.getCode()) {
                //查询积分是否已经解冻
                UserInteractionRecordsDOExample userInteractionRecordsDOExample = new UserInteractionRecordsDOExample();
                userInteractionRecordsDOExample.createCriteria().andTargetIdEqualTo(String.valueOf(tradeId)).andUserIdEqualTo(userId).andTypeEqualTo(PromotionType.POINT_USE_IN_TRADE_FROZEN_RESTORE.getCode());
                List<UserInteractionRecordsDO> userInteractionRecordsDOList = userInteractionRecordsDOMapper.selectByExample(userInteractionRecordsDOExample);
                if (userInteractionRecordsDOList != null && userInteractionRecordsDOList.size() > 0) {
                    continue;
                }

                userInteractionRequest.setType(PromotionType.POINT_USE_IN_TRADE_FROZEN_RESTORE.getCode());
                PointRecordDO pointRecordDO = pointRecordDOMapper.selectByPrimaryKey(usePromotionDO.getPromotionId());
                if (pointRecordDO != null) {
                    userInteractionRequest.setValue(pointRecordDO.getAmount());
                }
                userInteractionEffect.interactive(userInteractionRequest);
            }
            //优惠券
            else if (usePromotionDO.getType() == PromotionType.COUPON_USE_IN_TRADE_FROZEN.getCode()) {
                userInteractionRequest.setType(PromotionType.COUPON_USE_IN_TRADE_FROZEN_RESTORE.getCode());
                UserCouponDO userCouponDO = userCouponDOMapper.selectByPrimaryKey(usePromotionDO.getPromotionId());
                userInteractionRequest.getParams().putAll(ImmutableMap.of("user_coupon", userCouponDO));
                userInteractionEffect.interactive(userInteractionRequest);
            }
        }
    }


    public Map<String, Long> findUsePromotionByTradeId(long tradeId) {
        Map<String, Long> promotion = new HashMap<String, Long>();
        UsePromotionDOExample usePromotionDOExample = new UsePromotionDOExample();
        usePromotionDOExample.createCriteria().andTradeIdEqualTo(tradeId);
        List<UsePromotionDO> usePromotionDOs = usePromotionDOMapper.selectByExample(usePromotionDOExample);
        for (UsePromotionDO usePromotionDO : usePromotionDOs) {
            if (usePromotionDO.getType() == PromotionType.POINT_USE_IN_TRADE_FROZEN.getCode() || usePromotionDO.getType() == PromotionType.POINT_USE_IN_TRADE_DIRECT.getCode()) {
                PointRecordDO pointRecordDO = pointRecordDOMapper.selectByPrimaryKey(usePromotionDO.getPromotionId());
                if (pointRecordDO != null) {
                    promotion.put("point", pointRecordDO.getAmount());
                }
            } else if (usePromotionDO.getType() == PromotionType.COUPON_USE_IN_TRADE_FROZEN.getCode() || usePromotionDO.getType() == PromotionType.COUPON_USE_IN_TRADE_DIRECT.getCode()) {
                UserCouponDO userCouponDO = userCouponDOMapper.selectByPrimaryKey(usePromotionDO.getPromotionId());
                CouponDO couponDO = null;
                if (userCouponDO != null) {
                    couponDO = couponDOMapper.selectByPrimaryKey(userCouponDO.getCouponId());
                }
                if (userCouponDO != null && couponDO != null) {
                    promotion.put("coupon", couponDO.getValue().longValue());
                }
            }
        }
        return promotion;
    }

}
