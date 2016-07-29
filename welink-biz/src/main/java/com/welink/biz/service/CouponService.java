package com.welink.biz.service;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mysql.jdbc.Connection;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.CouponViewDO;
import com.welink.biz.common.model.WelinkVO;
import com.welink.buy.utils.Constants;
import com.welink.commons.domain.CouponDO;
import com.welink.commons.domain.CouponDOExample;
import com.welink.commons.domain.MikuGetpayDO;
import com.welink.commons.domain.MikuSalesRecordDO;
import com.welink.commons.domain.MikuSalesRecordDOExample;
import com.welink.commons.domain.UserCouponDO;
import com.welink.commons.domain.UserCouponDOExample;
import com.welink.commons.domain.UserInteractionRecordsDO;
import com.welink.commons.domain.UserInteractionRecordsDOExample;
import com.welink.commons.persistence.CouponDOMapper;
import com.welink.commons.persistence.UserCouponDOMapper;
import com.welink.commons.persistence.UserInteractionRecordsDOMapper;
import com.welink.commons.utils.NoNullFieldStringStyle;
import com.welink.commons.vo.LotteryDrawRewardVO;
import com.welink.promotion.CouponType;
import com.welink.promotion.PromotionErrorEnum;
import com.welink.promotion.PromotionType;
import com.welink.promotion.reactive.PromotionResult;
import com.welink.promotion.reactive.UserInteractionRequest;
import com.welink.promotion.reactive.services.coupons.UserCouponService;

/**
 * 用户优惠券服务
 * 基础服务，不涉及具体业务逻辑
 * 需要加事务
 */
@Service
public class CouponService implements InitializingBean {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(CouponService.class);

    @Resource
	private PlatformTransactionManager transactionManager;

	private TransactionTemplate transactionTemplate;
    
    @Resource
    private CouponDOMapper couponDOMapper;
    
    @Resource
    private UserCouponService userCouponService;

    @Resource
    private UserCouponDOMapper userCouponDOMapper;
    
    @Resource
    private UserInteractionRecordsDOMapper userInteractionRecordsDOMapper;

    //根据userId,totalFee获取其可使用的优惠券列表
    public List<CouponViewDO> findAvailableCouponByUserId(Long userId, Long totalFee) {
        Date now = new Date();
        UserCouponDOExample userCouponDOExample = new UserCouponDOExample();
        userCouponDOExample.createCriteria().andUserIdEqualTo(userId).andStatusEqualTo((byte) 1).andEndTimeGreaterThan(now);
        List<UserCouponDO> userCouponDOs = userCouponDOMapper.selectByExample(userCouponDOExample);
        List<CouponViewDO> availableUserCoupons = new ArrayList<CouponViewDO>();
        for (UserCouponDO userCouponDO : userCouponDOs) {
            CouponDO couponDO = couponDOMapper.selectByPrimaryKey(userCouponDO.getCouponId());
            Integer minValue = (null == couponDO.getMinValue() ? 0 : couponDO.getMinValue());
            if (couponDO != null && totalFee >= minValue && null != couponDO.getValue() && totalFee >= couponDO.getValue()) {
                CouponViewDO couponViewDO = new CouponViewDO();
                couponViewDO.setUserCouponId(userCouponDO.getId());
                couponViewDO.setCouponType(userCouponDO.getCouponType());
                couponViewDO.setEndTime(userCouponDO.getEndTime().getTime());
                couponViewDO.setHasBeenUsed(false);
                couponViewDO.setValid(true);
                couponViewDO.setName(couponDO.getName());
                couponViewDO.setDescription(couponDO.getDescription());
                couponViewDO.setValue(null == couponDO.getValue() ? 0 : couponDO.getValue());
                couponViewDO.setMinValue(null == couponDO.getMinValue() ? 0 :couponDO.getMinValue());
                couponViewDO.setPicUrl(null == couponDO.getPicUrl() ? "" :couponDO.getPicUrl());
                availableUserCoupons.add(couponViewDO);
            }
        }
        return availableUserCoupons;
    }

