package com.welink.buy.service.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mysql.jdbc.Connection;
import com.welink.buy.model.FailedItem;
import com.welink.buy.service.AppointmentTradeService;
import com.welink.buy.service.BuyItemService;
import com.welink.buy.utils.BaseResult;
import com.welink.buy.utils.BuyItemResultCode;
import com.welink.buy.utils.Constants;
import com.welink.commons.Env;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.commons.TradeUtil;
import com.welink.commons.domain.CommunityDO;
import com.welink.commons.domain.ConsigneeAddrDO;
import com.welink.commons.domain.ConsigneeAddrDOExample;
import com.welink.commons.domain.CouponDO;
import com.welink.commons.domain.GrouponDO;
import com.welink.commons.domain.GrouponDOExample;
import com.welink.commons.domain.InstallActiveDO;
import com.welink.commons.domain.InstallActiveDOExample;
import com.welink.commons.domain.Item;
import com.welink.commons.domain.ItemExample;
import com.welink.commons.domain.LogisticsDO;
import com.welink.commons.domain.MikuCrowdfundDO;
import com.welink.commons.domain.MikuCrowdfundDetailDO;
import com.welink.commons.domain.MikuItemShareParaDO;
import com.welink.commons.domain.MikuItemShareParaDOExample;
import com.welink.commons.domain.MikuMineDetectReportDO;
import com.welink.commons.domain.MikuMineScBoxDO;
import com.welink.commons.domain.MikuScratchCardDO;
import com.welink.commons.domain.ObjectTaggedDO;
import com.welink.commons.domain.ObjectTaggedDOExample;
import com.welink.commons.domain.Order;
import com.welink.commons.domain.OrderExample;
import com.welink.commons.domain.PointAccountDO;
import com.welink.commons.domain.PointAccountDOExample;
import com.welink.commons.domain.Snapshot;
import com.welink.commons.domain.TagsDO;
import com.welink.commons.domain.TagsDOExample;
import com.welink.commons.domain.Trade;
import com.welink.commons.domain.TradeExample;
import com.welink.commons.domain.TradeExample.Criteria;
import com.welink.commons.domain.UsePromotionDO;
import com.welink.commons.domain.UserCouponDO;
import com.welink.commons.domain.UserInteractionRecordsDO;
import com.welink.commons.domain.UserInteractionRecordsDOExample;
import com.welink.commons.model.ItemActivityTag;
import com.welink.commons.model.ItemLimitTag;
import com.welink.commons.persistence.CommunityDOMapper;
import com.welink.commons.persistence.ConsigneeAddrDOMapper;
import com.welink.commons.persistence.CouponDOMapper;
import com.welink.commons.persistence.GrouponDOMapper;
import com.welink.commons.persistence.InstallActiveDOMapper;
import com.welink.commons.persistence.ItemMapper;
import com.welink.commons.persistence.LogisticsDOMapper;
import com.welink.commons.persistence.MikuCrowdfundDOMapper;
import com.welink.commons.persistence.MikuCrowdfundDetailDOMapper;
import com.welink.commons.persistence.MikuItemShareParaDOMapper;
import com.welink.commons.persistence.MikuMineDetectReportDOMapper;
import com.welink.commons.persistence.MikuMineQuestionnaireRecordsDOMapper;
import com.welink.commons.persistence.MikuMineScBoxDOMapper;
import com.welink.commons.persistence.MikuScratchCardDOMapper;
import com.welink.commons.persistence.ObjectTaggedDOMapper;
import com.welink.commons.persistence.OrderMapper;
import com.welink.commons.persistence.PointAccountDOMapper;
import com.welink.commons.persistence.SnapshotMapper;
import com.welink.commons.persistence.TagsDOMapper;
import com.welink.commons.persistence.TradeMapper;
import com.welink.commons.persistence.UsePromotionDOMapper;
import com.welink.commons.persistence.UserCouponDOMapper;
import com.welink.commons.persistence.UserInteractionRecordsDOMapper;
import com.welink.commons.utils.TimeUtils;
import com.welink.commons.vo.MikuActiveTopicVO;
import com.welink.commons.vo.MikuMineScBoxVO;
import com.welink.commons.vo.TopicParameterVO;
import com.welink.promotion.PromotionType;
import com.welink.promotion.reactive.PromotionResult;
import com.welink.promotion.reactive.UserInteractionEffect;
import com.welink.promotion.reactive.UserInteractionRequest;

/**
 * 创建订单
 * <p/>
 * Created by saarixx on 15/9/14.
 */
@Service(value = "appointmentTradeService")
public class AppointmentTradeServiceImpl implements AppointmentTradeService, InitializingBean {

    static Logger logger = LoggerFactory.getLogger(AppointmentTradeServiceImpl.class);

    @Resource
    private ItemMapper itemMapper;

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private InstallActiveDOMapper installActiveDOMapper;

    @Resource
    private TradeMapper tradeMapper;

    @Resource
    private GrouponDOMapper grouponDOMapper;

    @Resource
    private SnapshotMapper snapshotMapper;

    @Resource
    private BuyItemService buyItemService;

    @Resource
    private PlatformTransactionManager transactionManager;

    @Resource
    private ObjectTaggedDOMapper objectTaggedDOMapper;

    @Resource
    private UserInteractionEffect userInteractionEffect;

    @Resource
    private CouponDOMapper couponDOMapper;

    @Resource
    private UserCouponDOMapper userCouponDOMapper;

    @Resource
    private PointAccountDOMapper pointAccountDOMapper;

    @Resource
    private UsePromotionDOMapper usePromotionDOMapper;
    
    @Resource
    private UserInteractionRecordsDOMapper userInteractionRecordsDOMapper;
    
    @Resource
    private ConsigneeAddrDOMapper consigneeAddrDOMapper;

    @Resource
    private TagsDOMapper tagsDOMapper;
    
    @Resource
    private MikuItemShareParaDOMapper mikuItemShareParaDOMapper;
    
    @Resource
	private MikuCrowdfundDOMapper mikuCrowdfundDOMapper;
    
    @Resource
	private MikuCrowdfundDetailDOMapper mikuCrowdfundDetailDOMapper;


    private TransactionTemplate transactionTemplate;
    
    @Resource
    private LogisticsDOMapper logisticsDOMapper;
    
    @Resource
    private CommunityDOMapper communityDOMapper;
    
    @Resource
    private MikuScratchCardDOMapper mikuScratchCardDOMapper;
    
    @Resource
    private MikuMineDetectReportDOMapper mikuMineDetectReportDOMapper;
    
    @Resource
	private MikuMineScBoxDOMapper mikuMineScBoxDOMapper;
    
    @Resource
    private Env env;

