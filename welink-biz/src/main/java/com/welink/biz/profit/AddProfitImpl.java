package com.welink.biz.profit;

import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mysql.jdbc.Connection;
import com.welink.buy.utils.Constants;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.Item;
import com.welink.commons.domain.MikuAgencyShareAccountDO;
import com.welink.commons.domain.MikuAgencyShareAccountDOExample;
import com.welink.commons.domain.MikuSalesRecordDO;
import com.welink.commons.domain.MikuUserAgencyDO;
import com.welink.commons.domain.MikuUserAgencyDOExample;
import com.welink.commons.domain.Order;
import com.welink.commons.domain.OrderExample;
import com.welink.commons.domain.ProfileDO;
import com.welink.commons.domain.Trade;
import com.welink.commons.domain.TradeExample;
import com.welink.commons.persistence.ItemMapper;
import com.welink.commons.persistence.MikuAgencyShareAccountDOMapper;
import com.welink.commons.persistence.MikuItemShareParaDOMapper;
import com.welink.commons.persistence.MikuSalesRecordDOMapper;
import com.welink.commons.persistence.MikuUserAgencyDOMapper;
import com.welink.commons.persistence.OrderMapper;
import com.welink.commons.persistence.ProfileDOMapper;
import com.welink.commons.persistence.TradeCourierDOMapper;
import com.welink.commons.persistence.TradeMapper;
import com.welink.commons.vo.LevelVO;

/**
 * Created by myron on 16/3/6.
 */
@Service
public class AddProfitImpl implements InitializingBean {

    @Resource
    private TradeMapper tradeMapper;
    
    @Resource
    private PlatformTransactionManager transactionManager;
	
	private TransactionTemplate transactionTemplate;
	
	@Resource
    private ProfileDOMapper profileDOMapper;
	
	@Resource
    private MikuUserAgencyDOMapper mikuUserAgencyDOMapper;
	
	@Resource
	private OrderMapper orderMapper;
	
	@Resource
	private ItemMapper itemMapper;
	
	@Resource
	private TradeCourierDOMapper tradeCourierDOMapper;
	
	@Resource
	private MikuItemShareParaDOMapper mikuItemShareParaDOMapper;
	
	@Resource
	private MikuSalesRecordDOMapper mikuSalesRecordDOMapper;
	
	@Resource
	private MikuAgencyShareAccountDOMapper mikuAgencyShareAccountDOMapper;

