package com.welink.biz.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.eventbus.AsyncEventBus;
import com.welink.biz.util.TimeUtils;
import com.welink.buy.utils.Constants;
import com.welink.commons.domain.CouponDO;
import com.welink.commons.domain.CouponDOExample;
import com.welink.commons.domain.UserCouponDO;
import com.welink.commons.domain.UserInteractionRecordsDO;
import com.welink.commons.domain.UserInteractionRecordsDOExample;
import com.welink.commons.persistence.CouponDOMapper;
import com.welink.commons.persistence.UserInteractionRecordsDOMapper;
import com.welink.promotion.CouponType;
import com.welink.promotion.PromotionType;
import com.welink.promotion.drools.Utils;
import com.welink.promotion.reactive.PromotionResult;
import com.welink.promotion.reactive.UserInteractionEffect;
import com.welink.promotion.reactive.UserInteractionRequest;

/**
 * 用户互动服务
 * 功能：增加互动记录和获得相应奖励
 * 需要加事务
 */
@Service
public class UserInteractionService {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(UserInteractionService.class);

    @Resource
    private UserInteractionRecordsDOMapper userInteractionRecordsDOMapper;
    @Resource
    private UserInteractionEffect userInteractionEffect;
    
    @Resource
    private CouponDOMapper couponDOMapper;

    @Resource
    private CouponService couponService;
    
    @Resource
    private AsyncEventBus asyncEventBus;
    
    public void setAsyncEventBus(AsyncEventBus asyncEventBus) {
        this.asyncEventBus = asyncEventBus;
    }

    //根据userId查询今天是否已签到领积分
    public boolean hasSignToday(Long userId) {
        Date nowDate = new Date();
        Date zeroDate = TimeUtils.getDateStartTime(nowDate);
        UserInteractionRecordsDOExample userInteractionRecordsDOExample = new UserInteractionRecordsDOExample();
        userInteractionRecordsDOExample.createCriteria().andUserIdEqualTo(userId).andTypeEqualTo(PromotionType.POINT_SIGN_IN.getCode()).andDateCreatedGreaterThan(zeroDate).andStatusEqualTo((byte) 1);
        List<UserInteractionRecordsDO> userInteractionRecordsDOList = userInteractionRecordsDOMapper.selectByExample(userInteractionRecordsDOExample);
        if (userInteractionRecordsDOList != null && userInteractionRecordsDOList.size() > 0) {
            return true;
        }
        return false;
    }

    //根据userId获取连续签到天数
    public int findContinueSignDays(Long userId) {
        UserInteractionRecordsDOExample userInteractionRecordsDOExample = new UserInteractionRecordsDOExample();
        userInteractionRecordsDOExample.createCriteria().andUserIdEqualTo(userId).andTypeEqualTo(PromotionType.POINT_SIGN_IN.getCode()).andStatusEqualTo((byte) 1);
        userInteractionRecordsDOExample.setOrderByClause("id DESC");
        userInteractionRecordsDOExample.setLimit(1);
        List<UserInteractionRecordsDO> userInteractionRecordsDOList = userInteractionRecordsDOMapper.selectByExample(userInteractionRecordsDOExample);
        if (userInteractionRecordsDOList != null && userInteractionRecordsDOList.size() > 0) {
            UserInteractionRecordsDO userInteractionRecordsDO = userInteractionRecordsDOList.get(0);
            if (hasSignToday(userId)) {
                return null == userInteractionRecordsDO.getValue() ? 0 : userInteractionRecordsDO.getValue();
            } else {
                if (Utils.isYesterday(userInteractionRecordsDO.getDateCreated())) {
                    return null == userInteractionRecordsDO.getValue() ? 0 : userInteractionRecordsDO.getValue();
                } else {
                    return 0;
                }
            }
        }
        return 0;
    }

    //根据userId获取其还可以摇优惠券的次数:总共三次
    public int findRemainShakeCouponCountBy(Long userId) {
        Date nowDate = new Date();
        Date zeroDate = TimeUtils.getDateStartTime(nowDate);
        UserInteractionRecordsDOExample userInteractionRecordsDOExample = new UserInteractionRecordsDOExample();
        userInteractionRecordsDOExample.createCriteria().andUserIdEqualTo(userId).andTypeEqualTo(PromotionType.COUPON_SHAKE.getCode()).andStatusEqualTo((byte) 1).andDateCreatedGreaterThan(zeroDate);
        List<UserInteractionRecordsDO> userInteractionRecordsDOList = userInteractionRecordsDOMapper.selectByExample(userInteractionRecordsDOExample);
        if (userInteractionRecordsDOList != null && userInteractionRecordsDOList.size() > 0) {
            return Constants.TOTAL_SHAKE_COUPON_COUNT - userInteractionRecordsDOList.size();
        }
        return Constants.TOTAL_SHAKE_COUPON_COUNT;
    }