    @Override
    public BaseResult<Trade> createNewAppointment(@Nonnull final Long sellerId,
                                                  @Nonnull Long shopId,
                                                  @Nonnull final Long communityId,
                                                  @Nonnull final Long buyerId,
                                                  @Nonnull final Long buildingId,
                                                  @Nonnull List<ImmutablePair<Long, Integer>> itemIdNumPairList,
                                                  @Nonnull final Date consignTime,
                                                  @Nullable final String picUrls,
                                                  @Nullable final String buyerMessage,
                                                  @Nullable final byte tradeFrom,
                                                  @Nonnull final byte payType,
                                                  @Nonnull long consigneeId,
                                                  @Nonnull final byte shippingType,
                                                  @Nonnull final long point,
                                                  @Nonnull final long userCouponId,
                                                  final Date appointDeliveryTime,
                                                  @Nonnull final Byte orderType2, 
                                                  final Long objectId,
                                                  final Long crowdfundDetailId,
                                                  final Long scratchCardId,
                                                  Long pUserId
    ) {
        checkNotNull(shopId);
        checkNotNull(communityId);
        checkNotNull(payType);
        checkNotNull(buyerId);
        checkNotNull(buildingId);
        checkArgument(!checkNotNull(itemIdNumPairList).isEmpty());
        checkNotNull(consignTime);
        checkNotNull(orderType2);

        /*if (shippingType != BizConstants.SELF_PICK_SHIPPING_TYPE && communityId < 0) {
            logger.error("非自提订单并且不在服务范围内，限制下单,buyerId:" + buyerId + ",shopId:" + shopId);
            return BaseResult.failure(BuyItemResultCode.NOT_IN_SERVICE_ZONE.getCode(), BuyItemResultCode.NOT_IN_SERVICE_ZONE.getMessage());
        }*/
        
        final List<Long> itemIds = Lists.newArrayListWithCapacity(itemIdNumPairList.size());
        final Map<Long, Integer> itemIdNumMap = Maps.newHashMapWithExpectedSize(itemIdNumPairList.size());

        for (ImmutablePair<Long, Integer> itemIdNumPair : itemIdNumPairList) {
            itemIds.add(itemIdNumPair.getLeft());
            itemIdNumMap.put(itemIdNumPair.getLeft(), itemIdNumPair.getRight());
        }
        
        if(orderType2.equals(Constants.TradeType.dz_type.getTradeTypeId())){
        	 Map<String, Object> paramMap = new HashMap<String, Object>();
        	 paramMap.put("boxId", objectId);	//盒子id
             paramMap.put("userId", buyerId);
             paramMap.put("hasTrade", 1);	//是否有订单(0=全部；1=有订单) 默认0
             paramMap.put("allTrade", 0);	//是否查询全部订单(0=没取消的订单；1=全部订单)
             List<MikuMineScBoxVO> mineScBoxTradeList = mikuMineScBoxDOMapper.getMineScBoxTradeList(paramMap);
             if(!mineScBoxTradeList.isEmpty()){
            	 //该盒子已下单，不能重复下单
            	 return BaseResult.failure(-999, "亲~该盒子已下单，不能重复下单~");
             }
        }
        		

        ItemExample itemExample = new ItemExample();
        itemExample.createCriteria() //
                .andIdIn(itemIds);

        final List<Item> items = itemMapper.selectByExample(itemExample);
        final ConsigneeAddrDO consigneeAddrDO = fetchDefConsignee(buyerId);	//获取默认地址
        boolean isTaxFree = false;	//true=有免税产品; false=没有免税产品;免税产品需要身份证验证 
        Byte hasReturnGoods = (byte)0;	//是否有可退货商品(0=没有;1=有)

        // 先判断商品是否存在
        if (items == null || items.size() != itemIdNumPairList.size()) {
            logger.error("it can not happen, the item with ids - {} dose not exist..", ToStringBuilder.reflectionToString(itemIds));
            return BaseResult.failure(BuyItemResultCode.BUY_ITEM_ERROR_ITEM_NOT_EXSIST.getCode(), BuyItemResultCode.BUY_ITEM_ERROR_ITEM_NOT_EXSIST.getMessage());
        }
        // 在判断商品是否下架
        Map<String, List<FailedItem>> failedMap = new HashMap<>();
        List<FailedItem> failStatusItems = Lists.newArrayList();
        List<FailedItem> failOutItems = Lists.newArrayList();
        Date nowDate = new Date();
        //LogisticsDO logisticsDO = logisticsDOMapper.selectByPrimaryKey(consigneeId);
        
        //获取抢购活动标
        final List<ObjectTaggedDO> panicBuyingItemTags =  fetchPanicBuyingTagViaItemIds(itemIds, BizConstants.SearchTagEnum.PANIC_BUYING.getTag());
        for (Item item : items) {
            if (Constants.ApproveStatus.ON_SALE.getApproveStatusId() != item.getApproveStatus()) {
                logger.error("the item with id {} is in stock", item.getId());
                FailedItem fItem = new FailedItem();
                fItem.setItemId(item.getId());
                fItem.setApproveStatus(item.getApproveStatus());
                failStatusItems.add(fItem);
                //return BaseResult.failure(BuyItemResultCode.BUY_ITEM_ERROR_NOT_START.getCode(), BuyItemResultCode.BUY_ITEM_ERROR_NOT_START.getMessage(),failItems);
            } else if (item.getNum() < itemIdNumMap.get(item.getId())) {
                FailedItem fItem = new FailedItem();
                fItem.setItemId(item.getId());
                fItem.setApproveStatus(item.getApproveStatus());
                failStatusItems.add(fItem);
                logger.error("buy num greater than stock itemId:", item.getId());
                //return BaseResult.failure(BuyItemResultCode.BUY_ITEM_ERROR_QUANTITY_TOO_LARGE.getCode(), BuyItemResultCode.BUY_ITEM_ERROR_QUANTITY_TOO_LARGE.getMessage(),failItems);
            }
//            } else {
//                ItemAtHalfDOExample itemAtHalfDOExample = new ItemAtHalfDOExample();
//                itemAtHalfDOExample.createCriteria().andStatusEqualTo((byte) 1).andActiveStatusEqualTo(15).andItemIdEqualTo(item.getId());
//                List<ItemAtHalfDO> itemAtHalfDOs = itemAtHalfDOMapper.selectByExample(itemAtHalfDOExample);
//                if (itemAtHalfDOs != null && itemAtHalfDOs.size() > 0) {
//                    if (itemIdNumMap.get(item.getId()) > itemAtHalfDOs.get(0).getLimitNum()) {
//                        logger.error("当前宝贝为活动商品，您购买的数量大于限购数量,itemId:", item.getId());
//                        return BaseResult.failure(BuyItemResultCode.BUY_ITEM_ERROR_QUANTITY_TOO_LARGE.getCode(), BuyItemResultCode.BUY_ITEM_ERROR_QUANTITY_TOO_LARGE.getMessage());
//                    }
//                }
//            }
            if(null == item.getBaseItemId()){
        		FailedItem fItem = new FailedItem();
                fItem.setItemId(item.getId());
                fItem.setApproveStatus(item.getApproveStatus());
                failStatusItems.add(fItem);
        	}
            
            if(orderType2.equals(Constants.TradeType.join_agency.getTradeTypeId()) 
            		&& !item.getType().equals(Constants.TradeType.join_agency.getTradeTypeId())){
            	//商品类型不为成为代理商品
            	return BaseResult.failure(-999, "亲~请选择礼包商品才能成为代理~");
            }else if(orderType2.equals(Constants.TradeType.scratch_card.getTradeTypeId())){
            	if(null == scratchCardId || scratchCardId < 0L){
            		return BaseResult.failure(-999, "亲~刮刮卡下单参数错误~");
            	}
            	if(!item.getType().equals(Constants.TradeType.scratch_card.getTradeTypeId())){
            		//商品类型不为刮刮卡商品
            		return BaseResult.failure(-999, "亲~不是刮刮卡商品不能领取~");
            	}
            	MikuScratchCardDO mikuScratchCardDO = mikuScratchCardDOMapper.selectByPrimaryKey(scratchCardId);
            	if(null != mikuScratchCardDO){
            		if(!mikuScratchCardDO.getUserId().equals(buyerId) || mikuScratchCardDO.getIsReward().equals((byte)0)
            				|| !mikuScratchCardDO.getItemId().equals(item.getId())){
                		return BaseResult.failure(-999, "亲~您未中刮刮卡奖~");
            		}else if(mikuScratchCardDO.getIsReward().equals((byte)1)
            				&& (mikuScratchCardDO.getStatus().equals(Constants.ScratchCardStatus.NO_PAY.getStatusId()) ||
            						mikuScratchCardDO.getStatus().equals(Constants.ScratchCardStatus.PAYED.getStatusId()))){
            			return BaseResult.failure(-999, "亲~您已领取刮刮卡奖品~");
            		}
            	}else{
            		return BaseResult.failure(-999, "亲~您未中刮刮卡奖~");
            	}
            }
           	 if(null != item.getType()
                		&& item.getType().equals(Constants.TradeType.join_agency.getTradeTypeId())){
           		 //代理礼包
           		 OrderExample orderExample = new OrderExample();
           		 orderExample.createCriteria().andBuyerIdEqualTo(buyerId)
           		 	.andArtificialIdEqualTo(item.getId());
           		 List<Order> orderList = orderMapper.selectByExample(orderExample);
           		 List<Long> tradeIds = new ArrayList<Long>();
           		 if(null != orderList && !orderList.isEmpty() && orderList.size() > 0){
           			 for(Order order : orderList){
           				tradeIds.add(order.getTradeId());
           			 }
           			 TradeExample tradeExample = new TradeExample();
           			 tradeExample.createCriteria().andTradeIdIn(tradeIds)
           				.andStatusNotEqualTo(Constants.TradeStatus.TRADE_CLOSED_BY_TAOBAO.getTradeStatusId());
           			 int joinAgencyCount = tradeMapper.countByExample(tradeExample);
           			 if(joinAgencyCount > 0){
           				 //商品类型不为成为代理商品
           				 return BaseResult.failure(-999, "亲~礼包商品一辈子只能买一次哟~");
           			 }
           		 }
           		 
           	 }
           	 
           	if(orderType2.equals(Constants.TradeType.dz_type.getTradeTypeId())
           			&& !item.getType().equals(Constants.TradeType.dz_type.getTradeTypeId())){
           		 //商品类型不为成为代理商品
  				 return BaseResult.failure(-999, "亲~请选择定制产品~");
           	}
            
            if(null != item.getIsTaxFree() && item.getIsTaxFree().equals((byte)1)){	//0=不免税；1=免税
            	//免税商品
            	isTaxFree = true;	//是免税产品
            }
            
            if(null != item.getIsrefund() && item.getIsrefund().equals((byte)0)){
            	//isrefund是否可退货（0=不可退货；1=可退货）
            	hasReturnGoods = (byte)1;	//是否有可退货商品(0=没有;1=有)
            }
            
            //限购检查
            List<Long> itemids = Lists.newArrayList();
            itemids.add(item.getId());
            List<ObjectTaggedDO> objectTaggedDOs = fetchTagViaItemIds(itemids, BizConstants.SearchTagEnum.LIMIT_BUY.getTag());
            if (null != objectTaggedDOs && objectTaggedDOs.size() > 0) {
                ObjectTaggedDO objectTaggedDO = objectTaggedDOs.get(0);
                int limit = fetchLimitCount(objectTaggedDO);
                if (limit > 0) {
                    InstallActiveDOExample qiExample = new InstallActiveDOExample();
                    qiExample.createCriteria().andBuyerIdEqualTo(buyerId).andItemIdEqualTo(itemids.get(0)).andDateCreatedGreaterThan(TimeUtils.long2Date(TimeUtils.getStartTime()));
                    List<InstallActiveDO> installActiveDOs = installActiveDOMapper.selectByExample(qiExample);
                    if (null != installActiveDOs && installActiveDOs.size() > 0) {
                        int buyedcount = installActiveDOs.get(0).getCount();
                        if (buyedcount >= limit) {
                            return BaseResult.failure(BuyItemResultCode.BUY_ITEM_ERROR_QUANTITY_TOO_LARGE.getCode(), BuyItemResultCode.BUY_ITEM_ERROR_QUANTITY_TOO_LARGE.getMessage()+limit+"件哦~");
                        }
                    }
                }
            }
            
          //获取抢购活动标
            if (null != panicBuyingItemTags && panicBuyingItemTags.size() > 0) {
            	for (ObjectTaggedDO itemTag : panicBuyingItemTags) {
                    int activityNum = (null == itemTag.getActivityNum() ? 0 : itemTag.getActivityNum());
                    if(itemIdNumMap.get(itemTag.getArtificialId()) > activityNum){
                    	//抢购已抢光
                    	return BaseResult.failure(BuyItemResultCode.BUY_ITEM_ERROR_PANIC_ITEM_LESS.getCode(), BuyItemResultCode.BUY_ITEM_ERROR_PANIC_ITEM_LESS.getMessage());
                    }
                    if(null == itemTag.getStartTime() || itemTag.getStartTime().getTime() > nowDate.getTime()){
                    	//抢购未开始
                    	return BaseResult.failure(BuyItemResultCode.BUY_ITEM_ERROR_PANIC_NOT_START.getCode(), BuyItemResultCode.BUY_ITEM_ERROR_PANIC_NOT_START.getMessage());
                    }
                    if(null == itemTag.getEndTime() || itemTag.getEndTime().getTime() < nowDate.getTime()){
                    	//抢购已结束
                    	return BaseResult.failure(BuyItemResultCode.BUY_ITEM_ERROR_PANIC_IS_END.getCode(), BuyItemResultCode.BUY_ITEM_ERROR_PANIC_IS_END.getMessage());
                    }
                }
            }
        }

        final List<Long> limitPointObject = fetchContainTagItemIdsViaItemIds(itemIds, BizConstants.SearchTagEnum.NON_POINT.getTag());
        final List<Long> limitCoupon = fetchContainTagItemIdsViaItemIds(itemIds, BizConstants.SearchTagEnum.NON_COUPON.getTag());

        //商品错误返回
        if (failOutItems.size() > 0) {
            failedMap.put(BizConstants.ITEM_OUT_OF_NUM, failOutItems);
        }
        if (failStatusItems.size() > 0) {
            failedMap.put(BizConstants.ITEM_NOT_ON_SAIL, failStatusItems);
        }
        if (failedMap.size() > 0) {
            return BaseResult.failure(BuyItemResultCode.BUY_ITEM_ERROR.getCode(), BuyItemResultCode.BUY_ITEM_ERROR.getMessage(), failedMap);
        }
        
        if(isTaxFree){
        	if(StringUtils.isBlank(consigneeAddrDO.getIdCard())){
        		return BaseResult.failure(-999, "亲~免税商品商品需填写真实姓名和身份证~");
        	}
        }

        final Trade trade = new Trade();

        final Long tradeId = TradeUtil.genTradeNO(buyerId);

        trade.setBuyerId(buyerId);
        //trade.setAppointDeliveryTime(appointDeliveryTime);
        trade.setShopId(shopId);
        //trade.setConsigneeId(consigneeId);
        trade.setTradeFrom(tradeFrom);
        trade.setTradeId(tradeId);
        trade.setShippingType(shippingType);
        trade.setpUserId(pUserId);
        if(null != hasReturnGoods && hasReturnGoods.equals((byte)1)){
        	trade.setHasReturnGoods(hasReturnGoods);	//是否有可退货商品(0=没有;1=有)
        }


        return transactionTemplate.execute(new TransactionCallback<BaseResult<Trade>>() {
            @Override
            public BaseResult<Trade> doInTransaction(TransactionStatus transactionStatus) {
            	MikuMineScBoxDO mikuMineScBoxDO = null;		//定制盒子
                if(orderType2.equals(Constants.TradeType.dz_type.getTradeTypeId())){	//定制订单
                	if(null == objectId){
                		return BaseResult.failure(-999, "亲~没有定制产品~");
                	}
                	mikuMineScBoxDO = mikuMineScBoxDOMapper.selectByPrimaryKey(objectId);
                	if(null == mikuMineScBoxDO || (null != mikuMineScBoxDO && null == mikuMineScBoxDO.getUserId())){
                		return BaseResult.failure(-999, "亲~没有定制产品~");
                	}else{
                		if(!mikuMineScBoxDO.getUserId().equals(buyerId)){
                			return BaseResult.failure(-999, "亲~此定制产品不是您的，不能下单~");
                		}
                	}
                }
            	
                String title = "";
                List<Long> orderIds = Lists.newArrayListWithCapacity(items.size());
                List<ImmutablePair<Order, Item>> orderItemPairList = Lists.newArrayListWithCapacity(items.size());
                
                Byte orderType = orderType2;
                for (Item item : items) {
                	 if(null != item.getType()
                     		&& item.getType().equals(Constants.TradeType.join_agency.getTradeTypeId())){
                		 orderType = Constants.TradeType.join_agency.getTradeTypeId();
                	 }
                }
                
                //插入物流信息
                Long logisticsId = addLogistics(consigneeAddrDO, -1L, (byte)0);
                if (logisticsId < 0) {
                	transactionStatus.setRollbackOnly();
                	return BaseResult.failure(29, "啊哦~订单收货地址出错了~");
                }
                trade.setConsigneeId(logisticsId);	//订单设置物流信息
                
                //查找专题列表
                Map<String, Object> topicParamMap = new HashMap<String, Object>();
                
                topicParamMap.put("inActive", 1);	//活动中
                topicParamMap.put("itemIds", itemIds);
        		List<MikuActiveTopicVO> mikuActiveTopicVOList = itemMapper.selectTopicVOsByItemIds(topicParamMap);	//商品专题列表
        		List<MikuActiveTopicVO> mikuActiveTopicVOList2 = new ArrayList<MikuActiveTopicVO>();	//没有重复的专题列表
        		//商品id对应专题Map
        		Map<Long, MikuActiveTopicVO> itemIdTopicMap = new HashMap<Long, MikuActiveTopicVO>();
        		if(null != mikuActiveTopicVOList && !mikuActiveTopicVOList.isEmpty()){
        			for(MikuActiveTopicVO vo : mikuActiveTopicVOList){
        				itemIdTopicMap.put(vo.getItemId(), vo);
        				if(!mikuActiveTopicVOList2.isEmpty()){
    						boolean topicFlag = false;
    						for(MikuActiveTopicVO vo2 : mikuActiveTopicVOList2){
    							if(vo.getId().equals(vo2.getId())){
    								topicFlag = true;
    							}
    						}
    						if(!topicFlag){	//如果没有重复的加入专题列表
    							mikuActiveTopicVOList2.add(vo);
    						}
    					}else{
    						mikuActiveTopicVOList2.add(vo);
    					}
        			}
        		}
                
                //-------------众筹订单-----------------------------------------------
                trade.setTimeoutActionTime(new DateTime(consignTime.getTime()).plusMinutes(BizConstants.TRADE_OUT_OF_DATE_MINUTE_24_HOUR).toDate());
                Date timeoutActionTime = null;
                //若是众筹订单
                if(Byte.compare(orderType, Constants.TradeType.crowdfund_type.getTradeTypeId()) == 0) {
	                MikuCrowdfundDetailDO mikuCrowdfundDetailDO = mikuCrowdfundDetailDOMapper.selectByPrimaryKey(crowdfundDetailId);
	                if(null != mikuCrowdfundDetailDO && null != mikuCrowdfundDetailDO.getCrowdfundId()){
	                	if(!mikuCrowdfundDetailDO.getItemId().equals(itemIds.get(0))){
	                		//此商品不是当前众筹商品
	                        return BaseResult.failure(BuyItemResultCode.NOT_CROWDFUND_ITEM.getCode(), BuyItemResultCode.NOT_CROWDFUND_ITEM.getMessage());
	                	}
	                	trade.setCrowdfundId(mikuCrowdfundDetailDO.getCrowdfundId()); 	//交易设置众筹id
	                	Map<String,Object> crowdfundDetailParamMap = new HashMap<String, Object>();
	                	crowdfundDetailParamMap.put("id", mikuCrowdfundDetailDO.getId());
	                	crowdfundDetailParamMap.put("soldNum", itemIdNumMap.get(itemIds.get(0)));
	                	if(tradeMapper.updateCrowdfundDetailSoldNum(crowdfundDetailParamMap) != 1){
	                		//更新众筹明细支持数
	                		transactionStatus.setRollbackOnly();
	                        return BaseResult.failure(-999, "更新众筹明细支持数失败");
	                	}
	                	MikuCrowdfundDO mikuCrowdfundDO = mikuCrowdfundDOMapper.selectByPrimaryKey(mikuCrowdfundDetailDO.getCrowdfundId());
	                	if(null != mikuCrowdfundDO){
	                		Date nowDate = new Date();
	                		if (Constants.ApproveStatus.ON_SALE.getApproveStatusId() != mikuCrowdfundDO.getApproveStatus()) {
	                			transactionStatus.setRollbackOnly();
	    	                	return BaseResult.failure(-999, "亲~该众筹未上架");
	                		}
	                		if (null == mikuCrowdfundDO.getStatus() ||
	                				(mikuCrowdfundDO.getStatus() != (byte)0)) {
	                			//众筹状态(-1=无效;0=正常;1=成功;2=失败)
	                			transactionStatus.setRollbackOnly();
	    	                	return BaseResult.failure(-999, "亲~该众筹已结束");
	                		}
	                		if(mikuCrowdfundDO.getStartTime() == null
	                				|| mikuCrowdfundDO.getEndTime() == null){
	                			transactionStatus.setRollbackOnly();
	    	                	return BaseResult.failure(-999, "亲~该众筹未开始或已过期");
	                		}
	                		if(mikuCrowdfundDO.getStartTime().getTime() > nowDate.getTime() 
	                				|| mikuCrowdfundDO.getEndTime().getTime() < nowDate.getTime()){
	                			transactionStatus.setRollbackOnly();
	    	                	return BaseResult.failure(-999, "亲~该众筹未开始或已过期");
	                		}
	                		timeoutActionTime = mikuCrowdfundDO.getEndTime();	//交易过期时间
	                		Integer plusDayCrowd = (null == mikuCrowdfundDO.getPlusDay() ? 0 : mikuCrowdfundDO.getPlusDay());	//众筹后多少天后发货
	                		trade.setTimeoutActionTime(TimeUtils.addDay(timeoutActionTime, plusDayCrowd));//设置众筹的过期时间
	                		Map<String,Object> crowdfundParamMap = new HashMap<String, Object>();
	                		crowdfundParamMap.put("id", mikuCrowdfundDO.getId());
	                		Long crowdfundTotalFee = items.get(0).getPrice() * itemIdNumMap.get(itemIds.get(0));
		                	crowdfundParamMap.put("soldNum", itemIdNumMap.get(itemIds.get(0)));
		                	crowdfundParamMap.put("totalFee", crowdfundTotalFee);	//更新众筹已购金额
		                	
		                	if(tradeMapper.updateCrowdfundSoldNum(crowdfundParamMap) != 1){
		                		//更新众筹支持数
		                		transactionStatus.setRollbackOnly();
		                        return BaseResult.failure(-999, "更新众筹支持数失败");
		                	}
	                	}else{
	                		transactionStatus.setRollbackOnly();
    	                	return BaseResult.failure(-999, "亲~没有此众筹");
	                	}
	                }
	                if(null == timeoutActionTime){
	                	//设置众筹的过期时间
	                	//trade.setTimeoutActionTime(new DateTime(timeoutActionTime.getTime()).plusMinutes(BizConstants.TRADE_OUT_OF_DATE_MINUTE_24_HOUR).toDate());
	                	transactionStatus.setRollbackOnly();
	                	return BaseResult.failure(-999, "亲~众筹不能为空");
	                }
                }
                //-------------------------------------------------------------
                

                long totalFee = 0L;

                Map<Long, Long> itemTotalFeeMap = new HashMap<Long, Long>();	//计算好后的商品金额
                Map<Long, Long> topicTotalFeeMap = new HashMap<Long, Long>();	//计算好后的专题金额
                boolean grouponItem = false;
                boolean isFirstInstall = false;
                long validCouponTotalFee = 0L;
                long validPointTotalFee = 0L;
                long tradeItemProfitFeeSum = 0L;		//订单的公司总利润；
                for (Item item : items) {
                    // 创建订单，任何服务都需要这样的订单
                    Order appointmentServiceOrder = new Order();
                    appointmentServiceOrder.setCommunityId(communityId);
                    appointmentServiceOrder.setCategoryId(item.getCategoryId());
                    appointmentServiceOrder.setArtificialId(item.getId());
                    appointmentServiceOrder.setBuyerId(buyerId);
                    appointmentServiceOrder.setShippingType((int) shippingType);
                    appointmentServiceOrder.setTradeId(tradeId);
                    long grouponPrice = 0;
                    String grouponTitle = item.getTitle();
                    if (Byte.compare(item.getType(), Constants.TradeType.groupon.getTradeTypeId()) == 0) {
                        grouponItem = true;
                        GrouponDOExample gExample = new GrouponDOExample();
                        gExample.createCriteria().andItemIdEqualTo(item.getId()).andStatusEqualTo((byte) 1);
                        gExample.setOrderByClause("online_end_time DESC");
                        List<GrouponDO> grouponDOs = grouponDOMapper.selectByExample(gExample);
                        if (null != grouponDOs && grouponDOs.size() > 0) {
                            appointmentServiceOrder.setTitle(grouponDOs.get(0).getItem_title());
                            grouponPrice = grouponDOs.get(0).getGrouponPrice();
                            grouponTitle = grouponDOs.get(0).getItem_title();
                        } else {
                            appointmentServiceOrder.setTitle(item.getTitle());
                        }
                    } else {
                        appointmentServiceOrder.setTitle(item.getTitle());
                    }
                    //通过打标确定是否需要运费
                    ObjectTaggedDO objectTaggedDO = buyItemService.fetchTagObjectsViaItemId(item.getId(), BizConstants.SearchTagEnum.NON_POST_FEE.getTag());
                    if (null != objectTaggedDO) {
                        logger.info("=======不收运费商品 itemId:" + item.getId());
                        isFirstInstall = true;
                    }

                    //去除预约时间
                    //appointmentServiceOrder.setConsignTime(consignTime);
                    appointmentServiceOrder.setDateCreated(new Date());
                    appointmentServiceOrder.setIsServiceOrder((byte) 1);
                    appointmentServiceOrder.setNum(itemIdNumMap.get(item.getId()));
                    appointmentServiceOrder.setSellerId(sellerId);
                    appointmentServiceOrder.setSellerType((byte) 1);
                    appointmentServiceOrder.setHasPanicTag((byte)0);	//是否有抢购标(0=没有;1=有)
                    appointmentServiceOrder.setIsReturnProfit((byte)0);	//是否退分润(0=没退;1=已退)
                    // 如果输入有图片，就把图片放到交易里面，如果商品没有图片，就把图片添加到商品里面；如果没有输入图片，就看商品表里面有没有，有图片就放入图片
                    if (StringUtils.isNoneBlank(picUrls)) {
                        appointmentServiceOrder.setPicUrl(picUrls);
                        if (StringUtils.isNoneBlank(item.getPicUrls())) {
                            item.setPicUrls(picUrls);
                        }
                    } else {
                        if (StringUtils.isNoneBlank(item.getPicUrls())) {
                            appointmentServiceOrder.setPicUrl(StringUtils.split(item.getPicUrls(), ';')[0]);
                        }
                    }
                    
                    //MikuMineScBoxDO mikuMineScBoxDO = null;		//定制盒子
                    if(orderType2.equals(Constants.TradeType.dz_type.getTradeTypeId())){	//定制订单
                    	appointmentServiceOrder.setPrice(mikuMineScBoxDO.getPrice());
                        appointmentServiceOrder.setTotalFee(mikuMineScBoxDO.getPrice());
                        totalFee += mikuMineScBoxDO.getPrice();
                        title += mikuMineScBoxDO.getBoxName();
                    }else{
                    	//不是定制订单
                    	Boolean pbFlag = false;		//判断有没抢购标，true=有抢购标；false=无抢购标
                        if (grouponItem) {
                            appointmentServiceOrder.setPrice(grouponPrice);
                            appointmentServiceOrder.setTotalFee(grouponPrice * itemIdNumMap.get(item.getId()));
                            totalFee += (grouponPrice * itemIdNumMap.get(item.getId()));
                            title += grouponTitle;
                        } else {
                        	if(Byte.compare(orderType, Constants.TradeType.scratch_card.getTradeTypeId()) == 0) {	//刮刮卡订单
                        		appointmentServiceOrder.setPrice(item.getPrice());
                        		appointmentServiceOrder.setTotalFee(0L);
                        		totalFee += 0L;
                        		title += item.getTitle();
                        	}else{
    	                    	//抢购标设置价格
    	                    	if(null != panicBuyingItemTags && !panicBuyingItemTags.isEmpty()){
    	                    		for(ObjectTaggedDO pbOtag : panicBuyingItemTags){
    	                    			if(item.getId().equals(pbOtag.getArtificialId()) && null != pbOtag.getKv() && !"".equals(pbOtag.getKv().trim())){
    	                    				ItemActivityTag itemLimitTag = JSON.parseObject(pbOtag.getKv(), ItemActivityTag.class);
    	                    				if(null != itemLimitTag && null != itemLimitTag.getActivityPrice()){
    	                    					Long activityPrice = (itemLimitTag.getActivityPrice() != null ? 
    	                    							itemLimitTag.getActivityPrice() : 0);	//活动价格
    	                    					appointmentServiceOrder.setPrice(activityPrice);
    	                    					appointmentServiceOrder.setTotalFee(activityPrice * itemIdNumMap.get(item.getId()));
    	                    					totalFee += activityPrice * itemIdNumMap.get(item.getId());
    	                    					title = item.getTitle();
    	                    					//计算积分和优惠券可抵扣金额
    	                    					if (!limitPointObject.contains(item.getId())) {
    	                    						validPointTotalFee += activityPrice * itemIdNumMap.get(item.getId());
    	                    					}
    	                    					if (!limitCoupon.contains(item.getId())) {
    	                    						validCouponTotalFee += activityPrice * itemIdNumMap.get(item.getId());
    	                    					}
    	                    					pbFlag=true;
    	                    					appointmentServiceOrder.setHasPanicTag((byte)1);	//是否有抢购标(0=没有;1=有)
    	                    				}
    	                    			}
    	                    		}
    	                    	}
    	                    	if(!pbFlag){
    	                    		appointmentServiceOrder.setPrice(item.getPrice());
    	                    		appointmentServiceOrder.setTotalFee(item.getPrice() * itemIdNumMap.get(item.getId()));
    	                    		totalFee += (item.getPrice() != null ? item.getPrice() : 0) * itemIdNumMap.get(item.getId());
    	                    		title += item.getTitle();
    	                    	}
                        	}
                        }

                        if(!pbFlag){	//无抢购标
                        	//计算积分和优惠券可抵扣金额
                        	if (!limitPointObject.contains(item.getId())) {
                        		validPointTotalFee += (item.getPrice() != null ? item.getPrice() : 0) * itemIdNumMap.get(item.getId());
                        	}
                        	if (!limitCoupon.contains(item.getId())) {
                        		validCouponTotalFee += (item.getPrice() != null ? item.getPrice() : 0) * itemIdNumMap.get(item.getId());
                        	}
                    	}
                        
                        //获取商品item所对应的分润比例参数
                        MikuItemShareParaDOExample mikuItemShareParaDOExample = new MikuItemShareParaDOExample();
    		        	mikuItemShareParaDOExample.createCriteria().andItemIdEqualTo(item.getBaseItemId());
    		        	//查询商品分润方案配置列表
    		        	List<MikuItemShareParaDO> mikuItemShareParaDOList = mikuItemShareParaDOMapper.selectByExample(mikuItemShareParaDOExample);
    		        	if(!mikuItemShareParaDOList.isEmpty()){
    		        		long itemProfitFee = (null == mikuItemShareParaDOList.get(0).getItemProfitFee() ? 
    		        				0L : mikuItemShareParaDOList.get(0).getItemProfitFee());
    		        		String parameter = "";
    	        			parameter += "[{\"id\":1,\"value\":\"4.8\",\"title\":\"一度\"},{\"id\":2,\"value\":\"4.8\",\"title\":\"二度\"},{\"id\":3,\"value\":\"6.4\",\"title\":\"三度\"},{\"id\":4,\"value\":\"8\",\"title\":\"四度\"},";
    		                parameter += "{\"id\":5,\"value\":\"8\",\"title\":\"五度\"},{\"id\":6,\"value\":\"8\",\"title\":\"六度\"},{\"id\":7,\"value\":\"8\",\"title\":\"七度\"},{\"id\":8,\"value\":\"32\",\"title\":\"八度\"},";
    		              	parameter += "{\"id\":9,\"value\":\"1\",\"title\":\"CEO4\"},{\"id\":10,\"value\":\"6\",\"title\":\"CEO3\"},{\"id\":11,\"value\":\"12\",\"title\":\"CEO2\"},{\"id\":12,\"value\":\"20\",\"title\":\"CEO1\"}]";
    		        		//appointmentServiceOrder.setProfitParameter(mikuItemShareParaDOList.get(0).getParameter());	//给订单设置商品分润参数
    		              	appointmentServiceOrder.setProfitParameter(parameter);	//给订单设置商品分润参数
    		              	appointmentServiceOrder.setItemCostFee(mikuItemShareParaDOList.get(0).getItemCostFee());
    		        		appointmentServiceOrder.setItemProfitFee(mikuItemShareParaDOList.get(0).getItemProfitFee());
    		        		appointmentServiceOrder.setItemShareFee(mikuItemShareParaDOList.get(0).getItemShareFee());
    		        		tradeItemProfitFeeSum += (itemProfitFee * itemIdNumMap.get(item.getId()));
    		        	}

                        orderItemPairList.add(ImmutablePair.of(appointmentServiceOrder, item));
                        
                        if(null != itemIdTopicMap && null != itemIdTopicMap.get(item.getId())){
                        	MikuActiveTopicVO mikuActiveTopicVO = itemIdTopicMap.get(item.getId());
                        	if(null != mikuActiveTopicVO){
                        		if(null != topicTotalFeeMap && null != mikuActiveTopicVO 
                        				&& null != topicTotalFeeMap.get(mikuActiveTopicVO.getId())){
                        			Long topicTotalFee = topicTotalFeeMap.get(mikuActiveTopicVO.getId());
                        			//计算好后的商品专题金额
                        			topicTotalFeeMap.put(mikuActiveTopicVO.getId(), topicTotalFee + appointmentServiceOrder.getTotalFee());
                        		}else{
                        			//计算好后的商品专题金额
                        			topicTotalFeeMap.put(mikuActiveTopicVO.getId(), appointmentServiceOrder.getTotalFee());
                        		}
                        		//设置oderd专题参数
                        		appointmentServiceOrder.setTopicId(mikuActiveTopicVO.getId());
                        		appointmentServiceOrder.setTopicName(mikuActiveTopicVO.getName());
                        		appointmentServiceOrder.setTopicParameter(mikuActiveTopicVO.getParameter());
                        	}
                        }
                    }
                    
                    
                    appointmentServiceOrder.setVersion(1L);
                    appointmentServiceOrder.setLastUpdated(new Date());
                    appointmentServiceOrder.setNum(itemIdNumMap.get(item.getId()));
                    appointmentServiceOrder.setStatus(Constants.TradeStatus.WAIT_BUYER_PAY.getTradeStatusId());
                    appointmentServiceOrder.setReturnStatus(Constants.ReturnGoodsStatus.NORMAL.getStatusId()); //正常单(不是退货单)

                    if (orderMapper.insert(appointmentServiceOrder) > 0) {
                        orderIds.add(appointmentServiceOrder.getId());
                      //更新库存和销售量 针对非物流订单
                      if(!updateStock2(null, appointmentServiceOrder, transactionStatus, 1)){	//更新库存(1=减库存)
                    	  transactionStatus.setRollbackOnly();
                    	  // TODO: 数据库异常，打点
                    	  return BaseResult.failure(BuyItemResultCode.BUY_ITEM_ERROR_UNKNOWN.getCode(), BuyItemResultCode.BUY_ITEM_ERROR_UNKNOWN.getMessage()+"，或库存不够！");
                      }
                    } else {
                        transactionStatus.setRollbackOnly();
                        // TODO: 数据库异常，打点
                        return BaseResult.failure(BuyItemResultCode.BUY_ITEM_ERROR_UNKNOWN.getCode(), BuyItemResultCode.BUY_ITEM_ERROR_UNKNOWN.getMessage());
                    }
                }

                //创建trade 创建完了服务订单，创建这笔交易
                trade.setBuildingId(buildingId);
                trade.setVersion(1L);
                if (StringUtils.isBlank(buyerMessage)) {
                    trade.setHasBuyerMessage((byte) 0);
                } else {
                    trade.setHasBuyerMessage((byte) 1);
                    trade.setBuyerMessage(buyerMessage);
                }
                if (Byte.compare(payType, Constants.PayType.OFF_LINE.getPayTypeId()) == 0) {
                    trade.setType(Constants.TradeType.cod.getTradeTypeId());
                    trade.setCodStatus(Constants.CodStatus.ACCEPTED_BY_COMPANY.getCodStatusId());
                    trade.setStatus(Constants.TradeStatus.WAIT_SELLER_SEND_GOODS.getTradeStatusId());
                } else {
                    trade.setType(Constants.TradeType.fixed.getTradeTypeId());
                    trade.setCodStatus(Constants.CodStatus.NEW_CREATED.getCodStatusId());
                    trade.setStatus(Constants.TradeStatus.WAIT_BUYER_PAY.getTradeStatusId());
                }
                if(Byte.compare(orderType, Constants.TradeType.crowdfund_type.getTradeTypeId()) == 0) {
                	//众筹类型
                	trade.setType(Constants.TradeType.crowdfund_type.getTradeTypeId());
                    trade.setCodStatus(Constants.CodStatus.NEW_CREATED.getCodStatusId());
                    trade.setStatus(Constants.TradeStatus.WAIT_BUYER_PAY.getTradeStatusId());
                }else if(orderType.equals(Constants.TradeType.join_agency.getTradeTypeId())){
                	//成为代理类型
                	trade.setType(Constants.TradeType.join_agency.getTradeTypeId());
                    trade.setCodStatus(Constants.CodStatus.NEW_CREATED.getCodStatusId());
                    trade.setStatus(Constants.TradeStatus.WAIT_BUYER_PAY.getTradeStatusId());
                }else if(orderType.equals(Constants.TradeType.scratch_card.getTradeTypeId())){
                	//成为代理类型
                	trade.setType(Constants.TradeType.scratch_card.getTradeTypeId());
                    trade.setCodStatus(Constants.CodStatus.NEW_CREATED.getCodStatusId());
                    trade.setStatus(Constants.TradeStatus.WAIT_BUYER_PAY.getTradeStatusId());
                }
                trade.setCommunityId(communityId);
                trade.setDateCreated(new Date());
                trade.setLastUpdated(new Date());
                if (StringUtils.isNoneBlank(picUrls)) {
                    trade.setPicUrl(StringUtils.split(picUrls, ';')[0]);
                }

                trade.setCategoryId(items.iterator().next().getCategoryId());
                //price指购买的商品原价
                trade.setPrice(totalFee);
                
                Long fullCutFee = 0L;	//总满减金额
                if(null != mikuActiveTopicVOList2 && !mikuActiveTopicVOList2.isEmpty()){
                	Long topicMin = null, topicValue = 0L;
        			for(MikuActiveTopicVO vo : mikuActiveTopicVOList2){
        				try {
	        				Long topicTotalFee = null == topicTotalFeeMap.get(vo.getId()) ? 0L : topicTotalFeeMap.get(vo.getId());
	        				List<TopicParameterVO> topicParameterVOList = JSON.parseArray(vo.getParameter(), TopicParameterVO.class);
	        				if(topicTotalFee > 0 && null != topicParameterVOList && !topicParameterVOList.isEmpty()){
	        					Collections.sort(topicParameterVOList, new Comparator<TopicParameterVO>() {
	                                public int compare(TopicParameterVO arg0, TopicParameterVO arg1) {
	                                	Long min1 = null == arg0.getMin() ? 0L : arg0.getMin();
	                                	Long min2 = null == arg1.getMin() ? 0L : arg1.getMin();
	                                	int i = 0;
	                                	i = min1.compareTo(min2);
	                                	if(i > 0){
	                                		return -1;
	                                	}else{
	                                		return 1;
	                                	}
	                                }
	                            });
	        					for(TopicParameterVO topicParameterVO : topicParameterVOList){
	        						topicMin = topicParameterVO.getMin();
	        						topicValue = null == topicParameterVO.getValue() ? 
	        								0L : topicParameterVO.getValue();
	        						if(null != topicMin && topicTotalFee >= topicMin){
	        							fullCutFee += topicValue; 	//总满减金额
	        							break;
	        						}
	        					}
	        				}
        				} catch (Exception e) {
        					fullCutFee = 0L;
						}
        			}
        		}
                
                //运费计算
                if (isFirstInstall /*|| tradeFrom != Constants.TradeFrom.H5.getTypeId() */ || shippingType == BizConstants.SELF_PICK_SHIPPING_TYPE) {
                    //初装有礼不收运费
                    trade.setTotalFee(totalFee);
                } else if ((!orderType.equals(Constants.TradeType.crowdfund_type.getTradeTypeId()) && totalFee < Constants.POST_FEE_STEP && !grouponItem)) {
                    totalFee += Constants.POST_FEE;
                    trade.setTotalFee(totalFee);//加运费
                    trade.setHasPostFee((byte) 1);
                    trade.setPostFee(Constants.POST_FEE);
                    validPointTotalFee += Constants.POST_FEE;
                    validCouponTotalFee += Constants.POST_FEE;
                    //创建运费订单
                    Order appointmentServiceOrder = new Order();
                    appointmentServiceOrder.setCommunityId(communityId);
                    appointmentServiceOrder.setCategoryId(Constants.AppointmentServiceCategory.PostFeeService.getCategoryId());
                    appointmentServiceOrder.setArtificialId(-1l);
                    appointmentServiceOrder.setBuyerId(buyerId);
                    appointmentServiceOrder.setTitle("运费");
                    appointmentServiceOrder.setShippingType((int) shippingType);
                    //去掉预约时间
                    //appointmentServiceOrder.setConsignTime(consignTime);
                    appointmentServiceOrder.setDateCreated(new Date());
                    appointmentServiceOrder.setIsServiceOrder((byte) 1);
                    appointmentServiceOrder.setNum(1);
                    appointmentServiceOrder.setSellerId(sellerId);
                    appointmentServiceOrder.setSellerType((byte) 1);
                    appointmentServiceOrder.setPicUrl(null);
                    appointmentServiceOrder.setPrice(Constants.POST_FEE);
                    appointmentServiceOrder.setTotalFee(Constants.POST_FEE);
                    appointmentServiceOrder.setVersion(1L);
                    appointmentServiceOrder.setLastUpdated(new Date());
                    appointmentServiceOrder.setStatus(Constants.TradeStatus.WAIT_BUYER_PAY.getTradeStatusId());
                    appointmentServiceOrder.setReturnStatus(Constants.ReturnGoodsStatus.NORMAL.getStatusId()); //正常单(不是退货单)
                    appointmentServiceOrder.setHasPanicTag((byte)0);	//是否有抢购标(0=没有;1=有)
                    appointmentServiceOrder.setIsReturnProfit((byte)0);	//是否退分润(0=没退;1=已退)
                    Item item = new Item();
                    item.setId(-1l);
                    item.setNum(1);
                    item.setPrice(Constants.POST_FEE);
                    orderItemPairList.add(ImmutablePair.of(appointmentServiceOrder, item));
                    if (orderMapper.insertSelective(appointmentServiceOrder) > 0) {
                        orderIds.add(appointmentServiceOrder.getId());
                    } else {
                        transactionStatus.setRollbackOnly();
                        // TODO: 数据库异常，打点
                        return BaseResult.failure(BuyItemResultCode.BUY_ITEM_ERROR_UNKNOWN.getCode(), BuyItemResultCode.BUY_ITEM_ERROR_UNKNOWN.getMessage());
                    }
                } else {
                    trade.setTotalFee(totalFee);
                }
                
                //有效积分和优惠券减去满减值fullCutFee
                validCouponTotalFee = (validCouponTotalFee - fullCutFee) > 0L ? (validCouponTotalFee - fullCutFee) : 0L;  
                validPointTotalFee = (validPointTotalFee - fullCutFee) > 0L ? (validPointTotalFee - fullCutFee) : 0L; 

                //0元付款判断
                UserCouponDO userCouponDO = null;
                CouponDO couponDO = null;
                List<PointAccountDO> pointAccountDOs = null;
                long couponValue = 0;
                boolean zeroPay = false;
                if (userCouponId > 0) {

                    userCouponDO = userCouponDOMapper.selectByPrimaryKey(userCouponId);

                    if (userCouponDO != null) {
                        couponDO = couponDOMapper.selectByPrimaryKey(userCouponDO.getCouponId());
                        if (couponDO != null) {
                            couponValue = couponDO.getValue();
                        }
                    }

                    if (userCouponDO == null || couponDO == null || couponDO.getMinValue() > validCouponTotalFee) {
                        transactionStatus.setRollbackOnly();
                        return BaseResult.failure(BuyItemResultCode.COUPON_PARAM_ERROR.getCode(), BuyItemResultCode.COUPON_PARAM_ERROR.getMessage());
                    }
                }
                if (point > 0) {
                    PointAccountDOExample pointAccountDOExample = new PointAccountDOExample();
                    pointAccountDOExample.createCriteria().andUserIdEqualTo(buyerId).andStatusEqualTo((byte) 1);
                    pointAccountDOs = pointAccountDOMapper.selectByExample(pointAccountDOExample);
                    if (pointAccountDOs == null || pointAccountDOs.size() <= 0 || pointAccountDOs.get(0).getAvailableBalance() < point) {
                        transactionStatus.setRollbackOnly();
                        return BaseResult.failure(BuyItemResultCode.INPUT_POINT_TOO_LARGE.getCode(), BuyItemResultCode.INPUT_POINT_TOO_LARGE.getMessage());
                    }
                    if (point > validPointTotalFee) {
                        transactionStatus.setRollbackOnly();
                        return BaseResult.failure(BuyItemResultCode.POINT_TOO_MORE_SERVICE_ZONE.getCode(), BuyItemResultCode.POINT_TOO_MORE_SERVICE_ZONE.getMessage());
                    }
                    long todayUsedPoint = findTodayUsePointByUserId(buyerId);
                    if (todayUsedPoint + point > Constants.MAX_USE_POINT) {
                        logger.warn("crated trade failed. use point too much... used today :" + todayUsedPoint + ",to use point:" + point + ",buyerId:" + buyerId);
                        transactionStatus.setRollbackOnly();
                        return BaseResult.failure(BuyItemResultCode.CREATE_TRADE_FAILED.getCode(), BuyItemResultCode.CREATE_TRADE_FAILED.getMessage());
                    }
                }

                if (couponValue + point == totalFee && totalFee == validPointTotalFee) {
                    zeroPay = true;
                } else if (couponValue + point > totalFee) {
                    transactionStatus.setRollbackOnly();
                    return BaseResult.failure(BuyItemResultCode.PAY_TOO_LARGE.getCode(), BuyItemResultCode.PAY_TOO_LARGE.getMessage());
                }

                Optional<PromotionResult> promotionResultOptional = Optional.absent();

                //优惠券处理
                if (userCouponId > 0) {
                    if (Byte.compare(payType, Constants.PayType.OFF_LINE.getPayTypeId()) == 0 || zeroPay) {
                        // 直接消费
                        promotionResultOptional = usePromotionAndRecord(buyerId, trade.getPrice(), trade.getTradeId(), PromotionType.COUPON_USE_IN_TRADE_DIRECT.getCode(),
                                ImmutableMap.of("user_coupon", userCouponDO, "couponDOs", Lists.newArrayList(couponDO)));
                    } else {
                        // 冻结，回调消费
                        promotionResultOptional = usePromotionAndRecord(buyerId, trade.getPrice(), trade.getTradeId(), PromotionType.COUPON_USE_IN_TRADE_FROZEN.getCode(),
                                ImmutableMap.of("user_coupon", userCouponDO, "couponDOs", Lists.newArrayList(couponDO)));
                    }
                    totalFee -= couponValue;

                    if (!promotionResultOptional.isPresent()) {
                        transactionStatus.setRollbackOnly();
                        return BaseResult.failure(BuyItemResultCode.BUY_ITEM_ERROR_UNKNOWN.getCode(), BuyItemResultCode.BUY_ITEM_ERROR_UNKNOWN.getMessage());
                    }

                    PromotionResult promotionResult = promotionResultOptional.get();

                    if (!promotionResult.getReward()) {
                        transactionStatus.setRollbackOnly();
                        return BaseResult.failure(promotionResult.getCode(), promotionResult.getMessage());
                    }
                }

                //积分处理
                if (point > 0) {
                    //货到付款直接使用积分，其他支付方式先冻结
                    if (Byte.compare(payType, Constants.PayType.OFF_LINE.getPayTypeId()) == 0 || zeroPay) {
                        promotionResultOptional = usePromotionAndRecord(buyerId, (int) point, trade.getTradeId(), PromotionType.POINT_USE_IN_TRADE_DIRECT.getCode());
                    } else {
                        promotionResultOptional = usePromotionAndRecord(buyerId, (int) point, trade.getTradeId(), PromotionType.POINT_USE_IN_TRADE_FROZEN.getCode());
                    }

                    if (!promotionResultOptional.isPresent()) {
                        transactionStatus.setRollbackOnly();
                        return BaseResult.failure(BuyItemResultCode.BUY_ITEM_ERROR_UNKNOWN.getCode(), BuyItemResultCode.BUY_ITEM_ERROR_UNKNOWN.getMessage());
                    }

                    PromotionResult promotionResult = promotionResultOptional.get();

                    if (!promotionResult.getReward()) {
                        transactionStatus.setRollbackOnly();
                        return BaseResult.failure(promotionResult.getCode(), promotionResult.getMessage());
                    }

                    totalFee -= point;
                    trade.setPointFee(point);
                }

                //discountFee-point = couponFee
                trade.setDiscountFee(couponValue + point);
                totalFee = (totalFee - fullCutFee) > 0L ? (totalFee - fullCutFee) : 0L;  //总金额减去满减的金额
                trade.setTotalFee(totalFee);
                trade.setFullCutFee(fullCutFee); 	//设置trade的满减金额

                /*if (buyerId == 4309 ||
                        buyerId == 275 || buyerId == 43 || buyerId == 207) {
                    trade.setTotalFee(1l);
                }*/
                trade.setOrders(StringUtils.join(orderIds, ';'));
                
                //去掉预约时间
//                trade.setConsignTime(consignTime);
                trade.setSellerId(sellerId);
                trade.setCanRate((byte) 1);
                trade.setBuyerRate((byte) 0);
                if (zeroPay) {
                    trade.setStatus(Constants.TradeStatus.WAIT_SELLER_SEND_GOODS.getTradeStatusId());
                    trade.setPayType(Constants.PayType.ONLINE_ZERO_PAY.getPayTypeId());
                } else {
                    trade.setPayType(payType);
                }
                trade.setTitle(StringUtils.substring(title, 0, 22) + "");
                trade.setIsProfit(Byte.valueOf("0"));	//是否分润（0=未分润，1=已分润）
                trade.setReturnStatus(Constants.TradeReturnStatus.NORMAL.getStatusId());	//交易退货状态(0=正常单；1=退货中;2=已退货)
                trade.setCrowdfundDetailId(crowdfundDetailId);	//交易设置众筹明细id
                if(orderType.equals(Constants.TradeType.crowdfund_type.getTradeTypeId())){	//众筹订单
                	//众筹退款状态(0=众筹中；1=退款中;2=已退款;3=众筹成功)
                	trade.setCrowdfundRefundStatus(Constants.CrowdfundRefundStatus.NORMAL.getStatusId()); //众筹退款状态
                }else{
                	//众筹退款状态(0=众筹中；1=退款中;2=已退款;3=众筹成功)
                	trade.setCrowdfundRefundStatus(Constants.CrowdfundRefundStatus.SUCCESS.getStatusId()); //众筹退款状态
                }
                if (1 != tradeMapper.insert(trade)) {
                    transactionStatus.setRollbackOnly();
                    // TODO: 数据库异常，打点
                    return BaseResult.failure(BuyItemResultCode.BUY_ITEM_ERROR_UNKNOWN.getCode(), BuyItemResultCode.BUY_ITEM_ERROR_UNKNOWN.getMessage());
                }
                
                if(orderType.equals(Constants.TradeType.scratch_card.getTradeTypeId())){	//刮刮卡订单
                	MikuScratchCardDO mikuScratchCardDO = mikuScratchCardDOMapper.selectByPrimaryKey(scratchCardId);
                	if(null != mikuScratchCardDO){
                		mikuScratchCardDO.setTradeId(trade.getTradeId());
                		mikuScratchCardDO.setStatus(Constants.ScratchCardStatus.NO_PAY.getStatusId());
                		mikuScratchCardDO.setLastUpdated(new Date());
                		if(mikuScratchCardDOMapper.updateByPrimaryKeySelective(mikuScratchCardDO) < 1){	//更新刮刮卡状态为未付款
                			transactionStatus.setRollbackOnly();
                			return BaseResult.failure(BuyItemResultCode.BUY_ITEM_ERROR_UNKNOWN.getCode(), BuyItemResultCode.BUY_ITEM_ERROR_UNKNOWN.getMessage());
                		}
                	}
                }

                for (ImmutablePair<Order, Item> orderItemImmutablePair : orderItemPairList) {
                    Snapshot snapshot = new Snapshot();
                    snapshot.setTradeId(trade.getTradeId());
                    snapshot.setOrderId(orderItemImmutablePair.getLeft().getId());
                    snapshot.setDateCreated(new Date());
                    snapshot.setLastUpdated(new Date());
                    snapshot.setDetail(JSON.toJSONString(orderItemImmutablePair.getRight()));

                    if (1 != snapshotMapper.insertSelective(snapshot)) {
                        transactionStatus.setRollbackOnly();
                        // TODO: 数据库异常，打点
                        return BaseResult.failure(BuyItemResultCode.BUY_ITEM_ERROR_UNKNOWN.getCode(), BuyItemResultCode.BUY_ITEM_ERROR_UNKNOWN.getMessage());
                    }
                    try {
                        OrderExample orderExample = new OrderExample();
                        orderExample.createCriteria().andIdEqualTo(orderItemImmutablePair.getLeft().getId());
                        Order order = new Order();
                        order.setSnapshotId(snapshot.getId());
                        if (orderMapper.updateByExampleSelective(order, orderExample) < 1) {
                            transactionStatus.setRollbackOnly();
                            return BaseResult.failure(BuyItemResultCode.ORDER_SNAPSHOT_FAILED.getCode(), BuyItemResultCode.ORDER_SNAPSHOT_FAILED.getMessage());
                        }
                    } catch (Exception e) {
                        transactionStatus.setRollbackOnly();
                        return BaseResult.failure(BuyItemResultCode.ORDER_SNAPSHOT_FAILED.getCode(), BuyItemResultCode.ORDER_SNAPSHOT_FAILED.getMessage());
                    }
                }

                return new BaseResult<Trade>(trade);
            }
        });

    }

