package com.welink.biz.schedule.monitor;

import com.welink.biz.common.MSG.SMSUtils;
import com.welink.biz.common.MSG.YupianSmsApi;
import com.welink.buy.utils.Constants;
import com.welink.commons.domain.TradeExample;
import com.welink.commons.persistence.TradeMapper;
import net.spy.memcached.MemcachedClient;
import org.joda.time.DateTime;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 如果已经到了送货时间还没有送货，搞死送货员
 * <p/>
 * Created by saarixx on 30/12/14.
 */
//@Service
public class CourierWarning {

    private static final String PREFIX = "$DISTRIBUTED_LIMIT_$";

    private static final String mobile = "13819198858";

    @Resource
    private MemcachedClient memcachedClient;

    @Resource
    private TradeMapper tradeMapper;

    // 上午 10:00
    @Scheduled(cron = "0 0 10 ? * *")
    public void preEarlyDeadlineWarning() throws IOException {

        long count = memcachedClient.incr(PREFIX + "PRE_EARLY_DEADLINE_WARNING", 1, 1, (int) TimeUnit.MINUTES.toSeconds(10));

        if (count == 1) {
            // 从昨天中午12点到凌晨24点
            TradeExample tradeExample = new TradeExample();
            tradeExample.createCriteria() //
                    .andStatusEqualTo(Constants.TradeStatus.WAIT_SELLER_SEND_GOODS.getTradeStatusId()) //
                    .andDateCreatedBetween(getStartEarlyDeadlineTime(), getEndEarlyDeadlineTime());

            int i = tradeMapper.countByExample(tradeExample);

            if (i > 0) {
                YupianSmsApi.tplSendSms(SMSUtils.APIKEY, 608021L, "#messag_in#=" + "亲，昨天12点到24点的订单，还有" + i + "单未发货，再不抓紧发货，董事长要扣工资啦。", mobile);
            }
        }
    }

    // 中午 12:30
    @Scheduled(cron = "0 30 12 ? * *")
    public void afterEarlyDeadlineWarning() throws IOException {

        long count = memcachedClient.incr(PREFIX + "AFTER_EARLY_DEADLINE_WARNING", 1, 1, (int) TimeUnit.MINUTES.toSeconds(10));

        if (count == 1) {
            // 从昨天中午12点到凌晨24点
            TradeExample tradeExample = new TradeExample();
            tradeExample.createCriteria() //
                    .andStatusEqualTo(Constants.TradeStatus.WAIT_SELLER_SEND_GOODS.getTradeStatusId()) //
                    .andDateCreatedBetween(getStartEarlyDeadlineTime(), getEndEarlyDeadlineTime());

            int i = tradeMapper.countByExample(tradeExample);

            if (i > 0) {
                YupianSmsApi.tplSendSms(SMSUtils.APIKEY, 608021L, "#messag_in#=" + "亲，昨天12点到24点的订单，还有" + i + "单未发货，客服电话已经被打爆，董事长说您明天不用来上班啦。", mobile);
            }
        }
    }

    // 下午16：00点提醒
    @Scheduled(cron = "0 0 16 ? * *")
    public void preLatterDeadlineWarning() throws IOException {

        long count = memcachedClient.incr(PREFIX + "PRE_LATTER_DEADLINE_WARNING", 1, 1, (int) TimeUnit.MINUTES.toSeconds(10));

        if (count == 1) {

            // 今天0点到今天中午12点
            TradeExample tradeExample = new TradeExample();
            tradeExample.createCriteria() //
                    .andStatusEqualTo(Constants.TradeStatus.WAIT_SELLER_SEND_GOODS.getTradeStatusId()) //
                    .andDateCreatedBetween(getStartLatterDeadlineTime(), getEndLatterDeadlineTime());

            int i = tradeMapper.countByExample(tradeExample);


            if (i > 0) {
                YupianSmsApi.tplSendSms(SMSUtils.APIKEY, 608021L, "#messag_in#=" + "亲，今天0点到12点的订单，还有" + i + "单未发货，再不抓紧发货，董事长要扣工资啦。", mobile);
            }
        }

    }

    // 下午18:30提醒
    @Scheduled(cron = "0 30 18 ? * *")
    public void afterLatterDeadlineWarning() throws IOException {
        long count = memcachedClient.incr(PREFIX + "PRE_LATTER_DEADLINE_WARNING", 1, 1, (int) TimeUnit.MINUTES.toSeconds(10));

        if (count == 1) {

            // 今天0点到今天中午12点
            TradeExample tradeExample = new TradeExample();
            tradeExample.createCriteria() //
                    .andStatusEqualTo(Constants.TradeStatus.WAIT_SELLER_SEND_GOODS.getTradeStatusId()) //
                    .andDateCreatedBetween(getStartLatterDeadlineTime(), getEndLatterDeadlineTime());

            int i = tradeMapper.countByExample(tradeExample);


            if (i > 0) {
                YupianSmsApi.tplSendSms(SMSUtils.APIKEY, 608021L, "#messag_in#=" + "亲，今天0点到12点的订单，还有" + i + "单未发货，客服电话已经被打爆，董事长说您明天不用来上班啦。", mobile);
            }
        }
    }

    /**
     * 从昨天中午12点到凌晨24点
     *
     * @return
     */
    public Date getStartEarlyDeadlineTime() {
        DateTime dateTime = new DateTime();
        return dateTime.withHourOfDay(12).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0).minusDays(1).toDate();
    }

    /**
     * 从昨天中午12点到凌晨24点
     *
     * @return
     */
    public Date getEndEarlyDeadlineTime() {
        DateTime dateTime = new DateTime();
        return dateTime.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0).minusMillis(1).toDate();
    }

    /**
     * 今天0点到今天中午12点
     *
     * @return
     */
    public Date getStartLatterDeadlineTime() {
        DateTime dateTime = new DateTime();
        return dateTime.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0).toDate();
    }

    /**
     * 今天0点到今天中午12点
     *
     * @return
     */
    public Date getEndLatterDeadlineTime() {
        DateTime dateTime = new DateTime();
        return dateTime.withHourOfDay(12).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0).toDate();
    }
}
