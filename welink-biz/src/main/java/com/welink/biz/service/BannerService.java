package com.welink.biz.service;

/**
 * Created by daniel on 15-1-23.
 */

import com.google.common.base.Function;
import com.google.common.cache.*;
import com.welink.biz.common.model.BannerViewDO;
import com.welink.biz.common.model.ItemCanBuy;
import com.welink.biz.util.ViewDOCopy;
import com.welink.buy.utils.Constants;
import com.welink.buy.utils.ParametersStringMaker;
import com.welink.commons.Env;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.BannerDO;
import com.welink.commons.domain.BannerDOExample;
import com.welink.commons.domain.Item;
import com.welink.commons.domain.ItemExample;
import com.welink.commons.persistence.BannerDOMapper;
import com.welink.commons.persistence.ItemMapper;

import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.annotation.Resource;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by daniel on 15-1-21.
 */
@Service
public class BannerService {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(BannerService.class);

    @Resource
    private BannerDOMapper bannerDOMapper;

    @Resource
    private ItemMapper itemMapper;
    
    @Resource
    private ItemService itemService;

    @Resource
    private Env env;

    private LoadingCache<String, List<BannerViewDO>> bannerCache = CacheBuilder.newBuilder()
            .recordStats()
            .concurrencyLevel(128)
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .removalListener(new RemovalListener<String, List<BannerViewDO>>() {
                @Override
                public void onRemoval(RemovalNotification<String, List<BannerViewDO>> objectObjectRemovalNotification) {
                    log.info("remove listener: {}", objectObjectRemovalNotification);
                }
            })
            .build(CacheLoader.<String, List<BannerViewDO>>from(new Function<String, List<BannerViewDO>>() {
                @Override
                public List<BannerViewDO> apply(@Nullable String key) {
                	String[] params = StringUtils.split(key, ParametersStringMaker.SEPARATOR);
                	Long categoryId = null; Integer moduleType = null;
                	if(null != params[0] && !"".equals(params[0].trim()) && StringUtils.isNumeric(params[0])){
                		categoryId = Long.valueOf(params[0]);
                	}
                	if(null != params[1] && !"".equals(params[1].trim()) && StringUtils.isNumeric(params[1])){
                		moduleType = Integer.valueOf(params[1]);
                	}
                	return fetchBanners(categoryId, moduleType);
                    //return fetchBanners();
                }
            }));
    

    /**
     * 获取首页banners
     *@param moduleType(0=首页banner; 1=目录banner)
     * @return
     */
    public List<BannerViewDO> fetchBanners(Long categoryId, Integer moduleType) {
        BannerDOExample bExample = new BannerDOExample();
        //bExample.createCriteria().andStatusEqualTo(BizConstants.STATUS_VALID).andShowStatusEqualTo(BizConstants.STATUS_VALID);
        if(null != moduleType){
        	if(null != categoryId && categoryId >0){
        		bExample.createCriteria().andStatusEqualTo(BizConstants.STATUS_VALID).andShowStatusEqualTo(BizConstants.STATUS_VALID)
        			.andCategoryIdEqualTo(categoryId).andModuleTypeEqualTo(moduleType);
        	}else{
        		bExample.createCriteria().andStatusEqualTo(BizConstants.STATUS_VALID).andShowStatusEqualTo(BizConstants.STATUS_VALID)
    				.andModuleTypeEqualTo(moduleType);
        	}
        }else{
        	bExample.createCriteria().andStatusEqualTo(BizConstants.STATUS_VALID).andShowStatusEqualTo(BizConstants.STATUS_VALID)
				.andModuleTypeEqualTo(0);
        }
        bExample.setOrderByClause("weight ASC");
        List<BannerDO> banners = bannerDOMapper.selectByExample(bExample);
        List<BannerViewDO> bannerViewDOs = new ArrayList<>();
        if (null != banners && banners.size() > 0) {
            for (BannerDO banner : banners) {
                BannerViewDO bannerViewDO = ViewDOCopy.buildBannerViewDO(banner);
                if (null != banner.getRedirectType() && banner.getRedirectType() == 310 && StringUtils.isNotBlank(banner.getTarget()) && banner.getTarget().startsWith("/")) {
                    if (env.isProd()) {
                        bannerViewDO.setTarget("http://" + BizConstants.ONLINE_DOMAIN  + banner.getTarget());
                    } else {
                        //bannerViewDO.setTarget("http://m." + BizConstants.ONLINE_DOMAIN + banner.getTarget());
                    	bannerViewDO.setTarget("http://" + BizConstants.ONLINE_DOMAIN_TEST + banner.getTarget());
                    }
                }
                if (null != banner.getRedirectType() && banner.getRedirectType() == 311 && null != banner.getTarget()
                		&& StringUtils.isNotBlank(banner.getTarget()) &&  StringUtils.isNumeric(banner.getTarget())){
                	//商品检测库存
                	List<Long> itemIds = new ArrayList<Long>();
                	itemIds.add(Long.valueOf(banner.getTarget()));
                	bannerViewDO.setItemNum(0);
                	Item item = itemMapper.selectByPrimaryKey(Long.valueOf(banner.getTarget()));
                	if (null != item && Constants.ApproveStatus.ON_SALE.getApproveStatusId() == item.getApproveStatus()) {
                		List<ItemCanBuy> itemCanBuys = itemService.fetchOutLimitItemsNoUser(itemIds);
                		if(null != itemCanBuys && !itemCanBuys.isEmpty()){
                			bannerViewDO.setItemNum(itemCanBuys.get(0).getCap());	//可购买数量
                		}
                	}
                }
                bannerViewDOs.add(bannerViewDO);
            }
        }
        return bannerViewDOs;
    }


