/**
 * Project Name:welink-biz
 * File Name:ProfitService.java
 * Package Name:com.welink.biz.service
 * Date:2015年11月1日上午10:38:29
 * Copyright (c) 2015, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.biz.service;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
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
import com.welink.biz.LevelVO;
import com.welink.biz.util.TimeUtils;
import com.welink.buy.utils.Constants;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.MikuAgencyShareAccountDO;
import com.welink.commons.domain.MikuAgencyShareAccountDOExample;
import com.welink.commons.domain.MikuSalesRecordDO;
import com.welink.commons.domain.MikuSalesRecordDOExample;
import com.welink.commons.domain.MikuUserAgencyDO;
import com.welink.commons.domain.MikuUserAgencyDOExample;
import com.welink.commons.domain.Order;
import com.welink.commons.domain.OrderExample;
import com.welink.commons.domain.ProfileDO;
import com.welink.commons.domain.Trade;
import com.welink.commons.domain.TradeCourierDO;
import com.welink.commons.domain.TradeCourierDOExample;
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

/**
 * ClassName:ProfitService <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2015年11月1日 上午10:38:29 <br/>
 * @author   LuoGuangChun
 */
@Service
public class ProfitService implements InitializingBean {
	
	@Resource
    private PlatformTransactionManager transactionManager;
	
	private TransactionTemplate transactionTemplate;
	
	@Resource
    private ProfileDOMapper profileDOMapper;
	
	@Resource
    private MikuUserAgencyDOMapper mikuUserAgencyDOMapper;
	
	@Resource
	private TradeMapper tradeMapper;
	
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
	
	//public void profitByProfileIdAndTradeId(@Nonnull final Long profileId, @Nonnull final Long tradeId){
	public void profitByTradeId(@Nonnull final Long tradeId){
	}
	
	
	public Boolean sureTrade(final Long id){
		return transactionTemplate.execute(new TransactionCallback<Boolean>() {
            @Override
            public Boolean doInTransaction(TransactionStatus transactionStatus) {
            	if(null == id || id < 0){
            		return false;
            	}
				Date nowDate = new Date();
				Date now7 =	TimeUtils.addDay(nowDate, 7); //加7天时间 
				//Trade trade = tradeMapper.selectByPrimaryKey(id);
				Trade trade = new Trade();
				trade.setId(id);
				trade.setStatus(Constants.TradeStatus.TRADE_FINISHED.getTradeStatusId());
				trade.setEndTime(nowDate);
				trade.setTimeoutActionTime(now7);
				if(tradeMapper.updateByPrimaryKeySelective(trade) < 1){
					if(null != transactionStatus){
						transactionStatus.setRollbackOnly();
						return false;
					}
				}
				
				if(null != trade && trade.getId() > 0){
					TradeCourierDOExample tradeCourierDOExample = new TradeCourierDOExample();
					tradeCourierDOExample.createCriteria().andTradeIdEqualTo(trade.getId()).andTypeEqualTo(1025).andStatusEqualTo(Byte.valueOf("1"));
					//tradeCourierDOMapper.selectByExample(tradeCourierDOExample);
					TradeCourierDO tradeCourierDO = new TradeCourierDO();
					tradeCourierDO.setType(1026);
					tradeCourierDO.setEndTime(nowDate);
					tradeCourierDOMapper.updateByExampleSelective(tradeCourierDO, tradeCourierDOExample);
				}
				return true;
            }
		});
	}
	
	/**
	 * 
	 * returnProfitByOrder:(根据已退货或众筹失败的order进行退分润). <br/>
	 *
	 * @author LuoGuangChun
	 * @param order
	 * @since JDK 1.6
	 */
	public void returnProfitByOrder(final Order order){
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
	

	
}