    //根据userId获取其未使用且未过期的优惠券列表
    public List<CouponViewDO> findUnUsedCouponsByUserId(Long userId) {
        Date now = new Date();
        UserCouponDOExample userCouponDOExample = new UserCouponDOExample();
        userCouponDOExample.createCriteria().andUserIdEqualTo(userId).andStatusEqualTo((byte) 1).andEndTimeGreaterThan(now);
        List<UserCouponDO> userCouponDOs = userCouponDOMapper.selectByExample(userCouponDOExample);
        List<CouponViewDO> unUsedUserCoupons = new ArrayList<CouponViewDO>();
        for (UserCouponDO userCouponDO : userCouponDOs) {
            CouponDO couponDO = couponDOMapper.selectByPrimaryKey(userCouponDO.getCouponId());
            CouponViewDO couponViewDO = new CouponViewDO();
            couponViewDO.setUserCouponId(userCouponDO.getId());
            couponViewDO.setCouponType(userCouponDO.getCouponType());
            couponViewDO.setEndTime(userCouponDO.getEndTime().getTime());
            couponViewDO.setHasBeenUsed(false);
            couponViewDO.setValid(true);
            couponViewDO.setName(couponDO.getName());
            couponViewDO.setDescription(couponDO.getDescription());
            couponViewDO.setValue(null == couponDO.getValue() ? 0 :couponDO.getValue());
            couponViewDO.setMinValue(null == couponDO.getMinValue() ? 0 :couponDO.getMinValue());
            couponViewDO.setPicUrl(null == couponDO.getPicUrl() ? "" :couponDO.getPicUrl());
            unUsedUserCoupons.add(couponViewDO);
        }
        return unUsedUserCoupons;
    }

    //根据userId获取已使用或已过期的优惠券列表
    public List<CouponViewDO> findUsedCouponsByUserId(Long userId) {
        UserCouponDOExample userCouponDOExample = new UserCouponDOExample();
        userCouponDOExample.createCriteria().andUserIdEqualTo(userId);
        List<UserCouponDO> userCouponDOs = userCouponDOMapper.selectByExample(userCouponDOExample);
        List<CouponViewDO> usedUserCoupons = new ArrayList<CouponViewDO>();
        Date now = new Date();
        for (UserCouponDO userCouponDO : userCouponDOs) {
            if (userCouponDO.getStatus() == -1 || userCouponDO.getStatus() == 0) {
                CouponDO couponDO = couponDOMapper.selectByPrimaryKey(userCouponDO.getCouponId());
                CouponViewDO couponViewDO = new CouponViewDO();
                couponViewDO.setUserCouponId(userCouponDO.getId());
                couponViewDO.setCouponType(userCouponDO.getCouponType());
                couponViewDO.setEndTime(userCouponDO.getEndTime().getTime());
                couponViewDO.setHasBeenUsed(true);
                couponViewDO.setValid(false);
                couponViewDO.setName(couponDO.getName());
                couponViewDO.setDescription(couponDO.getDescription());
                couponViewDO.setValue(null == couponDO.getValue() ? 0 :couponDO.getValue());
                couponViewDO.setMinValue(null == couponDO.getMinValue() ? 0 :couponDO.getMinValue());
                couponViewDO.setPicUrl(null == couponDO.getPicUrl() ? "" :couponDO.getPicUrl());
                usedUserCoupons.add(couponViewDO);
            } else if (userCouponDO.getEndTime().before(now)) {
                CouponDO couponDO = couponDOMapper.selectByPrimaryKey(userCouponDO.getCouponId());
                CouponViewDO couponViewDO = new CouponViewDO();
                couponViewDO.setUserCouponId(userCouponDO.getId());
                couponViewDO.setCouponType(userCouponDO.getCouponType());
                couponViewDO.setEndTime(userCouponDO.getEndTime().getTime());
                couponViewDO.setHasBeenUsed(false);
                couponViewDO.setValid(false);
                couponViewDO.setName(couponDO.getName());
                couponViewDO.setDescription(couponDO.getDescription());
                couponViewDO.setValue(null == couponDO.getValue() ? 0 :couponDO.getValue());
                couponViewDO.setMinValue(null == couponDO.getMinValue() ? 0 :couponDO.getMinValue());
                couponViewDO.setPicUrl(null == couponDO.getPicUrl() ? "" :couponDO.getPicUrl());
                usedUserCoupons.add(couponViewDO);
            }
        }
        return usedUserCoupons;
    }

