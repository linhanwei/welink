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
import java.util.Random;

import javax.annotation.Resource;

import net.spy.memcached.MemcachedClient;

import org.apache.commons.lang.StringUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.google.common.eventbus.AsyncEventBus;
import com.welink.biz.common.constants.TimeConstants;
import com.welink.biz.service.ProfitService;
import com.welink.buy.utils.Constants;
import com.welink.commons.domain.MikuCrowdfundDO;
import com.welink.commons.domain.MikuCrowdfundDOExample;
import com.welink.commons.domain.Order;
import com.welink.commons.domain.Trade;
import com.welink.commons.domain.TradeExample;
import com.welink.commons.events.ProfitEvent;
import com.welink.commons.persistence.MikuCrowdfundDOMapper;
import com.welink.commons.persistence.MikuCrowdfundDetailDOMapper;
import com.welink.commons.persistence.MikuSalesRecordDOMapper;
import com.welink.commons.persistence.TradeMapper;

/**
 * 
 * ClassName: CrowdfundTask <br/>
 * Function: TODO 定时处理众筹 <br/>
 * Reason: TODO ADD REASON(可选). <br/>
 * date: 2016年2月18日 上午11:39:53 <br/>
 *
 * @author LuoGuangChun
 * @version 
 * @since JDK 1.6
 */
public class CrowdfundTask extends QuartzJobBean {
	private static org.slf4j.Logger logger = LoggerFactory.getLogger(CrowdfundTask.class);

	public static final String CROWDFUND_TASK_FLAG = "crowdfund_task_flag";
	
	@Resource
    private TradeMapper tradeMapper;
	
	@Resource
	private MikuCrowdfundDOMapper mikuCrowdfundDOMapper;
	
	@Resource
	private MikuCrowdfundDetailDOMapper mikuCrowdfundDetailDOMapper;
	
	@Resource
	private MikuSalesRecordDOMapper mikuSalesRecordDOMapper;
	
	@Resource
	private ProfitService profitService;
	
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
		Random random = new Random();
        long sec = random.nextInt(10) * 1000l;
        try {
            Thread.sleep(sec);
        } catch (InterruptedException e) {
            logger.error("CrowdfundTask . thread sleep failed. exp:" + e.getMessage());
        }
		Object oFlag = memcachedClient.get(CROWDFUND_TASK_FLAG);
		if (null == oFlag || (null != oFlag && StringUtils.equals("1", oFlag.toString()))) {
            memcachedClient.set(CROWDFUND_TASK_FLAG, TimeConstants.REDIS_EXPIRE_SECONDS_5, "0");
        } else if (null != oFlag && StringUtils.equals("0", oFlag.toString())) {
            return;
        }
		
		//更新时间过期了的众筹状态为成功[1]或失败[2]和更新交易的众筹退款状态为申请中[1]
		mikuCrowdfundDOMapper.updateCrowdfundStatusByOutTime();	
		
		//需退分润order
		List<Order> returnProfitOrderList = mikuSalesRecordDOMapper.selectReturnProfitOrderList();
		if(null != returnProfitOrderList && !returnProfitOrderList.isEmpty()){
			for(Order order : returnProfitOrderList){
				//profitService.returnProfitByOrder(order);
				asyncEventBus.post(new ProfitEvent(order.getId(), 2));	//减分润；通过事件总线 交易分润 //1=加分润；2=减分润
			}
		}
		
        memcachedClient.set(CROWDFUND_TASK_FLAG, TimeConstants.REDIS_EXPIRE_SECONDS_5, "1");
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

	public MikuCrowdfundDOMapper getMikuCrowdfundDOMapper() {
		return mikuCrowdfundDOMapper;
	}

	public void setMikuCrowdfundDOMapper(MikuCrowdfundDOMapper mikuCrowdfundDOMapper) {
		this.mikuCrowdfundDOMapper = mikuCrowdfundDOMapper;
	}

	public MikuCrowdfundDetailDOMapper getMikuCrowdfundDetailDOMapper() {
		return mikuCrowdfundDetailDOMapper;
	}

	public void setMikuCrowdfundDetailDOMapper(
			MikuCrowdfundDetailDOMapper mikuCrowdfundDetailDOMapper) {
		this.mikuCrowdfundDetailDOMapper = mikuCrowdfundDetailDOMapper;
	}

	public MemcachedClient getMemcachedClient() {
		return memcachedClient;
	}

	public void setMemcachedClient(MemcachedClient memcachedClient) {
		this.memcachedClient = memcachedClient;
	}

	public MikuSalesRecordDOMapper getMikuSalesRecordDOMapper() {
		return mikuSalesRecordDOMapper;
	}

	public void setMikuSalesRecordDOMapper(
			MikuSalesRecordDOMapper mikuSalesRecordDOMapper) {
		this.mikuSalesRecordDOMapper = mikuSalesRecordDOMapper;
	}

	public AsyncEventBus getAsyncEventBus() {
		return asyncEventBus;
	}

	public ProfitService getProfitService() {
		return profitService;
	}

	public void setProfitService(ProfitService profitService) {
		this.profitService = profitService;
	}
    
}

