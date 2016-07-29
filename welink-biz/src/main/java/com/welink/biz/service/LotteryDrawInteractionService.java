package com.welink.biz.service;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.annotation.Resource;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.welink.buy.service.AppointmentTradeService;
import com.welink.commons.domain.CouponDO;
import com.welink.commons.domain.Item;
import com.welink.commons.domain.PointAccountDO;
import com.welink.commons.domain.PointAccountDOExample;
import com.welink.commons.domain.PointRecordDO;
import com.welink.commons.domain.UserCouponDO;
import com.welink.commons.domain.UserInteractionRecordsDO;
import com.welink.commons.domain.UserInteractionRecordsDOExample;
import com.welink.commons.persistence.CouponDOMapper;
import com.welink.commons.persistence.ItemMapper;
import com.welink.commons.persistence.PointAccountDOMapper;
import com.welink.commons.persistence.UserCouponDOMapper;
import com.welink.commons.persistence.UserInteractionRecordsDOMapper;
import com.welink.commons.utils.NoNullFieldStringStyle;
import com.welink.commons.vo.LotteryDrawRewardVO;
import com.welink.promotion.PromotionErrorEnum;
import com.welink.promotion.PromotionType;
import com.welink.promotion.drools.DroolsExecutor;
import com.welink.promotion.drools.Utils;
import com.welink.promotion.reactive.PromotionResult;
import com.welink.promotion.reactive.UserInteractionRequest;
import com.welink.promotion.reactive.services.coupons.UserCouponService;
import com.welink.promotion.reactive.services.points.UserPointService;

/**
 * Created by saarixx on 5/3/15.
 */
@Service
public class LotteryDrawInteractionService {

    static Logger logger = LoggerFactory.getLogger(LotteryDrawInteractionService.class);

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
    
    @Resource
    private AppointmentTradeService appointmentTradeService;
    
    @Resource
    private ItemMapper itemMapper;
    