    //根据userId获取其未使用的优惠券张数
    public int findUserCouponCountByUserId(Long userId) {
        List<CouponViewDO> unUsedCoupons = findUnUsedCouponsByUserId(userId);
        if (unUsedCoupons != null && unUsedCoupons.size() > 0) {
            return unUsedCoupons.size();
        }
        return 0;
    }

    //获取新人可领的优惠券列表
    public List<CouponDO> findRookieCoupons() {
        CouponDOExample couponDOExample = new CouponDOExample();
        couponDOExample.createCriteria().andTypeEqualTo(CouponType.ROOKIE.getCode()).andStatusEqualTo((byte) 1);
        List<CouponDO> couponDOs = couponDOMapper.selectByExample(couponDOExample);
        return couponDOs;
    }
    
    /*
     * 送单个优惠券
     */
    public WelinkVO giveCoupon(final UserInteractionRequest userInteractionRequest, final long userId, final long couponId) {
		return transactionTemplate
				.execute(new TransactionCallback<WelinkVO>() {
					@Override
					public WelinkVO doInTransaction(
							TransactionStatus transactionStatus) {
						WelinkVO welinkVO = new WelinkVO();
						
						UserInteractionRecordsDO userInteractionRecordsDO = transform(userInteractionRequest);
				        checkNotNull(userInteractionRecordsDO);
			        	// 互动插入，绝对不能放到上面去
			            userInteractionRecordsDO.setVersion(1L);
			            userInteractionRecordsDO.setDateCreated(new Date());
			            if (userInteractionRecordsDOMapper.insert(userInteractionRecordsDO) != 1) {
			            	transactionStatus.setRollbackOnly();
			            	welinkVO.setStatus(0);
							welinkVO.setCode(BizErrorEnum.SYSTEM_BUSY.getCode());
							welinkVO.setMsg(BizErrorEnum.SYSTEM_BUSY.getMsg());
							return welinkVO;
			            }
			        	
			        	UserCouponDO userCouponDO = new UserCouponDO();
				        userCouponDO.setCouponId(couponId);
				        userCouponDO.setCouponType(CouponType.OFF_FOR_CONDITION.getCode());
				        userCouponDO.setDateCreated(new Date());
				        userCouponDO.setLastUpdated(new Date());
				        userCouponDO.setPickTime(new Date());
				        userCouponDO.setStatus((byte) 1);
				        userCouponDO.setUserId(userInteractionRecordsDO.getUserId());
				        userCouponDO.setVersion(1L);
				        
				        userCouponDO.setStartTime(new Date());
				        userCouponDO.setEndTime(new DateTime().plusDays(15).toDate());
				        
				        boolean b = userCouponService.updateUserCoupon(userInteractionRecordsDO, userCouponDO);
			            if (!b) {
			            	transactionStatus.setRollbackOnly();
			            	welinkVO.setStatus(0);
							welinkVO.setCode(BizErrorEnum.SYSTEM_BUSY.getCode());
							welinkVO.setMsg(BizErrorEnum.SYSTEM_BUSY.getMsg());
							return welinkVO;
			            }
						
						welinkVO.setStatus(1);
						return welinkVO;
					}
				});
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

	@Override
	public void afterPropertiesSet() throws Exception {
		checkNotNull(userCouponService);
		checkNotNull(transactionManager);
		
		transactionTemplate = new TransactionTemplate(transactionManager);
		transactionTemplate.setName("userAgentcy-transaction");
		transactionTemplate
				.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		transactionTemplate
				.setIsolationLevel(Connection.TRANSACTION_READ_COMMITTED);
	}
	
}
