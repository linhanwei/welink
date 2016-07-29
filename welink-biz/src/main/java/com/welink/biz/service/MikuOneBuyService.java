/**
 * Project Name:welink-biz
 * File Name:MikuOneBuyService.java
 * Package Name:com.welink.biz.service
 * Date:2015年12月29日下午4:56:52
 * Copyright (c) 2015, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.biz.service;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.tuple.ImmutablePair;
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
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mysql.jdbc.Connection;
import com.welink.biz.common.model.ItemCanBuy;
import com.welink.buy.service.AppointmentTradeService;
import com.welink.buy.service.BuyItemService;
import com.welink.buy.utils.BaseResult;
import com.welink.buy.utils.BuyItemResultCode;
import com.welink.buy.utils.Constants;
import com.welink.commons.commons.TradeUtil;
import com.welink.commons.domain.Item;
import com.welink.commons.domain.ItemExample;
import com.welink.commons.domain.MikuOneBuyDO;
import com.welink.commons.domain.MikuOneBuyDOExample;
import com.welink.commons.domain.MikuUserOneBuyDO;
import com.welink.commons.domain.MikuUserOneBuyDOExample;
import com.welink.commons.domain.Order;
import com.welink.commons.domain.OrderExample;
import com.welink.commons.domain.Snapshot;
import com.welink.commons.domain.Trade;
import com.welink.commons.persistence.ItemMapper;
import com.welink.commons.persistence.MikuItemShareParaDOMapper;
import com.welink.commons.persistence.MikuOneBuyDOMapper;
import com.welink.commons.persistence.MikuUserOneBuyDOMapper;
import com.welink.commons.persistence.OrderMapper;
import com.welink.commons.persistence.SnapshotMapper;
import com.welink.commons.persistence.TradeMapper;
import com.welink.promotion.reactive.UserInteractionEffect;

/**
 * ClassName:MikuOneBuyService <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2015年12月29日 下午4:56:52 <br/>
 * @author   LuoGuangChun
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
@Service(value = "mikuOneBuyService")
public class MikuOneBuyService implements InitializingBean {
	
	static Logger logger = LoggerFactory.getLogger(MikuOneBuyService.class);

    @Resource
    private ItemMapper itemMapper;

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private TradeMapper tradeMapper;

    @Resource
    private SnapshotMapper snapshotMapper;

    @Resource
    private BuyItemService buyItemService;
    
    @Resource
    private AppointmentTradeService appointmentTradeService;

    @Resource
    private PlatformTransactionManager transactionManager;

    @Resource
    private UserInteractionEffect userInteractionEffect;
    
    @Resource
    private MikuItemShareParaDOMapper mikuItemShareParaDOMapper;
    
    @Resource
    private MikuUserOneBuyDOMapper mikuUserOneBuyDOMapper;
    
    @Resource
    private MikuOneBuyDOMapper mikuOneBuyDOMapper;


    private TransactionTemplate transactionTemplate;

    public BaseResult<Trade> createNewOneBuyTrade(@Nonnull final Long sellerId,
                                                  @Nonnull Long shopId,
                                                  @Nonnull final Long communityId,
                                                  @Nonnull final Long buyerId,
                                                  @Nonnull List<ImmutablePair<Long, Integer>> itemIdNumPairList,
                                                  @Nullable final String picUrls,
                                                  @Nullable final String buyerMessage,
                                                  @Nullable final byte tradeFrom,
                                                  @Nonnull final byte payType,
                                                  @Nonnull final byte shippingType
    ) {
    	checkNotNull(shopId);
        checkNotNull(communityId);
        checkNotNull(payType);
        checkNotNull(buyerId);
        checkArgument(!checkNotNull(itemIdNumPairList).isEmpty());

        //if (shippingType != BizConstants.SELF_PICK_SHIPPING_TYPE && communityId < 0) {
        if (communityId < 0) {
            logger.error("非自提订单并且不在服务范围内，限制下单,buyerId:" + buyerId + ",shopId:" + shopId);
            return BaseResult.failure(BuyItemResultCode.NOT_IN_SERVICE_ZONE.getCode(), BuyItemResultCode.NOT_IN_SERVICE_ZONE.getMessage());
        }

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
        //trade.setConsigneeId(consigneeId);
        trade.setTradeFrom(tradeFrom);
        trade.setTradeId(tradeId);
        trade.setShippingType(shippingType);


        return transactionTemplate.execute(new TransactionCallback<BaseResult<Trade>>() {
            @Override
            public BaseResult<Trade> doInTransaction(TransactionStatus transactionStatus) {
            	String title = "";
            	Date nowDate = new Date();
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
					appointmentServiceOrder.setTotalFee(Long.valueOf(itemIdNumMap.get(item.getId())));
                    
                    orderItemPairList.add(ImmutablePair.of(appointmentServiceOrder, item));

                    if (orderMapper.insert(appointmentServiceOrder) > 0) {	//插入商品订单
                        orderIds.add(appointmentServiceOrder.getId());
                      //更新库存和销售量 针对非物流订单
                      //if(!appointmentTradeService.updateStock2(appointmentServiceOrder, transactionStatus, 1)){	//更新库存(1=减库存)
                        if(!updateOneBuyStock(appointmentServiceOrder, transactionStatus, 1)){
                    	  transactionStatus.setRollbackOnly();
                    	  // TODO: 数据库异常，打点
                    	  return BaseResult.failure(BuyItemResultCode.BUY_ITEM_ERROR_UNKNOWN.getCode(), BuyItemResultCode.BUY_ITEM_ERROR_UNKNOWN.getMessage()+"，或库存不够！");
                      }
                    } else {
                        transactionStatus.setRollbackOnly();
                        // TODO: 数据库异常，打点
                        return BaseResult.failure(BuyItemResultCode.BUY_ITEM_ERROR_UNKNOWN.getCode(), BuyItemResultCode.BUY_ITEM_ERROR_UNKNOWN.getMessage());
                    }
                    
                    //插入用户一元购
                    MikuUserOneBuyDO mikuUserOneBuyDO = new MikuUserOneBuyDO();
                    mikuUserOneBuyDO.setUserId(buyerId);
                    mikuUserOneBuyDO.setTradeId(tradeId);
                    mikuUserOneBuyDO.setItemId(item.getId());
                    mikuUserOneBuyDO.setItemName(item.getTitle());
                    mikuUserOneBuyDO.setNum(itemIdNumMap.get(item.getId()));
                    mikuUserOneBuyDO.setIsWin((byte)0);
                    mikuUserOneBuyDO.setStatus((byte)0);	//-1=取消；0=下单；1=已付款
                    mikuUserOneBuyDO.setDateCreated(nowDate);
                    mikuUserOneBuyDO.setLastUpdated(nowDate);
                    mikuUserOneBuyDOMapper.insertSelective(mikuUserOneBuyDO);	//插入用户一元购
            	}
            	
            	//创建trade 创建完了服务订单，创建这笔交易
                //trade.setBuildingId(buildingId);
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
            	//运费计算
            	/*totalFee += Constants.POST_FEE;
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
            	}*/
                
                trade.setOrders(StringUtils.join(orderIds, ';'));
                //trade.setTimeoutActionTime(new DateTime(consignTime.getTime()).plusMinutes(BizConstants.TRADE_OUT_OF_DATE_MINUTE_24_HOUR).toDate());
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
                trade.setType(Constants.TradeType.one_buy_type.getTradeTypeId());	//一元购

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
    
    //可购买数量
  	public List<ItemCanBuy> fetchOutLimitItems(List<Long> itemIds, long profileId){
  		List<ItemCanBuy> itemCanBuys = new ArrayList<>();
  		if(null != itemIds && !itemIds.isEmpty()){
  			for (Long itemId : itemIds) {
  				MikuOneBuyDOExample mikuOneBuyDOExample = new MikuOneBuyDOExample();
  				mikuOneBuyDOExample.createCriteria().andItemIdEqualTo(itemId).andStatusEqualTo((byte)1);
  				List<MikuOneBuyDO> mikuOneBuyDOList = mikuOneBuyDOMapper.selectByExample(mikuOneBuyDOExample);
  				ItemCanBuy itemCanBuy = new ItemCanBuy();
  				if(!mikuOneBuyDOList.isEmpty() && mikuOneBuyDOList.size() == 1){
  					MikuOneBuyDO mikuOneBuyDO = mikuOneBuyDOList.get(0);
  					//int price = (null == mikuOneBuyDO.getItemPrice() ? 0 : Integer.valueOf(String.valueOf(mikuOneBuyDO.getItemPrice())));
  					int canBuyNum = (null == mikuOneBuyDO.getTimes() ? 0 : mikuOneBuyDO.getTimes());
  					itemCanBuy.setItemId(itemId);
  					itemCanBuy.setCap(canBuyNum);
  					itemCanBuy.setRealCap(canBuyNum);
  				}else{
  					itemCanBuy.setItemId(itemId);
  					itemCanBuy.setCap(0);
  					itemCanBuy.setRealCap(0);
  				}
  				itemCanBuys.add(itemCanBuy);
  			}
  		}
  		return itemCanBuys;
  	}
  	
  	/**
     * 更新库存
     *
     * @param type 如果type=2加库存,type=1或其它减库存
     * @param order
     * @return
     */
    public boolean updateOneBuyStock(Order order, TransactionStatus transactionStatus, Integer type) {
    	
        long itemId = order.getArtificialId();
        Integer num = (null == order.getNum() ? 0 : order.getNum());
        if(itemId < 1){
        	return false;
        }
        Map<String,Object> map = new HashMap<String, Object>();
        if(2 == type){		//如果type=2加库存,type=1或其它减库存
        	map.put("times", -num);
        }else{			//其它减库存
        	map.put("times", num);
        }
        map.put("itemId", itemId);
    	if (tradeMapper.updateOneBuyTimes(map) < 1) {	//更新一元购购买人次
        	logger.error("update mikuOneBuyDO times failed. itemId:" + itemId + ",times:" + num);
            //com.welink.web.common.filter.Profiler.release();
            return false;
        }
        return true;
    }
    
    /**
     * 
     * updateUserOneBuyByTrade:(更新用户一元购为已付款). <br/>
     *
     * @author LuoGuangChun
     * @param tradeId
     * @param status	//status(-1=取消;0=未付款;1=已付款)
     * @return
     */
    public boolean updateUserOneBuyByTrade(Long tradeId, byte status){
    	if(null != tradeId && tradeId > 0){
	    	Date nowDate = new Date();
	    	MikuUserOneBuyDO mikuUserOneBuyDO = new MikuUserOneBuyDO();
	    	mikuUserOneBuyDO.setStatus(status);
	    	mikuUserOneBuyDO.setLastUpdated(nowDate);
	    	MikuUserOneBuyDOExample mikuUserOneBuyDOExample = new MikuUserOneBuyDOExample();
	    	mikuUserOneBuyDOExample.createCriteria().andTradeIdEqualTo(tradeId);
	    	if(mikuUserOneBuyDOMapper.updateByExampleSelective(mikuUserOneBuyDO, mikuUserOneBuyDOExample) < 1){
	    		logger.error("订单号为:"+tradeId+"更新用户一元购为已付款失败");
	    		return false;
	    	}
	    }
    	return true;
    }
    
    /**
     * 
     * calculateOneBuyRewardByItemId:(根据商品id计算一元购中奖结果). <br/>
     * TODO(这里描述这个方法适用条件 – 可选).<br/>
     *
     * @author LuoGuangChun
     * @param itemId
     * @return
     */
    public boolean calculateOneBuyRewardByItemId(Long itemId){
    	
    	MikuOneBuyDOExample mikuOneBuyDOExample = new MikuOneBuyDOExample();
    	mikuOneBuyDOExample.createCriteria().andItemIdEqualTo(itemId);
    	List<MikuOneBuyDO> mikuOneBuyDOList = mikuOneBuyDOMapper.selectByExample(mikuOneBuyDOExample);
    	if(null != mikuOneBuyDOList && !mikuOneBuyDOList.isEmpty()){
    		Date nowDate = new Date();
    		MikuOneBuyDO mikuOneBuyDO = mikuOneBuyDOList.get(0);
    		MikuUserOneBuyDOExample mikuUserOneBuyDOExample = new MikuUserOneBuyDOExample();
    		mikuUserOneBuyDOExample.createCriteria().andItemIdEqualTo(itemId)
    			.andPeriodsEqualTo(mikuOneBuyDO.getPeriods()).andStatusEqualTo((byte)1);	//status(-1=取消;0=未付款;1=已付款)
    		//已付款的订单数
    		int sumNumUserOneBuy = mikuUserOneBuyDOMapper.sumNumByParams(mikuUserOneBuyDOExample);
    		Integer itemPrice = (null == mikuOneBuyDO.getItemPrice() ? 0 : Integer.valueOf(mikuOneBuyDO.getItemPrice().toString()));
    		if(itemPrice > 0 && sumNumUserOneBuy >= itemPrice){	//计算中奖名单
    			MikuUserOneBuyDOExample mikuUserOneBuyDOExample100 = new MikuUserOneBuyDOExample();
    			mikuUserOneBuyDOExample100.createCriteria().andStatusEqualTo((byte)1);	//status(-1=取消;0=未付款;1=已付款)
    			mikuUserOneBuyDOExample100.setOffset(0);
    			mikuUserOneBuyDOExample100.setLimit(100);
    			mikuUserOneBuyDOExample100.setOrderByClause("date_created DESC");
    			List<MikuUserOneBuyDO> mikuUserOneBuyDOList100 = mikuUserOneBuyDOMapper.selectByExample(mikuUserOneBuyDOExample100);
    			Long rewardCode = null;
    			for(MikuUserOneBuyDO mikuUserOneBuyDO : mikuUserOneBuyDOList100){
    				//中奖规则
    			}
    			MikuUserOneBuyDO mikuUserOneBuyDO = new MikuUserOneBuyDO();
    			mikuUserOneBuyDO.setItemId(itemId);
    			mikuUserOneBuyDO.setPeriods(mikuOneBuyDO.getPeriods());
    			mikuUserOneBuyDO.setStatus((byte)1);
    			mikuUserOneBuyDO.setStartCode(rewardCode);
    			MikuUserOneBuyDO rewardMikuUserOneBuyDO = mikuUserOneBuyDOMapper.selectRewardUserOneBuy(mikuUserOneBuyDO);
    			if(null != rewardMikuUserOneBuyDO){
    				rewardMikuUserOneBuyDO.setRewardDate(nowDate);
    				rewardMikuUserOneBuyDO.setIsWin((byte)1);	//(否=0; 是=1)
    				//更新中奖者记录
    				if(mikuUserOneBuyDOMapper.updateByPrimaryKeySelective(rewardMikuUserOneBuyDO) > 0 
    						&& mikuOneBuyDO.getStatus().equals(1)){	
    					mikuOneBuyDO.setPeriods(mikuOneBuyDO.getPeriods()+1);	//设置期数
    					mikuOneBuyDOMapper.updateByPrimaryKeySelective(mikuOneBuyDO);	//更新下一期期数
    				}
    			}
    		}
    	}
    	return true;
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
	
}

