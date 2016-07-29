/**
 * Project Name:welink-biz
 * File Name:ProfitTask.java
 * Package Name:com.welink.biz.common.task
 * Date:2015年11月6日下午2:48:26
 * Copyright (c) 2015, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.biz.common.task;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.annotation.Resource;

import net.spy.memcached.MemcachedClient;

import org.apache.commons.lang.StringUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.google.common.eventbus.AsyncEventBus;
import com.welink.biz.common.constants.TimeConstants;
import com.welink.buy.utils.Constants;
import com.welink.commons.domain.Trade;
import com.welink.commons.domain.TradeExample;
import com.welink.commons.events.ProfitEvent;
import com.welink.commons.persistence.TradeMapper;

/**
 * ClassName:ProfitTask <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 定时分润 <br/>
 * Date:     2015年11月6日 下午2:48:26 <br/>
 * @author   LuoGuangChun
 */
public class ProfitTask extends QuartzJobBean {
	private static org.slf4j.Logger logger = LoggerFactory.getLogger(ProfitTask.class);

	public static final String PROFIT_TASK_FLAG = "profit_task_flag";
	
	@Resource
    private TradeMapper tradeMapper;
	
	@Resource
    private MemcachedClient memcachedClient;
	
	private AsyncEventBus asyncEventBus;

    public void setAsyncEventBus(AsyncEventBus asyncEventBus) {
        this.asyncEventBus = asyncEventBus;
    }
	
	/*@Resource
    private AsyncEventBus asyncEventBus;*/
	
	@Override
	protected void executeInternal(JobExecutionContext arg0)
			throws JobExecutionException {
		Object oFlag = memcachedClient.get(PROFIT_TASK_FLAG);
		if (null == oFlag || (null != oFlag && StringUtils.equals("1", oFlag.toString()))) {
            memcachedClient.set(PROFIT_TASK_FLAG, TimeConstants.REDIS_EXPIRE_SECONDS_5, "0");
        } else if (null != oFlag && StringUtils.equals("0", oFlag.toString())) {
            return;
        }
		System.out.println("-----------------------------------------------------------------------");
		System.out.println("=================================ProfitTask:"+System.currentTimeMillis());
		//分润(已收货的订单未分润的重新进行分润)
		List<Byte> toDealStatus = new ArrayList<>();	//可分润trade状态
		toDealStatus.add(Constants.TradeStatus.SELLER_CONSIGNED_PART.getTradeStatusId());
		toDealStatus.add(Constants.TradeStatus.WAIT_SELLER_SEND_GOODS.getTradeStatusId());
		toDealStatus.add(Constants.TradeStatus.WAIT_BUYER_CONFIRM_GOODS.getTradeStatusId());
		toDealStatus.add(Constants.TradeStatus.TRADE_BUYER_SIGNED.getTradeStatusId());
		toDealStatus.add(Constants.TradeStatus.TRADE_FINISHED.getTradeStatusId());
		TradeExample toShareFeeExample = new TradeExample();
		toShareFeeExample.createCriteria().andStatusIn(toDealStatus)
        	.andIsProfitEqualTo((byte)0);
        List<Trade> toShareFeeTrades = tradeMapper.selectByExample(toShareFeeExample);
        for(Trade trade : toShareFeeTrades){
        	if(null != trade && null != trade.getTradeId()){
        		asyncEventBus.post(new ProfitEvent(trade.getTradeId(), 1));	//加分润；通过事件总线 交易分润 //1=加分润；2=减分润
        	}
        }
        memcachedClient.set(PROFIT_TASK_FLAG, TimeConstants.REDIS_EXPIRE_SECONDS_5, "1");
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

	public TradeMapper getTradeMapper() {
		return tradeMapper;
	}

	public void setTradeMapper(TradeMapper tradeMapper) {
		this.tradeMapper = tradeMapper;
	}

	public MemcachedClient getMemcachedClient() {
		return memcachedClient;
	}

	public void setMemcachedClient(MemcachedClient memcachedClient) {
		this.memcachedClient = memcachedClient;
	}

	public AsyncEventBus getAsyncEventBus() {
		return asyncEventBus;
	}
}