    /**
     * 
     * interactive:(抽奖送礼品). <br/>
     * TODO(这里描述这个方法适用条件 – 可选).<br/>
     *
     * @author LuoGuangChun
     * @param profileId
     * @param lotteryDrawRewardVO
     * @return
     * @since JDK 1.6
     */
    public LotteryDrawRewardVO interactive(Long profileId) {
    	LotteryDrawRewardVO lotteryDrawRewardVO = new LotteryDrawRewardVO();
    	lotteryDrawRewardVO.setIndex(-1);
    	
    	UserInteractionRequest userInteractionRequest = new UserInteractionRequest();
        userInteractionRequest.setUserId(profileId);
        int tatol = countUserInteractionRecordsDO(profileId, PromotionType.CHANCE_LOTTERY_DRAW.getCode(), false);
        int usetatol = countUserInteractionRecordsDO(profileId, PromotionType.USE_CHANCE_LOTTERY_DRAW.getCode(), false);
        if((tatol-usetatol) < 1){	//没有抽奖机会
        	return null;
        }
        
        //userInteractionRequest.getParams().putAll(params);
        /*if(Utils.lucky("0.65")){
        	Random r = new Random();
        	lotteryDrawRewardVO.setType(r.nextInt(2)+1);
        }else{
        	lotteryDrawRewardVO.setType(3);
        	//lotteryDrawRewardVO.setType(1);
        }*/
        /*Random r = new Random();
    	lotteryDrawRewardVO.setType(r.nextInt(3)+1);*/
    	
    	int todayPointCount = countUserInteractionRecordsDO(profileId, PromotionType.POINT_LOTTERY_DRAW.getCode(), false);
    	int todayCouponCount = countUserInteractionRecordsDO(profileId, PromotionType.COUPON_LOTTERY_DRAW.getCode(), false);
    	int todayItemCount = countUserInteractionRecordsDO(profileId, PromotionType.ITEM_LOTTERY_DRAW.getCode(), false);
    	
    	if(todayPointCount < 1 && todayCouponCount < 1 && todayItemCount < 1){
    		return null;
    	}else if(todayPointCount > 0 && todayCouponCount < 1 && todayItemCount < 1){
    		lotteryDrawRewardVO.setType(1);
    	}else if(todayCouponCount > 0 && todayPointCount < 1 && todayItemCount < 1){
    		lotteryDrawRewardVO.setType(2);
    	}else if(todayItemCount > 0 && todayPointCount < 1 && todayCouponCount < 1){
    		lotteryDrawRewardVO.setType(3);
    	}else{
    		Random r = new Random();
        	lotteryDrawRewardVO.setType(r.nextInt(2)+1);
    	}
    	
    	long userId = checkNotNull(userInteractionRequest).getUserId();
    	Optional<PromotionResult> promotionResultOptional = null;
    	//送积分
    	if(lotteryDrawRewardVO.getType() == 1){
    		userInteractionRequest.setType(PromotionType.POINT_LOTTERY_DRAW.getCode());		//抽奖
    		promotionResultOptional = givePoint(userInteractionRequest, userId, lotteryDrawRewardVO);
    	}
    	//送优惠券
    	if(lotteryDrawRewardVO.getType() == 2){
    		userInteractionRequest.setType(PromotionType.COUPON_LOTTERY_DRAW.getCode());		//抽奖
    		promotionResultOptional = giveCoupon(userInteractionRequest, userId, lotteryDrawRewardVO);
    	}
    	
    	//送商品
    	if(lotteryDrawRewardVO.getType() == 3){
    		userInteractionRequest.setType(PromotionType.ITEM_LOTTERY_DRAW.getCode());		//抽奖
    		promotionResultOptional = giveItem(userInteractionRequest, userId, lotteryDrawRewardVO);
    	}
    	//记录抽奖使用机会
    	UserInteractionRecordsDO userInteractionRecordsDO = new UserInteractionRecordsDO();
    	userInteractionRecordsDO.setUserId(profileId);
    	userInteractionRecordsDO.setType(PromotionType.USE_CHANCE_LOTTERY_DRAW.getCode());
    	userInteractionRecordsDO.setStatus((byte)1);
    	userInteractionRecordsDO.setDestination(String.valueOf(lotteryDrawRewardVO.getIndex()));
    	userInteractionRecordsDO.setVersion(1L);
    	userInteractionRecordsDO.setDateCreated(new Date());
    	userInteractionRecordsDO.setLastUpdated(new Date());
    	if (userInteractionRecordsDOMapper.insertSelective(userInteractionRecordsDO) != 1) {
    		logger.error("userInteractionRecordsDOMapper insert error, the input parameters is {}",
    				ToStringBuilder.reflectionToString(userInteractionRecordsDO, new NoNullFieldStringStyle()));
    		return null;
    	}
    	//if(null == promotionResultOptional || !promotionResultOptional.isPresent() || (promotionResultOptional.isPresent() && promotionResultOptional.get() != null && !promotionResultOptional.get().getReward())){
    	if(null == promotionResultOptional || 
    			null == lotteryDrawRewardVO.getIndex() || lotteryDrawRewardVO.getIndex() < 0){
    		return null;
    	}
        return lotteryDrawRewardVO;
    }
    
    /**
     * 
     * givePoint:(抽奖送积分). <br/>
     * TODO(这里描述这个方法适用条件 – 可选).<br/>
     *
     * @author LuoGuangChun
     * @param userInteractionRequest
     * @param userId
     * @return
     */
    public Optional<PromotionResult> givePoint(UserInteractionRequest userInteractionRequest, long userId, LotteryDrawRewardVO lotteryDrawRewardVO){
    	
    	UserInteractionRecordsDO userInteractionRecordsDO = transform(userInteractionRequest);
        checkNotNull(userInteractionRecordsDO);

        PromotionResult promotionResult = new PromotionResult();
        promotionResult.setType(userInteractionRequest.getType());
        promotionResult.setFrom(userInteractionRequest.getFrom());
    	
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

        List<UserInteractionRecordsDO> latestUserInteractionRecordsDOs = getLatestUserInteractionRecordsDO(userId,PromotionType.POINT_LOTTERY_DRAW.getCode(), 3);
        
        if (!latestUserInteractionRecordsDOs.isEmpty()) {
            droolsExecutor.execute(promotionResult, pointAccountDO, userInteractionRecordsDO, latestUserInteractionRecordsDOs.get(0), pointRecordDO, lotteryDrawRewardVO);
        } else {
            droolsExecutor.execute(promotionResult, pointAccountDO, userInteractionRecordsDO, pointRecordDO, lotteryDrawRewardVO);
        }

        if (promotionResult.getReward() == null || !promotionResult.getReward()) {
            return null;
        }

        boolean b = userPointService.updateUserPoint(userInteractionRecordsDO, pointRecordDO, pointAccountDO);

        if (!b) {
            return Optional.absent();
        }

        promotionResult.setActionId(userInteractionRecordsDO.getId());
        promotionResult.setPromotionId(pointRecordDO.getId());
        return Optional.of(promotionResult);
    }
    
