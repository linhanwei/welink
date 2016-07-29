package com.welink.promotion.reactive.impl;

import com.google.common.base.Optional;
import com.welink.commons.domain.*;
import com.welink.commons.persistence.CouponDOMapper;
import com.welink.commons.persistence.PointAccountDOMapper;
import com.welink.commons.persistence.UserCouponDOMapper;
import com.welink.commons.persistence.UserInteractionRecordsDOMapper;
import com.welink.commons.utils.NoNullFieldStringStyle;
import com.welink.promotion.PromotionErrorEnum;
import com.welink.promotion.drools.DroolsExecutor;
import com.welink.promotion.reactive.PromotionResult;
import com.welink.promotion.reactive.UserInteractionEffect;
import com.welink.promotion.reactive.UserInteractionRequest;
import com.welink.promotion.reactive.services.coupons.UserCouponService;
import com.welink.promotion.reactive.services.points.UserPointService;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by saarixx on 5/3/15.
 */
@Service
public class DefaultUserInteractionEffect implements UserInteractionEffect {

    static Logger logger = LoggerFactory.getLogger(DefaultUserInteractionEffect.class);

    @Resource
    private PointAccountDOMapper pointAccountDOMapper;

    @Resource
    private CouponDOMapper couponDOMapper;

    @Resource
    private UserCouponDOMapper userCouponDOMapper;

    @Resource
    private DroolsExecutor droolsExecutor;

    @Resource
    private UserInteractionRecordsDOMapper userInteractionRecordsDOMapper;

    @Resource
    private UserPointService userPointService;

    @Resource
    private UserCouponService userCouponService;


    @Override
    public Optional<PromotionResult> interactive(UserInteractionRequest userInteractionRequest) {
        checkNotNull(userInteractionRequest);
        checkNotNull(userInteractionRequest.getUserId());

        long userId = checkNotNull(userInteractionRequest).getUserId();

        UserInteractionRecordsDO userInteractionRecordsDO = transform(userInteractionRequest);
        checkNotNull(userInteractionRecordsDO);

        PromotionResult promotionResult = new PromotionResult();
        promotionResult.setType(userInteractionRequest.getType());
        promotionResult.setFrom(userInteractionRequest.getFrom());

        switch (userInteractionRequest.getType() / 100000) {
            case 1: {
                // 获取积分相关账户和积分纪录
                Optional<PointAccountDO> pointAccountDOOptional = getUserPointAccount(userId);

                if (!pointAccountDOOptional.isPresent()) {
                    return Optional.absent();
                }

                PointAccountDO pointAccountDO = pointAccountDOOptional.get();

                PointRecordDO pointRecordDO = new PointRecordDO();
                pointRecordDO.setAccountId(pointAccountDO.getId());
                pointRecordDO.setDateCreated(new Date());
                pointRecordDO.setType(userInteractionRequest.getType());
                pointRecordDO.setUserId(userId);
                pointRecordDO.setVersion(1L);

                List<UserInteractionRecordsDO> latestUserInteractionRecordsDOs = getLatestUserInteractionRecordsDO(userId, userInteractionRequest.getType(), 1);
                if (!latestUserInteractionRecordsDOs.isEmpty()) {
                    droolsExecutor.execute(promotionResult, pointAccountDO, userInteractionRecordsDO, latestUserInteractionRecordsDOs.get(0), pointRecordDO);
                } else {
                    droolsExecutor.execute(promotionResult, pointAccountDO, userInteractionRecordsDO, pointRecordDO);
                }

                if (promotionResult.getReward() == null) {
                    return Optional.absent();
                }

                boolean b = userPointService.updateUserPoint(userInteractionRecordsDO, pointRecordDO, pointAccountDO);

                if (!b) {
                    return Optional.absent();
                }

                promotionResult.setActionId(userInteractionRecordsDO.getId());
                promotionResult.setPromotionId(pointRecordDO.getId());

                break;
            }
            case 2: {
                List objects = getLatestUserInteractionRecordsDO(userId, userInteractionRecordsDO.getType(), 3);

                UserCouponDO userCouponDO = checkNotNull((UserCouponDO) userInteractionRequest.getParams().get("user_coupon"));

                List<CouponDO> couponDOs = (List<CouponDO>) userInteractionRequest.getParams().get("couponDOs");

                if (couponDOs != null) {
                    Collections.shuffle(couponDOs);
                    objects.addAll(couponDOs);
                }

                objects.add(userCouponDO);
                objects.add(userInteractionRecordsDO);
                objects.add(promotionResult);


                droolsExecutor.execute(objects.toArray());

                // 互动插入，绝对不能放到上面去
                userInteractionRecordsDO.setVersion(1L);
                userInteractionRecordsDO.setDateCreated(new Date());
                if (userInteractionRecordsDOMapper.insert(userInteractionRecordsDO) != 1) {
                    logger.error("userInteractionRecordsDOMapper insert error, the input parameters is {}",
                            ToStringBuilder.reflectionToString(userInteractionRecordsDO, new NoNullFieldStringStyle()));
                    return Optional.absent();
                }

                if (promotionResult.getReward() == null) {
                    promotionResult.setReward(false);
                    promotionResult.setCode(PromotionErrorEnum.COUPON_LACK_LUCKY.getCode());
                    promotionResult.setMessage(PromotionErrorEnum.COUPON_LACK_LUCKY.getMsg());
                }

                if (promotionResult.getReward()) {

                    boolean b = userCouponService.updateUserCoupon(userInteractionRecordsDO, userCouponDO);

                    if (!b) {
                        return Optional.absent();
                    }

                    promotionResult.setActionId(userInteractionRecordsDO.getId());
                    promotionResult.setPromotionId(userCouponDO.getId());
                }

                break;
            }
        }

        return Optional.of(promotionResult);
    }

