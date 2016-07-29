package com.welink.biz.common.task;

import com.welink.biz.common.constants.TimeConstants;
import com.welink.biz.service.BannerService;
import com.welink.biz.service.ItemService;
import com.welink.commons.domain.ItemAtHalfDO;
import com.welink.commons.persistence.ItemMapper;
import net.spy.memcached.MemcachedClient;
import org.apache.commons.lang.StringUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.annotation.Resource;
import java.util.List;
import java.util.Random;

/**
 * 每天0点0分1秒出发
 * Created by daniel on 14-11-20.
 */
public class ItemStatusTask extends QuartzJobBean {

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(ItemStatusTask.class);

    @Resource
    private ItemService itemService;

    @Resource
    private ItemMapper itemMapper;

    @Resource
    private MemcachedClient memcachedClient;

    @Resource
    private BannerService bannerService;

    public static final String HALF_BANNER_TASK_FLAG = "half_banner_task_flag_00";

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        Random random = new Random();
        long sec = random.nextInt(10) * 1000l;
        try {
            Thread.sleep(sec);
        } catch (InterruptedException e) {
            logger.error("item status task . thread sleep failed. exp:" + e.getMessage());
        }
        Object oFlag = memcachedClient.get(HALF_BANNER_TASK_FLAG);
        if (null == oFlag) {
            memcachedClient.set(HALF_BANNER_TASK_FLAG, TimeConstants.REDIS_EXPIRE_DAY_1_2, "1");
        } else if (StringUtils.equals("1", oFlag.toString())) {
            return;
        }
        //更新半价活动状态
        List<ItemAtHalfDO> itemAtHalfDOs = itemService.fetchItemAtHalfEndTimeBeforeNow();
        if (null != itemAtHalfDOs && itemAtHalfDOs.size() > 0) {
            //更新banner表中show_status=0 不展示 结束时间过了0点则更新其状态
            //bannerService.updateBannerShowStatus(itemAtHalfDOs, (byte) 0);
            //更新item_at_half表中active_status=1 活动结束
            //itemService.updateItemAtHalf(itemAtHalfDOs, BizConstants.HalfItemStatus.ACTIVE_ENDS.getStatus());
            //更新商品表中的商品信息
            //itemService.updateItemActiveBack(itemAtHalfDOs);
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
