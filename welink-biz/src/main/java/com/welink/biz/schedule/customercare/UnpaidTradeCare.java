package com.welink.biz.schedule.customercare;

import com.welink.biz.common.MSG.SMSUtils;
import com.welink.biz.common.MSG.YupianSmsApi;
import com.welink.biz.common.constants.TimeConstants;
import com.welink.buy.utils.Constants;
import com.welink.commons.domain.ProfileDO;
import com.welink.commons.domain.Trade;
import com.welink.commons.domain.TradeExample;
import com.welink.commons.persistence.ProfileDOMapper;
import com.welink.commons.persistence.TradeMapper;

import net.spy.memcached.MemcachedClient;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by saarixx on 30/12/14.
 */
@Service
public class UnpaidTradeCare {

    private static final String PREFIX = "$DISTRIBUTED_LIMIT_$";
    
    public static final String UNPAID_TRADE_TASK_FLAG = "unpaid_trade_task_flag";

    @Resource
    private TradeMapper tradeMapper;

    @Resource
    private ProfileDOMapper profileDOMapper;

    @Resource
    private MemcachedClient memcachedClient;

    @Resource
    private SMSUtils smsUtils;

    // 30 秒钟刷新一次
    @Scheduled(fixedDelay = 30000)
    public void doExecute() throws IOException {
    	
    	Object oFlag = memcachedClient.get(UNPAID_TRADE_TASK_FLAG);
        if (null == oFlag || (null != oFlag && StringUtils.equals("1", oFlag.toString()))) {
            memcachedClient.set(UNPAID_TRADE_TASK_FLAG, TimeConstants.REDIS_EXPIRE_SECONDS_5, "0");
        } else if (null != oFlag && StringUtils.equals("0", oFlag.toString())) {
            return;
        }

        DateTime dateTime = new DateTime();

        // 获取45分钟以前所有的未付款订单
        TradeExample tradeExample = new TradeExample();
        tradeExample.createCriteria() //
                .andStatusEqualTo(Constants.TradeStatus.WAIT_BUYER_PAY.getTradeStatusId()) //
                .andDateCreatedBetween(dateTime.minusMinutes(45).toDate(), dateTime.minusMinutes(40).toDate());

        List<Trade> trades = tradeMapper.selectByExample(tradeExample);

        for (Trade trade : trades) {
            Long buyerId = trade.getBuyerId();

            checkNotNull(buyerId, "buyer id from trade [%s] can not be null ... ", trade.getId());

            ProfileDO profileDO = profileDOMapper.selectByPrimaryKey(buyerId);

            checkNotNull(profileDO, "profile should not be null, the buyer id is [%s]", buyerId);
            checkArgument(StringUtils.isNotBlank(profileDO.getMobile()), "mobile should not be null, the buyer id is [%s]", buyerId);

            // 每个用户20分钟内只提醒一次
            long count = memcachedClient.incr(PREFIX + "CUSTOMER_CARE_UNPAID_TRADE_" + trade.getBuyerId(), 1, 1, (int) TimeUnit.MINUTES.toSeconds(20));

            if (count == 1) {
                YupianSmsApi.tplSendSms(SMSUtils.APIKEY, 985465L, "#tradeTitle#=" + trade.getTitle(), profileDO.getMobile());
            }
        }
        
        memcachedClient.set(UNPAID_TRADE_TASK_FLAG, TimeConstants.REDIS_EXPIRE_SECONDS_5, "1");
    }

}