    public Optional<PromotionResult> usePromotionAndRecord(long userId, long value, long tradeId, int type, Map<String, Object> params) {
        UserInteractionRequest userInteractionRequest = new UserInteractionRequest();
        userInteractionRequest.setUserId(userId);
        userInteractionRequest.setTargetId(String.valueOf(tradeId));
        userInteractionRequest.setType(type);
        userInteractionRequest.setValue(value);
        userInteractionRequest.getParams().putAll(params);
        Optional<PromotionResult> promotionResultOptional = userInteractionEffect.interactive(userInteractionRequest);
        if (promotionResultOptional.isPresent()) {
            PromotionResult promotionResult = promotionResultOptional.get();
            UsePromotionDO usePromotionDO = new UsePromotionDO();
            usePromotionDO.setDateCreated(new Date());
            usePromotionDO.setLastUpdated(new Date());
            usePromotionDO.setPromotionId(promotionResult.getPromotionId());
            usePromotionDO.setTradeId(tradeId);
            usePromotionDO.setType(promotionResult.getType());
            usePromotionDO.setVersion(1L);
            if (usePromotionDOMapper.insertSelective(usePromotionDO) != 1) {
                return Optional.absent();
            }
        }

        return promotionResultOptional;
    }

    public Optional<PromotionResult> usePromotionAndRecord(long userId, long value, long tradeId, int type) {
        return usePromotionAndRecord(userId, value, tradeId, type, Maps.<String, Object>newHashMap());
    }