    //给新人发积分和优惠券
    public boolean sendCouponsToNewPerson(Long userId) {
        //先去掉新人送积分和优惠券
        /*if (true) {
            return true;
        }*/
        List<Integer> newPersonStatus = new ArrayList<Integer>();
        //newPersonStatus.add(PromotionType.POINT_NEW_USER.getCode());
        newPersonStatus.add(PromotionType.COUPON_NEW_USER.getCode());
        UserInteractionRecordsDOExample userInteractionRecordsDOExample = new UserInteractionRecordsDOExample();
        userInteractionRecordsDOExample.createCriteria().andUserIdEqualTo(userId).andTypeIn(newPersonStatus);
        List<UserInteractionRecordsDO> userInteractionRecordsDOList = userInteractionRecordsDOMapper.selectByExample(userInteractionRecordsDOExample);
        if (userInteractionRecordsDOList != null && userInteractionRecordsDOList.size() > 0) {
            //已经发过，直接返回
            return true;
        } else {

            //新人发积分
            UserInteractionRequest pointRequest = new UserInteractionRequest();
            pointRequest.setUserId(userId);
            pointRequest.setType(PromotionType.POINT_NEW_USER.getCode());
            pointRequest.setValue(Constants.ROOKIE_POINT);
            Optional<PromotionResult> promotionResultOptional = userInteractionEffect.interactive(pointRequest);
            if (promotionResultOptional == null || (promotionResultOptional.isPresent() && promotionResultOptional.get() != null && !promotionResultOptional.get().getReward())) {
                log.error("新用户发送积分失败...userId:" + userId);
                return false;
            }
            //新人发优惠券
            List<CouponDO> couponDOs = couponService.findRookieCoupons();
            for (CouponDO couponDO : couponDOs) {
            	if(null != couponDO.getGiveNum() && couponDO.getGiveNum() > 0 
            			&& null != couponDO.getValidity() && couponDO.getValidity() > 0){
            		for(int i = 0; i<couponDO.getGiveNum(); i++){
            			CouponDO couponDOtemp = new CouponDO();
            			try {
            				org.springframework.beans.BeanUtils.copyProperties(couponDO, couponDOtemp);
							UserInteractionRequest couponRequest = new UserInteractionRequest();
							couponRequest.setUserId(userId);
							couponRequest.setType(PromotionType.COUPON_NEW_USER.getCode());
							couponRequest.getParams().putAll(ImmutableMap.of("user_coupon", new UserCouponDO(), "couponDOs", Lists.newArrayList(couponDOtemp)));
							//asyncEventBus.post(couponRequest);	//用户交互处理事件，此处发送优惠券
							
							Optional<PromotionResult> promotionResultOptional1 = userInteractionEffect.interactive(couponRequest);
							if (promotionResultOptional1 == null || (promotionResultOptional1.isPresent() && promotionResultOptional1.get() != null && !promotionResultOptional1.get().getReward())) {
								log.error("新用户发送优惠券失败...userId:" + userId);
								return false;
							}
						} catch (Exception e) {
						}
            		}
            	}else{
            		UserInteractionRequest couponRequest = new UserInteractionRequest();
					couponRequest.setUserId(userId);
					couponRequest.setType(PromotionType.COUPON_NEW_USER.getCode());
					couponRequest.getParams().putAll(ImmutableMap.of("user_coupon", new UserCouponDO(), "couponDOs", Lists.newArrayList(couponDO)));
					Optional<PromotionResult> promotionResultOptional1 = userInteractionEffect.interactive(couponRequest);
					if (promotionResultOptional1 == null || (promotionResultOptional1.isPresent() && promotionResultOptional1.get() != null && !promotionResultOptional1.get().getReward())) {
						log.error("新用户发送优惠券失败...userId:" + userId);
						return false;
					}
            	}
            }
            return true;
        }
    }
    
    //给邀请人发积分和优惠券
    public boolean sendCouponsToInvitePerson(Long userId) {
        //先去掉新人送积分和优惠券
        /*if (true) {
            return true;
        }*/
        /*List<Integer> newPersonStatus = new ArrayList<Integer>();
        newPersonStatus.add(PromotionType.COUPON_INVITE.getCode());
        UserInteractionRecordsDOExample userInteractionRecordsDOExample = new UserInteractionRecordsDOExample();
        userInteractionRecordsDOExample.createCriteria().andUserIdEqualTo(userId).andTypeIn(newPersonStatus);
        List<UserInteractionRecordsDO> userInteractionRecordsDOList = userInteractionRecordsDOMapper.selectByExample(userInteractionRecordsDOExample);
        if (userInteractionRecordsDOList != null && userInteractionRecordsDOList.size() > 0) {*/
    	if(1>2){
            //已经发过，直接返回
            return true;
        } else {

            //给邀请人发积分
            UserInteractionRequest pointRequest = new UserInteractionRequest();
            pointRequest.setUserId(userId);
            pointRequest.setType(PromotionType.POINT_INVITE.getCode());
            pointRequest.setValue(Constants.INVITE_POINT);
            Optional<PromotionResult> promotionResultOptional = userInteractionEffect.interactive(pointRequest);
            if (promotionResultOptional == null || (promotionResultOptional.isPresent() && promotionResultOptional.get() != null && !promotionResultOptional.get().getReward())) {
                log.error("给邀请人发送积分失败...userId:" + userId);
                return false;
            }
            //给邀请人发优惠券
            //List<CouponDO> couponDOs = couponService.findRookieCoupons();
            CouponDOExample couponDOExample = new CouponDOExample();
            couponDOExample.createCriteria().andTypeEqualTo(CouponType.INVITE.getCode()).andStatusEqualTo((byte) 1);
            List<CouponDO> couponDOs = couponDOMapper.selectByExample(couponDOExample);
            for (CouponDO couponDO : couponDOs) {
                UserInteractionRequest couponRequest = new UserInteractionRequest();
                couponRequest.setUserId(userId);
                couponRequest.setType(PromotionType.COUPON_INVITE.getCode());
                couponRequest.getParams().putAll(ImmutableMap.of("user_coupon", new UserCouponDO(), "couponDOs", Lists.newArrayList(couponDO)));
                Optional<PromotionResult> promotionResultOptional1 = userInteractionEffect.interactive(couponRequest);
                if (promotionResultOptional1 == null || (promotionResultOptional1.isPresent() && promotionResultOptional1.get() != null && !promotionResultOptional1.get().getReward())) {
                    log.error("给邀请人发送优惠券失败...userId:" + userId);
                    return false;
                }
            }
            return true;
        }
    }
    
}
