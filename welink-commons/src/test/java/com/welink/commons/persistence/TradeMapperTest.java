/**
 * Project Name:welink-commons
 * File Name:TradeMapper.java
 * Package Name:com.welink.commons.persistence
 * Date:2016年1月12日上午9:17:44
 * Copyright (c) 2016, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.commons.persistence;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.CategoryDO;
import com.welink.commons.domain.MikuBrandDO;
import com.welink.commons.domain.Order;
import com.welink.commons.domain.Trade;
import com.welink.commons.domain.TradeExample;

/**
 * ClassName:TradeMapper <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2016年1月12日 上午9:17:44 <br/>
 * @author   LuoGuangChun
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:commons-applicationContext.xml"})
@ActiveProfiles("test")
public class TradeMapperTest {
	@Resource
    private TradeMapper tradeMapper;
	
	@Resource
    private MikuBrandDOMapper mikuBrandDOMapper;
	
	@Resource
    private MikuReturnGoodsDOMapper mikuReturnGoodsDOMapper;
	
	@Test
	public void getCanReturnOrders(){
		List<Byte> status = new ArrayList<Byte>();
		status.add((byte)0);
		status.add((byte)1);
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("buyerId", 69371L);
		paramMap.put("notInReturnStatus", status);
		//paramMap.put("timeoutActionTime", new Date());
		paramMap.put("limit", 3);
		paramMap.put("offset", 0);
		paramMap.put("orderByClause", "o.date_created DESC");
		List<Order> orders = tradeMapper.getCanReturnOrders(paramMap);
		System.out.println("1111111111111111111111111111111111111111111111111");
		for(Order order : orders){
			System.out.println("..............TradeId:"+order.getTradeId()+"...........Title:"+order.getTitle());
		}
		System.out.println("22222222222222222222222222222222222222222222222222");
	}
	
	@Test
	public void getReturnGoodsVOList(){
		Map<String, Object> paramMap = new HashMap<String, Object>();
		//paramMap.put("profileId", 69371L);
		Map<String, Object> sumByBuyer = tradeMapper.sumByBuyer(paramMap);
	}
	
	@Test
	public void getBrandsByCates(){
		Long categoryId = 20000003L;
		Map<String,Object> paramMap = new HashMap<String, Object>();
    	if(null != categoryId && categoryId > -2L){
    		paramMap.put("cateLevel", 1);
    		paramMap.put("categoryId", categoryId);
    	}
    	List<MikuBrandDO> list = mikuBrandDOMapper.getBrandsByCates(paramMap);
    	System.out.println("----MikuBrandDOList----------------------------------------"+list.size());
	}
	
	@Test
	public void getCatesByBrands(){
		Long cateParentId = null, brandId = 4L;
		Integer level = null;
		Map<String,Object> paramMap = new HashMap<String, Object>();
		if(null != level && level > 0L){
			paramMap.put("cateLevel", level);
		}
		if(null != cateParentId && cateParentId > 0){
			paramMap.put("cateParentId", cateParentId);
		}
		if(null != brandId && brandId > 0){
			paramMap.put("brandId", brandId);
		}
		List<CategoryDO> list = mikuBrandDOMapper.getCatesByBrands(paramMap);
    	System.out.println("----CategoryDOList----------------------------------------"+list.size());
    	for(CategoryDO c : list){
    		System.out.println("-----------------id: "+c.getId()+"....ParentId."+c.getParentId()+".....name."+c.getName()+"....Level.:"+c.getLevel());
    	}
    }
	
	@Test
	public void tradeCheckerTask(){
		List<Byte> toDealStatus = new ArrayList<>();	
        toDealStatus.add((byte)2);
        toDealStatus.add((byte)5);
        toDealStatus.add((byte)7);
        //toDealStatus.add(Constants.TradeStatus.TRADE_COMMENTED.getTradeStatusId());
        //toDealStatus.add(Constants.TradeStatus.TRADE_RETURNNING.getTradeStatusId());
        
        List<Byte> toSuccessedStatus = new ArrayList<>();	//更新为交易完成之前的交易状态
    	toSuccessedStatus.add((byte)7);
    	//toSuccessedStatus.add(Constants.TradeStatus.TRADE_COMMENTED.getTradeStatusId());
    	//toSuccessedStatus.add(Constants.TradeStatus.TRADE_RETURNNING.getTradeStatusId());
        
        Date nowDate = new Date();
        
        //统一处理
        TradeExample tExample = new TradeExample();
        //tExample.createCriteria().andTimeoutActionTimeGreaterThan(addDay(nowDate, -7)).andStatusIn(toDealStatus);//.andTimeoutActionTimeBetween(addDay(new Date(), -7), addDay(new Date(), 7));
        tExample.createCriteria().andStatusIn(toDealStatus);
        List<Trade> trades = tradeMapper.selectByExample(tExample);
        List<Long> toCloseTradeIds = new ArrayList<>();
        List<Long> toConfirmTradeIds = new ArrayList<>();
        List<Long> toEvalTradeIds = new ArrayList<>();	//更新为已评价的交易ids
        List<Long> toFinishIds = new ArrayList<>();		//更新为确认收货的交易ids
        List<Long> toSuccessedIds = new ArrayList<>();	//更新为交易完成的交易ids
        long buyerId = -1;
        if (null != trades && trades.size() > 0) {
            Date dn = new Date();
            for (Trade t : trades) {
                buyerId = t.getBuyerId();
                //1. 一天内未付款的订单取消掉
                //if (t.getStatus() == Constants.TradeStatus.WAIT_BUYER_PAY.getTradeStatusId() && t.getTimeoutActionTime().getTime() <= dn.getTime()) {
                if (t.getStatus() == (byte)2 && 
                		(new DateTime(t.getDateCreated().getTime()).plusMinutes(BizConstants.TRADE_OUT_OF_DATE_MINUTE_24_HOUR).toDate()).getTime() <= dn.getTime()) {
                	toCloseTradeIds.add(t.getTradeId());
                }
//                }//3. 7天如果未评价默认好评和状态改为已完成(TRADE_SUCCESSED)
                else if (t.getStatus() == (byte)7 && t.getTimeoutActionTime().getTime() <= dn.getTime() && t.getBuyerRate() != (byte)1) {
                	//更新为已评价的交易ids
                	toEvalTradeIds.add(t.getTradeId());
                } else if (toSuccessedStatus.contains(t.getStatus())
                		&& t.getTimeoutActionTime().getTime() <= dn.getTime() ) {
                	//更新为交易完成的交易ids
                	toSuccessedIds.add(t.getTradeId());
                } else{
                	//配送后24小时自动完成 
                	if (null != t.getStatus() && t.getStatus() == (byte)5 
                			&& !t.getReturnStatus().equals((byte)2) && t.getTimeoutActionTime().getTime() <= dn.getTime()) {
                		//更新为确认收货的交易ids
                		toFinishIds.add(t.getTradeId());
                	}
                }
            }
            
            if(toSuccessedIds.size() > 0){
            	//把确认收货、已评价、退货中等超过7天过期的订单更新为交易完成
            	TradeExample tradeExample = new TradeExample();//buyerRate
                /*tradeExample.createCriteria().andTradeIdIn(toSuccessedIds).andTimeoutActionTimeGreaterThan(nowDate)
                        .andStatusIn(toSuccessedStatus).andReturnStatusNotEqualTo((byte)2);*/
            	tradeExample.createCriteria().andTradeIdIn(toSuccessedIds).andTimeoutActionTimeLessThan(nowDate)
                .andStatusIn(toSuccessedStatus).andReturnStatusNotEqualTo((byte)2);
                /*Trade tradeSuccessed = new Trade();
                tradeSuccessed.setStatus((byte)20);
                tradeSuccessed.setReturnStatus((byte)0);
                tradeMapper.updateByExampleSelective(tradeSuccessed, tradeExample);*/
                
                int countByExample = tradeMapper.countByExample(tradeExample);
                System.out.println("----------------id----------------------------------------countByExample: "+countByExample);
                System.out.println("----------------id----------------------------------------");
                System.out.println("ids.size: "+toSuccessedIds.size()+"id: ");
                for(Long id : toSuccessedIds){
                	System.out.print(id+"---");
                }
                System.out.println();
                
            }
            
        }
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