    //根据userId查询今天总共使用的积分数
    public long findTodayUsePointByUserId(Long userId) {
        Date nowDate = new Date();
        Date zeroDate = getDateStartTime(nowDate);
        List<Integer> pointUseType = new ArrayList<Integer>();
        long usePoint = 0;
        pointUseType.add(PromotionType.POINT_USE_IN_TRADE_FROZEN.getCode());
        pointUseType.add(PromotionType.POINT_USE_IN_TRADE_DIRECT.getCode());
        UserInteractionRecordsDOExample userInteractionRecordsDOExample = new UserInteractionRecordsDOExample();
        userInteractionRecordsDOExample.createCriteria().andUserIdEqualTo(userId).andTypeIn(pointUseType).andStatusEqualTo((byte) 1).andDateCreatedGreaterThan(zeroDate);
        List<UserInteractionRecordsDO> userInteractionRecordsDOList = userInteractionRecordsDOMapper.selectByExample(userInteractionRecordsDOExample);
        for (UserInteractionRecordsDO userInteractionRecordsDO : userInteractionRecordsDOList) {
            usePoint += userInteractionRecordsDO.getValue();
        }
        return usePoint;
    }

    //获取指定日期的开始时间
    public Date getDateStartTime(Date date) {
        Calendar cDate = Calendar.getInstance();
        cDate.setTime(date);
        cDate.set(Calendar.HOUR_OF_DAY, 0);
        cDate.set(Calendar.MINUTE, 0);
        cDate.set(Calendar.SECOND, 0);
        cDate.set(Calendar.MILLISECOND, 0);
        return cDate.getTime();
    }