	/**
	 * 
	 * handle:(分润处理) <br/>
	 * TODO(这里描述这个方法适用条件 – 可选).<br/>
	 *
	 * @author LuoGuangChun
	 * @param tradeId
	 */
    public void handle(final Long tradeId) {
    	transactionTemplate.execute(new TransactionCallback<Boolean>() {
            @Override
            public Boolean doInTransaction(TransactionStatus transactionStatus) {
            	Date nowDate = new Date();
				Date nowDate14 = addDay(nowDate, 14);	//往后延期14天，表示14天后才能提现
		        TradeExample tradeExample = new TradeExample();
		        tradeExample.createCriteria().andTradeIdEqualTo(tradeId);
		        List<Trade> trades = tradeMapper.selectByExample(tradeExample);
		
		        if(!trades.isEmpty()){
		        	List<Byte> toDealStatus = new ArrayList<>();	//可分润trade状态
					toDealStatus.add(Constants.TradeStatus.SELLER_CONSIGNED_PART.getTradeStatusId());
					toDealStatus.add(Constants.TradeStatus.WAIT_SELLER_SEND_GOODS.getTradeStatusId());
					toDealStatus.add(Constants.TradeStatus.WAIT_BUYER_CONFIRM_GOODS.getTradeStatusId());
					toDealStatus.add(Constants.TradeStatus.TRADE_BUYER_SIGNED.getTradeStatusId());
					toDealStatus.add(Constants.TradeStatus.TRADE_FINISHED.getTradeStatusId());
					toDealStatus.add(Constants.TradeStatus.TRADE_SUCCESSED.getTradeStatusId());
		        	Trade trade = trades.get(0);
		        	/*if(null == tradeId || trade.getIsProfit().equals((byte)1) || !toDealStatus.contains(trade.getStatus())){
		        		return true;
		        	}*/
					Long profileId = trade.getBuyerId();	//交易购买者
		        	OrderExample orderExample = new OrderExample();
		        	orderExample.createCriteria().andTradeIdEqualTo(tradeId)
		        		.andReturnStatusNotEqualTo(Constants.ReturnGoodsStatus.FINISHED.getStatusId());	//未退货的t_order
		        	//查询交易所对应的订单列表
		        	List<Order> orderList = orderMapper.selectByExample(orderExample);
		        	Map<Long, Integer> itemCounts = new HashMap<>();		//商品id和商品所对应的数量
		        	//Map<Long, Item> itemMap = new HashMap<>();		//商品id所对应商品
		        	Map<Long, Order> itemOrderTotalFeeMap = new HashMap<Long, Order>();		//商品id和order订单Map
		        	List<Long> itemIds = new ArrayList<Long>();
		        	Map<Long, JSONObject> itemParameterMap = new HashMap<Long, JSONObject>();		//商品id所对应的分润参数Map
		        	BigDecimal ceoShareRatio = BigDecimal.ZERO, ceo2ShareRatio = BigDecimal.ZERO,
		        			ceo3ShareRatio = BigDecimal.ZERO, ceo4ShareRatio = BigDecimal.ZERO;	//CEO分润比例
		        	BigDecimal ceoGiftShareRatio = BigDecimal.ZERO, ceo2GiftShareRatio = BigDecimal.ZERO,
		        			ceo3GiftShareRatio = BigDecimal.ZERO, ceo4GiftShareRatio = BigDecimal.ZERO;	//CEOGift分润比例
		        	for(Order order : orderList){
		        		if(null != order.getArtificialId() && order.getArtificialId() > 0){		//再价格活动判断，若是活动就不分润
		        			itemCounts.put(order.getArtificialId(), order.getNum());
		        			itemIds.add(order.getArtificialId());
		        			itemOrderTotalFeeMap.put(order.getArtificialId(), order);
		        			
		        			//String parameter = order.getProfitParameter();	//分润参数
		        			String parameter = "";
		        			parameter += "[{\"id\":1,\"value\":\"4.8\",\"title\":\"一度\"},{\"id\":2,\"value\":\"4.8\",\"title\":\"二度\"},{\"id\":3,\"value\":\"6.4\",\"title\":\"三度\"},{\"id\":4,\"value\":\"8\",\"title\":\"四度\"},";
			                parameter += "{\"id\":5,\"value\":\"8\",\"title\":\"五度\"},{\"id\":6,\"value\":\"8\",\"title\":\"六度\"},{\"id\":7,\"value\":\"8\",\"title\":\"七度\"},{\"id\":8,\"value\":\"32\",\"title\":\"八度\"},";
			              	parameter += "{\"id\":9,\"value\":\"1\",\"title\":\"CEO4\"},{\"id\":10,\"value\":\"6\",\"title\":\"CEO3\"},{\"id\":11,\"value\":\"12\",\"title\":\"CEO2\"},{\"id\":12,\"value\":\"20\",\"title\":\"CEO1\"}]";
		        			byte isActivity = (null == order.getIsActivity() ? (byte)0 : order.getIsActivity());
		        			if(null != parameter && !"".equals(parameter.trim()) && isActivity != 1){
				        		List<LevelVO> levelVOList = JSON.parseArray(parameter, LevelVO.class);
				        		JSONObject jsonObject = new JSONObject();
				        		for(LevelVO levelVO : levelVOList){
				        			jsonObject.put(levelVO.getId().toString(), (null == levelVO.getValue() ? BigDecimal.ZERO : levelVO.getValue()));
				        			if(BizConstants.AgencyLevel.CEO1.getId() == levelVO.getId()){	//CEO1
				        				ceoShareRatio =  (null == levelVO.getValue() ? BigDecimal.ZERO : levelVO.getValue());
				        			}else if(BizConstants.AgencyLevel.CEO2.getId() == levelVO.getId()){	//CEO2
				        				ceo2ShareRatio =  (null == levelVO.getValue() ? BigDecimal.ZERO : levelVO.getValue());
				        			}else if(BizConstants.AgencyLevel.CEO3.getId() == levelVO.getId()){	//CEO3
				        				ceo3ShareRatio =  (null == levelVO.getValue() ? BigDecimal.ZERO : levelVO.getValue());
				        			}else if(BizConstants.AgencyLevel.CEO4.getId() == levelVO.getId()){	//CEO4
				        				ceo4ShareRatio =  (null == levelVO.getValue() ? BigDecimal.ZERO : levelVO.getValue());
				        			}else if(BizConstants.AgencyLevel.CEO1Gift.getId() == levelVO.getId()){	//CEO1Gift
				        				ceoGiftShareRatio =  (null == levelVO.getValue() ? BigDecimal.ZERO : levelVO.getValue());
				        			}else if(BizConstants.AgencyLevel.CEO2Gift.getId() == levelVO.getId()){	//CEO2Gift
				        				ceo2GiftShareRatio =  (null == levelVO.getValue() ? BigDecimal.ZERO : levelVO.getValue());
				        			}else if(BizConstants.AgencyLevel.CEO3Gift.getId() == levelVO.getId()){	//CEO3Gift
				        				ceo3GiftShareRatio =  (null == levelVO.getValue() ? BigDecimal.ZERO : levelVO.getValue());
				        			}else if(BizConstants.AgencyLevel.CEO4Gift.getId() == levelVO.getId()){	//CEO4Gift
				        				ceo4GiftShareRatio =  (null == levelVO.getValue() ? BigDecimal.ZERO : levelVO.getValue());
				        			}
				        			
				        		}
				        		itemParameterMap.put(order.getArtificialId(), jsonObject);		//添加商品id所对应的分润参数Map
		        			}
		        		}
		        	}
		        	
		        	
		        	MikuUserAgencyDOExample mikuUserAgencyDOExample = new MikuUserAgencyDOExample();
		        	mikuUserAgencyDOExample.createCriteria().andUserIdEqualTo(profileId);
		        	//查询代理关系
		        	List<MikuUserAgencyDO> mikuUserAgencyDOList = mikuUserAgencyDOMapper.selectByExample(mikuUserAgencyDOExample);	
		        	
		        	Map<Integer, Long> profileIdsMap = new HashMap<Integer, Long>();		//三级分润代理id对应用户id Map
		        	MikuUserAgencyDO mikuUserAgencyDO = null;
		        	boolean isShareItem = false;	//是否分享商品(true=是分享；false=不是分享)
		        	if(!mikuUserAgencyDOList.isEmpty()){
		        		mikuUserAgencyDO = mikuUserAgencyDOList.get(0);
		        	}else if(mikuUserAgencyDOList.isEmpty() && !trade.getBuyerId().equals(trade.getpUserId())){
		        		//mikuUserAgencyDO = new MikuUserAgencyDO();
		        		if(null != trade.getpUserId() && trade.getpUserId() > 0){
		        			MikuUserAgencyDOExample mikuUserAgencyDOExampleTradePid = new MikuUserAgencyDOExample();
		        			mikuUserAgencyDOExampleTradePid.createCriteria().andUserIdEqualTo(trade.getpUserId());
		        			//查询代理关系
				        	List<MikuUserAgencyDO> mikuUserAgencyDOTradePidList = mikuUserAgencyDOMapper.selectByExample(mikuUserAgencyDOExampleTradePid);	
				        	if(!mikuUserAgencyDOTradePidList.isEmpty()){
				        		ProfileDO tradePidProfileDO = profileDOMapper.selectByPrimaryKey(mikuUserAgencyDOTradePidList.get(0).getUserId());
				        		if(null != tradePidProfileDO && tradePidProfileDO.getIsAgency().equals((byte)1)){
				        			//判断推荐人是否代理,是代理按推荐人的代理关系分润
				        			mikuUserAgencyDO = mikuUserAgencyDOTradePidList.get(0);
				        			isShareItem = true;	//是否分享商品(true=是分享；false=不是分享)
				        		}
				        	}
		        		}
		        	}
		        	if(null == mikuUserAgencyDO){
		        		Trade updTrade = new Trade();
			        	updTrade.setId(trade.getId());
			        	updTrade.setIsProfit(Byte.valueOf("2"));		//(1=已分润；0=未分润;2=已分润但无代理关系)
			        	tradeMapper.updateByPrimaryKeySelective(updTrade);	//更新交易订单表为已分润
		        		return true;
		        	}
		        	
		        	Long ceoUserId = 0L, ceo2UserId = 0L, ceo3UserId = 0L, ceo4UserId = 0L;
		        	//CEO
		        	profileIdsMap.put(BizConstants.AgencyLevel.CEO1.getId(), null == mikuUserAgencyDO.getCeoUserId() ? 0L : mikuUserAgencyDO.getCeoUserId());
		        	profileIdsMap.put(BizConstants.AgencyLevel.CEO2.getId(), null == mikuUserAgencyDO.getCeo2UserId() ? 0L : mikuUserAgencyDO.getCeo2UserId());
		        	profileIdsMap.put(BizConstants.AgencyLevel.CEO3.getId(), null == mikuUserAgencyDO.getCeo3UserId() ? 0L : mikuUserAgencyDO.getCeo3UserId());
		        	profileIdsMap.put(BizConstants.AgencyLevel.CEO4.getId(), null == mikuUserAgencyDO.getCeo4UserId() ? 0L : mikuUserAgencyDO.getCeo4UserId());
		        	ceoUserId = null == mikuUserAgencyDO.getCeoUserId() ? 0L : mikuUserAgencyDO.getCeoUserId();
		        	ceo2UserId = null == mikuUserAgencyDO.getCeo2UserId() ? 0L : mikuUserAgencyDO.getCeo2UserId();
		        	ceo3UserId = null == mikuUserAgencyDO.getCeo3UserId() ? 0L : mikuUserAgencyDO.getCeo3UserId();
		        	ceo4UserId = null == mikuUserAgencyDO.getCeo4UserId() ? 0L : mikuUserAgencyDO.getCeo4UserId();
		        	
		        	//CEOGift
		        	profileIdsMap.put(BizConstants.AgencyLevel.CEO1Gift.getId(), null == mikuUserAgencyDO.getCeoUserId() ? 0L : mikuUserAgencyDO.getCeoUserId());
		        	profileIdsMap.put(BizConstants.AgencyLevel.CEO2Gift.getId(), null == mikuUserAgencyDO.getCeo2UserId() ? 0L : mikuUserAgencyDO.getCeo2UserId());
		        	profileIdsMap.put(BizConstants.AgencyLevel.CEO3Gift.getId(), null == mikuUserAgencyDO.getCeo3UserId() ? 0L : mikuUserAgencyDO.getCeo3UserId());
		        	profileIdsMap.put(BizConstants.AgencyLevel.CEO4Gift.getId(), null == mikuUserAgencyDO.getCeo4UserId() ? 0L : mikuUserAgencyDO.getCeo4UserId());
		        	
		        	ProfileDO profileDO = profileDOMapper.selectByPrimaryKey(profileId);	//获取用户
		        	Byte isAgency = profileDO.getIsAgency();
		        	if((null != isAgency && 1 == isAgency) || isShareItem){		//购买者是代理(isAgency： 1=代理)
		        		//八级代理
		        		profileIdsMap.put(-1, null == mikuUserAgencyDO.getP8UserId() ? 0L : mikuUserAgencyDO.getP8UserId());
		        		profileIdsMap.put(BizConstants.AgencyLevel.aaa.getId(), null == mikuUserAgencyDO.getP7UserId() ? 0L : mikuUserAgencyDO.getP7UserId());
		        		profileIdsMap.put(BizConstants.AgencyLevel.bbb.getId(), null == mikuUserAgencyDO.getP6UserId() ? 0L : mikuUserAgencyDO.getP6UserId());
		        		profileIdsMap.put(BizConstants.AgencyLevel.ccc.getId(), null == mikuUserAgencyDO.getP5UserId() ? 0L : mikuUserAgencyDO.getP5UserId());
		        		profileIdsMap.put(BizConstants.AgencyLevel.ddd.getId(), null == mikuUserAgencyDO.getP4UserId() ? 0L : mikuUserAgencyDO.getP4UserId());
		        		profileIdsMap.put(BizConstants.AgencyLevel.eee.getId(), null == mikuUserAgencyDO.getP3UserId() ? 0L : mikuUserAgencyDO.getP3UserId());
	            		profileIdsMap.put(BizConstants.AgencyLevel.fff.getId(), null == mikuUserAgencyDO.getP2UserId() ? 0L : mikuUserAgencyDO.getP2UserId());
	            		profileIdsMap.put(BizConstants.AgencyLevel.ggg.getId(), null == mikuUserAgencyDO.getpUserId() ? 0L : mikuUserAgencyDO.getpUserId());
	            		profileIdsMap.put(BizConstants.AgencyLevel.hhh.getId(), mikuUserAgencyDO.getUserId());
	            		profileIdsMap.put(BizConstants.AgencyLevel.CEO5Gift.getId(),  null == mikuUserAgencyDO.getpUserId() ? 0L : mikuUserAgencyDO.getpUserId());
		        	}else{				//购买者不是代理
		        		//八级代理
		        		profileIdsMap.put(BizConstants.AgencyLevel.aaa.getId(), null == mikuUserAgencyDO.getP8UserId() ? 0L : mikuUserAgencyDO.getP8UserId());
		        		profileIdsMap.put(BizConstants.AgencyLevel.bbb.getId(), null == mikuUserAgencyDO.getP7UserId() ? 0L : mikuUserAgencyDO.getP7UserId());
		        		profileIdsMap.put(BizConstants.AgencyLevel.ccc.getId(), null == mikuUserAgencyDO.getP6UserId() ? 0L : mikuUserAgencyDO.getP6UserId());
		        		profileIdsMap.put(BizConstants.AgencyLevel.ddd.getId(), null == mikuUserAgencyDO.getP5UserId() ? 0L : mikuUserAgencyDO.getP5UserId());
		        		profileIdsMap.put(BizConstants.AgencyLevel.eee.getId(), null == mikuUserAgencyDO.getP4UserId() ? 0L : mikuUserAgencyDO.getP4UserId());
		        		profileIdsMap.put(BizConstants.AgencyLevel.fff.getId(), null == mikuUserAgencyDO.getP3UserId() ? 0L : mikuUserAgencyDO.getP3UserId());
	            		profileIdsMap.put(BizConstants.AgencyLevel.ggg.getId(), null == mikuUserAgencyDO.getP2UserId() ? 0L : mikuUserAgencyDO.getP2UserId());
	            		profileIdsMap.put(BizConstants.AgencyLevel.hhh.getId(), null == mikuUserAgencyDO.getpUserId() ? 0L : mikuUserAgencyDO.getpUserId());
	            		profileIdsMap.put(BizConstants.AgencyLevel.CEO5Gift.getId(), null == mikuUserAgencyDO.getpUserId() ? 0L : mikuUserAgencyDO.getpUserId());
		        	}
		        	
		        	List<Integer> duShareAgencyLevels = new ArrayList<Integer>();		//度代理等级
		        	List<Integer> ceoGiftShareAgencyLevels = new ArrayList<Integer>();	//ceoGift代理等级
		        	List<Integer> ceoShareAgencyLevels = new ArrayList<Integer>();		//ceo代理等级
		        	//度代理等级
		        	duShareAgencyLevels.add(BizConstants.AgencyLevel.aaa.getId());
		        	duShareAgencyLevels.add(BizConstants.AgencyLevel.bbb.getId());
		        	duShareAgencyLevels.add(BizConstants.AgencyLevel.ccc.getId());
		        	duShareAgencyLevels.add(BizConstants.AgencyLevel.ddd.getId());
		        	duShareAgencyLevels.add(BizConstants.AgencyLevel.eee.getId());
		        	duShareAgencyLevels.add(BizConstants.AgencyLevel.fff.getId());
		        	duShareAgencyLevels.add(BizConstants.AgencyLevel.ggg.getId());
		        	duShareAgencyLevels.add(BizConstants.AgencyLevel.hhh.getId());
		        	//ceo代理等级
		        	ceoShareAgencyLevels.add(BizConstants.AgencyLevel.CEO1.getId());
		        	ceoShareAgencyLevels.add(BizConstants.AgencyLevel.CEO2.getId());
		        	ceoShareAgencyLevels.add(BizConstants.AgencyLevel.CEO3.getId());
		        	ceoShareAgencyLevels.add(BizConstants.AgencyLevel.CEO4.getId());
		        	//ceoGift代理等级
		        	ceoGiftShareAgencyLevels.add(BizConstants.AgencyLevel.CEO1Gift.getId());
		        	ceoGiftShareAgencyLevels.add(BizConstants.AgencyLevel.CEO2Gift.getId());
		        	ceoGiftShareAgencyLevels.add(BizConstants.AgencyLevel.CEO3Gift.getId());
		        	ceoGiftShareAgencyLevels.add(BizConstants.AgencyLevel.CEO4Gift.getId());
		        	ceoGiftShareAgencyLevels.add(BizConstants.AgencyLevel.CEO5Gift.getId());
		        	
		        	//duUserIdList 上级度用户Id List
		        	List<Long> duUserIdList = new ArrayList<Long>();	
		        	duUserIdList.add(profileIdsMap.get(BizConstants.AgencyLevel.aaa.getId()));
		        	duUserIdList.add(profileIdsMap.get(BizConstants.AgencyLevel.bbb.getId()));
		        	duUserIdList.add(profileIdsMap.get(BizConstants.AgencyLevel.ccc.getId()));
		        	duUserIdList.add(profileIdsMap.get(BizConstants.AgencyLevel.ddd.getId()));
		        	duUserIdList.add(profileIdsMap.get(BizConstants.AgencyLevel.eee.getId()));
		        	duUserIdList.add(profileIdsMap.get(BizConstants.AgencyLevel.fff.getId()));
		        	duUserIdList.add(profileIdsMap.get(BizConstants.AgencyLevel.ggg.getId()));
		        	duUserIdList.add(profileIdsMap.get(BizConstants.AgencyLevel.hhh.getId()));
		        	
		        	
		        	JSONObject itemParameter = null;	//商品id所对应的分润参数JSON	
		        	//List<LevelVO> levelVOList = null;	//商品id所对应的分润参数JSON	
		        	for (Integer profileIdKey : profileIdsMap.keySet()) {
		        		if(profileIdsMap.get(profileIdKey) > 0){
		        			Long totalAmount = 0L;		//用户所对应的交易的总销售金额
		        			Long totalShareFee = 0L;	//用户所对应的交易的分润金额
		        			
		        			for(Long itemId : itemIds){
		        				Item item = itemMapper.selectByPrimaryKey(itemId);
		        				/*if(null != item && null != item.getType() && item.getType().equals(Constants.TradeType.join_agency.getTradeTypeId())){
		        				//成为代理分润处理,看是否包好对应成为代理分润的代理用户
		        					if(!ceoGiftShareAgencyLevels.contains(profileIdKey)){
		        						break;
		        					}
		        				}else{
		        					//正常分润处理,看是否包好对应正常分润的代理用户
		        					if(ceoGiftShareAgencyLevels.contains(profileIdKey)){
		        						break;
		        					}
		        				}*/
		        				if(ceoGiftShareAgencyLevels.contains(profileIdKey)){
	        						break;
	        					}
								Order order = itemOrderTotalFeeMap.get(itemId);
		        				String parameter = order.getProfitParameter();	//分润参数
		        				byte isActivity = (null == order.getIsActivity() ? (byte)0 : order.getIsActivity());	//是否活动
			        			if(null != parameter && !"".equals(parameter.trim()) && isActivity == 1){	//如果没有分润或是活动不分润
			        				continue;
			        			}
			        			itemParameter = itemParameterMap.get(itemId);	//获取商品所对应的分润参数（json-kv）
		        				//Item item = itemMap.get(itemId);
			        			//商品可分润金额
		        				Long itemProfitFee = (null == order.getItemProfitFee() ? 0L : order.getItemProfitFee());	//商品公司利润
		        				Long itemCostFee = (null == order.getItemCostFee() ? 0L : order.getItemCostFee());		//商品成本
		        				Long orderTotalFee = (null == order.getTotalFee() ? 0L : order.getTotalFee());				//订单总金额
		        				Long orderCanProfit = (orderTotalFee - (itemProfitFee + itemCostFee) * order.getNum());		//订单可分润金额
		        				if(null == orderCanProfit || orderCanProfit < 100){
		        					//continue;
		        					//商品可分润如果小于1块钱，则按1块钱来计算分润
		        					orderCanProfit = 100L;
		        				}
		        				
		        				BigDecimal ceoShareRatioResult = BigDecimal.ZERO;	//代理CEO所对应的分润比例
		        				//if(null != item && null != item.getType() && item.getType().equals(Constants.TradeType.join_agency.getTradeTypeId())){
		        				if(1 > 2){
		        					//成为代理分润处理
	        						if(BizConstants.AgencyLevel.CEO1Gift.getId().equals(profileIdKey)){
			        					if(ceoGiftShareRatio.compareTo(BigDecimal.ZERO) > 0){
			        						ceoShareRatioResult = ceoGiftShareRatio;	//CEO1Gift分润比例
			        						if(ceo2GiftShareRatio.compareTo(BigDecimal.ZERO) > 0
			        								&& profileIdsMap.get(BizConstants.AgencyLevel.CEO2Gift.getId()) > 0L){
			        							ceoShareRatioResult = ceoGiftShareRatio.subtract(ceo2GiftShareRatio);
			        						}
				        				}
									}else if(BizConstants.AgencyLevel.CEO2Gift.getId().equals(profileIdKey)){
										if(ceo2GiftShareRatio.compareTo(BigDecimal.ZERO) > 0){
											ceoShareRatioResult = ceo2GiftShareRatio;	//CEO2Gift分润比例
			        						if(ceo3GiftShareRatio.compareTo(BigDecimal.ZERO) > 0
			        								&& profileIdsMap.get(BizConstants.AgencyLevel.CEO3Gift.getId()) > 0L){
			        							ceoShareRatioResult = ceo2GiftShareRatio.subtract(ceo3GiftShareRatio);
			        						}
				        				}
									}else if(BizConstants.AgencyLevel.CEO3Gift.getId().equals(profileIdKey)){
										if(ceo3GiftShareRatio.compareTo(BigDecimal.ZERO) > 0){
											ceoShareRatioResult = ceo3GiftShareRatio;	//CEO3Gift分润比例
			        						if(ceo4GiftShareRatio.compareTo(BigDecimal.ZERO) > 0
			        								&& profileIdsMap.get(BizConstants.AgencyLevel.CEO4Gift.getId()) > 0L){
			        							ceoShareRatioResult = ceo3GiftShareRatio.subtract(ceo4GiftShareRatio);
			        						}
				        				}
									}else if(BizConstants.AgencyLevel.CEO4Gift.getId().equals(profileIdKey)){
										ceoShareRatioResult = ceo4GiftShareRatio;		//CEO4Gift分润比例
									}else if(BizConstants.AgencyLevel.CEO5Gift.getId().equals(profileIdKey)){
										//CEO5Gift分润比例	上级代理
										ceoShareRatioResult = itemParameter.getBigDecimal(profileIdKey.toString());		
									}
		        				}else{
		        					//正常分润处理
		        					
	        						//分润等级度的分润比例
	        						//ceoShareRatioResult = itemParameter.getBigDecimal(profileIdKey.toString());
	        						if(null != isAgency && 1 == isAgency && null != item && null != item.getType() 
	        								&& item.getType().equals(Constants.TradeType.join_agency.getTradeTypeId())){
	        							//成为代理分润处理,看是否包好对应成为代理分润的代理用户
	        							if(duShareAgencyLevels.contains(profileIdKey) && !BizConstants.AgencyLevel.hhh.getId().equals(profileIdKey)){
	        								ceoShareRatioResult = itemParameter.getBigDecimal(profileIdKey.toString());
	        								if(BizConstants.AgencyLevel.aaa.getId().equals(profileIdKey)){
		        								ceoShareRatioResult = itemParameter.getBigDecimal(BizConstants.AgencyLevel.bbb.getId().toString());
		        							}else if(BizConstants.AgencyLevel.bbb.getId().equals(profileIdKey)){
		        								ceoShareRatioResult = itemParameter.getBigDecimal(BizConstants.AgencyLevel.ccc.getId().toString());
		        							}else if(BizConstants.AgencyLevel.ccc.getId().equals(profileIdKey)){
		        								ceoShareRatioResult = itemParameter.getBigDecimal(BizConstants.AgencyLevel.ddd.getId().toString());
		        							}else if(BizConstants.AgencyLevel.ddd.getId().equals(profileIdKey)){
		        								ceoShareRatioResult = itemParameter.getBigDecimal(BizConstants.AgencyLevel.eee.getId().toString());
		        							}else if(BizConstants.AgencyLevel.eee.getId().equals(profileIdKey)){
		        								ceoShareRatioResult = itemParameter.getBigDecimal(BizConstants.AgencyLevel.fff.getId().toString());
		        							}else if(BizConstants.AgencyLevel.fff.getId().equals(profileIdKey)){
		        								ceoShareRatioResult = itemParameter.getBigDecimal(BizConstants.AgencyLevel.ggg.getId().toString());
		        							}else if(BizConstants.AgencyLevel.ggg.getId().equals(profileIdKey)){
		        								ceoShareRatioResult = itemParameter.getBigDecimal(BizConstants.AgencyLevel.hhh.getId().toString());
		        							}
	        							}
	        							if(profileIdKey.equals(-1)){
	        								ceoShareRatioResult = itemParameter.getBigDecimal(BizConstants.AgencyLevel.aaa.getId().toString());
	        							}
	        							
    		        				}else{
    		        					if(duShareAgencyLevels.contains(profileIdKey)){
    		        						ceoShareRatioResult = itemParameter.getBigDecimal(profileIdKey.toString()); 
    		        					}
    		        					//ceoShareRatioResult = itemParameter.getBigDecimal(profileIdKey.toString()); 
    		        				}
		        						
		        					//分润等级CEO的分润比例
		        					if(BizConstants.AgencyLevel.CEO1.getId().equals(profileIdKey)){
			        					if(ceoShareRatio.compareTo(BigDecimal.ZERO) > 0){
			        						ceoShareRatioResult = ceoShareRatio;	//CEO1分润比例
			        						if(ceo2ShareRatio.compareTo(BigDecimal.ZERO) > 0
			        								&& profileIdsMap.get(BizConstants.AgencyLevel.CEO2.getId()) > 0L){
			        							ceoShareRatioResult = ceoShareRatio.subtract(ceo2ShareRatio);
			        						}else if(ceo3ShareRatio.compareTo(BigDecimal.ZERO) > 0
			        								&& profileIdsMap.get(BizConstants.AgencyLevel.CEO3.getId()) > 0L){
			        							ceoShareRatioResult = ceoShareRatio.subtract(ceo3ShareRatio);
			        						}else if(ceo4ShareRatio.compareTo(BigDecimal.ZERO) > 0
			        								&& profileIdsMap.get(BizConstants.AgencyLevel.CEO4.getId()) > 0L){
			        							ceoShareRatioResult = ceoShareRatio.subtract(ceo4ShareRatio);
			        						}
				        				}
									}else if(BizConstants.AgencyLevel.CEO2.getId().equals(profileIdKey)){
										if(ceo2ShareRatio.compareTo(BigDecimal.ZERO) > 0){
											ceoShareRatioResult = ceo2ShareRatio;	//CEO2分润比例
			        						if(ceo3ShareRatio.compareTo(BigDecimal.ZERO) > 0
			        								&& profileIdsMap.get(BizConstants.AgencyLevel.CEO3.getId()) > 0L){
			        							ceoShareRatioResult = ceo2ShareRatio.subtract(ceo3ShareRatio);
			        						}else if(ceo4ShareRatio.compareTo(BigDecimal.ZERO) > 0
			        								&& profileIdsMap.get(BizConstants.AgencyLevel.CEO4.getId()) > 0L){
			        							ceoShareRatioResult = ceo2ShareRatio.subtract(ceo4ShareRatio);
			        						}
				        				}
									}else if(BizConstants.AgencyLevel.CEO3.getId().equals(profileIdKey)){
										if(ceo3ShareRatio.compareTo(BigDecimal.ZERO) > 0){
											ceoShareRatioResult = ceo3ShareRatio;	//CEO3分润比例
			        						if(ceo4ShareRatio.compareTo(BigDecimal.ZERO) > 0
			        								&& profileIdsMap.get(BizConstants.AgencyLevel.CEO4.getId()) > 0L){
			        							ceoShareRatioResult = ceo3ShareRatio.subtract(ceo4ShareRatio);
			        						}
				        				}
									}else if(BizConstants.AgencyLevel.CEO4.getId().equals(profileIdKey)){
										ceoShareRatioResult = ceo4ShareRatio;		//CEO4分润比例
									}
		        				}
		        				Long shareFee = 0L;
		        				if(ceoShareRatioResult.compareTo(BigDecimal.ZERO) > 0){
		        					shareFee = (long)ceoShareRatioResult.multiply(new BigDecimal(orderCanProfit))
		        							.divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP).doubleValue();	//分润
		        					totalShareFee += shareFee;	//总分润
		        				}
		        				//插入分润记录
		        				if(null != shareFee && shareFee > 0){
		        					if(!insertMikuSalesRecordDO(transactionStatus, profileDO, itemParameter, trade, order, 
		        							totalShareFee, totalAmount, shareFee, profileIdsMap.get(profileIdKey), profileIdKey)){
		        						transactionStatus.setRollbackOnly();
			        					return false;
		        					}
		        				}
				        	}
		        			
		        			if(null == totalShareFee || totalShareFee < 1){
		        				totalShareFee = 0L;
		        				continue;
		        			}
		        			
		        			MikuAgencyShareAccountDOExample mikuAgencyShareAccountDOExample = new MikuAgencyShareAccountDOExample();
		        			mikuAgencyShareAccountDOExample.createCriteria().andAgencyIdEqualTo(profileIdsMap.get(profileIdKey));
		        			//查询代理帐户信息
		        			List<MikuAgencyShareAccountDO> mikuAgencyShareAccountDOList = mikuAgencyShareAccountDOMapper.selectByExample(mikuAgencyShareAccountDOExample);
		        			if(mikuAgencyShareAccountDOList.isEmpty()){
		        				MikuAgencyShareAccountDO mikuAgencyShareAccountDO = new MikuAgencyShareAccountDO();
		        				int insertSelective = mikuAgencyShareAccountDOMapper.insertSelective(
		        						setMikuAgencyShareAccountDO(profileDO.getId(), mikuAgencyShareAccountDO));		//插入代理用户帐号
		        				if(insertSelective < 1){
		        					transactionStatus.setRollbackOnly();
		        					return false;
		        				}
		        				if(null != mikuAgencyShareAccountDO && null != mikuAgencyShareAccountDO.getId() 
		        						&& mikuAgencyShareAccountDO.getId() > 0){
		        					mikuAgencyShareAccountDOList.add(mikuAgencyShareAccountDO);
		        				}
		        			}
		        			if((null != isAgency && 1 == isAgency) || isShareItem){		//购买者是代理(isAgency： 1=代理)
		        				
		        			}else{	//	购买者不是代理
		        				if(BizConstants.AgencyLevel.hhh.getId() == profileIdKey){	//三级代理
		        					//2、更新购买者的贡献分润
				        			MikuAgencyShareAccountDOExample mikuAgencyShareAccountDOExampleBuyer = new MikuAgencyShareAccountDOExample();
				        			mikuAgencyShareAccountDOExampleBuyer.createCriteria().andAgencyIdEqualTo(profileId);
				        			List<MikuAgencyShareAccountDO> mikuAgencyShareAccountDOBuyerList 
				        				= mikuAgencyShareAccountDOMapper.selectByExample(mikuAgencyShareAccountDOExampleBuyer);	//查询购买者帐户信息
				        			if(!mikuAgencyShareAccountDOBuyerList.isEmpty()){
				        				MikuAgencyShareAccountDO mikuAgencyShareAccountDO = mikuAgencyShareAccountDOBuyerList.get(0);
				        				mikuAgencyShareAccountDO.setP2TradesCount(1L+
				    							(null==mikuAgencyShareAccountDO.getP2TradesCount() ? 0L : mikuAgencyShareAccountDO.getP2TradesCount()));//自己分润订单
				        				mikuAgencyShareAccountDO.setId(null);
				        				//更新购买者的订单数
				        				mikuAgencyShareAccountDOMapper.updateByExampleSelective(mikuAgencyShareAccountDO, mikuAgencyShareAccountDOExampleBuyer);
				        			}
		        				}
		        			}
		        			if(!mikuAgencyShareAccountDOList.isEmpty()){
		        				boolean isCeoShareLine = false;	//是否ceo分润线，也就是ceo是否分2次润(true=是；false=不是)
		        				if(profileIdKey > 0 && ceoShareAgencyLevels.contains(profileIdKey)){
		        					for (Integer profileIdKey2 : profileIdsMap.keySet()) {
		        						if(duShareAgencyLevels.contains(profileIdKey2) && profileIdsMap.get(profileIdKey2) > 0){
		        							if(duUserIdList.contains(profileIdsMap.get(profileIdKey))){
		        								isCeoShareLine = true;		//是否ceo分润线，也就是ceo是否分2次润(true=是；false=不是)
		        							}
		        						}
		        					}
		        				}
		        				
		        				//更新代理帐户信息
		        				if(!updateMikuAgencyShareAccount(transactionStatus, mikuAgencyShareAccountDOList.get(0),mikuAgencyShareAccountDOExample,
	        							totalShareFee, totalAmount, isAgency, profileIdKey, isCeoShareLine)){
		        					transactionStatus.setRollbackOnly();
		        					return false;
		        				}
		        			}
		        			
		        		}
		        	}
		        	if(isShareItem){
		        		//如果是分享商品的订单，统计购买人订单
	        			MikuAgencyShareAccountDOExample mikuAgencyShareAccountDOExampleBuyer = new MikuAgencyShareAccountDOExample();
	        			mikuAgencyShareAccountDOExampleBuyer.createCriteria().andAgencyIdEqualTo(profileId);
	        			List<MikuAgencyShareAccountDO> mikuAgencyShareAccountDOBuyerList 
	        				= mikuAgencyShareAccountDOMapper.selectByExample(mikuAgencyShareAccountDOExampleBuyer);	//查询购买者帐户信息
	        			if(!mikuAgencyShareAccountDOBuyerList.isEmpty()){
	        				MikuAgencyShareAccountDO mikuAgencyShareAccountDO = mikuAgencyShareAccountDOBuyerList.get(0);
	        				mikuAgencyShareAccountDO.setP2TradesCount(1L+
	    							(null==mikuAgencyShareAccountDO.getP2TradesCount() ? 0L : mikuAgencyShareAccountDO.getP2TradesCount()));//自己分润订单
	        				mikuAgencyShareAccountDO.setId(null);
	        				//更新购买者的订单数
	        				mikuAgencyShareAccountDOMapper.updateByExampleSelective(mikuAgencyShareAccountDO, mikuAgencyShareAccountDOExampleBuyer);
	        			}
		        	}
		        	
		        	
		        	Trade updTrade = new Trade();
		        	updTrade.setId(trade.getId());
		        	updTrade.setIsProfit(Byte.valueOf("1"));		//(1=已分润；0=未分润；2=已分润但无代理关系)
		        	tradeMapper.updateByPrimaryKeySelective(updTrade);	//更新交易订单表为已分润
		        	
		        }
		        
		        
		        return true;
            }
    	});
    }
    
    /**
     * 
     * updateMikuAgencyShareAccount:(更新代理帐户). <br/>
     * TODO(这里描述这个方法适用条件 – 可选).<br/>
     *
     * @author LuoGuangChun
     * @param transactionStatus
     * @param mikuAgencyShareAccountDO
     * @param mikuAgencyShareAccountDOExample
     * @param totalShareFee
     * @param totalAmount
     * @param isAgency
     * @param profileIdKey
     */
    private boolean updateMikuAgencyShareAccount(TransactionStatus transactionStatus, MikuAgencyShareAccountDO mikuAgencyShareAccountDO, MikuAgencyShareAccountDOExample mikuAgencyShareAccountDOExample, 
			Long totalShareFee, Long totalAmount, Byte isAgency, Integer profileIdKey, boolean isCeoShareLine){
		if(null != mikuAgencyShareAccountDO){
			mikuAgencyShareAccountDO.setNoGetpayFee(totalShareFee+
					(null==mikuAgencyShareAccountDO.getNoGetpayFee() ? 0L : mikuAgencyShareAccountDO.getNoGetpayFee()));//余额
			mikuAgencyShareAccountDO.setTotalShareFee(totalShareFee+
					(null==mikuAgencyShareAccountDO.getTotalShareFee() ? 0L : mikuAgencyShareAccountDO.getTotalShareFee()));//分润
			if(!isCeoShareLine){	//若ceo分润2次，只统计一次
				//总销售额
				mikuAgencyShareAccountDO.setpSalesFee(totalAmount+
						(null==mikuAgencyShareAccountDO.getpSalesFee() ? 0L : mikuAgencyShareAccountDO.getpSalesFee()));
				//总订单数
				mikuAgencyShareAccountDO.setpTradesCount(1L+
						(null==mikuAgencyShareAccountDO.getpTradesCount() ? 0L : mikuAgencyShareAccountDO.getpTradesCount()));
			}
			
			if(null != isAgency && 1 == isAgency){		//购买者是代理(isAgency： 1=代理)
				if(BizConstants.AgencyLevel.hhh.getId() == profileIdKey && !isCeoShareLine){	//三级代理
					//订单数
					mikuAgencyShareAccountDO.setP2TradesCount(1L+
							(null==mikuAgencyShareAccountDO.getP2TradesCount() ? 0L : mikuAgencyShareAccountDO.getP2TradesCount()));//自己分润订单
					//销售额
					mikuAgencyShareAccountDO.setP2SalesFee(totalAmount+
							(null==mikuAgencyShareAccountDO.getP2SalesFee() ? 0L : mikuAgencyShareAccountDO.getP2SalesFee()));
				}
			}
			mikuAgencyShareAccountDO.setLastUpdated(new Date());
			mikuAgencyShareAccountDO.setId(null);
			//更新代理分润帐户表销售额和分润额
			if(mikuAgencyShareAccountDOMapper.updateByExampleSelective(mikuAgencyShareAccountDO, mikuAgencyShareAccountDOExample) < 1){
				transactionStatus.setRollbackOnly();
				return false;
			}
		}
		return true;
	}
    
    /**
     * 
     * insertMikuSalesRecordDO:(插入分润信息). <br/>
     * TODO(这里描述这个方法适用条件 – 可选).<br/>
     *
     * @author LuoGuangChun
     * @param transactionStatus
     * @param profileDO
     * @param itemParameter
     * @param trade
     * @param order
     * @param totalShareFee
     * @param totalAmount
     * @param shareFee
     * @param agencyId
     * @param profileIdKey
     * @return
     */
    private boolean insertMikuSalesRecordDO(TransactionStatus transactionStatus, ProfileDO profileDO, JSONObject itemParameter, Trade trade, Order order, 
    		Long totalShareFee, Long totalAmount, Long shareFee, Long agencyId, Integer profileIdKey){
    	Date nowDate = new Date();
    	Date nowDate14 = addDay(nowDate, 14);	//往后延期14天，表示14天后才能提现
    	MikuSalesRecordDO mikuSalesRecordDO = new MikuSalesRecordDO();
		//itemParameter = itemParameterMap.get(itemId);	//获取商品所对应的分润参数（json-kv）
		
		//商品可分润金额
		/*Long itemProfitFee = (null == order.getItemProfitFee() ? 0L : order.getItemProfitFee());	//商品公司利润
		Long itemCostFee = (null == order.getItemCostFee() ? 0L : order.getItemCostFee());		//商品成本
		Long orderTotalFee = (null == order.getTotalFee() ? 0L : order.getTotalFee());				//订单总金额
		Long orderCanProfit = (orderTotalFee - (itemProfitFee + itemCostFee) * order.getNum());		//订单可分润金额
		if(null == orderCanProfit || orderCanProfit < 1){
			//continue;
			orderCanProfit = 0L;
		}
		totalShareFee += (orderCanProfit *
				itemParameter.getLongValue(profileIdKey.toString()) / 100);*/		//获取代理等级所对应的商品分润金额 （乘以） 商品数量
		
		totalAmount += (null == order.getTotalFee() ? 0L : order.getTotalFee());
		//mikuSalesRecordDO.setAgencyId(profileIdsMap.get(profileIdKey));
		mikuSalesRecordDO.setAgencyId(agencyId);
		mikuSalesRecordDO.setAgencyLevelName(profileIdKey.toString());
		mikuSalesRecordDO.setBuyerId(order.getBuyerId());
		mikuSalesRecordDO.setBuyerName(profileDO.getNickname());
		mikuSalesRecordDO.setBuyerMobile(profileDO.getMobile());
		mikuSalesRecordDO.setItemId(order.getArtificialId());
		mikuSalesRecordDO.setItemName(order.getTitle());
		mikuSalesRecordDO.setPrice(order.getPrice());
		mikuSalesRecordDO.setAmount((null == order.getTotalFee() ? 0L : order.getTotalFee()));
		/*mikuSalesRecordDO.setShareFee(orderCanProfit *
				itemParameter.getLongValue(profileIdKey.toString()) / 100);*/ //获取代理等级所对应的商品分润金额 （乘以） 商品数量
		mikuSalesRecordDO.setShareFee(shareFee);
		mikuSalesRecordDO.setTradeId(order.getTradeId());
		mikuSalesRecordDO.setNum(order.getNum());
		mikuSalesRecordDO.setIsGetpay(Constants.GetPayStatus.NOGETPAY.getStatusId());	//提现状态(-1=未提现/删除；0=提现中/待审核；1=已提现/已审核;2=提现异常)
		//mikuSalesRecordDO.setShareSchemeId(shareSchemeId);
		mikuSalesRecordDO.setDateCreated(nowDate);
		mikuSalesRecordDO.setLastUpdated(nowDate);
		
		mikuSalesRecordDO.setParameter(order.getProfitParameter());
		mikuSalesRecordDO.setPayTime(trade.getPayTime());
		mikuSalesRecordDO.setConfirmDate(trade.getConfirmTime());
		mikuSalesRecordDO.setReturnDate(nowDate);
		mikuSalesRecordDO.setReturnStatus(Constants.ReturnGoodsStatus.NORMAL.getStatusId());	//分润状态不是退款状态
		mikuSalesRecordDO.setTimeoutActionTime(nowDate14);
		mikuSalesRecordDO.setVersion(0L);
		if(mikuSalesRecordDOMapper.insertSelective(mikuSalesRecordDO) < 1){		//插入代理分润记录
			transactionStatus.setRollbackOnly();
			return false;
		}
		return true;
    }

    private void handleOrder(){
    	
    }

	@Override
	public void afterPropertiesSet() throws Exception {
		
		checkNotNull(profileDOMapper);
		checkNotNull(transactionManager);
		checkNotNull(mikuSalesRecordDOMapper);
		checkNotNull(mikuAgencyShareAccountDOMapper);
		checkNotNull(tradeCourierDOMapper);
		checkNotNull(tradeMapper);
		//checkNotNull(orderMapper);

        transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setName("userAgentcy-transaction");
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        transactionTemplate.setIsolationLevel(Connection.TRANSACTION_READ_COMMITTED);
		
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
    
    private MikuAgencyShareAccountDO setMikuAgencyShareAccountDO(Long id, MikuAgencyShareAccountDO mikuAgencyShareAccountDO) {
		if(null == mikuAgencyShareAccountDO){
			mikuAgencyShareAccountDO = new MikuAgencyShareAccountDO();
		}
		Date now = new Date();
		mikuAgencyShareAccountDO.setVersion(0L);
		mikuAgencyShareAccountDO.setAgencyId(id);
		mikuAgencyShareAccountDO.setTotalShareFee(0L);
		mikuAgencyShareAccountDO.setTotalGotpayFee(0L);
		mikuAgencyShareAccountDO.setDirectSalesFee(0L);
		mikuAgencyShareAccountDO.setDirectShareFee(0L);
		mikuAgencyShareAccountDO.setGetpayingFee(0L);
		mikuAgencyShareAccountDO.setIndirectSalesFee(0L);
		mikuAgencyShareAccountDO.setIndirectShareFee(0L);
		mikuAgencyShareAccountDO.setNoGetpayFee(0L);
		mikuAgencyShareAccountDO.setpOfferFee(0L);
		mikuAgencyShareAccountDO.setP2OfferFee(0L);
		mikuAgencyShareAccountDO.setP3OfferFee(0L);
		mikuAgencyShareAccountDO.setP4OfferFee(0L);
		mikuAgencyShareAccountDO.setP5OfferFee(0L);
		mikuAgencyShareAccountDO.setpSalesFee(0L);
		mikuAgencyShareAccountDO.setP2SalesFee(0L);
		mikuAgencyShareAccountDO.setP3SalesFee(0L);
		mikuAgencyShareAccountDO.setP4SalesFee(0L);
		mikuAgencyShareAccountDO.setP5SalesFee(0L);
		mikuAgencyShareAccountDO.setpTradesCount(0L);
		mikuAgencyShareAccountDO.setP2TradesCount(0L);
		mikuAgencyShareAccountDO.setP3TradesCount(0L);
		mikuAgencyShareAccountDO.setP4TradesCount(0L);
		mikuAgencyShareAccountDO.setP5TradesCount(0L);
		mikuAgencyShareAccountDO.setDateCreated(now);
		mikuAgencyShareAccountDO.setLastUpdated(now);
		mikuAgencyShareAccountDO.setVersion(0L);
		return mikuAgencyShareAccountDO;
	}
    
    public static void main(String[] args) {
    	Long aaa = (long)(new BigDecimal("4.8").multiply(new BigDecimal("6480"))
		.divide(new BigDecimal("100"), 3, BigDecimal.ROUND_HALF_UP).doubleValue());
		System.out.println(aaa);
		
		ProfileDO tradePidProfileDO = new ProfileDO();
		tradePidProfileDO.setIsAgency((byte)1);
		if(null != tradePidProfileDO && tradePidProfileDO.getIsAgency().equals((byte)1)){
			System.out.println("11111111111111111111111");
		}else{
			System.out.println("2222222222222222222");
		}
		
	}
    
}