    /**
     * 
     * giveCoupon:(抽奖送优惠券). <br/>
     *
     * @author LuoGuangChun
     * @param userInteractionRequest
     * @param userId
     * @return
     * @since JDK 1.6
     */
    public Optional<PromotionResult> giveCoupon(UserInteractionRequest userInteractionRequest, long userId, LotteryDrawRewardVO lotteryDrawRewardVO){
    	userInteractionRequest.getParams().putAll(ImmutableMap.of("user_coupon", new UserCouponDO(), "couponDOs", Lists.newArrayList(new CouponDO())));
    	
    	UserInteractionRecordsDO userInteractionRecordsDO = transform(userInteractionRequest);
        checkNotNull(userInteractionRecordsDO);

        PromotionResult promotionResult = new PromotionResult();
        promotionResult.setType(userInteractionRequest.getType());
        promotionResult.setFrom(userInteractionRequest.getFrom());
    	
    	List objects = getLatestUserInteractionRecordsDO(userId, PromotionType.COUPON_LOTTERY_DRAW.getCode(), 3);

        UserCouponDO userCouponDO = checkNotNull((UserCouponDO) userInteractionRequest.getParams().get("user_coupon"));

        List<CouponDO> couponDOs = (List<CouponDO>) userInteractionRequest.getParams().get("couponDOs");

        if (couponDOs != null) {
            Collections.shuffle(couponDOs);
            objects.addAll(couponDOs);
        }

        objects.add(userCouponDO);
        objects.add(userInteractionRecordsDO);
        objects.add(promotionResult);
        objects.add(lotteryDrawRewardVO);


        droolsExecutor.execute(objects.toArray());

        if (promotionResult.getReward() == null || !promotionResult.getReward()) {
            promotionResult.setReward(false);
            promotionResult.setCode(PromotionErrorEnum.COUPON_LACK_LUCKY.getCode());
            promotionResult.setMessage(PromotionErrorEnum.COUPON_LACK_LUCKY.getMsg());
        }

        if (promotionResult.getReward()) {
        	
        	// 互动插入，绝对不能放到上面去
            userInteractionRecordsDO.setVersion(1L);
            userInteractionRecordsDO.setDateCreated(new Date());
            if (userInteractionRecordsDOMapper.insert(userInteractionRecordsDO) != 1) {
                logger.error("userInteractionRecordsDOMapper insert error, the input parameters is {}",
                        ToStringBuilder.reflectionToString(userInteractionRecordsDO, new NoNullFieldStringStyle()));
                return Optional.absent();
            }

            boolean b = userCouponService.updateUserCoupon(userInteractionRecordsDO, userCouponDO);

            if (!b) {
                return Optional.absent();
            }

            promotionResult.setActionId(userInteractionRecordsDO.getId());
            promotionResult.setPromotionId(userCouponDO.getId());
        }
    	return Optional.of(promotionResult);
    }
    