    @Override
    public BaseResult<List<Trade>> findByExample(@Nonnull TradeExample tradeExample) {

        List<Trade> trades = tradeMapper.selectByExample(tradeExample);

        // 本身有乐观锁，不需要事务
        try {
            for (Iterator<Trade> iterator = trades.iterator(); iterator.hasNext(); ) {
                Trade trade = iterator.next();
                BaseResult<Boolean> result = closeTrade(trade.getCommunityId(), trade.getTradeId(), false);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        trades = tradeMapper.selectByExample(tradeExample);
        int total = tradeMapper.countByExample(tradeExample);

        return new BaseResult<>(trades).putExternal("total", total);
    }

    @Override
    public BaseResult<List<Trade>> findByBuyerTrades(long buyerId, int offset, int limit) {
        TradeExample tradeExample = new TradeExample();
        tradeExample.createCriteria() //
                .andBuyerIdEqualTo(buyerId); //
        tradeExample.setOrderByClause("id DESC");
        tradeExample.setOffset(offset);
        tradeExample.setLimit(limit);
        List<Trade> trades = tradeMapper.selectByExample(tradeExample);
        return new BaseResult<>(trades).putExternal("total", trades.size());
    }
    
    @Override
    public BaseResult<List<Trade>> findTradesByParams(Trade trade, List<Byte> tradeStatusList, int offset, int limit){
    	TradeExample tradeExample = new TradeExample();
        Criteria createCriteria = tradeExample.createCriteria(); //
        createCriteria.andBuyerIdEqualTo(trade.getBuyerId()); //
        if(null != trade.getBuyerRate()){
        	createCriteria.andBuyerRateEqualTo(trade.getBuyerRate());
        }
        if(null != tradeStatusList && !tradeStatusList.isEmpty()){
        	createCriteria.andStatusIn(tradeStatusList);
        }
        tradeExample.setOrderByClause("id DESC");
        tradeExample.setOffset(offset);
        tradeExample.setLimit(limit);
        List<Trade> trades = tradeMapper.selectByExample(tradeExample);
        return new BaseResult<>(trades).putExternal("total", trades.size());
    }

    @Override
    public BaseResult<List<Trade>> findByBuyerTradesWithStatusAndType(long buyerId, List<Byte> status, int offset, int limit) {
        //closeTradeByBuyerId(buyerId);
        TradeExample tradeExample = new TradeExample();
        tradeExample.setOrderByClause("id DESC");
        TradeExample.Criteria criteria = tradeExample.createCriteria();
        criteria.andBuyerIdEqualTo(buyerId);
        criteria.andStatusIn(status);
        tradeExample.setOffset(offset);
        tradeExample.setLimit(limit);
        List<Trade> trades = tradeMapper.selectByExample(tradeExample);

        return new BaseResult<>(trades).putExternal("total", trades.size());
    }

    @Override
    public BaseResult<Boolean> closeTrade(@Nonnull Long communityId, @Nonnull final Long tradeId, final boolean manual) {

        checkNotNull(communityId);
        checkNotNull(tradeId);

        final TradeExample tradeExample = new TradeExample();
        final TradeExample.Criteria tradeCriteria = tradeExample.createCriteria();//
        tradeCriteria.andTradeIdEqualTo(tradeId);//

        List<Trade> trades = tradeMapper.selectByExample(tradeExample);
        if (trades.size() > 0) {
            final Trade trade = trades.get(0);
            final byte tradeStatus = trade.getStatus();
            return transactionTemplate.execute(new TransactionCallback<BaseResult<Boolean>>() {
                @Override
                public BaseResult<Boolean> doInTransaction(TransactionStatus transactionStatus) {
                    // 人工关闭订单 如果是人工关闭，如果物业确定了，就不能取消订单
                    if (manual) {
                        trade.setLastUpdated(new Date());
                        trade.setVersion(trade.getVersion() + 1L);
                        trade.setCodStatus(Constants.CodStatus.CANCELED.getCodStatusId());
                        trade.setStatus(Constants.TradeStatus.TRADE_CLOSED_BY_TAOBAO.getTradeStatusId());

                        tradeCriteria.andVersionEqualTo(trade.getVersion() - 1);
                        tradeCriteria.andCodStatusEqualTo(Constants.CodStatus.NEW_CREATED.getCodStatusId());

                        if (1 != tradeMapper.updateByExampleSelective(trade, tradeExample)) {
                            transactionStatus.setRollbackOnly();
                            logger.error("update trade status failed. close trade failed. tradeId:" + tradeId);
                            return BaseResult.failure(BuyItemResultCode.OFFLINE_PAY_CLOSE_ORDER_FAILED.getCode(), BuyItemResultCode.OFFLINE_PAY_CLOSE_ORDER_FAILED.getMessage());
                        }
                        List<Order> orderList = findOrdersByTradeId(tradeId);	//根据交易号查找订单列表
                        if(null != orderList && !orderList.isEmpty()){
                        	for(Order order : orderList){
                        		if(null != order){
                        			updateStock2(trade, order, transactionStatus, 2);		//更新库存(2=加库存)
                        		}
                        	}
                        }
                    }
                    // 超时未付款 取消订单
                    if (!manual && tradeStatus == Constants.TradeStatus.WAIT_BUYER_PAY.getTradeStatusId() && new Date().compareTo(trade.getTimeoutActionTime()) == 1) {
                        // 超时，并且物业没有安排送货
                        if (trade.getCodStatus() == Constants.CodStatus.NEW_CREATED.getCodStatusId()) {
                            trade.setVersion(trade.getVersion() + 1);
                            trade.setStatus(Constants.TradeStatus.TRADE_CLOSED_BY_TAOBAO.getTradeStatusId());
                            trade.setCodStatus(Constants.CodStatus.CANCELED.getCodStatusId());
                            trade.setLastUpdated(new Date());

                            TradeExample query = new TradeExample();
                            query.createCriteria() //
                                    .andCommunityIdEqualTo(trade.getCommunityId()) //
                                    .andTradeIdEqualTo(trade.getTradeId()) //
                                    .andVersionEqualTo(trade.getVersion() - 1); //

                            // 如果没有更新成功也无所谓
                            if (tradeMapper.updateByExampleSelective(trade, query) < 1) {
                                transactionStatus.setRollbackOnly();

                                logger.error("update trade status failed. close trade failed. tradeId:" + tradeId);
                                return BaseResult.failure(BuyItemResultCode.OFFLINE_PAY_CLOSE_ORDER_FAILED.getCode(), BuyItemResultCode.OFFLINE_PAY_CLOSE_ORDER_FAILED.getMessage());
                            }
                            
                            List<Order> orderList = findOrdersByTradeId(tradeId);	//根据交易号查找订单列表
                            if(null != orderList && !orderList.isEmpty()){
                            	for(Order order : orderList){
                            		if(null != order){
                            			updateStock2(trade, order, transactionStatus, 2);		//更新库存(2=加库存)
                            		}
                            	}
                            }
                            
                        }
                    }
                    return new BaseResult<>(true);
                }
            });


        } else {
            return new BaseResult<>(true);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        checkNotNull(itemMapper);
        checkNotNull(orderMapper);
        checkNotNull(tradeMapper);
        checkNotNull(snapshotMapper);
        checkNotNull(transactionManager);

        transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setName("trade-transaction");
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        transactionTemplate.setIsolationLevel(Connection.TRANSACTION_READ_COMMITTED);
    }

    /**
     * 根据商品ids 获取特定标记DO
     *
     * @param itemIds
     * @param tag
     * @return
     */
    private List<ObjectTaggedDO> fetchTagViaItemIds(List<Long> itemIds, Long tag) {
        if (null != itemIds && itemIds.size() > 0) {
            //查询限购标记的id
            TagsDOExample qtExample = new TagsDOExample();
            qtExample.createCriteria().andBitEqualTo(BigInteger.valueOf(tag)).andStatusEqualTo((byte) 1);
            List<TagsDO> tagsDOs = new ArrayList<>();
            tagsDOs = tagsDOMapper.selectByExample(qtExample);
            if (null != tagsDOs && tagsDOs.size() > 0) {
                long tagId = tagsDOs.get(0).getId();
                ObjectTaggedDOExample qoExample = new ObjectTaggedDOExample();
                qoExample.createCriteria().andArtificialIdIn(itemIds).andTagIdEqualTo(tagId).andStatusEqualTo((byte) 1);
                List<ObjectTaggedDO> objectTaggedDOs = objectTaggedDOMapper.selectByExample(qoExample);
                if (null != objectTaggedDOs) {
                    return objectTaggedDOs;
                }
            }
        }
        return null;
    }
    
    /**
     * 根据商品ids 获取有开始结束时间的特定标记DO
     *
     * @param itemIds
     * @param tag
     * @return
     */
    private List<ObjectTaggedDO> fetchPanicBuyingTagViaItemIds(List<Long> itemIds, Long tag) {
        if (null != itemIds && itemIds.size() > 0) {
        	Date now = new Date();
            //查询限购标记的id
            TagsDOExample qtExample = new TagsDOExample();
            qtExample.createCriteria().andBitEqualTo(BigInteger.valueOf(tag)).andStatusNotEqualTo((byte)0);
            List<TagsDO> tagsDOs = new ArrayList<>();
            tagsDOs = tagsDOMapper.selectByExample(qtExample);
            if (null != tagsDOs && tagsDOs.size() > 0) {
                long tagId = tagsDOs.get(0).getId();
                ObjectTaggedDOExample qoExample = new ObjectTaggedDOExample();
                qoExample.createCriteria().andArtificialIdIn(itemIds).andTagIdEqualTo(tagId).andStatusEqualTo((byte) 1)
                	.andStartTimeLessThanOrEqualTo(now).andEndTimeGreaterThan(now);
                List<ObjectTaggedDO> objectTaggedDOs = objectTaggedDOMapper.selectByExample(qoExample);
                if (null != objectTaggedDOs) {
                    return objectTaggedDOs;
                }
            }
        }
        return null;
    }
    
    /**
     * 根据商品ids 获取特定标记DO
     *
     * @param itemIds
     * @param tag
     * @return
     */
    public ObjectTaggedDO fetchTagViaItemId(Long itemId, Long tag) {
        if (null != itemId && itemId > 0) {
            //查询限购标记的id
            TagsDOExample qtExample = new TagsDOExample();
            qtExample.createCriteria().andBitEqualTo(BigInteger.valueOf(tag)).andStatusNotEqualTo((byte)0);
            List<TagsDO> tagsDOs = new ArrayList<>();
            tagsDOs = tagsDOMapper.selectByExample(qtExample);
            if (null != tagsDOs && tagsDOs.size() > 0) {
                long tagId = tagsDOs.get(0).getId();
                ObjectTaggedDOExample qoExample = new ObjectTaggedDOExample();
                qoExample.createCriteria().andArtificialIdEqualTo(itemId).andTagIdEqualTo(tagId);
                List<ObjectTaggedDO> objectTaggedDOs = objectTaggedDOMapper.selectByExample(qoExample);
                if (!objectTaggedDOs.isEmpty()) {
                    return objectTaggedDOs.get(0);
                }
            }
        }
        return null;
    }
    
    /**
     * 根据商品ids 获取有开始时间和结束时间的特定标记DO
     *
     * @param itemIds
     * @param tag
     * @return
     */
    public ObjectTaggedDO fetchPanicBuyingTagViaItemId(Long itemId, Long tag) {
        if (null != itemId && itemId > 0) {
        	Date now = new Date();
            //查询限购标记的id
            TagsDOExample qtExample = new TagsDOExample();
            qtExample.createCriteria().andBitEqualTo(BigInteger.valueOf(tag)).andStatusNotEqualTo((byte)0);
            List<TagsDO> tagsDOs = new ArrayList<>();
            tagsDOs = tagsDOMapper.selectByExample(qtExample);
            if (null != tagsDOs && tagsDOs.size() > 0) {
                long tagId = tagsDOs.get(0).getId();
                ObjectTaggedDOExample qoExample = new ObjectTaggedDOExample();
                qoExample.createCriteria().andArtificialIdEqualTo(itemId).andTagIdEqualTo(tagId).andStatusEqualTo((byte) 1)
            		.andStartTimeLessThanOrEqualTo(now).andEndTimeGreaterThan(now);
                List<ObjectTaggedDO> objectTaggedDOs = objectTaggedDOMapper.selectByExample(qoExample);
                if (!objectTaggedDOs.isEmpty()) {
                    return objectTaggedDOs.get(0);
                }
            }
        }
        return null;
    }

    /**
     * 根据itemIds获取特定tag的item ids
     *
     * @param itemIds
     * @param tag
     * @return
     */
    @Override
    public List<Long> fetchContainTagItemIdsViaItemIds(List<Long> itemIds, Long tag) {
        List<Long> tagItemIds = Lists.newArrayList();
        if (null != itemIds && itemIds.size() > 0) {
            //查询限购标记的id
            TagsDOExample qtExample = new TagsDOExample();
            qtExample.createCriteria().andBitEqualTo(BigInteger.valueOf(tag))
            		.andStatusEqualTo((byte)1);
            List<TagsDO> tagsDOs = new ArrayList<>();
            tagsDOs = tagsDOMapper.selectByExample(qtExample);
            if (null != tagsDOs && tagsDOs.size() > 0) {
                long tagId = tagsDOs.get(0).getId();
                ObjectTaggedDOExample qoExample = new ObjectTaggedDOExample();
                qoExample.createCriteria().andArtificialIdIn(itemIds).andTagIdEqualTo(tagId).andStatusEqualTo((byte) 1);
                List<ObjectTaggedDO> objectTaggedDOs = objectTaggedDOMapper.selectByExample(qoExample);
                if (null != objectTaggedDOs && objectTaggedDOs.size() > 0) {
                    for (ObjectTaggedDO objectTaggedDO : objectTaggedDOs) {
                        tagItemIds.add(objectTaggedDO.getArtificialId());
                    }
                }
            }
        }
        return tagItemIds;
    }

    /**
     * 获取限购数量
     *
     * @param itemTag
     * @return
     */
    public int fetchLimitCount(ObjectTaggedDO itemTag) {
        int limit = -1;
        if (null != itemTag && StringUtils.isNotBlank(itemTag.getKv())) {
            ItemLimitTag itemLimitTag = JSON.parseObject(itemTag.getKv(), ItemLimitTag.class);
            if (null == itemLimitTag.getXgLimitNum()) {
                limit = 1;
            } else {
                limit = itemLimitTag.getXgLimitNum();
            }
        }
        return limit;
    }
    
    /**
     * 更新库存2
     *
     * @param type 如果type=2加库存,type=1或其它减库存
     * @param order
     * @return
     */
    public boolean updateStock2(Trade trade, Order order, TransactionStatus transactionStatus, Integer type) {
        //if (Long.compare(order.getCategoryId(), Constants.AppointmentServiceCategory.PostFeeService.getCategoryId()) != 0) {
            long itemId = order.getArtificialId();
            Integer num = (null == order.getNum() ? 0 : order.getNum());
            if(itemId < 1){
            	return true;
            }
            
            Map<String,Object> map = new HashMap<String, Object>();
            if(2 == type){		//如果type=2加库存,type=1或其它减库存
            	map.put("num", -num);
            }else{			//其它减库存
            	map.put("num", num);
            }
            
            //如果是众筹订单，更改支持数
            if(null != trade && null != trade.getType() && null != trade.getCrowdfundDetailId()
            		&& trade.getType().equals(Constants.TradeType.crowdfund_type.getTradeTypeId())){
            	MikuCrowdfundDetailDO mikuCrowdfundDetailDO = mikuCrowdfundDetailDOMapper.selectByPrimaryKey(trade.getCrowdfundDetailId());
            	if(null != mikuCrowdfundDetailDO && null != mikuCrowdfundDetailDO.getCrowdfundId()){
            		MikuCrowdfundDO mikuCrowdfundDO = mikuCrowdfundDOMapper.selectByPrimaryKey(mikuCrowdfundDetailDO.getCrowdfundId());
            		Date nowDate = new Date();
            		if(mikuCrowdfundDO.getEndTime().getTime() < nowDate.getTime()){
            			return true;
            		}
                	trade.setCrowdfundId(mikuCrowdfundDetailDO.getCrowdfundId()); 	//交易设置众筹id
                	Map<String,Object> crowdfundDetailParamMap = new HashMap<String, Object>();
                	crowdfundDetailParamMap.put("id", mikuCrowdfundDetailDO.getId());
                	if(2 == type){		//如果type=2加库存,type=1或其它减库存
                		crowdfundDetailParamMap.put("soldNum", -num);
                    }else{			//其它减库存
                    	crowdfundDetailParamMap.put("soldNum", num);
                    }
                	if(tradeMapper.updateCrowdfundDetailSoldNum(crowdfundDetailParamMap) != 1){
                		//更新众筹明细支持数
                		transactionStatus.setRollbackOnly();
                        return false;
                	}
                	//MikuCrowdfundDO mikuCrowdfundDO = mikuCrowdfundDOMapper.selectByPrimaryKey(mikuCrowdfundDetailDO.getCrowdfundId());
                	if(null != mikuCrowdfundDO){
                		Map<String,Object> crowdfundParamMap = new HashMap<String, Object>();
                		crowdfundParamMap.put("id", mikuCrowdfundDO.getId());
                		if(2 == type){		//如果type=2加库存,type=1或其它减库存(减已售数量和减已筹金额)
                			crowdfundParamMap.put("soldNum", -num);
                        	crowdfundParamMap.put("totalFee", -trade.getTotalFee());	//更新众筹已购金额
                        }
                		/*else{			//其它减库存
                        	crowdfundParamMap.put("soldNum", num);
                        	crowdfundParamMap.put("totalFee", trade.getTotalFee());	//更新众筹已购金额
                        }*/
	                	if(tradeMapper.updateCrowdfundSoldNum(crowdfundParamMap) != 1){
	                		//更新众筹支持数
	                		transactionStatus.setRollbackOnly();
	                		return false;
	                	}
                	}
                }
            }
            
            if(null != order.getHasPanicTag() && order.getHasPanicTag().equals((byte)1)){
            	//HasPanicTag 是否有抢购标(0=没有;1=有)
            	List<Long> itemIds = new ArrayList<Long>();
                itemIds.add(itemId);
                List<ObjectTaggedDO> panicBuyingItemTags =  fetchPanicBuyingTagViaItemIds(itemIds, BizConstants.SearchTagEnum.PANIC_BUYING.getTag());
                if(null != panicBuyingItemTags && !panicBuyingItemTags.isEmpty()){
                	ObjectTaggedDO objectTaggedDO = panicBuyingItemTags.get(0);
                	if(null != objectTaggedDO && null != objectTaggedDO.getId()){
                		map.put("objectTaggedId", objectTaggedDO.getId());
                		if(tradeMapper.updateObjectTaggedNumById(map) < 1){
                			logger.error("update ObjectTagged stock quantity failed. itemId:" + itemId + ",num:" + num);
                			if(null != transactionStatus){
                				transactionStatus.setRollbackOnly();
                			}
                			return false;
                		}
                	}
                }else{
                	if(null != type && type.equals(1)){
                		//如果购买商品减库存时无抢购标时，下单失败
                		return false;
                	}
                }
            }else{
            	map.put("itemId", itemId);
            	//正常商品(不是抢购标的)
            	GrouponDOExample gExample = new GrouponDOExample();
                gExample.createCriteria().andItemIdEqualTo(itemId).andStatusEqualTo(BizConstants.ItemApproveStatus.ON_SALE.getStatus());
                gExample.setOrderByClause("online_end_time DESC");
                List<GrouponDO> grouponDOs = grouponDOMapper.selectByExample(gExample);
                
                //是团购商品
                if (null != grouponDOs && grouponDOs.size() > 0) {
            		if(tradeMapper.updateGrouponItemNum(map) < 1){
            			logger.error("update groupon stock quantity failed. itemId:" + itemId + ",num:" + num);
            			if(null != transactionStatus){
            				transactionStatus.setRollbackOnly();
            			}
            			return false;
            		}else{
            			if (tradeMapper.updateItemNum(map) < 1) {
                        	logger.error("update item sold quantity and stock failed. itemId:" + itemId + ",num:" + num);
                            //com.welink.web.common.filter.Profiler.release();
                        	if(null != transactionStatus){
                				transactionStatus.setRollbackOnly();
                			}
                            return false;
                        }
            		}
                }else{
                	if (tradeMapper.updateItemNum(map) < 1) {
                    	logger.error("update item sold quantity and stock failed. itemId:" + itemId + ",num:" + num);
                    	if(null != transactionStatus){
            				transactionStatus.setRollbackOnly();
            			}
                        //com.welink.web.common.filter.Profiler.release();
                        return false;
                    }
                }
            }
            //获取抢购活动标
            
            /*List<Long> itemIds = new ArrayList<Long>();
            itemIds.add(itemId);
            List<ObjectTaggedDO> panicBuyingItemTags =  fetchPanicBuyingTagViaItemIds(itemIds, BizConstants.SearchTagEnum.PANIC_BUYING.getTag());
            if(null != panicBuyingItemTags && !panicBuyingItemTags.isEmpty()){
            	ObjectTaggedDO objectTaggedDO = panicBuyingItemTags.get(0);
            	Integer activityNum = (null == objectTaggedDO.getActivityNum() ? 0 : objectTaggedDO.getActivityNum());
            	Integer activitySoldNum = (null == objectTaggedDO.getActivitySoldNum() ? 0 : objectTaggedDO.getActivitySoldNum());
            	if(2 == type){		//如果type=2加库存,type=1或其它减库存
                	objectTaggedDO.setActivityNum(activityNum+num);
                	objectTaggedDO.setActivitySoldNum(activitySoldNum-num);
                }else{			//其它减库存
                	objectTaggedDO.setActivityNum(activityNum-num);
                	if((activityNum-num) < 0){
                		logger.error("update ObjectTagged sold quantity and stock failed. itemId:" + itemId + ",num:" + num);
                		return false;
                	}
                	objectTaggedDO.setActivitySoldNum(activitySoldNum+num);
                }
            	if(objectTaggedDOMapper.updateByPrimaryKeySelective(objectTaggedDO) < 1){
            		if(null != transactionStatus){
        				transactionStatus.setRollbackOnly();
        			}
            		return false;
            	}
            }*/
        //}
        return true;
    }
    
    /**
     * 更新库存
     *
     * @param order
     * @return
     */
    private boolean updateStock(Order order, TransactionStatus transactionStatus) {
        if (Long.compare(order.getCategoryId(), Constants.AppointmentServiceCategory.PostFeeService.getCategoryId()) != 0) {
            long itemId = order.getArtificialId();
            long num = order.getNum();
            GrouponDOExample gExample = new GrouponDOExample();
            gExample.createCriteria().andItemIdEqualTo(itemId).andStatusEqualTo(BizConstants.ItemApproveStatus.ON_SALE.getStatus());
            gExample.setOrderByClause("online_end_time DESC");
            List<GrouponDO> grouponDOs = grouponDOMapper.selectByExample(gExample);
            //是团购商品
            if (null != grouponDOs && grouponDOs.size() > 0) {
                long currentStock = grouponDOs.get(0).getQuantity();
                long soldQuantity = grouponDOs.get(0).getSoldQuantity();
                long grouponId = grouponDOs.get(0).getId();
                long version = grouponDOs.get(0).getVersion();
                currentStock = currentStock - num;
                if (currentStock < 0) {
                    currentStock = 0;
                }
                soldQuantity = soldQuantity + num;
                GrouponDO grouponDO = new GrouponDO();
                grouponDO.setQuantity(currentStock);
                grouponDO.setSoldQuantity(soldQuantity);
                grouponDO.setVersion(version + 1l);
                GrouponDOExample gExample1 = new GrouponDOExample();
                gExample1.createCriteria().andIdEqualTo(grouponId).andVersionEqualTo(version);
                if (grouponDOMapper.updateByExampleSelective(grouponDO, gExample1) < 1) {
                	logger.error("update groupon stock quantity failed. itemId:" + itemId + ",num:" + num);
                    return true;
                } else {
                    //更新商品销售数量
                    Item item = itemMapper.selectByPrimaryKey(itemId);
                    if (null != item) {
                        long itemSoldQuantity = 0;
                        if (null == item.getSoldQuantity()) {
                            itemSoldQuantity = 0;
                        } else {
                            if (null == item.getSoldQuantity()) {
                                itemSoldQuantity = 0;
                            } else {
                                itemSoldQuantity = item.getSoldQuantity();
                            }
                        }
                        itemSoldQuantity = itemSoldQuantity + num;
                        Item item1 = new Item();
                        item1.setSoldQuantity((int) itemSoldQuantity);
                        long itemNum = item.getNum();
                        itemNum = itemNum - num;
                        item1.setNum((int) itemNum);
                        item1.setVersion(item.getVersion() + 1l);
                        item1.setLastUpdated(new Date());
                        ItemExample iExample = new ItemExample();
                        iExample.createCriteria().andIdEqualTo(item.getId()).andVersionEqualTo(item.getVersion());
                        if (itemMapper.updateByExampleSelective(item1, iExample) < 1) {
                        	logger.error("update item sold quantity and stock failed. itemId:" + itemId + ",num:" + num);
                            //com.welink.web.common.filter.Profiler.release();
                            return true;
                        }
                    }
                }
            } else {//非团购商品  更新item表中的库存
                Item item = itemMapper.selectByPrimaryKey(itemId);
                if (null != item) {
                    long itemSoldQuantity = 0;
                    if (null == item.getSoldQuantity()) {
                        itemSoldQuantity = 0;
                    } else {
                        if (null == item.getSoldQuantity()) {
                            itemSoldQuantity = 0;
                        } else {
                            itemSoldQuantity = item.getSoldQuantity();
                        }
                    }
                    itemSoldQuantity = itemSoldQuantity + num;
                    Item item1 = new Item();
                    item1.setSoldQuantity((int) itemSoldQuantity);
                    long itemNum = item.getNum();
                    itemNum = itemNum - num;
                    ItemExample iExample = new ItemExample();
                    item1.setVersion(item.getVersion() + 1l);
                    item1.setLastUpdated(new Date());
                    if (itemNum <= 0) {
                        //下架商品
                        item1.setNum(0);
                        item1.setApproveStatus(BizConstants.ItemApproveStatus.OFF_SALE.getStatus());
                        iExample.createCriteria().andIdEqualTo(item.getId()).andVersionEqualTo(item.getVersion());
                    } else {
                        item1.setNum((int) itemNum);
                        iExample.createCriteria().andIdEqualTo(item.getId()).andVersionEqualTo(item.getVersion());
                    }
                    if (itemMapper.updateByExampleSelective(item1, iExample) < 1) {
                    	logger.error("update item sold quantity and stock failed. itemId:" + itemId + ",num:" + num);
                        //com.welink.web.common.filter.Profiler.release();
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    public List<Order> findOrdersByTradeId(Long tradeId){
    	checkNotNull(tradeId);
    	OrderExample orderExample = new OrderExample();
    	orderExample.createCriteria().andTradeIdEqualTo(tradeId).andArtificialIdGreaterThan(0L);
    	List<Order> orderList = orderMapper.selectByExample(orderExample);
    	return orderList;
    }
    
    @Override
    public BaseResult<Trade> createNewLotteryDrawTrade(@Nonnull final Long sellerId,
                                                  @Nonnull Long shopId,
                                                  @Nonnull final Long communityId,
                                                  @Nonnull final Long buyerId,
                                                  @Nonnull final Long buildingId,
                                                  @Nonnull List<ImmutablePair<Long, Integer>> itemIdNumPairList,
                                                  @Nonnull final Date consignTime,
                                                  @Nullable final String picUrls,
                                                  @Nullable final String buyerMessage,
                                                  @Nullable final byte tradeFrom,
                                                  @Nonnull final byte payType,
                                                  @Nonnull long consigneeId,
                                                  @Nonnull final byte shippingType,
                                                  final Date appointDeliveryTime
    ) {
    	checkNotNull(shopId);
        checkNotNull(communityId);
        checkNotNull(payType);
        checkNotNull(buyerId);
        checkNotNull(buildingId);
        checkArgument(!checkNotNull(itemIdNumPairList).isEmpty());
        checkNotNull(consignTime);

        /*if (shippingType != BizConstants.SELF_PICK_SHIPPING_TYPE && communityId < 0) {
            logger.error("非自提订单并且不在服务范围内，限制下单,buyerId:" + buyerId + ",shopId:" + shopId);
            return BaseResult.failure(BuyItemResultCode.NOT_IN_SERVICE_ZONE.getCode(), BuyItemResultCode.NOT_IN_SERVICE_ZONE.getMessage());
        }*/

        final List<Long> itemIds = Lists.newArrayListWithCapacity(itemIdNumPairList.size());
        final Map<Long, Integer> itemIdNumMap = Maps.newHashMapWithExpectedSize(itemIdNumPairList.size());

        for (ImmutablePair<Long, Integer> itemIdNumPair : itemIdNumPairList) {
            itemIds.add(itemIdNumPair.getLeft());
            itemIdNumMap.put(itemIdNumPair.getLeft(), itemIdNumPair.getRight());
        }

        ItemExample itemExample = new ItemExample();
        itemExample.createCriteria() //
                .andIdIn(itemIds);

        final List<Item> items = itemMapper.selectByExample(itemExample);

        // 先判断商品是否存在
        if (items == null || items.size() != itemIdNumPairList.size()) {
            logger.error("it can not happen, the item with ids - {} dose not exist..", ToStringBuilder.reflectionToString(itemIds));
            return BaseResult.failure(BuyItemResultCode.BUY_ITEM_ERROR_ITEM_NOT_EXSIST.getCode(), BuyItemResultCode.BUY_ITEM_ERROR_ITEM_NOT_EXSIST.getMessage());
        }
        
        final Trade trade = new Trade();

        final Long tradeId = TradeUtil.genTradeNO(buyerId);

        trade.setBuyerId(buyerId);
        //trade.setAppointDeliveryTime(appointDeliveryTime);
        trade.setShopId(shopId);
        trade.setConsigneeId(consigneeId);
        trade.setTradeFrom(tradeFrom);
        trade.setTradeId(tradeId);
        trade.setShippingType(shippingType);


        return transactionTemplate.execute(new TransactionCallback<BaseResult<Trade>>() {
            @Override
            public BaseResult<Trade> doInTransaction(TransactionStatus transactionStatus) {
            	String title = "";
                List<Long> orderIds = Lists.newArrayListWithCapacity(items.size());
                List<ImmutablePair<Order, Item>> orderItemPairList = Lists.newArrayListWithCapacity(items.size());

                long totalFee = 0L;
            	for(Item item : items){
	            	Order appointmentServiceOrder = new Order();
	                appointmentServiceOrder.setCommunityId(communityId);
	                appointmentServiceOrder.setCategoryId(item.getCategoryId());
	                appointmentServiceOrder.setArtificialId(item.getId());
	                appointmentServiceOrder.setBuyerId(buyerId);
	                appointmentServiceOrder.setShippingType((int) shippingType);
	                appointmentServiceOrder.setTradeId(tradeId);
	                appointmentServiceOrder.setTitle(item.getTitle());
	                //去除预约时间
                    //appointmentServiceOrder.setConsignTime(consignTime);
                    appointmentServiceOrder.setDateCreated(new Date());
                    appointmentServiceOrder.setIsServiceOrder((byte) 1);
                    appointmentServiceOrder.setNum(itemIdNumMap.get(item.getId()));
                    appointmentServiceOrder.setSellerId(sellerId);
                    appointmentServiceOrder.setSellerType((byte) 1);
                    appointmentServiceOrder.setHasPanicTag((byte)0);	//是否有抢购标(0=没有;1=有)
                    appointmentServiceOrder.setIsReturnProfit((byte)0);	//是否退分润(0=没退;1=已退)
                    // 如果输入有图片，就把图片放到交易里面，如果商品没有图片，就把图片添加到商品里面；如果没有输入图片，就看商品表里面有没有，有图片就放入图片
                    if (StringUtils.isNoneBlank(picUrls)) {
                        appointmentServiceOrder.setPicUrl(picUrls);
                        if (StringUtils.isNoneBlank(item.getPicUrls())) {
                            item.setPicUrls(picUrls);
                        }
                    } else {
                        if (StringUtils.isNoneBlank(item.getPicUrls())) {
                            appointmentServiceOrder.setPicUrl(StringUtils.split(item.getPicUrls(), ';')[0]);
                        }
                    }
                    
                    appointmentServiceOrder.setVersion(1L);
                    appointmentServiceOrder.setLastUpdated(new Date());
                    appointmentServiceOrder.setNum(itemIdNumMap.get(item.getId()));
                    appointmentServiceOrder.setStatus(Constants.TradeStatus.WAIT_BUYER_PAY.getTradeStatusId());
                    appointmentServiceOrder.setPrice(item.getPrice());
					appointmentServiceOrder.setTotalFee(0L);
                    
                    orderItemPairList.add(ImmutablePair.of(appointmentServiceOrder, item));

                    if (orderMapper.insert(appointmentServiceOrder) > 0) {	//插入商品订单
                        orderIds.add(appointmentServiceOrder.getId());
                      //更新库存和销售量 针对非物流订单
                      if(!updateStock2(null, appointmentServiceOrder, transactionStatus, 1)){	//更新库存(1=减库存)
                    	  transactionStatus.setRollbackOnly();
                    	  // TODO: 数据库异常，打点
                    	  return BaseResult.failure(BuyItemResultCode.BUY_ITEM_ERROR_UNKNOWN.getCode(), BuyItemResultCode.BUY_ITEM_ERROR_UNKNOWN.getMessage()+"，或库存不够！");
                      }
                    } else {
                        transactionStatus.setRollbackOnly();
                        // TODO: 数据库异常，打点
                        return BaseResult.failure(BuyItemResultCode.BUY_ITEM_ERROR_UNKNOWN.getCode(), BuyItemResultCode.BUY_ITEM_ERROR_UNKNOWN.getMessage());
                    }
                    
            	}
            	//抽奖记录操作
            	UserInteractionRecordsDOExample userInteractionRecordsDOExample = new UserInteractionRecordsDOExample();
            	userInteractionRecordsDOExample.createCriteria().andUserIdEqualTo(buyerId).andTypeEqualTo(PromotionType.ITEM_LOTTERY_DRAW.getCode())
            		.andValueEqualTo(0).andDestinationEqualTo(items.get(0).getId().toString())
            		.andDateCreatedGreaterThan(addDay(new Date(), -7));	//value(0=未领取；1=已领取)
            	//查询中奖记录
            	List<UserInteractionRecordsDO> itemLotteryDrawList = userInteractionRecordsDOMapper.selectByExample(userInteractionRecordsDOExample);
            	if(!itemLotteryDrawList.isEmpty()){
            		UserInteractionRecordsDO userInteractionRecordsDO = itemLotteryDrawList.get(0);
            		if(null != userInteractionRecordsDO && null != userInteractionRecordsDO.getId()){
            			userInteractionRecordsDO.setValue(1);		//value(0=未领取；1=已领取)
            			userInteractionRecordsDO.setTargetId(String.valueOf(tradeId));
            			//更新中奖记录为已领取
            			if(userInteractionRecordsDOMapper.updateByPrimaryKeySelective(userInteractionRecordsDO) < 1){
            				transactionStatus.setRollbackOnly();
            				return BaseResult.failure(BuyItemResultCode.NOT_LOTTERY_DRAW_REWARD.getCode(), BuyItemResultCode.NOT_LOTTERY_DRAW_REWARD.getMessage());
            			}
            		}
            	}
            	
            	//创建trade 创建完了服务订单，创建这笔交易
                trade.setBuildingId(buildingId);
                trade.setVersion(1L);
                if (StringUtils.isBlank(buyerMessage)) {
                    trade.setHasBuyerMessage((byte) 0);
                } else {
                    trade.setHasBuyerMessage((byte) 1);
                    trade.setBuyerMessage(buyerMessage);
                }
                if (Byte.compare(payType, Constants.PayType.OFF_LINE.getPayTypeId()) == 0) {
                    trade.setType(Constants.TradeType.cod.getTradeTypeId());
                    trade.setCodStatus(Constants.CodStatus.ACCEPTED_BY_COMPANY.getCodStatusId());
                    trade.setStatus(Constants.TradeStatus.WAIT_SELLER_SEND_GOODS.getTradeStatusId());
                } else {
                    trade.setType(Constants.TradeType.fixed.getTradeTypeId());
                    trade.setCodStatus(Constants.CodStatus.NEW_CREATED.getCodStatusId());
                    trade.setStatus(Constants.TradeStatus.WAIT_BUYER_PAY.getTradeStatusId());
                }
                trade.setCommunityId(communityId);
                trade.setDateCreated(new Date());
                trade.setLastUpdated(new Date());
                if (StringUtils.isNoneBlank(picUrls)) {
                    trade.setPicUrl(StringUtils.split(picUrls, ';')[0]);
                }

                trade.setCategoryId(items.iterator().next().getCategoryId());
                //price指购买的商品原价
                trade.setPrice(totalFee);
                if(1>2){
                	//运费计算
                	totalFee += Constants.POST_FEE;
                	trade.setTotalFee(totalFee);//加运费
                	trade.setHasPostFee((byte) 1);
                	trade.setPostFee(Constants.POST_FEE);
                	//创建运费订单
                	Order appointmentServiceOrderPost = new Order();
                	appointmentServiceOrderPost.setCommunityId(communityId);
                	appointmentServiceOrderPost.setCategoryId(Constants.AppointmentServiceCategory.PostFeeService.getCategoryId());
                	appointmentServiceOrderPost.setArtificialId(-1l);
                	appointmentServiceOrderPost.setBuyerId(buyerId);
                	appointmentServiceOrderPost.setTitle("运费");
                	appointmentServiceOrderPost.setShippingType((int) shippingType);
                	//去掉预约时间
                	//appointmentServiceOrder.setConsignTime(consignTime);
                	appointmentServiceOrderPost.setDateCreated(new Date());
                	appointmentServiceOrderPost.setIsServiceOrder((byte) 1);
                	appointmentServiceOrderPost.setNum(1);
                	appointmentServiceOrderPost.setSellerId(sellerId);
                	appointmentServiceOrderPost.setSellerType((byte) 1);
                	appointmentServiceOrderPost.setPicUrl(null);
                	appointmentServiceOrderPost.setPrice(Constants.POST_FEE);
                	appointmentServiceOrderPost.setTotalFee(Constants.POST_FEE);
                	appointmentServiceOrderPost.setVersion(1L);
                	appointmentServiceOrderPost.setLastUpdated(new Date());
                	appointmentServiceOrderPost.setStatus(Constants.TradeStatus.WAIT_BUYER_PAY.getTradeStatusId());
                	Item itemPost = new Item();
                	itemPost.setId(-1l);
                	itemPost.setNum(1);
                	itemPost.setPrice(Constants.POST_FEE);
                	orderItemPairList.add(ImmutablePair.of(appointmentServiceOrderPost, itemPost));
                	if (orderMapper.insertSelective(appointmentServiceOrderPost) > 0) {	//插入运费订单
                		orderIds.add(appointmentServiceOrderPost.getId());
                	} else {
                		transactionStatus.setRollbackOnly();
                		// TODO: 数据库异常，打点
                		return BaseResult.failure(BuyItemResultCode.BUY_ITEM_ERROR_UNKNOWN.getCode(), BuyItemResultCode.BUY_ITEM_ERROR_UNKNOWN.getMessage());
                	}
                }
                
                trade.setOrders(StringUtils.join(orderIds, ';'));
                trade.setTimeoutActionTime(new DateTime(consignTime.getTime()).plusMinutes(BizConstants.TRADE_OUT_OF_DATE_MINUTE_24_HOUR).toDate());
                //去掉预约时间
//                trade.setConsignTime(consignTime);
                trade.setSellerId(sellerId);
                trade.setCanRate((byte) 1);
                trade.setBuyerRate((byte) 0);
                /*if (zeroPay) {
                } else {*/
                trade.setPayType(payType);
                //}
                trade.setStatus(Constants.TradeStatus.WAIT_SELLER_SEND_GOODS.getTradeStatusId());
                trade.setPayType(Constants.PayType.ONLINE_ZERO_PAY.getPayTypeId());
                trade.setType(Constants.TradeType.fixed.getTradeTypeId());
                trade.setCodStatus(Constants.CodStatus.ACCEPTED_BY_COMPANY.getCodStatusId());
                trade.setStatus(Constants.TradeStatus.WAIT_SELLER_SEND_GOODS.getTradeStatusId());
                
                trade.setTotalFee(0L);
                trade.setTitle(StringUtils.substring(title, 0, 22) + "");
                trade.setIsProfit(Byte.valueOf("1"));	//是否分润（0=未分润，1=已分润）
                trade.setCrowdfundDetailId(-1L);
                trade.setCrowdfundRefundStatus(Constants.CrowdfundRefundStatus.SUCCESS.getStatusId()); //众筹退款状态

                if (1 != tradeMapper.insert(trade)) {
                    transactionStatus.setRollbackOnly();
                    // TODO: 数据库异常，打点
                    return BaseResult.failure(BuyItemResultCode.BUY_ITEM_ERROR_UNKNOWN.getCode(), BuyItemResultCode.BUY_ITEM_ERROR_UNKNOWN.getMessage());
                }
                
                for (ImmutablePair<Order, Item> orderItemImmutablePair : orderItemPairList) {
                    Snapshot snapshot = new Snapshot();
                    snapshot.setTradeId(trade.getTradeId());
                    snapshot.setOrderId(orderItemImmutablePair.getLeft().getId());
                    snapshot.setDateCreated(new Date());
                    snapshot.setLastUpdated(new Date());
                    snapshot.setDetail(JSON.toJSONString(orderItemImmutablePair.getRight()));

                    if (1 != snapshotMapper.insertSelective(snapshot)) {
                        transactionStatus.setRollbackOnly();
                        // TODO: 数据库异常，打点
                        return BaseResult.failure(BuyItemResultCode.BUY_ITEM_ERROR_UNKNOWN.getCode(), BuyItemResultCode.BUY_ITEM_ERROR_UNKNOWN.getMessage());
                    }
                    try {
                        OrderExample orderExample = new OrderExample();
                        orderExample.createCriteria().andIdEqualTo(orderItemImmutablePair.getLeft().getId());
                        Order order = new Order();
                        order.setSnapshotId(snapshot.getId());
                        if (orderMapper.updateByExampleSelective(order, orderExample) < 1) {
                            transactionStatus.setRollbackOnly();
                            return BaseResult.failure(BuyItemResultCode.ORDER_SNAPSHOT_FAILED.getCode(), BuyItemResultCode.ORDER_SNAPSHOT_FAILED.getMessage());
                        }
                    } catch (Exception e) {
                        transactionStatus.setRollbackOnly();
                        return BaseResult.failure(BuyItemResultCode.ORDER_SNAPSHOT_FAILED.getCode(), BuyItemResultCode.ORDER_SNAPSHOT_FAILED.getMessage());
                    }
                }
            	
            	return new BaseResult<Trade>(trade);
            }
        });
    	//return null;
    }
    
    /**
     * 创建定制订单
     * @param sellerId
     * @param shopId
     * @param communityId
     * @param buyerId
     * @param buildingId
     * @param consignTime
     * @param picUrls
     * @param buyerMessage
     * @param tradeFrom
     * @param payType
     * @param consigneeId
     * @param shippingType
     * @param appointDeliveryTime
     * @param detectReportId
     * @return
     */
    @Override
	public BaseResult<Trade> createNewAppointmentDz(@Nonnull final Long sellerId, Long shopId,
			@Nonnull final long point,
            @Nonnull final long userCouponId,
			@Nonnull final Long communityId, @Nonnull final Long buyerId, 
			@Nonnull final Long buildingId, @Nonnull final Date consignTime,
			@Nonnull final String picUrls, @Nonnull final String buyerMessage, 
			byte tradeFrom, final byte payType,
			@Nonnull final long consigneeId, @Nonnull final byte shippingType, 
			@Nonnull final Date appointDeliveryTime,
			@Nonnull final Long detectReportId) {
    	checkNotNull(shopId);
        checkNotNull(communityId);
        checkNotNull(payType);
        checkNotNull(buyerId);
        checkNotNull(buildingId);
        checkNotNull(consignTime);
        
        //测肤报告
        final MikuMineDetectReportDO mikuMineDetectReportDO = mikuMineDetectReportDOMapper.selectByPrimaryKey(detectReportId);
        if(null != mikuMineDetectReportDO){
        	if(!mikuMineDetectReportDO.getUserId().equals(buyerId)){
        		return BaseResult.failure(-999, "亲~您未有测肤报告纪录，不能下单~");
        	}else{
        		if(null == mikuMineDetectReportDO.getMoney()){
        			return BaseResult.failure(-999, "亲~此测肤报告未定价，不能下单~");
        		}
        	}
        }else{
        	return BaseResult.failure(-999, "亲~您未有测肤报告纪录，不能下单~");
        }
        
        Long pUserId = null == mikuMineDetectReportDO.getServiceId() ? 0L : mikuMineDetectReportDO.getServiceId();
        
        final Trade trade = new Trade();

        final Long tradeId = TradeUtil.genTradeNO(buyerId);

        trade.setBuyerId(buyerId);
        //trade.setAppointDeliveryTime(appointDeliveryTime);
        trade.setShopId(shopId);
        trade.setConsigneeId(consigneeId);
        trade.setTradeFrom(tradeFrom);
        trade.setTradeId(tradeId);
        trade.setShippingType(shippingType);
        trade.setDetectReportId(detectReportId);
        trade.setpUserId(pUserId);


        return transactionTemplate.execute(new TransactionCallback<BaseResult<Trade>>() {
            @Override
            public BaseResult<Trade> doInTransaction(TransactionStatus transactionStatus) {
            	String title = "";
            	List<Long> orderIds = Lists.newArrayListWithCapacity(1);
                List<ImmutablePair<Order, Item>> orderItemPairList = Lists.newArrayListWithCapacity(1);
                
                long validCouponTotalFee = 0L;
                long validPointTotalFee = 0L;

                long totalFee = 0L;
            	Order appointmentServiceOrder = new Order();
                appointmentServiceOrder.setCommunityId(communityId);
                appointmentServiceOrder.setCategoryId(-1L);
                appointmentServiceOrder.setArtificialId(-2L);	//定制商品
                appointmentServiceOrder.setBuyerId(buyerId);
                appointmentServiceOrder.setShippingType((int) shippingType);
                appointmentServiceOrder.setTradeId(tradeId);
                appointmentServiceOrder.setTitle("定制产品");
                //去除预约时间
                //appointmentServiceOrder.setConsignTime(consignTime);
                appointmentServiceOrder.setDateCreated(new Date());
                appointmentServiceOrder.setIsServiceOrder((byte) 1);
                appointmentServiceOrder.setNum(1);
                appointmentServiceOrder.setSellerId(sellerId);
                appointmentServiceOrder.setSellerType((byte) 1);
                appointmentServiceOrder.setHasPanicTag((byte)0);	//是否有抢购标(0=没有;1=有)
                appointmentServiceOrder.setIsReturnProfit((byte)0);	//是否退分润(0=没退;1=已退)
                // 如果输入有图片，就把图片放到交易里面，如果商品没有图片，就把图片添加到商品里面；如果没有输入图片，就看商品表里面有没有，有图片就放入图片
                /*if (StringUtils.isNoneBlank(picUrls)) {
                    appointmentServiceOrder.setPicUrl(picUrls);
                    if (StringUtils.isNoneBlank(item.getPicUrls())) {
                        item.setPicUrls(picUrls);
                    }
                } else {
                    if (StringUtils.isNoneBlank(item.getPicUrls())) {
                        appointmentServiceOrder.setPicUrl(StringUtils.split(item.getPicUrls(), ';')[0]);
                    }
                }*/
                
                appointmentServiceOrder.setVersion(1L);
                appointmentServiceOrder.setLastUpdated(new Date());
                appointmentServiceOrder.setStatus(Constants.TradeStatus.WAIT_BUYER_PAY.getTradeStatusId());
                appointmentServiceOrder.setPrice(Long.valueOf(mikuMineDetectReportDO.getMoney()));
				appointmentServiceOrder.setTotalFee(Long.valueOf(mikuMineDetectReportDO.getMoney()));
				
				if(null != mikuMineDetectReportDO.getMoney()){
					validCouponTotalFee += Long.valueOf(mikuMineDetectReportDO.getMoney());
					validPointTotalFee += Long.valueOf(mikuMineDetectReportDO.getMoney());
				}
                

                if (orderMapper.insert(appointmentServiceOrder) > 0) {	//插入商品订单
                    orderIds.add(appointmentServiceOrder.getId());
                  //更新库存和销售量 针对非物流订单
                } else {
                    transactionStatus.setRollbackOnly();
                    // TODO: 数据库异常，打点
                    return BaseResult.failure(BuyItemResultCode.BUY_ITEM_ERROR_UNKNOWN.getCode(), BuyItemResultCode.BUY_ITEM_ERROR_UNKNOWN.getMessage());
                }
                totalFee += Long.valueOf(mikuMineDetectReportDO.getMoney());
                Item itemDz = new Item();
                itemDz.setId(-2L);
                itemDz.setNum(1);
                itemDz.setPrice(Constants.POST_FEE);
            	orderItemPairList.add(ImmutablePair.of(appointmentServiceOrder, itemDz));
            	
            	//price指购买的商品原价
                trade.setPrice(totalFee);
                
                if (totalFee < Constants.POST_FEE_STEP) {
                    totalFee += Constants.POST_FEE;
                    trade.setTotalFee(totalFee);//加运费
                    trade.setHasPostFee((byte) 1);
                    trade.setPostFee(Constants.POST_FEE);
                    validPointTotalFee += Constants.POST_FEE;
                    validCouponTotalFee += Constants.POST_FEE;
                    //创建运费订单
                    Order appointmentServiceOrderPost = new Order();
                    appointmentServiceOrderPost.setCommunityId(communityId);
                    appointmentServiceOrderPost.setCategoryId(Constants.AppointmentServiceCategory.PostFeeService.getCategoryId());
                    appointmentServiceOrderPost.setArtificialId(-1l);
                    appointmentServiceOrderPost.setBuyerId(buyerId);
                    appointmentServiceOrderPost.setTitle("运费");
                    appointmentServiceOrderPost.setShippingType((int) shippingType);
                    //去掉预约时间
                    //appointmentServiceOrder.setConsignTime(consignTime);
                    appointmentServiceOrderPost.setDateCreated(new Date());
                    appointmentServiceOrderPost.setIsServiceOrder((byte) 1);
                    appointmentServiceOrderPost.setNum(1);
                    appointmentServiceOrderPost.setSellerId(sellerId);
                    appointmentServiceOrderPost.setSellerType((byte) 1);
                    appointmentServiceOrderPost.setPicUrl(null);
                    appointmentServiceOrderPost.setPrice(Constants.POST_FEE);
                    appointmentServiceOrderPost.setTotalFee(Constants.POST_FEE);
                    appointmentServiceOrderPost.setVersion(1L);
                    appointmentServiceOrderPost.setLastUpdated(new Date());
                    appointmentServiceOrderPost.setStatus(Constants.TradeStatus.WAIT_BUYER_PAY.getTradeStatusId());
                    appointmentServiceOrderPost.setReturnStatus(Constants.ReturnGoodsStatus.NORMAL.getStatusId()); //正常单(不是退货单)
                    appointmentServiceOrderPost.setHasPanicTag((byte)0);	//是否有抢购标(0=没有;1=有)
                    appointmentServiceOrderPost.setIsReturnProfit((byte)0);	//是否退分润(0=没退;1=已退)
                    Item item = new Item();
                    item.setId(-1l);
                    item.setNum(1);
                    item.setPrice(Constants.POST_FEE);
                    orderItemPairList.add(ImmutablePair.of(appointmentServiceOrder, item));
                    if (orderMapper.insertSelective(appointmentServiceOrder) > 0) {
                        orderIds.add(appointmentServiceOrder.getId());
                    } else {
                        transactionStatus.setRollbackOnly();
                        // TODO: 数据库异常，打点
                        return BaseResult.failure(BuyItemResultCode.BUY_ITEM_ERROR_UNKNOWN.getCode(), BuyItemResultCode.BUY_ITEM_ERROR_UNKNOWN.getMessage());
                    }
                }
            	
            	UserCouponDO userCouponDO = null;
                CouponDO couponDO = null;
                List<PointAccountDO> pointAccountDOs = null;
                long couponValue = 0;
                boolean zeroPay = false;
                if (userCouponId > 0) {

                    userCouponDO = userCouponDOMapper.selectByPrimaryKey(userCouponId);

                    if (userCouponDO != null) {
                        couponDO = couponDOMapper.selectByPrimaryKey(userCouponDO.getCouponId());
                        if (couponDO != null) {
                            couponValue = couponDO.getValue();
                        }
                    }

                    if (userCouponDO == null || couponDO == null || couponDO.getMinValue() > validCouponTotalFee) {
                        transactionStatus.setRollbackOnly();
                        return BaseResult.failure(BuyItemResultCode.COUPON_PARAM_ERROR.getCode(), BuyItemResultCode.COUPON_PARAM_ERROR.getMessage());
                    }
                }
                if (point > 0) {
                    PointAccountDOExample pointAccountDOExample = new PointAccountDOExample();
                    pointAccountDOExample.createCriteria().andUserIdEqualTo(buyerId).andStatusEqualTo((byte) 1);
                    pointAccountDOs = pointAccountDOMapper.selectByExample(pointAccountDOExample);
                    if (pointAccountDOs == null || pointAccountDOs.size() <= 0 || pointAccountDOs.get(0).getAvailableBalance() < point) {
                        transactionStatus.setRollbackOnly();
                        return BaseResult.failure(BuyItemResultCode.INPUT_POINT_TOO_LARGE.getCode(), BuyItemResultCode.INPUT_POINT_TOO_LARGE.getMessage());
                    }
                    if (point > validPointTotalFee) {
                        transactionStatus.setRollbackOnly();
                        return BaseResult.failure(BuyItemResultCode.POINT_TOO_MORE_SERVICE_ZONE.getCode(), BuyItemResultCode.POINT_TOO_MORE_SERVICE_ZONE.getMessage());
                    }
                    long todayUsedPoint = findTodayUsePointByUserId(buyerId);
                    if (todayUsedPoint + point > Constants.MAX_USE_POINT) {
                        logger.warn("crated trade failed. use point too much... used today :" + todayUsedPoint + ",to use point:" + point + ",buyerId:" + buyerId);
                        transactionStatus.setRollbackOnly();
                        return BaseResult.failure(BuyItemResultCode.CREATE_TRADE_FAILED.getCode(), BuyItemResultCode.CREATE_TRADE_FAILED.getMessage());
                    }
                }

                if (couponValue + point == totalFee && totalFee == validPointTotalFee) {
                    zeroPay = true;
                } else if (couponValue + point > totalFee) {
                    transactionStatus.setRollbackOnly();
                    return BaseResult.failure(BuyItemResultCode.PAY_TOO_LARGE.getCode(), BuyItemResultCode.PAY_TOO_LARGE.getMessage());
                }

                Optional<PromotionResult> promotionResultOptional = Optional.absent();

                //优惠券处理
                if (userCouponId > 0) {
                    if (Byte.compare(payType, Constants.PayType.OFF_LINE.getPayTypeId()) == 0 || zeroPay) {
                        // 直接消费
                        promotionResultOptional = usePromotionAndRecord(buyerId, trade.getPrice(), trade.getTradeId(), PromotionType.COUPON_USE_IN_TRADE_DIRECT.getCode(),
                                ImmutableMap.of("user_coupon", userCouponDO, "couponDOs", Lists.newArrayList(couponDO)));
                    } else {
                        // 冻结，回调消费
                        promotionResultOptional = usePromotionAndRecord(buyerId, trade.getPrice(), trade.getTradeId(), PromotionType.COUPON_USE_IN_TRADE_FROZEN.getCode(),
                                ImmutableMap.of("user_coupon", userCouponDO, "couponDOs", Lists.newArrayList(couponDO)));
                    }
                    totalFee -= couponValue;

                    if (!promotionResultOptional.isPresent()) {
                        transactionStatus.setRollbackOnly();
                        return BaseResult.failure(BuyItemResultCode.BUY_ITEM_ERROR_UNKNOWN.getCode(), BuyItemResultCode.BUY_ITEM_ERROR_UNKNOWN.getMessage());
                    }

                    PromotionResult promotionResult = promotionResultOptional.get();

                    if (!promotionResult.getReward()) {
                        transactionStatus.setRollbackOnly();
                        return BaseResult.failure(promotionResult.getCode(), promotionResult.getMessage());
                    }
                }

                //积分处理
                if (point > 0) {
                    //货到付款直接使用积分，其他支付方式先冻结
                    if (Byte.compare(payType, Constants.PayType.OFF_LINE.getPayTypeId()) == 0 || zeroPay) {
                        promotionResultOptional = usePromotionAndRecord(buyerId, (int) point, trade.getTradeId(), PromotionType.POINT_USE_IN_TRADE_DIRECT.getCode());
                    } else {
                        promotionResultOptional = usePromotionAndRecord(buyerId, (int) point, trade.getTradeId(), PromotionType.POINT_USE_IN_TRADE_FROZEN.getCode());
                    }

                    if (!promotionResultOptional.isPresent()) {
                        transactionStatus.setRollbackOnly();
                        return BaseResult.failure(BuyItemResultCode.BUY_ITEM_ERROR_UNKNOWN.getCode(), BuyItemResultCode.BUY_ITEM_ERROR_UNKNOWN.getMessage());
                    }

                    PromotionResult promotionResult = promotionResultOptional.get();

                    if (!promotionResult.getReward()) {
                        transactionStatus.setRollbackOnly();
                        return BaseResult.failure(promotionResult.getCode(), promotionResult.getMessage());
                    }

                    totalFee -= point;
                    trade.setPointFee(point);
                }
                    
            	
            	//创建trade 创建完了服务订单，创建这笔交易
                trade.setBuildingId(buildingId);
                trade.setVersion(1L);
                if (StringUtils.isBlank(buyerMessage)) {
                    trade.setHasBuyerMessage((byte) 0);
                } else {
                    trade.setHasBuyerMessage((byte) 1);
                    trade.setBuyerMessage(buyerMessage);
                }
                if (Byte.compare(payType, Constants.PayType.OFF_LINE.getPayTypeId()) == 0) {
                    trade.setType(Constants.TradeType.cod.getTradeTypeId());
                    trade.setCodStatus(Constants.CodStatus.ACCEPTED_BY_COMPANY.getCodStatusId());
                    trade.setStatus(Constants.TradeStatus.WAIT_SELLER_SEND_GOODS.getTradeStatusId());
                } else {
                    trade.setType(Constants.TradeType.fixed.getTradeTypeId());
                    trade.setCodStatus(Constants.CodStatus.NEW_CREATED.getCodStatusId());
                    trade.setStatus(Constants.TradeStatus.WAIT_BUYER_PAY.getTradeStatusId());
                }
                trade.setCommunityId(communityId);
                trade.setDateCreated(new Date());
                trade.setLastUpdated(new Date());
                if (StringUtils.isNoneBlank(picUrls)) {
                    trade.setPicUrl(StringUtils.split(picUrls, ';')[0]);
                }

                trade.setCategoryId(-1L);
                //price指购买的商品原价
                trade.setPrice(totalFee);
                
                trade.setOrders(StringUtils.join(orderIds, ';'));
                trade.setTimeoutActionTime(new DateTime(consignTime.getTime()).plusMinutes(BizConstants.TRADE_OUT_OF_DATE_MINUTE_24_HOUR).toDate());
                //去掉预约时间
                trade.setSellerId(sellerId);
                trade.setCanRate((byte) 1);
                trade.setBuyerRate((byte) 1);
                trade.setStatus(Constants.TradeStatus.WAIT_SELLER_SEND_GOODS.getTradeStatusId());
                if (zeroPay) {
                    trade.setStatus(Constants.TradeStatus.WAIT_SELLER_SEND_GOODS.getTradeStatusId());
                    trade.setPayType(Constants.PayType.ONLINE_ZERO_PAY.getPayTypeId());
                } else {
                    trade.setPayType(payType);
                }
                trade.setType(Constants.TradeType.dz_type.getTradeTypeId());
                /*trade.setCodStatus(Constants.CodStatus.ACCEPTED_BY_COMPANY.getCodStatusId());
                trade.setStatus(Constants.TradeStatus.WAIT_SELLER_SEND_GOODS.getTradeStatusId());*/
                
                trade.setTitle(StringUtils.substring(title, 0, 22) + "");
                trade.setIsProfit(Byte.valueOf("0"));	//是否分润（0=未分润，1=已分润）
                trade.setCrowdfundDetailId(-1L);
                trade.setReturnStatus(Constants.TradeReturnStatus.NORMAL.getStatusId());	//交易退货状态(0=正常单；1=退货中;2=已退货)
                trade.setCrowdfundRefundStatus(Constants.CrowdfundRefundStatus.SUCCESS.getStatusId()); //众筹退款状态

                if (1 != tradeMapper.insert(trade)) {
                    transactionStatus.setRollbackOnly();
                    // TODO: 数据库异常，打点
                    return BaseResult.failure(BuyItemResultCode.BUY_ITEM_ERROR_UNKNOWN.getCode(), BuyItemResultCode.BUY_ITEM_ERROR_UNKNOWN.getMessage());
                }
                
                for (ImmutablePair<Order, Item> orderItemImmutablePair : orderItemPairList) {
                    Snapshot snapshot = new Snapshot();
                    snapshot.setTradeId(trade.getTradeId());
                    snapshot.setOrderId(orderItemImmutablePair.getLeft().getId());
                    snapshot.setDateCreated(new Date());
                    snapshot.setLastUpdated(new Date());
                    snapshot.setDetail(JSON.toJSONString(orderItemImmutablePair.getRight()));

                    if (1 != snapshotMapper.insertSelective(snapshot)) {
                        transactionStatus.setRollbackOnly();
                        // TODO: 数据库异常，打点
                        return BaseResult.failure(BuyItemResultCode.BUY_ITEM_ERROR_UNKNOWN.getCode(), BuyItemResultCode.BUY_ITEM_ERROR_UNKNOWN.getMessage());
                    }
                    try {
                        OrderExample orderExample = new OrderExample();
                        orderExample.createCriteria().andIdEqualTo(orderItemImmutablePair.getLeft().getId());
                        Order order = new Order();
                        order.setSnapshotId(snapshot.getId());
                        if (orderMapper.updateByExampleSelective(order, orderExample) < 1) {
                            transactionStatus.setRollbackOnly();
                            return BaseResult.failure(BuyItemResultCode.ORDER_SNAPSHOT_FAILED.getCode(), BuyItemResultCode.ORDER_SNAPSHOT_FAILED.getMessage());
                        }
                    } catch (Exception e) {
                        transactionStatus.setRollbackOnly();
                        return BaseResult.failure(BuyItemResultCode.ORDER_SNAPSHOT_FAILED.getCode(), BuyItemResultCode.ORDER_SNAPSHOT_FAILED.getMessage());
                    }
                }
            	
            	return new BaseResult<Trade>(trade);
            }
        });
	}
    
    /**
     * 获取用户的默认收货地址
     *
     * @param userId
     * @return
     */
    public ConsigneeAddrDO fetchDefConsignee(long userId) {
        ConsigneeAddrDOExample cExample = new ConsigneeAddrDOExample();
        cExample.createCriteria().andUserIdEqualTo(userId).andGetDefEqualTo((byte) 1).andStatusEqualTo(1);
        List<ConsigneeAddrDO> consigneeAddrDOs = consigneeAddrDOMapper.selectByExample(cExample);
        if (null != consigneeAddrDOs && consigneeAddrDOs.size() > 0) {
            return consigneeAddrDOs.get(0);
        }
        return null;
    }
    
    /**
     * 添加物流单据
     *
     * @param consigneeAddrDO
     * @param type 类型(0=购买物流;1=退货物流)
     * @return
     */
    public long addLogistics(ConsigneeAddrDO consigneeAddrDO, long shippingId, byte type) {
        if (shippingId > 0) {
            CommunityDO communityDO = communityDOMapper.selectByPrimaryKey(shippingId);
            if (null != communityDO) {
                LogisticsDO logisticsDO = new LogisticsDO();
                logisticsDO.setLastUpdated(new Date());
                //logisticsDO.setAddr(communityDO.getLocation());
                //logisticsDO.setAddr(consigneeAddrDO.getCommunity() + BizConstants.SPLIT + consigneeAddrDO.getReceiverAddress());
                logisticsDO.setAddr(consigneeAddrDO.getReceiver_state() + BizConstants.SPLIT + consigneeAddrDO.getReceiverCity()
                		+ BizConstants.SPLIT + consigneeAddrDO.getReceiverDistrict() + BizConstants.SPLIT + consigneeAddrDO.getReceiverAddress());
                
                logisticsDO.setCancelDef((byte) 0);
                logisticsDO.setCity(communityDO.getCity());
                logisticsDO.setCommunity_id(shippingId);
                logisticsDO.setContactName(consigneeAddrDO.getReceiverName());
                logisticsDO.setCountry("中国");
                logisticsDO.setGetDef((byte) 0);
                logisticsDO.setDistrictId(-1l);
                logisticsDO.setMemo(null);
                logisticsDO.setMobile(consigneeAddrDO.getReceiverMobile());
                logisticsDO.setPhone(consigneeAddrDO.getReceiverPhone());
                logisticsDO.setUserId(consigneeAddrDO.getUserId());
                logisticsDO.setZipCode(null);
                logisticsDO.setDateCreated(new Date());
                logisticsDO.setSellerCompany(null);
                logisticsDO.setProvince(communityDO.getProvince());
                logisticsDO.setConsigneeId(BizConstants.SHIPPING_SELF_PICK_CONSING_ID);//自提consignId -1
                logisticsDO.setType(type);
                logisticsDO.setIdCard(consigneeAddrDO.getIdCard());
                if (logisticsDOMapper.insertSelective(logisticsDO) < 0) {
                    return -1;
                }
                return logisticsDO.getId();
            }
            return -1l;
        } else {
            LogisticsDO logisticsDO = new LogisticsDO();
            logisticsDO.setLastUpdated(new Date());
            logisticsDO.setAddr(consigneeAddrDO.getReceiver_state() + BizConstants.SPLIT + consigneeAddrDO.getReceiverCity()
            		+ BizConstants.SPLIT + consigneeAddrDO.getReceiverDistrict() + BizConstants.SPLIT + consigneeAddrDO.getReceiverAddress());
            logisticsDO.setLatitude(consigneeAddrDO.getLatitude());
            logisticsDO.setLongitude(consigneeAddrDO.getLongitude());
            logisticsDO.setCancelDef((byte) 0);
            logisticsDO.setCity(consigneeAddrDO.getReceiverCity());
            logisticsDO.setCommunity_id(consigneeAddrDO.getCommunityId());
            logisticsDO.setContactName(consigneeAddrDO.getReceiverName());
            logisticsDO.setCountry("中国");
            logisticsDO.setGetDef((byte) 0);
            logisticsDO.setDistrictId(-1l);
            logisticsDO.setMemo(null);
            logisticsDO.setMobile(consigneeAddrDO.getReceiverMobile());
            logisticsDO.setPhone(consigneeAddrDO.getReceiverPhone());
            logisticsDO.setUserId(consigneeAddrDO.getUserId());
            logisticsDO.setZipCode(null);
            logisticsDO.setDateCreated(new Date());
            logisticsDO.setSellerCompany(null);
            logisticsDO.setProvince(consigneeAddrDO.getReceiver_state());
            logisticsDO.setConsigneeId(consigneeAddrDO.getId());
            logisticsDO.setIdCard(consigneeAddrDO.getIdCard());
            if (logisticsDOMapper.insertSelective(logisticsDO) < 0) {
                return -1;
            }
            return logisticsDO.getId();
        }
    }
    
    public Long getTopicTotalValue(List<Item> items){
    	
    	return null;
    }
    
    /**
     * 时间向后推n天
     *
     * @param date
     * @param day
     * @return
     */

    public Date addDay(Date date, int day) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(calendar.DATE, day);//把日期往后增加day天.整数往后推,负数往前移动
        date = calendar.getTime();   //这个时间就是日期往后推day天的结果
        return date;
    }
    
}
