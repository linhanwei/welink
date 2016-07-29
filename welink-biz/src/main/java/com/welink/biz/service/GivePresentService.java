package com.welink.biz.service;


import java.util.List;

import javax.annotation.Resource;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.welink.commons.domain.Item;
import com.welink.commons.domain.ItemExample;
import com.welink.commons.domain.Order;
import com.welink.commons.domain.Trade;
import com.welink.commons.domain.TradeExample;
import com.welink.commons.domain.UserInteractionRecordsDO;
import com.welink.commons.domain.UserInteractionRecordsDOExample;
import com.welink.commons.persistence.ItemMapper;
import com.welink.commons.persistence.OrderMapper;
import com.welink.commons.persistence.TradeMapper;
import com.welink.commons.persistence.UserInteractionRecordsDOMapper;
import com.welink.promotion.PromotionType;
import com.welink.promotion.drools.DroolsExecutor;
import com.welink.promotion.reactive.PromotionResult;
import com.welink.promotion.reactive.UserInteractionEffect;

/**
 * 赠品的操作
 * 
 * @author Mr.Lai 12/16/2015
 *
 */
@Service
public class GivePresentService {
	  private static org.slf4j.Logger log = LoggerFactory.getLogger(UsePromotionService.class);
	  
	  @Resource
	  private OrderMapper orderMapper;
	  
	  @Resource
	  private TradeMapper tradeMapper;
	  
	  @Resource
	  private UserInteractionRecordsDOMapper userInteractionRecordsDOMapper;
	  
	  @Resource
	  private ItemMapper itemMapper;
	  
	  @Resource
	  private UserInteractionEffect userInteractionEffect;
	  
	  @Resource
	  private DroolsExecutor droolsExecutor;
	  
	  //进行赠品操作【规范做法】
	  public void giveOnePresentToConsumerStander(long tradeId, long userId){
		  //赠品操作条件如下:
		  //赠品库存>0  &&　第一次购买商品 [类型为300001]
		  //符合item条件
		  //互动表
		  UserInteractionRecordsDOExample userInteractionRecordsDOExample = new UserInteractionRecordsDOExample();
          userInteractionRecordsDOExample.createCriteria().andUserIdEqualTo(userId).andTypeEqualTo(PromotionType.ITEM_ENOUGY_NUM_STATUS.getCode());
		  List<UserInteractionRecordsDO> recordList=userInteractionRecordsDOMapper.selectByExample(userInteractionRecordsDOExample);
		  UserInteractionRecordsDO userInteractionRecordsDO=new UserInteractionRecordsDO();
		  Order order=new Order();
		  PromotionResult promotionResult = new PromotionResult();
		  //记录有多少个赠品的标志
		  promotionResult.setCode(recordList.size());
		  droolsExecutor.execute(order,promotionResult,userInteractionRecordsDO);
		  //判断是否有商品id && 对应的状态
//		  if(null!=order.getArtificialId() && order.getArtificialId()>0 && userInteractionRecordsDO.equals(PromotionType.ITEM_ENOUGY_NUM_STATUS.getCode())){
		  if(null != promotionResult.getReward() && promotionResult.getReward()){
			  ItemExample itemExample=new ItemExample();
			  itemExample.createCriteria().andIdEqualTo(order.getArtificialId());
			  List<Item> iList=itemMapper.selectByExample(itemExample);
			  if(iList.size()>0){
				  Item item=iList.get(0);
				  if(null!=item && item.getNum()>0 && item.getApproveStatus()==(byte)1){
					  order.setTradeId(tradeId);
					  order.setBuyerId(userId);
					  order.setTitle("赠品");
					  order.setPicUrl(item.getPicUrls());
					  order.setCategoryId(item.getCategoryId());
					  order.setPrice(item.getPrice());
					  userInteractionRecordsDO.setUserId(userId);
					  userInteractionRecordsDO.setTargetId(String.valueOf(tradeId));
					  userInteractionRecordsDO.setDestination("赠品");
					  //进行对3个表的进行修改
					  orderMapper.insert(order);
					  TradeExample tradeExample=new TradeExample();
					  tradeExample.createCriteria().andTradeIdEqualTo(tradeId);
					  List<Trade> tradeList=tradeMapper.selectByExample(tradeExample);
					  if(tradeList.size()>0){
						  Trade trade=tradeList.get(0);
						  if(null!=trade && trade.getOrders().length()>0){
							  trade.setOrders(trade.getOrders()+";"+order.getId());
						  }
						  tradeMapper.updateByPrimaryKeySelective(trade);
						  userInteractionRecordsDOMapper.insert(userInteractionRecordsDO);
						  //库存
						  item.setNum(item.getNum()-1);
						  itemMapper.updateByPrimaryKey(item);
					  } 
				  }
			  }
		  }
	  }
	  
	  
	  
	  //进行赠品的操作
	  public void giveOnePresentTOConsumer(long tradeId, long userId){
		  //赠品操作条件如下:
		  //赠品库存>0  &&　第一次购买商品 [类型为300001]
		  //符合item条件
		  ItemExample itemExample=new ItemExample();
		  itemExample.createCriteria().andTitleEqualTo("赠品").andShopTypeEqualTo((byte)1);
		  List<Item> iList=itemMapper.selectByExample(itemExample);
		  //互动表
		  UserInteractionRecordsDOExample userInteractionRecordsDOExample = new UserInteractionRecordsDOExample();
          userInteractionRecordsDOExample.createCriteria().andTargetIdEqualTo(String.valueOf(tradeId)).andUserIdEqualTo(userId).andTypeEqualTo(PromotionType.ITEM_ENOUGY_NUM_STATUS.getCode());
		  List<UserInteractionRecordsDO> recordList=userInteractionRecordsDOMapper.selectByExample(userInteractionRecordsDOExample);
		  //赠品对象
		  if (null != iList && iList.size() > 0 && recordList.size()==0) {
			  Item item=iList.get(0);
			  Order order=new Order();
			  order.setArtificialId(item.getId());
			  order.setTradeId(tradeId);
			  order.setBuyerId(userId);
			  order.setTitle("赠品");
			  order.setPicUrl(item.getPicUrls());
			  order.setCategoryId(item.getCategoryId());
			  PromotionResult promotionResult = new PromotionResult();
			  UserInteractionRecordsDO userInteractionRecordsDO = new UserInteractionRecordsDO();
			  userInteractionRecordsDO.setUserId(userId);
			  userInteractionRecordsDO.setTargetId(String.valueOf(tradeId));
			  userInteractionRecordsDO.setDestination("赠品");
			  droolsExecutor.execute(order,promotionResult,userInteractionRecordsDO);
			  //进行对3个表的进行修改
			  orderMapper.insert(order);
			  TradeExample tradeExample=new TradeExample();
			  tradeExample.createCriteria().andTradeIdEqualTo(tradeId);
			  List<Trade> tradeList=tradeMapper.selectByExample(tradeExample);
			  if(tradeList.size()>0){
				  Trade trade=tradeList.get(0);
				  if(null!=trade && trade.getOrders().length()>0){
					  trade.setOrders(trade.getOrders()+";"+order.getId());
				  }
				  tradeMapper.updateByPrimaryKeySelective(trade);
				  userInteractionRecordsDOMapper.insert(userInteractionRecordsDO);
				  //库存
				  item.setNum(item.getNum()-1);
				  itemMapper.updateByPrimaryKey(item);
			  }
          }
	  }
	 
	  
	  

}