    /**
     * 
     * giveItem:(抽奖送商品). <br/>
     * TODO(这里描述这个方法适用条件 – 可选).<br/>
     *
     * @author LuoGuangChun
     * @param userInteractionRequest
     * @param userId
     * @param lotteryDrawRewardVO
     * @return
     */
    public Optional<PromotionResult> giveItem(UserInteractionRequest userInteractionRequest, long userId, LotteryDrawRewardVO lotteryDrawRewardVO){
    	UserInteractionRecordsDO userInteractionRecordsDO = transform(userInteractionRequest);
        checkNotNull(userInteractionRecordsDO);

        PromotionResult promotionResult = new PromotionResult();
        promotionResult.setType(userInteractionRequest.getType());
        promotionResult.setFrom(userInteractionRequest.getFrom());
    	
    	List objects = getLatestUserInteractionRecordsDO(userId, PromotionType.ITEM_LOTTERY_DRAW.getCode(), 3);
    	
        objects.add(userInteractionRecordsDO);
        objects.add(promotionResult);
        objects.add(lotteryDrawRewardVO);


        droolsExecutor.execute(objects.toArray());


        if (promotionResult.getReward() != null && promotionResult.getReward() && null != lotteryDrawRewardVO && null != lotteryDrawRewardVO.getRewardId()) {
        	Item item = itemMapper.selectByPrimaryKey(lotteryDrawRewardVO.getRewardId());
        	if(null != item && null != item.getNum() && item.getNum() > 0
        			&& null != item.getApproveStatus() && item.getApproveStatus().equals((byte)1)){		//判断商品是否有库存
        		// 互动插入，绝对不能放到上面去
            	userInteractionRecordsDO.setDestination(lotteryDrawRewardVO.getRewardId().toString());		//商品id保存到这个字段
            	userInteractionRecordsDO.setValue(0); 		//只适合抽奖商品记录value(0=未领取；1=已领取)
            	userInteractionRecordsDO.setVersion(1L);
            	userInteractionRecordsDO.setDateCreated(new Date());
            	if (userInteractionRecordsDOMapper.insert(userInteractionRecordsDO) != 1) {
            		logger.error("userInteractionRecordsDOMapper insert error, the input parameters is {}",
            				ToStringBuilder.reflectionToString(userInteractionRecordsDO, new NoNullFieldStringStyle()));
            		return Optional.absent();
            	}else{
            		return Optional.of(promotionResult);
            	}
        	}else{
        		promotionResult.setReward(false);
                return Optional.absent();
        	}
        	
        }else{
        	promotionResult.setReward(false);
        	//promotionResult.setCode(PromotionErrorEnum.COUPON_LACK_LUCKY.getCode());
            //promotionResult.setMessage(PromotionErrorEnum.COUPON_LACK_LUCKY.getMsg());
            return Optional.absent();
        }
    	
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
        if(limit > 0){
        	userInteractionRecordsDOExample.setLimit(limit);
        }
        userInteractionRecordsDOExample.createCriteria() //
                .andUserIdEqualTo(userId) //
                .andTypeEqualTo(type)
                .andDateCreatedGreaterThan(getTimesmorning());

        return userInteractionRecordsDOMapper.selectByExample(userInteractionRecordsDOExample);
    }
    
    public int countUserInteractionRecordsDO(long userId, int type, boolean isToday) {
        UserInteractionRecordsDOExample userInteractionRecordsDOExample = new UserInteractionRecordsDOExample();
        userInteractionRecordsDOExample.setOrderByClause("id DESC");
        if(isToday){
        	userInteractionRecordsDOExample.createCriteria() //
        	.andUserIdEqualTo(userId) //
        	.andTypeEqualTo(type)
        	.andDateCreatedGreaterThan(getTimesmorning());
        }else{
        	userInteractionRecordsDOExample.createCriteria() //
        	.andUserIdEqualTo(userId) //
        	.andTypeEqualTo(type);
        }

        return userInteractionRecordsDOMapper.countByExample(userInteractionRecordsDOExample);
    }
    
    private List<UserInteractionRecordsDO> getLotteryDrawLatestUserInteractionRecordsDO(long userId, int limit) {
    	List<Integer> types = new ArrayList<Integer>();
    	types.add(PromotionType.POINT_LOTTERY_DRAW.getCode());
    	types.add(PromotionType.COUPON_LOTTERY_DRAW.getCode());
    	types.add(PromotionType.ITEM_LOTTERY_DRAW.getCode());
        UserInteractionRecordsDOExample userInteractionRecordsDOExample = new UserInteractionRecordsDOExample();
        userInteractionRecordsDOExample.setOrderByClause("id DESC");
        userInteractionRecordsDOExample.setLimit(limit);
        userInteractionRecordsDOExample.createCriteria() //
                .andUserIdEqualTo(userId) //
                .andTypeIn(types)
                .andDateCreatedGreaterThan(getTimesmorning());

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
    
    private Date getTimesmorning(){  
        Calendar todayStart = Calendar.getInstance();  
        todayStart.set(Calendar.HOUR, 0);  
        todayStart.set(Calendar.MINUTE, 0);  
        todayStart.set(Calendar.SECOND, 0);  
        todayStart.set(Calendar.MILLISECOND, 0);  
        return todayStart.getTime();  
    }

}
