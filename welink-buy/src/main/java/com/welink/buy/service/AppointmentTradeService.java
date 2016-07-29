package com.welink.buy.service;

import com.welink.buy.utils.BaseResult;
import com.welink.commons.domain.Order;
import com.welink.commons.domain.Trade;
import com.welink.commons.domain.TradeExample;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.transaction.TransactionStatus;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Date;
import java.util.List;

/**
 * 预约家政、预约维修、预约送水逻辑，预约服务完全都是货到付款类型的
 * <p/>
 *
 * @since 1.0
 * Created by saarixx on 15/9/14.
 */
public interface AppointmentTradeService {

    /**
     * 创建一个家政、维修、送水预约交易
     *
     * @param shopId            店铺id
     * @param communityId       小区Id
     * @param itemIdNumPairList 商品Id和数量List
     * @param buyerId           购买者Id
     * @param buildingId        购买者的房屋Id
     * @param consignTime       预约送货时间
     * @param picUrls           如果有图片的话，放入图片链接，英文';'号隔离，无视商品里面的图片
     * @param buyerMessage      如果有买家留言的话，买家留言
     * @param consigneeId       收貨地址ID
     * @return
     */
    BaseResult<Trade> createNewAppointment(@Nonnull Long sellerId,
                                           @Nonnull Long shopId,
                                           @Nonnull Long communityId,
                                           @Nonnull Long buyerId,
                                           @Nonnull Long buildingId,
                                           @Nonnull List<ImmutablePair<Long, Integer>> itemIdNumPairList,
                                           @Nonnull Date consignTime,
                                           @Nullable String picUrls,
                                           @Nullable String buyerMessage,
                                           @Nullable byte tradeFrom,
                                           @Nullable byte payType,
                                           @Nullable long consigneeId,
                                           @Nullable byte shippingType,
                                           @Nullable long point,
                                           @Nullable long couponId,
                                           @Nullable Date appointDeliveryTime,
                                           Byte orderType, 
                                           Long objectId,
                                           Long crowdfundDetailId,
                                           Long scratchCardId,
                                           Long pUserId); 


    /**
     *
     * @param itemIds
     * @param tag
     * @return
     */
    public List<Long> fetchContainTagItemIdsViaItemIds(List<Long> itemIds, Long tag);

    /**
     * 通过trade的example来做检索，同时会返回一个总数，帮助分页
     *
     * @return
     */
    BaseResult<List<Trade>> findByExample(@Nonnull TradeExample tradeExample);

    BaseResult<List<Trade>> findByBuyerTrades(long buyerId, int offset, int limit);

    BaseResult<List<Trade>> findByBuyerTradesWithStatusAndType(long buyerId, List<Byte> status, int offset, int limit);

    /**
     * 关闭订单，所有的预约订单都是货到付款订单
     * <p/>
     * 如果未付款关闭，状态是"TRADE_CLOSED_BY_TAOBAO"，物业与买家都能够关闭
     * 如果是Alipay已付款订单，状态直接是"TRADE_FINISHED"，不支持买家付款后的退款或者希望取消订单的操作
     * 如果订单一直未Alipay付款，等待订单超时（find的时候检查，时间是预约送货时间的一天后），
     * 状态是"TRADE_FINISHED"，payType标示为线下支付
     *
     * @param communityId 小区Id
     * @param tradeId     订单Id
     * @param manual      是否是人工关闭
     * @return
     */
    BaseResult<Boolean> closeTrade(@Nonnull Long communityId,
                                   @Nonnull Long tradeId, boolean manual);

    
    /**
     * 
     * createNewLotteryDrawTrade:(创建抽奖订单). <br/>
     * TODO(这里描述这个方法适用条件 – 可选).<br/>
     *
     * @author LuoGuangChun
     * @param sellerId
     * @param shopId
     * @param communityId
     * @param buyerId
     * @param buildingId
     * @param itemIdNumPairList
     * @param consignTime
     * @param picUrls
     * @param buyerMessage
     * @param tradeFrom
     * @param payType
     * @param consigneeId
     * @param shippingType
     * @param appointDeliveryTime
     * @return
     */
	BaseResult<Trade> createNewLotteryDrawTrade(Long sellerId, Long shopId,
			Long communityId, Long buyerId, Long buildingId,
			List<ImmutablePair<Long, Integer>> itemIdNumPairList,
			Date consignTime, String picUrls, String buyerMessage,
			byte tradeFrom, byte payType, long consigneeId, byte shippingType,
			Date appointDeliveryTime);
	
	/**
     * 
     * createNewAppointmentDz:(创建定制订单). <br/>
     * TODO(这里描述这个方法适用条件 – 可选).<br/>
     *
     * @author LuoGuangChun
     * @param sellerId
     * @param shopId
     * @param communityId
     * @param buyerId
     * @param buildingId
     * @param itemIdNumPairList
     * @param consignTime
     * @param picUrls
     * @param buyerMessage
     * @param tradeFrom
     * @param payType
     * @param consigneeId
     * @param shippingType
     * @param appointDeliveryTime
     * @return
     */
	BaseResult<Trade> createNewAppointmentDz(Long sellerId, Long shopId,
			long point,
            long userCouponId,
			Long communityId, Long buyerId, Long buildingId,
			Date consignTime, String picUrls, String buyerMessage,
			byte tradeFrom, byte payType, long consigneeId, byte shippingType,
			Date appointDeliveryTime, Long detectReportId);

	/**
	 * 
	 * updateStock2:(更新库存). <br/>
	 * TODO(这里描述这个方法适用条件 – 可选).<br/>
	 *
	 * @author LuoGuangChun
	 * @param type 如果type=2加库存,type=1或其它减库存
	 * @param transactionStatus
	 * @param type
	 * @return
	 */
	boolean updateStock2(Trade trade, Order order, TransactionStatus transactionStatus, Integer type);
	
	BaseResult<List<Trade>> findTradesByParams(Trade trade, List<Byte> tradeStatusList, int offset, int limit);
	
}