    /**
     * 缓存获取banners
     *
     * @return
     */
    public List<BannerViewDO> fetchBannersWithCache(Long categoryId, Integer moduleType) {
        BannerDOExample bExample = new BannerDOExample();
        bExample.createCriteria().andStatusEqualTo(BizConstants.STATUS_VALID).andShowStatusEqualTo(BizConstants.STATUS_VALID);
        List<BannerViewDO> banners = fetchBanners(categoryId, moduleType);
        return banners;
    }

    /**
     * 获取首页banners as map
     *@param categoryId(分类id)
     *@param moduleType(0=首页banner; 1=分类banner)
     * @return
     */
    public Map<String, List<BannerViewDO>> fetchBannersMapWitchCatch(long shopId, Long categoryId, Integer moduleType) {
    	//List<BannerViewDO> bannerDOs = bannerCache.getUnchecked(ParametersStringMaker.parametersMake(categoryId, moduleType));
    	List<BannerViewDO> bannerDOs = null;
    	try {
    		bannerDOs = bannerCache.getUnchecked(ParametersStringMaker.parametersMake(categoryId, moduleType));
		} catch (Exception e) {
			bannerDOs = fetchBanners(categoryId, moduleType);
		}
        Map<String, List<BannerViewDO>> bannersMap = new HashMap<>();
        if (null != bannerDOs && bannerDOs.size() > 0) {
            //trans item id -- shop
            bannerDOs = transId(bannerDOs, shopId);
            for (BannerViewDO banner : bannerDOs) {
                if (null != banner.getRedirectType() && Integer.compare(banner.getRedirectType(), 311) == 0 && Integer.compare(banner.getType(), 421) == 0 && null != banner.getTarget() && StringUtils.isNotBlank(banner.getTarget())) {
                    Long itemId = Long.valueOf(banner.getTarget());
                    Item item = itemMapper.selectByPrimaryKey(itemId);
                    if (null != item) {
                        long lCnt = ViewDOCopy.buildSoldCount(item.getId(), item.getSoldQuantity());
                        banner.setSoldCount(Integer.valueOf(String.valueOf(lCnt)));
                    }
                }

                if (null != banner.getType() && null != bannersMap.get(String.valueOf(banner.getType()))) {
                    bannersMap.get(String.valueOf(banner.getType())).add(banner);
                } else {
                    List<BannerViewDO> list = new ArrayList<>();
                    list.add(banner);
                    bannersMap.put(String.valueOf(banner.getType()), list);
                }
            }
        }

        /*for (String key : bannersMap.keySet()) {
            Collections.sort(bannersMap.get(key), new Comparator<BannerViewDO>() {
                public int compare(BannerViewDO sort, BannerViewDO sort1) {
                    //return new Integer(sort.getWeight()).compareTo(sort1.getWeight());
                	return new Integer(sort1.getWeight()).compareTo(sort.getWeight());
                }
            });
            bannersMap.put(key, bannersMap.get(key));
        }*/
        return bannersMap;
    }
    

    /**
     * banner target转换
     *
     * @param bannerDOs
     * @param shopId
     * @return
     */
    private List<BannerViewDO> transId(List<BannerViewDO> bannerDOs, long shopId) {
        List<BannerViewDO> banners = new ArrayList<>();
        for (BannerViewDO banner : bannerDOs) {
        	BannerViewDO tempBannerViewDO = new BannerViewDO();
        	BeanUtils.copyProperties(banner, tempBannerViewDO);
            if (null != banner.getRedirectType() && banner.getRedirectType() == BizConstants.BannerActionEnum.TO_ITEM.getAction()
            		&& null != banner.getTarget() && StringUtils.isNumeric(banner.getTarget())) {
                ItemExample qExample = new ItemExample();
                //qExample.createCriteria().andShopIdEqualTo(shopId).andBaseItemIdEqualTo(Long.valueOf(banner.getTarget()));
                //qExample.createCriteria().andBaseItemIdEqualTo(Long.valueOf(banner.getTarget()));
                qExample.createCriteria().andIdEqualTo(Long.valueOf(banner.getTarget()));
                List<Item> items = itemMapper.selectByExample(qExample);
                if (null != items && items.size() > 0) {
                	tempBannerViewDO.setTarget(String.valueOf(items.get(0).getId()));
                    banners.add(tempBannerViewDO);
                } else {
                	banners.add(tempBannerViewDO);
                    log.error("首页banner配置数据出错......banner对应分站商品不存在. banner target:" + banner.getTarget() + ",shopId:" + shopId);
                }
            } else {
                banners.add(tempBannerViewDO);
            }
        }
        return banners;
    }

    /**
     * 异步更新banner表中商品已经下架的banner状态
     */
    @Async
    public void updateBannerShowStatus(List<BannerDO> bannerDOs) {
        for (BannerDO bannerDO : bannerDOs) {
            if (bannerDO.getRedirectType() == BizConstants.BannerActionEnum.TO_ITEM.getAction()) {
                String itemId = bannerDO.getTarget();
                Item item = itemMapper.selectByPrimaryKey(Long.valueOf(itemId));
                if (item.getApproveStatus() == 0) {
                    BannerDOExample bannerDOExample = new BannerDOExample();
                    bannerDOExample.createCriteria().andIdEqualTo(bannerDO.getId());
                    bannerDO.setShowStatus(BizConstants.STATUS_INVALID);
                    bannerDO.setVersion(bannerDO.getVersion() + 1L);
                    bannerDO.setLastUpdated(new Date());
                    if (bannerDOMapper.updateByExample(bannerDO, bannerDOExample) < 1) {
                        log.error("商品itemId=" + itemId + "已下架，将其对应的banner表中show_status更新为-1失败");
                    }
                }
            }
        }
    }
}