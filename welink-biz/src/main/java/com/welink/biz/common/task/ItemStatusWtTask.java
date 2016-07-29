package com.welink.biz.common.task;

import com.welink.biz.common.constants.TimeConstants;
import com.welink.biz.service.BannerService;
import com.welink.biz.service.ItemService;
import com.welink.biz.util.TimeUtils;
import com.welink.commons.domain.ItemAtHalfDO;
import com.welink.commons.persistence.ItemMapper;
import net.spy.memcached.MemcachedClient;
import org.apache.commons.lang.StringUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Created by daniel on 15-2-2.
 */
public class ItemStatusWtTask extends QuartzJobBean {

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(ItemStatusTask.class);

    @Resource
    private ItemService itemService;

    @Resource
    private BannerService bannerService;

    @Resource
    private ItemMapper itemMapper;

    @Resource
    private MemcachedClient memcachedClient;

    public static final String HALF_BANNER_TASK_FLAG = "half_banner_task_flag_23";

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        Date nowDate = new Date();
        TimeUtils timeUtils = new TimeUtils();
        Date nextDate = timeUtils.getDateBeforeOrAfter(nowDate, 1);
        Date zeroDate = TimeUtils.getDateStartTime(nextDate);
        Random random = new Random();
        long sec = random.nextInt(10) * 1000l;
        try {
            Thread.sleep(sec);
        } catch (InterruptedException e) {
            logger.error("item status task . thread sleep failed. exp:" + e.getMessage());
        }
        Object oFlag = memcachedClient.get(HALF_BANNER_TASK_FLAG);
        List<ItemAtHalfDO> itemAtHalfDOs = itemService.fetchItemAtHalfAtNextDay(zeroDate);
        if (null == oFlag) {
            memcachedClient.set(HALF_BANNER_TASK_FLAG, TimeConstants.REDIS_EXPIRE_DAY_1_2, "1");
        } else if (StringUtils.equals("1", oFlag.toString())) {
            return;
        }
        //更新半价活动状态
        if (null != itemAtHalfDOs && itemAtHalfDOs.size() > 0) {
            //更新banner表中show_status=1为展示
            //bannerService.updateBannerShowStatus(itemAtHalfDOs, (byte) 1);
            //更新item_at_half表中active_status=7
            //itemService.updateItemAtHalf(itemAtHalfDOs, BizConstants.HalfItemStatus.ACTIVING_IN_STOCK.getStatus());
            //更新商品表信息和插入快照
            //itemService.updateItemActive(itemAtHalfDOs);
        }
    }

    public ItemService getItemService() {
        return itemService;
    }

    public void setItemService(ItemService itemService) {
        this.itemService = itemService;
    }

    public ItemMapper getItemMapper() {
        return itemMapper;
    }

    public void setItemMapper(ItemMapper itemMapper) {
        this.itemMapper = itemMapper;
    }

    public MemcachedClient getMemcachedClient() {
        return memcachedClient;
    }

    public void setMemcachedClient(MemcachedClient memcachedClient) {
        this.memcachedClient = memcachedClient;
    }

    public BannerService getBannerService() {
        return bannerService;
    }

    public void setBannerService(BannerService bannerService) {
        this.bannerService = bannerService;
    }
}