    private UserCouponDO queryUserCouponDO(long id) {
        return userCouponDOMapper.selectByPrimaryKey(id);
    }

    private CouponDO queryCouponDO(long id) {
        return couponDOMapper.selectByPrimaryKey(id);
    }

    private UserInteractionRecordsDO transform(UserInteractionRequest userInteractionRequest) {
        UserInteractionRecordsDO userInteractionRecordsDO = new UserInteractionRecordsDO();
        userInteractionRecordsDO.setUserId(userInteractionRequest.getUserId());
        userInteractionRecordsDO.setDateCreated(new Date());
        userInteractionRecordsDO.setDestination(userInteractionRequest.getDestination());
        userInteractionRecordsDO.setFrom(userInteractionRequest.getFrom());
        userInteractionRecordsDO.setValue(userInteractionRequest.getValue() == null ? null : userInteractionRequest.getValue().intValue());
        userInteractionRecordsDO.setLastUpdated(new Date());
        userInteractionRecordsDO.setStatus((byte) 1);
        userInteractionRecordsDO.setTargetId(userInteractionRequest.getTargetId());
        userInteractionRecordsDO.setType(userInteractionRequest.getType());
        userInteractionRecordsDO.setVersion(1L);
        return userInteractionRecordsDO;
    }

    private List<UserInteractionRecordsDO> getLatestUserInteractionRecordsDO(long userId, int type, int limit) {
        UserInteractionRecordsDOExample userInteractionRecordsDOExample = new UserInteractionRecordsDOExample();
        userInteractionRecordsDOExample.setOrderByClause("id DESC");
        userInteractionRecordsDOExample.setLimit(limit);
        userInteractionRecordsDOExample.createCriteria() //
                .andUserIdEqualTo(userId) //
                .andTypeEqualTo(type);

        return userInteractionRecordsDOMapper.selectByExample(userInteractionRecordsDOExample);
    }

    private Optional<PointAccountDO> getUserPointAccount(long userId) {
        PointAccountDOExample pointAccountDOExample = new PointAccountDOExample();
        pointAccountDOExample.createCriteria().andUserIdEqualTo(userId);

        List<PointAccountDO> pointAccountDOs = pointAccountDOMapper.selectByExample(pointAccountDOExample);

        if (pointAccountDOs.isEmpty()) {
            PointAccountDO pointAccountDO = new PointAccountDO();
            pointAccountDO.setAvailableBalance(0L);
            pointAccountDO.setDateCreated(new Date());
            pointAccountDO.setLastUpdated(new Date());
            pointAccountDO.setStatus((byte) 1);
            pointAccountDO.setUserId(userId);
            pointAccountDO.setVersion(1L);

            // 如果插入失败
            if (1 != pointAccountDOMapper.insert(pointAccountDO)) {
                logger.error("pointAccountDOMapper insert fail, the input parameters is {}",
                        ToStringBuilder.reflectionToString(pointAccountDO, new NoNullFieldStringStyle()));
                return Optional.absent();
            }

            checkNotNull(pointAccountDO.getId());
            return Optional.of(pointAccountDO);

        } else {
            checkArgument(pointAccountDOs.size() == 1);
            return Optional.of(pointAccountDOs.get(0));
        }
    }
}
