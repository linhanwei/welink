package com.welink.biz.service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;
import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Function;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.Lists;
import com.ibm.icu.text.SimpleDateFormat;
import com.welink.biz.common.GroupItem;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.ItemCanBuy;
import com.welink.biz.common.model.ItemLimitTag;
import com.welink.biz.common.model.ItemViewDO;
import com.welink.biz.common.model.SearchResult;
import com.welink.biz.common.model.TagViewDO;
import com.welink.biz.util.ItemUtil;
import com.welink.biz.util.OpenSearchType;
import com.welink.biz.util.TimeUtils;
import com.welink.biz.util.ViewDOCopy;
import com.welink.buy.utils.BaseResult;
import com.welink.buy.utils.Constants;
import com.welink.buy.utils.ParametersStringMaker;
import com.welink.buy.utils.TimeUtil;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.ActiveItemDO;
import com.welink.commons.domain.ActiveItemDOExample;
import com.welink.commons.domain.CartDO;
import com.welink.commons.domain.CartDOExample;
import com.welink.commons.domain.CategoryDO;
import com.welink.commons.domain.CategoryDOExample;
import com.welink.commons.domain.GrouponDO;
import com.welink.commons.domain.GrouponDOExample;
import com.welink.commons.domain.InstallActiveDO;
import com.welink.commons.domain.InstallActiveDOExample;
import com.welink.commons.domain.Item;
import com.welink.commons.domain.ItemAtHalfDO;
import com.welink.commons.domain.ItemAtHalfDOExample;
import com.welink.commons.domain.ItemExample;
import com.welink.commons.domain.ItemExample.Criteria;
import com.welink.commons.domain.MikuActiveTopicDO;
import com.welink.commons.domain.MikuActiveTopicDOExample;
import com.welink.commons.domain.MikuBrandDO;
import com.welink.commons.domain.MikuItemShareParaDO;
import com.welink.commons.domain.MikuItemShareParaDOExample;
import com.welink.commons.domain.ObjectTaggedDO;
import com.welink.commons.domain.ObjectTaggedDOExample;
import com.welink.commons.domain.Order;
import com.welink.commons.domain.OrderExample;
import com.welink.commons.domain.ShopDOExample;
import com.welink.commons.domain.TagsDO;
import com.welink.commons.domain.TagsDOExample;
import com.welink.commons.domain.Trade;
import com.welink.commons.model.ItemActivityTag;
import com.welink.commons.persistence.ActiveItemDOMapper;
import com.welink.commons.persistence.CartDOMapper;
import com.welink.commons.persistence.CategoryDOMapper;
import com.welink.commons.persistence.GrouponDOMapper;
import com.welink.commons.persistence.InstallActiveDOMapper;
import com.welink.commons.persistence.ItemAtHalfDOMapper;
import com.welink.commons.persistence.ItemMapper;
import com.welink.commons.persistence.MikuActiveTopicDOMapper;
import com.welink.commons.persistence.MikuBrandDOMapper;
import com.welink.commons.persistence.MikuItemShareParaDOMapper;
import com.welink.commons.persistence.ObjectTaggedDOMapper;
import com.welink.commons.persistence.OrderMapper;
import com.welink.commons.persistence.TagsDOMapper;
import com.welink.commons.vo.ItemTagActivtyVO;
import com.welink.commons.vo.LevelVO;
import com.welink.commons.vo.MikuActiveTopicVO;

/**
 * Created by daniel on 14-11-10.
 */
@Service
public class ItemService {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(ItemService.class);

    @Resource
    private ItemMapper itemMapper;

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private GrouponDOMapper grouponDOMapper;

    @Resource
    private ItemSearchService itemSearchService;

    @Resource
    private ItemAtHalfDOMapper itemAtHalfDOMapper;

    @Resource
    private ActiveItemDOMapper activeItemDOMapper;

    @Resource
    private ObjectTaggedDOMapper objectTaggedDOMapper;

    @Resource
    private TagsDOMapper tagsDOMapper;

    @Resource
    private InstallActiveDOMapper installActiveDOMapper;

    @Resource
    private CartDOMapper cartDOMapper;
    
    @Resource
    private MikuItemShareParaDOMapper mikuItemShareParaDOMapper;
    
    @Resource
    private MikuBrandDOMapper mikuBrandDOMapper;
    
    @Resource
    private CategoryDOMapper categoryDOMapper;
    
    @Resource
    private MikuActiveTopicDOMapper mikuActiveTopicDOMapper;

    /**
     * 获取当前时间前后n小时的半价活动
     *
     * @return
     */
    public List<ItemAtHalfDO> fetchItemAtHalfAtHours(int hour) {
        Date dt = new Date();
        //查找今日需要更新的item half
        ItemAtHalfDOExample qExample = new ItemAtHalfDOExample();
        qExample.createCriteria().andStatusEqualTo((byte) 1).andAnnounceTimeBetween(TimeUtil.getDateAfterHourAsDate(dt, -hour), TimeUtil.getDateAfterHourAsDate(dt, hour));
        qExample.or().andStartTimeBetween(TimeUtil.getDateAfterHourAsDate(dt, -hour), TimeUtil.getDateAfterHourAsDate(dt, hour));
        qExample.or().andEndTimeBetween(TimeUtil.getDateAfterHourAsDate(dt, -hour), TimeUtil.getDateAfterHourAsDate(dt, hour));
        List<ItemAtHalfDO> itemAtHalfDOs = itemAtHalfDOMapper.selectByExample(qExample);
        return itemAtHalfDOs;
    }

    /**
     * 获取当前时间下一天开始的半价活动
     *
     * @return
     */
    public List<ItemAtHalfDO> fetchItemAtHalfAtNextDay(Date date) {
        //查找今日需要更新的item half
        ItemAtHalfDOExample qExample = new ItemAtHalfDOExample();
        qExample.createCriteria().andStatusEqualTo((byte) 1).andStartTimeEqualTo(date).andActiveStatusEqualTo(BizConstants.HalfItemStatus.DONT_ANNOUNCE.getStatus());
        List<ItemAtHalfDO> itemAtHalfDOs = itemAtHalfDOMapper.selectByExample(qExample);
        return itemAtHalfDOs;
    }

    /**
     * 获取当前时间之前结束的所有活动
     *
     * @return
     */
    public List<ItemAtHalfDO> fetchItemAtHalfEndTimeBeforeNow() {
        //查找今日需要更新的item half
        ItemAtHalfDOExample qExample = new ItemAtHalfDOExample();
        List<Integer> startingStatus = new ArrayList<>();
        startingStatus.add(BizConstants.HalfItemStatus.ACTIVING_IN_STOCK.getStatus());
        startingStatus.add(BizConstants.HalfItemStatus.ACTIVING_NO_STOCK.getStatus());
        qExample.createCriteria().andStatusEqualTo((byte) 1).andEndTimeLessThan(new Date()).andActiveStatusIn(startingStatus);
        List<ItemAtHalfDO> itemAtHalfDOs = itemAtHalfDOMapper.selectByExample(qExample);
        return itemAtHalfDOs;
    }

    //更新ItemAtHalf表中active_status
    public void updateItemAtHalf(List<ItemAtHalfDO> itemAtHalfDOs, Integer status) {
        for (ItemAtHalfDO itemAtHalfDO : itemAtHalfDOs) {
            itemAtHalfDO.setLastUpdated(new Date());
            itemAtHalfDO.setVersion(itemAtHalfDO.getVersion() + 1l);
            itemAtHalfDO.setActiveStatus(status);
            ItemAtHalfDOExample itemAtHalfDOExample = new ItemAtHalfDOExample();
            itemAtHalfDOExample.createCriteria().andIdEqualTo(itemAtHalfDO.getId());
            itemAtHalfDOMapper.updateByExampleSelective(itemAtHalfDO, itemAtHalfDOExample);
        }
    }

    //更新Item表中的价格、库存等为半价时的信息，并记录半价的快照用于展示历史数据
    public void updateItemActive(List<ItemAtHalfDO> itemAtHalfDOs) {
        List<Long> itemIds = new ArrayList<>();
        Map<Long, ItemAtHalfDO> itemAtHalfDOMap = new HashMap<Long, ItemAtHalfDO>();
        for (ItemAtHalfDO itemAtHalfDO : itemAtHalfDOs) {
            itemIds.add(itemAtHalfDO.getItemId());
            itemAtHalfDOMap.put(itemAtHalfDO.getItemId(), itemAtHalfDO);
        }
        ItemExample qExample = new ItemExample();
        qExample.createCriteria().andIdIn(itemIds);
        List<com.welink.commons.domain.Item> items = itemMapper.selectByExample(qExample);
        if (items != null && items.size() > 0) {
            for (Item item : items) {
                Item uItem = new Item();
                uItem.setLastUpdated(new Date());
                uItem.setVersion(item.getVersion() + 1l);
                //uItem.setNum(itemAtHalfDOMap.get(item.getId()).getInventory().intValue());//分站点不具有设置库存的权限了
                uItem.setPrice(itemAtHalfDOMap.get(item.getId()).getActivityPrice());
                Long orginprice = item.getPrice();
                ItemExample itemExample = new ItemExample();
                itemExample.createCriteria().andIdEqualTo(item.getId());
                //如果已经修改过则直接返回
                ActiveItemDOExample aqExample = new ActiveItemDOExample();
                aqExample.createCriteria().andActiveIdEqualTo(itemAtHalfDOMap.get(item.getId()).getId())
                        .andItemIdEqualTo(item.getId());
                List<ActiveItemDO> activeItemDOs = activeItemDOMapper.selectByExample(aqExample);
                if (null != activeItemDOs && activeItemDOs.size() > 0) {
                    return;
                }
                //是否已经插入过
                ActiveItemDOExample qaExample = new ActiveItemDOExample();
                qaExample.createCriteria().andActiveIdEqualTo(itemAtHalfDOMap.get(item.getId()).getId()).andItemIdEqualTo(item.getId());
                List<ActiveItemDO> activeItemDOs1 = activeItemDOMapper.selectByExample(qaExample);
                itemMapper.updateByExampleSelective(uItem, itemExample);
                if (activeItemDOs1 != null && activeItemDOs1.size() > 0) {
                    //不再插入
                } else {
                    ActiveItemDO activeItemDO = new ActiveItemDO();
                    activeItemDO.setPrice(uItem.getPrice());
                    activeItemDO.setVersion(1l);
                    activeItemDO.setLastUpdated(new Date());
                    activeItemDO.setActiveId(itemAtHalfDOMap.get(item.getId()).getId());
                    activeItemDO.setItemId(item.getId());
                    activeItemDO.setNum(itemAtHalfDOMap.get(item.getId()).getInventory());
                    activeItemDO.setDateCreated(new Date());
                    activeItemDO.setPic(item.getPicUrls());
                    activeItemDO.setSoldQuantity(0l);
                    activeItemDO.setOriginPrice(orginprice);
                    ItemUtil itemUtil = new ItemUtil();
                    activeItemDO.setRefPrice(itemUtil.getReferencePrice(item));
                    activeItemDOMapper.insert(activeItemDO);
                }
            }
        }
    }

    /**
     * 更新Item表中的价格、库存等，恢复到原来的价格
     */
    public void updateItemActiveBack(List<ItemAtHalfDO> itemAtHalfDOs) {
        List<Long> itemIds = new ArrayList<>();
        Map<Long, ItemAtHalfDO> itemAtHalfDOMap = new HashMap<Long, ItemAtHalfDO>();
        for (ItemAtHalfDO itemAtHalfDO : itemAtHalfDOs) {
            itemIds.add(itemAtHalfDO.getItemId());
            itemAtHalfDOMap.put(itemAtHalfDO.getItemId(), itemAtHalfDO);
        }
        ItemExample qExample = new ItemExample();
        qExample.createCriteria().andIdIn(itemIds);
        List<com.welink.commons.domain.Item> items = itemMapper.selectByExample(qExample);
        if (items != null && items.size() > 0) {
            for (Item item : items) {
                Item uItem = new Item();
                uItem.setLastUpdated(new Date());
                uItem.setVersion(item.getVersion() + 1l);
                //uItem.setNum(10000);//分站点不具有设置库存的权限了
                ActiveItemDOExample aqExample = new ActiveItemDOExample();
                aqExample.createCriteria().andActiveIdEqualTo(itemAtHalfDOMap.get(item.getId()).getId())
                        .andItemIdEqualTo(item.getId());
                List<ActiveItemDO> activeItemDOs = activeItemDOMapper.selectByExample(aqExample);
                if (activeItemDOs == null || activeItemDOs.size() < 1) {
                    return;
                } else if (null != activeItemDOs && activeItemDOs.size() > 0) {
                    uItem.setPrice(activeItemDOs.get(0).getOriginPrice());
                }
                ItemExample itemExample = new ItemExample();
                itemExample.createCriteria().andIdEqualTo(item.getId());
                itemMapper.updateByExampleSelective(uItem, itemExample);
            }
        }
    }

    private LoadingCache<String, List<Item>> itemCache = CacheBuilder.newBuilder()
            .recordStats()
            .concurrencyLevel(128)
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .removalListener(new RemovalListener<String, List<Item>>() {
                @Override
                public void onRemoval(RemovalNotification<String, List<Item>> objectObjectRemovalNotification) {
                    log.info("remove listener: {}", objectObjectRemovalNotification);
                }
            })
            .build(CacheLoader.<String, List<Item>>from(new Function<String, List<Item>>() {
                @Override
                public List<Item> apply(@Nullable String key) {
                    String[] params = StringUtils.split(key, ParametersStringMaker.SEPARATOR);
                    long shopId = Long.parseLong(params[0]);
                    Long categoryId = null;
                    if(null != params[1] && !"".equals(params[1]) && StringUtils.isNumeric(params[1])){
                    	categoryId = Long.parseLong(params[1]);
                    }
                    Long brandId = null;
                    if(null != params[2] && !"".equals(params[2]) && StringUtils.isNumeric(params[2])){
                    	brandId = Long.parseLong(params[2]);
                    }
                    int offset = Integer.parseInt(params[3]);
                    int limit = Integer.parseInt(params[4]);
                    byte excludeType = Byte.parseByte(params[5]);
                    return fetchItems(shopId, categoryId, brandId, offset, limit, excludeType);
                }
            }));
    
    private LoadingCache<String, List<ItemViewDO>> itemViewListCache = CacheBuilder.newBuilder()
            .recordStats()
            .concurrencyLevel(128)
            .expireAfterWrite(10, TimeUnit.SECONDS)
            /*.initialCapacity(10)	//设置缓存容器的初始容量为10
            .maximumSize(1000)	//设置缓存最大容量为1000，超过1000之后就会按照LRU最近虽少使用算法来移除缓存项*/
            .removalListener(new RemovalListener<String, List<ItemViewDO>>() {
                @Override
                public void onRemoval(RemovalNotification<String, List<ItemViewDO>> objectObjectRemovalNotification) {
                    log.info("remove listener: {}", objectObjectRemovalNotification);
                }
            })
            .build(CacheLoader.<String, List<ItemViewDO>>from(new Function<String, List<ItemViewDO>>() {
                @Override
                public List<ItemViewDO> apply(@Nullable String key) {
                    String[] params = StringUtils.split(key, ParametersStringMaker.SEPARATOR);
                    long shopId = Long.parseLong(params[0]);
                    Long categoryId = null;
                    if(null != params[1] && !"".equals(params[1]) && StringUtils.isNumeric(params[1])){
                    	categoryId = Long.parseLong(params[1]);
                    }
                    Long brandId = null;
                    if(null != params[2] && !"".equals(params[2]) && StringUtils.isNumeric(params[2])){
                    	brandId = Long.parseLong(params[2]);
                    }
                    int offset = Integer.parseInt(params[3]);
                    int limit = Integer.parseInt(params[4]);
                    List<Byte> types = new ArrayList<Byte>();
                    if(!StringUtils.isBlank(params[5]) && !"null".equals(params[5])){
                    	String[] typesStr = params[5].split(",");
                    	for(String type : typesStr){
                    		types.add(Byte.valueOf(type));
                    	}
                    }
                    //byte excludeType = Byte.parseByte(params[5]);
                    int isAgency = Integer.parseInt(params[6]);
                    String orderByClause = "weight ASC";
                    if(!StringUtils.isBlank(params[7]) && !"null".equals(params[7])){
                    	orderByClause = params[7];
                    }
                    Integer cateLevel = null;
                    if(!StringUtils.isBlank(params[8]) && !"null".equals(params[8]) && StringUtils.isNumeric(params[8])){
                    	cateLevel = Integer.valueOf(params[8]);
                    }
                    
                    Long topicId = -1L;
                    if(!StringUtils.isBlank(params[9]) && !"null".equals(params[9]) && StringUtils.isNumeric(params[9])){
                    	topicId = Long.valueOf(params[9]);
                    }
                    
                    //List<Item> items = fetchItemsByTypes(shopId, categoryId, brandId, offset, limit, types, orderByClause, cateLevel);
                    List<Item> items = null;
                    if(null != topicId && topicId > 0){	//专题查询
                    	Map<String, Object> paramMap = new HashMap<String, Object>();
                		paramMap.put("topicId", topicId);
                		if(null != types && types.size() == 1){
                			paramMap.put("type", types.get(0));
                		}else if(null != types && types.size() > 1){
                			paramMap.put("types", types);
                		}
                		if (null != brandId && brandId > -1L) {
                			paramMap.put("brandId", brandId);
                		}
                		//商品分类查询
                		if(null != cateLevel && cateLevel.equals(1) && null != categoryId && categoryId > -1L){	//第一级类目
                			paramMap.put("category1Id", categoryId);
                        }else if(null != cateLevel && cateLevel.equals(2) && null != categoryId && categoryId > -1L){	//第二级类目
                        	paramMap.put("category2Id", categoryId);
                        }else if(null != cateLevel && null != categoryId && categoryId > -1L){	//第三级类目
                        	paramMap.put("categoryId", categoryId);
                        }
                		paramMap.put("orderByClause", "i."+orderByClause);
                		paramMap.put("offset", offset);
                		paramMap.put("limit", limit);
                		items = itemMapper.selectItemByTopic(paramMap);
                    }else{
                    	items = fetchItemsByTypes(shopId, categoryId, brandId, offset, limit, types, orderByClause, cateLevel);
                    }
                    
                    List<ItemViewDO> itemViewDOs = combineItemTags(items);		//设置标签
                    if(BizConstants.QUERY_BROKERAGEFEE && isAgency == 1 && null != itemViewDOs && !itemViewDOs.isEmpty()){		//如果是代理设置商品佣金
                    	for(ItemViewDO itemViewDO : itemViewDOs){
                    		setBrokerageFeeInItemViewDO(itemViewDO);	//设置佣金
                    	}
                    }
                    itemViewDOSetTopic(itemViewDOs, -1L);	//设置专题
                    return itemViewDOs;
                }
            }));
    
    private LoadingCache<String, ItemViewDO> itemViewCache = CacheBuilder.newBuilder()
            .recordStats()
            .concurrencyLevel(128)
            .expireAfterWrite(10, TimeUnit.SECONDS)
            .removalListener(new RemovalListener<String, ItemViewDO>() {
                @Override
                public void onRemoval(RemovalNotification<String, ItemViewDO> objectObjectRemovalNotification) {
                    log.info("remove listener: {}", objectObjectRemovalNotification);
                }
            })
            .build(CacheLoader.<String, ItemViewDO>from(new Function<String, ItemViewDO>() {
                @Override
                public ItemViewDO apply(@Nullable String key) {
                    String[] params = StringUtils.split(key, ParametersStringMaker.SEPARATOR);
                    Long itemId = null;
                    if(null != params[0] && !"".equals(params[0]) && StringUtils.isNumeric(params[0])){
                    	itemId = Long.parseLong(params[0]);
                    }
                    Integer isAgency = 0;
                    if(null != params[1] && !"".equals(params[1]) && StringUtils.isNumeric(params[1])){
                    	isAgency = Integer.parseInt(params[1]);
                    }
                    ItemExample iiExample = new ItemExample();
                    iiExample.createCriteria().andIdEqualTo(itemId);//查询商品资料
                    List<Item> tmpItems = itemMapper.selectByExample(iiExample);
                    List<ItemViewDO> itemViewDOs = combineItemTags(tmpItems);		//设置标签
                    ItemViewDO itemViewDO = null;
                    if(null !=itemViewDOs && !itemViewDOs.isEmpty()){
                    	itemViewDO = itemViewDOs.get(0);
                    }
                    if(isAgency == 1 && null != itemViewDO){		//如果是代理设置商品佣金
                    	setBrokerageFeeInItemViewDO(itemViewDO);	//设置佣金
                    }
                    itemViewDOSetTopic(itemViewDOs, -1L);	//设置专题
                    return itemViewDO;
                }
            }));
    
    /**
     * 查询专题cache
     */
    private LoadingCache<String, List<MikuActiveTopicDO>> activeTopicCache = CacheBuilder.newBuilder()
            .recordStats()
            .concurrencyLevel(128)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .removalListener(new RemovalListener<String, List<MikuActiveTopicDO>>() {
                @Override
                public void onRemoval(RemovalNotification<String, List<MikuActiveTopicDO>> objectObjectRemovalNotification) {
                    log.info("remove listener: {}", objectObjectRemovalNotification);
                }
            })
            .build(CacheLoader.<String, List<MikuActiveTopicDO>>from(new Function<String, List<MikuActiveTopicDO>>() {
                @Override
                public List<MikuActiveTopicDO> apply(@Nullable String key) {
                    String[] params = StringUtils.split(key, ParametersStringMaker.SEPARATOR);
                    Long topicId = null;
                    if(null != params[0] && !"".equals(params[0]) && StringUtils.isNumeric(params[0])){
                    	topicId = Long.parseLong(params[0]);
                    }
                    MikuActiveTopicDOExample mikuActiveTopicDOExample = new MikuActiveTopicDOExample();
                    mikuActiveTopicDOExample.createCriteria().andIdEqualTo(topicId);
                    return mikuActiveTopicDOMapper.selectByExample(mikuActiveTopicDOExample);
                }
            }));
    
    
    private LoadingCache<String, List<ItemTagActivtyVO>> ItemTagActivtyVOListCache = CacheBuilder.newBuilder()
    .recordStats()
    .concurrencyLevel(128)
    .expireAfterWrite(8, TimeUnit.SECONDS)
    .removalListener(new RemovalListener<String, List<ItemTagActivtyVO>>() {
        @Override
        public void onRemoval(RemovalNotification<String, List<ItemTagActivtyVO>> objectObjectRemovalNotification) {
            log.info("remove listener: {}", objectObjectRemovalNotification);
        }
    })
    .build(CacheLoader.<String, List<ItemTagActivtyVO>>from(new Function<String, List<ItemTagActivtyVO>>() {
        @Override
        public List<ItemTagActivtyVO> apply(@Nullable String key) {
            String[] params = StringUtils.split(key, ParametersStringMaker.SEPARATOR);
            Long shopId = null;
            if(null != params[0] && !"".equals(params[0]) && StringUtils.isNumeric(params[0])){
            	shopId = Long.parseLong(params[0]);
            }
            BigInteger bit = null;
            if(null != params[1] && !"".equals(params[1]) && StringUtils.isNumeric(params[1])){
            	bit = new BigInteger(params[1]);
            }
            Long startTime = null;
            if(null != params[2] && !"".equals(params[2]) && StringUtils.isNumeric(params[2])){
            	startTime = Long.parseLong(params[2]);
            }
            Long startTime2 = null;
            if(null != params[3] && !"".equals(params[3]) && StringUtils.isNumeric(params[3])){
            	startTime2 = Long.parseLong(params[3]);
            }
            int offset = Integer.parseInt(params[4]);
            int limit = Integer.parseInt(params[5]);
            //byte excludeType = Byte.parseByte(params[5]);
            List<ItemTagActivtyVO> fetchItemTagActivtyVOList = fetchItemTagActivtyVOList(shopId, bit, startTime, startTime2, offset, limit);
            return combineItemTagActivtyVOTags(fetchItemTagActivtyVOList);
        }
    }));

    //BizConstants.WELINK_ID, null, cateId, page, size, OpenSearchType.RECOMMEND_DESC
    private List<Item> openSearchItems(long shopId, String q, Long categoryId, Long brandId, int page, int size, int searchType, List<Long> tags, boolean multiTagOp) {
        SearchResult<Item> itemSearchResult = null;
        itemSearchResult = itemSearchService.defaultSearch(shopId, q, categoryId, brandId, page, size, OpenSearchType.findByType(searchType), tags, multiTagOp);
        if (itemSearchResult.isSuccess() && null != itemSearchResult.getResult() && itemSearchResult.getResult().getResultList().size() > 0) {
            return itemSearchResult.getResult().getResultList();
        }
        return null;
    }

    public List<Item> searchOpenSearchItems(Long shopId, String q, Long categoryId, Long brandId, int page, int size, int searchType, List<Long> tags, boolean multiOp) {
        List<Item> tmpItems = Lists.newArrayList();
        try {
            tmpItems = openSearchItems(shopId, q, categoryId, brandId, page, size, searchType, tags, multiOp);
        } catch (Exception e) {
            log.error("do search error. exp:" + e.getMessage());
        }
        if (null != tmpItems && tmpItems.size() > 0) {
            return tmpItems;
        }
        return tmpItems;
    }
    
    /**
     * 获取商品的团购扩展信息
     *
     * @param itemId
     * @return
     */
    public GrouponDO fetchGrouponDO(long itemId, long grouponId) {
        GrouponDOExample gExample = new GrouponDOExample();
        List<Byte> toShowStatus = new ArrayList<>();
        toShowStatus.add(Constants.GrouponItemStatus.VALID.getStatus());
        toShowStatus.add(Constants.GrouponItemStatus.NOT_START.getStatus());
        gExample.createCriteria().andItemIdEqualTo(itemId).andStatusIn(toShowStatus).andIdEqualTo(grouponId);//.andStatusEqualTo(BizConstants.ItemApproveStatus.ON_SALE.getStatus());
        List<GrouponDO> grouponDOs = grouponDOMapper.selectByExample(gExample);
        if (null != grouponDOs && grouponDOs.size() > 0) {
            return grouponDOs.get(0);
        }
        return null;
    }

    public GrouponDO fetchGrouponDOForConfirm(long itemId) {
        GrouponDOExample gExample = new GrouponDOExample();
        gExample.createCriteria().andItemIdEqualTo(itemId).andStatusEqualTo(Constants.GrouponItemStatus.VALID.getStatus());//.andStatusIn(toShowStatus);//.andStatusEqualTo(BizConstants.ItemApproveStatus.ON_SALE.getStatus());
        List<GrouponDO> grouponDOs = grouponDOMapper.selectByExample(gExample);
        if (null != grouponDOs && grouponDOs.size() > 0) {
            return grouponDOs.get(0);
        }
        return null;
    }

    /**
     * 合并团购商品
     *
     * @param item
     * @param grouponDO
     * @return
     */
    public GroupItem combineGrouponItem(Item item, GrouponDO grouponDO) {
        GroupItem groupItem = new GroupItem();
        groupItem.setItem(item);
        if (null != grouponDO) {
            groupItem.setStatus(grouponDO.getStatus());
            groupItem.setGrouponPrice(grouponDO.getGrouponPrice());
            groupItem.setOnlineEndTime(grouponDO.getOnlineEndTime());
            groupItem.setOnlineStartTime(grouponDO.getOnlineStartTime());
            groupItem.setPurchasingPrice(grouponDO.getPurchasingPrice());
            groupItem.setQuantity(grouponDO.getQuantity());
            groupItem.setShopId(grouponDO.getShopId());
            groupItem.setReferencePrice(grouponDO.getReferencePrice());
            groupItem.setSoldQuantity(grouponDO.getSoldQuantity());
            groupItem.setTitle(grouponDO.getItem_title());
            groupItem.setType(grouponDO.getType());
        }
        return groupItem;
    }

    /**
     * 查找团购推荐商品
     *
     * @param shopId
     * @return
     */
    public BaseResult<List<GroupItem>> fetchRecommandItemsFromGroupon(long shopId) {
        BaseResult result = new BaseResult();
        result.setSuccess(false);
        Date dn = new Date();
        GrouponDOExample gExample = new GrouponDOExample();
        List<Byte> toShowGrouponStatus = new ArrayList<>();
        toShowGrouponStatus.add(Constants.GrouponItemStatus.VALID.getStatus());
        toShowGrouponStatus.add(Constants.GrouponItemStatus.NOT_START.getStatus());
        gExample.createCriteria().andShopIdEqualTo(shopId).andStatusIn(toShowGrouponStatus);
        List<GrouponDO> grouponDOs = grouponDOMapper.selectByExample(gExample);
        if (null == grouponDOs || (null != grouponDOs && grouponDOs.size() < 1)) {
            result.setCode(BizErrorEnum.NO_ITEMS_IN_RECOMMEND.getCode());
            result.setMessage(BizErrorEnum.NO_ITEMS_IN_RECOMMEND.getMsg());
            return result;
        }
        List<Long> recommendIds = new ArrayList<>();
        Map<Long, Long> toCloseGrouponIdsMap = new HashMap<>();
        Map<Long, Long> toStartGrouponIdsMap = new HashMap<>();
        Map<Long, Long> toBeStartGrouponIdsMap = new HashMap<>();

        Map<Long, GrouponDO> groupMap = new HashMap<>();

        for (GrouponDO grp : grouponDOs) {
            if (dn.getTime() > grp.getOnlineEndTime() && grp.getStatus() != Constants.GrouponItemStatus.INVALID.getStatus()) {
                //更新团购表 过期部分
                toCloseGrouponIdsMap.put(grp.getItemId(), grp.getId());
            } else {
                //尚未开团
                if (dn.getTime() < grp.getOnlineStartTime()) {
                    recommendIds.add(grp.getItemId());
                    groupMap.put(grp.getItemId(), grp);
                    toBeStartGrouponIdsMap.put(grp.getItemId(), grp.getId());
                }
                if (dn.getTime() >= grp.getOnlineStartTime() && dn.getTime() < grp.getOnlineEndTime()) {
                    groupMap.put(grp.getItemId(), grp);
                    recommendIds.add(grp.getItemId());
                }
            }
            //已开始
            if (dn.getTime() >= grp.getOnlineStartTime() && dn.getTime() < grp.getOnlineEndTime() && grp.getStatus() != Constants.GrouponItemStatus.VALID.getStatus()) {
                toStartGrouponIdsMap.put(grp.getItemId(), grp.getId());
            }
        }
        //该下架下架
        if (toCloseGrouponIdsMap.size() > 0) {
            closeGrouponItems(toCloseGrouponIdsMap);
        }
        //该上架上架
        if (toStartGrouponIdsMap.size() > 0) {
            startGrouponItems(toStartGrouponIdsMap);
        }
        //该未开团
        if (toBeStartGrouponIdsMap.size() > 0) {
            toBeStartGrouponItems(toBeStartGrouponIdsMap);
        }
        if (recommendIds.size() < 0) {
            result.setCode(BizErrorEnum.NO_ITEMS_IN_RECOMMEND.getCode());
            result.setMessage(BizErrorEnum.NO_ITEMS_IN_RECOMMEND.getMsg());
            return result;
        }

        ItemExample itemExample = new ItemExample();
        //pre check
        if (recommendIds.size() > 0) {
            itemExample.createCriteria().andIdIn(recommendIds).andShopIdEqualTo(shopId).andTypeEqualTo(Constants.TradeType.groupon.getTradeTypeId());
            List<Item> tmpItems = itemMapper.selectByExample(itemExample);
            try {
                if (null != tmpItems && tmpItems.size() > 0) {
                    //再次查找
                    itemExample.setOrderByClause("date_created ASC");
                    tmpItems = itemMapper.selectByExample(itemExample);

                    result.setSuccess(true);
                    List<GroupItem> groupItems = new ArrayList<>();
                    if (null != tmpItems && tmpItems.size() > 0) {
                        for (Item item : tmpItems) {
                            GroupItem groupItem = new GroupItem();
                            groupItem.setItem(item);
                            if (null != groupMap.get(item.getId())) {
                                groupItem.setStatus(groupMap.get(item.getId()).getStatus());
                                groupItem.setGrouponPrice(groupMap.get(item.getId()).getGrouponPrice());
                                groupItem.setOnlineEndTime(groupMap.get(item.getId()).getOnlineEndTime());
                                groupItem.setOnlineStartTime(groupMap.get(item.getId()).getOnlineStartTime());
                                groupItem.setPurchasingPrice(groupMap.get(item.getId()).getPurchasingPrice());
                                groupItem.setQuantity(groupMap.get(item.getId()).getQuantity());
                                groupItem.setShopId(groupMap.get(item.getId()).getShopId());
                                groupItem.setReferencePrice(groupMap.get(item.getId()).getReferencePrice());
                                groupItem.setSoldQuantity(groupMap.get(item.getId()).getSoldQuantity());
                                groupItem.setTitle(groupMap.get(item.getId()).getItem_title());
                                groupItem.setType(groupMap.get(item.getId()).getType());
                                groupItem.setBannerUrl(groupMap.get(item.getId()).getBannerUrl());
                                groupItem.setGrouponId(groupMap.get(item.getId()).getId());
                            }
                            groupItems.add(groupItem);
                        }
                    }
                    result.setResult(groupItems);
                    return result;
                } else {
                    result.setCode(BizErrorEnum.NO_ITEMS_IN_CATEGORY.getCode());
                    result.setMessage(BizErrorEnum.NO_ITEMS_IN_CATEGORY.getMsg());
                    return result;
                }
            } catch (Exception e) {
                log.error(e.getMessage() + ",cause:" + e.getCause());
                result.setCode(BizErrorEnum.SYSTEM_BUSY.getCode());
                result.setMessage(BizErrorEnum.SYSTEM_BUSY.getMsg());
                return result;
            }
        } else {
            result.setCode(BizErrorEnum.NO_ITEMS_IN_CATEGORY.getCode());
            result.setMessage(BizErrorEnum.NO_ITEMS_IN_CATEGORY.getMsg());
        }

        return result;
    }

    /**
     * 更新团购商品状态
     *
     * @param toCloseGrouponIdsMap
     */
    private void closeGrouponItems(Map<Long, Long> toCloseGrouponIdsMap) {
        for (Long key : toCloseGrouponIdsMap.keySet()) {
            GrouponDOExample qgExample = new GrouponDOExample();
            qgExample.createCriteria().andItemIdEqualTo(key.longValue()).andIdEqualTo(toCloseGrouponIdsMap.get(key)).andStatusNotEqualTo(Constants.GrouponItemStatus.INVALID.getStatus());
            List<GrouponDO> grouponDOs = grouponDOMapper.selectByExample(qgExample);
            if (null != grouponDOs && grouponDOs.size() > 0) {
                GrouponDO grouponDO = new GrouponDO();
                grouponDO.setStatus(Constants.GrouponItemStatus.INVALID.getStatus());
                grouponDO.setVersion(grouponDOs.get(0).getVersion() + 1l);
                GrouponDOExample gExample = new GrouponDOExample();
                gExample.createCriteria().andItemIdEqualTo(key.longValue()).andIdEqualTo(toCloseGrouponIdsMap.get(key))
                        .andVersionEqualTo(grouponDOs.get(0).getVersion());
                if (grouponDOMapper.updateByExampleSelective(grouponDO, gExample) < 1) {
                    log.warn("更新团购信息 下架团购商品失败. ");
                }
            }
        }
    }

    /**
     * 上架团购商品
     *
     * @param toStartGrouponIdsMap
     */
    private void startGrouponItems(Map<Long, Long> toStartGrouponIdsMap) {
        for (Long key : toStartGrouponIdsMap.keySet()) {
            GrouponDOExample qgExample = new GrouponDOExample();
            qgExample.createCriteria().andItemIdEqualTo(key.longValue()).andIdEqualTo(toStartGrouponIdsMap.get(key)).andStatusNotEqualTo(Constants.GrouponItemStatus.VALID.getStatus());
            List<GrouponDO> grouponDOs = grouponDOMapper.selectByExample(qgExample);
            if (null != grouponDOs && grouponDOs.size() > 0) {
                GrouponDO grouponDO = new GrouponDO();
                grouponDO.setStatus(BizConstants.ItemApproveStatus.ON_SALE.getStatus());
                grouponDO.setVersion(grouponDOs.get(0).getVersion() + 1l);
                GrouponDOExample gExample = new GrouponDOExample();
                gExample.createCriteria().andItemIdEqualTo(key.longValue()).andIdEqualTo(toStartGrouponIdsMap.get(key))
                        .andVersionEqualTo(grouponDOs.get(0).getVersion());
                if (grouponDOMapper.updateByExampleSelective(grouponDO, gExample) < 1) {
                    log.warn("更新团购信息 上架团购商品失败. ");
                }
            }
        }
    }

    private void toBeStartGrouponItems(Map<Long, Long> toBeStartGrouponIdsMap) {
        for (Long key : toBeStartGrouponIdsMap.keySet()) {
            GrouponDOExample qgExample = new GrouponDOExample();
            qgExample.createCriteria().andItemIdEqualTo(key.longValue()).andIdEqualTo(toBeStartGrouponIdsMap.get(key)).andStatusNotEqualTo(Constants.GrouponItemStatus.NOT_START.getStatus());
            List<GrouponDO> grouponDOs = grouponDOMapper.selectByExample(qgExample);
            if (null != grouponDOs && grouponDOs.size() > 0) {
                GrouponDO grouponDO = new GrouponDO();
                grouponDO.setStatus(Constants.GrouponItemStatus.NOT_START.getStatus());
                grouponDO.setVersion(grouponDOs.get(0).getVersion() + 1l);
                GrouponDOExample gExample = new GrouponDOExample();
                gExample.createCriteria().andItemIdEqualTo(key.longValue()).andIdEqualTo(toBeStartGrouponIdsMap.get(key))
                        .andVersionEqualTo(grouponDOs.get(0).getVersion());
                if (grouponDOMapper.updateByExampleSelective(grouponDO, gExample) < 1) {
                    log.warn("更新团购信息 未上架团购商品失败. ");
                }
            }
        }
    }

    /**
     * 根据id查找item
     *
     * @param itemId
     * @return
     */
    public BaseResult<Item> fetchItemById(long itemId) {
        BaseResult result = new BaseResult();
        result.setSuccess(false);
        //pre check
        List<Item> tmpItems = null;
        ItemExample iiExample = new ItemExample();
        iiExample.createCriteria().andIdEqualTo(itemId);//查询商品资料
        tmpItems = itemMapper.selectByExample(iiExample);
        if (null != tmpItems && tmpItems.size() > 0) {
            if (tmpItems.get(0).getType() != Constants.TradeType.groupon.getTradeTypeId() && tmpItems.get(0).getApproveStatus() == BizConstants.ItemApproveStatus.OFF_SALE.getStatus()) {
                result.setCode(BizErrorEnum.NO_ITEMS_IN_CATEGORY.getCode());
                result.setMessage(BizErrorEnum.NO_ITEMS_IN_CATEGORY.getMsg());
                return result;
            } else {
                result.setSuccess(true);
                result.setResult(tmpItems.get(0));
                return result;

            }
        } else {
            result.setCode(BizErrorEnum.NO_ITEMS_IN_CATEGORY.getCode());
            result.setMessage(BizErrorEnum.NO_ITEMS_IN_CATEGORY.getMsg());
            return result;
        }
    }


    /**
     * 根据商品Id获取商品
     *
     * @param itemId
     * @return
     */
    public Item fetchItemByIdIgnoreStatus(long itemId) {
        if (itemId < 0) {
            return null;
        }
        return itemMapper.selectByPrimaryKey(itemId);
    }

    /**
     * 查找商品，不在查找处更新
     *
     * @param itemId
     * @return
     */
    public BaseResult<Item> fetchItemByIdWithoutUpdate(long itemId) {
        BaseResult result = new BaseResult();
        result.setSuccess(false);
        ItemExample iiExample = new ItemExample();
        iiExample.createCriteria().andIdEqualTo(itemId);//查询商品资料
        List<Item> tmpItems = itemMapper.selectByExample(iiExample);
        if (null != tmpItems && tmpItems.size() > 0) {
            if (tmpItems.get(0).getType() != Constants.TradeType.groupon.getTradeTypeId() && tmpItems.get(0).getApproveStatus() == BizConstants.ItemApproveStatus.OFF_SALE.getStatus()) {
                result.setCode(BizErrorEnum.NO_ITEMS_IN_CATEGORY.getCode());
                result.setMessage(BizErrorEnum.NO_ITEMS_IN_CATEGORY.getMsg());
                return result;
            } else {
                result.setSuccess(true);
                result.setResult(tmpItems.get(0));
                return result;

            }
        } else {
            result.setCode(BizErrorEnum.NO_ITEMS_IN_CATEGORY.getCode());
            result.setMessage(BizErrorEnum.NO_ITEMS_IN_CATEGORY.getMsg());
            return result;
        }
    }
    
    /**
     * 查找商品，不在查找处更新
     *
     * @param itemId
     * @return
     */
    public ItemViewDO fetchItemViewDOByIdWithoutUpdateCache(Long itemId, Integer isAgency) {
        ItemViewDO tmpItemView = null;
        try {
        	tmpItemView = itemViewCache.getUnchecked(ParametersStringMaker.parametersMake(itemId, isAgency));
		} catch (Exception e) {
			// TODO: handle exception
		}
        if (null != tmpItemView) {
            return tmpItemView;
        } else {
        	ItemExample iiExample = new ItemExample();
            iiExample.createCriteria().andIdEqualTo(itemId);//查询商品资料
            List<Item> tmpItems = itemMapper.selectByExample(iiExample);
            List<ItemViewDO> tmpItemViews = combineItemTags(tmpItems);
            ItemViewDO itemViewDO = new ItemViewDO();
                
            if (null != tmpItemViews && tmpItemViews.size() > 0) {
            	itemViewDO = tmpItemViews.get(0);
            	if(BizConstants.QUERY_BROKERAGEFEE && isAgency == 1 && null != itemViewDO){		//如果是代理设置商品佣金
            		setBrokerageFeeInItemViewDO(itemViewDO);	//设置佣金
                }
            	itemViewDOSetTopic(tmpItemViews, -1L);
                return itemViewDO;
            }
            /*result.setCode(BizErrorEnum.NO_ITEMS_IN_CATEGORY.getCode());
            result.setMessage(BizErrorEnum.NO_ITEMS_IN_CATEGORY.getMsg());*/
            return null;
        }
    }

    /**
     * 上架团购商品
     *
     * @param toOnSaleItmes
     * @return
     */
    public boolean onSaleGrouponItems(List<Long> toOnSaleItmes) {
        //更新状态
        GrouponDOExample iExample = new GrouponDOExample();
        iExample.createCriteria().andItemIdIn(toOnSaleItmes);//.andIdIn(toOnSaleItmes);
        GrouponDO grouponDO = new GrouponDO();
        grouponDO.setStatus(Constants.GrouponItemStatus.VALID.getStatus());
        if (grouponDOMapper.updateByExampleSelective(grouponDO, iExample) < 1) {
            log.error("update groupon approve status ON SAILE items failed.");
            return false;
        }
        return true;
    }

    /**
     * 上架团购商品
     *
     * @param toOnSaleItme
     * @param grouponId
     * @return
     */
    public boolean onSaleGrouponItem(long toOnSaleItme, long grouponId) {
        //更新状态
        GrouponDOExample iExample = new GrouponDOExample();
        iExample.createCriteria().andItemIdEqualTo(toOnSaleItme).andIdEqualTo(grouponId);
        GrouponDO grouponDO = new GrouponDO();
        grouponDO.setStatus(Constants.GrouponItemStatus.VALID.getStatus());
        if (grouponDOMapper.updateByExampleSelective(grouponDO, iExample) < 1) {
            log.error("update groupon approve status ON SAILE items failed.");
            return false;
        }
        return true;
    }

    /**
     * online groupon item
     *
     * @param grouponDO
     * @return
     */
    public boolean onSaleGrouponItem(GrouponDO grouponDO) {
        //更新状态
        GrouponDOExample iExample = new GrouponDOExample();
        iExample.createCriteria().andItemIdEqualTo(grouponDO.getItemId()).andIdEqualTo(grouponDO.getId()).andVersionEqualTo(grouponDO.getVersion());
        GrouponDO grouponDOTmp = new GrouponDO();
        grouponDOTmp.setStatus(Constants.GrouponItemStatus.VALID.getStatus());
        grouponDOTmp.setVersion(grouponDO.getVersion() + 1l);
        if (grouponDOMapper.updateByExampleSelective(grouponDOTmp, iExample) < 1) {
            log.error("update groupon approve status ON SAILE items failed. grouponId:" + grouponDO.getId());
            return false;
        }
        return true;
    }

    /**
     * 根据商品ID获取商品
     *
     * @param itemIds
     * @return
     */
    public BaseResult<List<Item>> fetchItemsByItemIds(List<Long> itemIds) {
        BaseResult result = new BaseResult();
        result.setSuccess(false);
        ItemExample itemExample = new ItemExample();
        if (null == itemIds || (itemIds != null && itemIds.size() < 1)) {
            //result.setResult(null);
            return result;
        }
        //pre check
        itemExample.createCriteria().andIdIn(itemIds);
        List<Item> tmpItems = itemMapper.selectByExample(itemExample);
        if (null != tmpItems && tmpItems.size() > 0) {
            ItemExample iExample = new ItemExample();
            iExample.createCriteria().andIdIn(itemIds).andApproveStatusEqualTo(Constants.ApproveStatus.ON_SALE.getApproveStatusId());
            tmpItems = itemMapper.selectByExample(itemExample);
            result.setSuccess(true);
            result.setResult(tmpItems);
            return result;

        } else {
            result.setCode(BizErrorEnum.NO_ITEMS_IN_CATEGORY.getCode());
            result.setMessage(BizErrorEnum.NO_ITEMS_IN_CATEGORY.getMsg());
            return result;
        }
    }

    public List<Item> fetchItems(long shopId, Long categoryId, Long brandId, int offset, int limit, byte excludeType) {
        ShopDOExample sExample = new ShopDOExample();
        sExample.createCriteria() //
                .andSellerTypeEqualTo(Constants.SellerType.SHOP_SUPPORT.getSellerTypeId()) //
                .andShopIdEqualTo(shopId);
        ItemExample itemExample = new ItemExample();
        itemExample.setOrderByClause("weight ASC");
        if (null == categoryId || categoryId < 1) {
            //pre check
        	if (null == brandId || brandId < 1) {
        		itemExample.createCriteria() //
        		//.andShopIdEqualTo(shopId) //
        		.andShopIdNotEqualTo(999L)
        		.andApproveStatusEqualTo(BizConstants.ItemApproveStatus.ON_SALE.getStatus()) //
        		.andTypeNotEqualTo(excludeType);
        	}else{
        		itemExample.createCriteria() //
        		//.andShopIdEqualTo(shopId) //
        		.andShopIdNotEqualTo(999L)
        		.andApproveStatusEqualTo(BizConstants.ItemApproveStatus.ON_SALE.getStatus()) //
        		.andTypeNotEqualTo(excludeType)
        		.andBrandIdEqualTo(brandId);
        	}
            itemExample.setOffset(offset);
            itemExample.setLimit(limit);
        } else {
        	Criteria createCriteria = itemExample.createCriteria();
        	createCriteria //
	        	.andApproveStatusEqualTo(BizConstants.ItemApproveStatus.ON_SALE.getStatus()) //
	        	.andTypeNotEqualTo(excludeType);
        	CategoryDO categoryDO = categoryDOMapper.selectByPrimaryKey(categoryId);
        	if(null != categoryDO && null != categoryDO.getParentId() && categoryDO.getParentId() > 0){
        		createCriteria.andCategoryIdEqualTo(categoryId);
        	}else{
        		CategoryDOExample categoryDOExample = new CategoryDOExample();
        		categoryDOExample.createCriteria().andParentIdEqualTo(categoryId).andStatusEqualTo((byte)1);
        		List<CategoryDO> categoryDOList = categoryDOMapper.selectByExample(categoryDOExample);
        		if(null != categoryDOList && !categoryDOList.isEmpty()){
        			List<Long> categoryIds = new ArrayList<Long>();
        			for(CategoryDO cateDO : categoryDOList){
        				categoryIds.add(cateDO.getId());
        			}
        			createCriteria.andCategoryIdIn(categoryIds);
        		}else{
        			createCriteria.andCategoryIdEqualTo(categoryId);
        		}
        	}
        	if (null != brandId && brandId > 0) {
        		createCriteria.andBrandIdEqualTo(brandId);
        	}
        	/*if (null == brandId || brandId < 1) {
        		itemExample.createCriteria() //
        		.andShopIdEqualTo(shopId) //
        		.andApproveStatusEqualTo(BizConstants.ItemApproveStatus.ON_SALE.getStatus()) //
        		.andCategoryIdEqualTo(categoryId) //
        		.andTypeNotEqualTo(excludeType);

        	}else{
        		itemExample.createCriteria() //
        		.andShopIdEqualTo(shopId) //
        		.andApproveStatusEqualTo(BizConstants.ItemApproveStatus.ON_SALE.getStatus()) //
        		.andCategoryIdEqualTo(categoryId) //
        		.andTypeNotEqualTo(excludeType)
        		.andBrandIdEqualTo(brandId);
        	}*/
        	itemExample.setOrderByClause("weight ASC");
            itemExample.setOffset(offset);
            itemExample.setLimit(limit);
        }

        return itemMapper.selectByExample(itemExample);
    }
    
    /**
     * 
     * fetchItemsByTypes:(根据商品类型查询商品). <br/>
     * TODO(这里描述这个方法适用条件 – 可选).<br/>
     *
     * @author LuoGuangChun
     * @param shopId
     * @param categoryId
     * @param brandId
     * @param offset
     * @param limit
     * @param types
     * @return
     */
    public List<Item> fetchItemsByTypes(long shopId, Long categoryId, Long brandId, int offset, int limit, 
    		List<Byte> types, String orderByClause, Integer cateLevel) {
        ShopDOExample sExample = new ShopDOExample();
        sExample.createCriteria() //
                .andSellerTypeEqualTo(Constants.SellerType.SHOP_SUPPORT.getSellerTypeId()) //
                .andShopIdEqualTo(shopId);
        ItemExample itemExample = new ItemExample();
        if(StringUtils.isNotBlank(orderByClause)){
        	itemExample.setOrderByClause(orderByClause);
        }else{
        	itemExample.setOrderByClause("weight ASC");
        }
        Criteria itemCriteria = itemExample.createCriteria(); //
        //itemCriteria.andShopIdEqualTo(shopId); //
        itemCriteria.andShopIdNotEqualTo(999L);
		itemCriteria.andApproveStatusEqualTo(BizConstants.ItemApproveStatus.ON_SALE.getStatus()); //
		if(null != types && types.size() == 1){
			itemCriteria.andTypeEqualTo(types.get(0));
		}else if(null != types && types.size() > 1){
			itemCriteria.andTypeIn(types);
		}
		//itemCriteria.andTypeNotEqualTo(excludeType);
		if (null != brandId && brandId > -1L) {
			itemCriteria.andBrandIdEqualTo(brandId);
		}
		//商品分类查询
		if(null != cateLevel && cateLevel.equals(1) && null != categoryId && categoryId > -1L){	//第一级类目
			itemCriteria.andCategory1IdEqualTo(categoryId);
        }else if(null != cateLevel && cateLevel.equals(2) && null != categoryId && categoryId > -1L){	//第二级类目
        	itemCriteria.andCategory2IdEqualTo(categoryId);
        }else if(null != cateLevel && null != categoryId && categoryId > -1L){	//第三级类目
        	itemCriteria.andCategoryIdEqualTo(categoryId);
        }
        /*if (null != categoryId && categoryId > -1L) {
        	CategoryDO categoryDO = categoryDOMapper.selectByPrimaryKey(categoryId);
        	if(null != categoryDO && null != categoryDO.getParentId() && categoryDO.getParentId() > 0){
        		itemCriteria.andCategoryIdEqualTo(categoryId);
        	}else{
        		CategoryDOExample categoryDOExample = new CategoryDOExample();
        		categoryDOExample.createCriteria().andParentIdEqualTo(categoryId).andStatusEqualTo((byte)1);
        		List<CategoryDO> categoryDOList = categoryDOMapper.selectByExample(categoryDOExample);
        		if(null != categoryDOList && !categoryDOList.isEmpty()){
        			List<Long> categoryIds = new ArrayList<Long>();
        			for(CategoryDO cateDO : categoryDOList){
        				categoryIds.add(cateDO.getId());
        			}
        			itemCriteria.andCategoryIdIn(categoryIds);
        		}else{
        			itemCriteria.andCategoryIdEqualTo(categoryId);
        		}
        	}
        }*/
        itemExample.setOffset(offset);
        itemExample.setLimit(limit);

        return itemMapper.selectByExample(itemExample);
    }

    /**
     * 分页查找商品
     *
     * @param shopId
     * @param categoryId
     * @param limit
     * @param offset
     * @return
     */
    public BaseResult<List<Item>> fetchItemsByPageAndCateId(long shopId, Long categoryId, Long brandId, int offset, int limit, byte excludeType) {
        BaseResult result = new BaseResult();
        result.setSuccess(false);

        List<Item> tmpItems = null;
        try {
        	tmpItems = itemCache.getUnchecked(ParametersStringMaker.parametersMake(shopId, categoryId, brandId, offset, limit, excludeType));
		} catch (Exception e) {
			// TODO: handle exception
		}
        if (null != tmpItems && !tmpItems.isEmpty()) {
            result.setSuccess(true);
            result.setResult(tmpItems);
            return result;

        } else {
            tmpItems = fetchItems(shopId, categoryId, brandId, offset, limit, excludeType);
            if (tmpItems.size() > 0) {
                result.setSuccess(true);
                result.setResult(tmpItems);
                return result;
            }
            result.setCode(BizErrorEnum.NO_ITEMS_IN_CATEGORY.getCode());
            result.setMessage(BizErrorEnum.NO_ITEMS_IN_CATEGORY.getMsg());
            return result;
        }
    }
    
    /**
     * 分页查找商品view
     *
     * @param shopId
     * @param categoryId
     * @param limit
     * @param offset
     * @return
     */
    //public BaseResult<List<ItemViewDO>> fetchItemViewDOsByPageAndCateId(long shopId, Long categoryId, Long brandId, int offset, int limit, byte excludeType, int isAgency) {
    public BaseResult<List<ItemViewDO>> fetchItemViewDOsByPageAndCateId(long shopId, Long categoryId, 
    		Long brandId, int offset, int limit, List<Byte> types, int isAgency, String orderByClause, 
    		Integer cateLevel, Long topicId) {
    	BaseResult result = new BaseResult();
        result.setSuccess(false);

        List<ItemViewDO> tmpItemViews = null;
        try {
        	String typesStr = "";
        	if(null != types && types.size() > 0){
        		for(Byte type : types){
        			typesStr += type + ",";
        		}
        		typesStr = typesStr.substring(0, typesStr.length()-1);
        	}
        	tmpItemViews = itemViewListCache.getUnchecked(ParametersStringMaker.parametersMake(shopId, categoryId, brandId, offset, limit,
        			typesStr, isAgency, orderByClause, cateLevel, topicId));
		} catch (Exception e) {
			// TODO: handle exception
		}
        if (null != tmpItemViews && !tmpItemViews.isEmpty()) {
            result.setSuccess(true);
            result.setResult(tmpItemViews);
            return result;

        } else {
        	//List<Item> items = fetchItems(shopId, categoryId, brandId, offset, limit, excludeType);
        	//List<Item> items = fetchItemsByTypes(shopId, categoryId, brandId, offset, limit, types, orderByClause, cateLevel);
        	List<Item> items = null;
        	if(null != topicId && topicId > 0){
        		Map<String, Object> paramMap = new HashMap<String, Object>();
        		paramMap.put("topicId", topicId);
        		if(null != types && types.size() == 1){
        			paramMap.put("type", types.get(0));
        		}else if(null != types && types.size() > 1){
        			paramMap.put("types", types);
        		}
        		//itemCriteria.andTypeNotEqualTo(excludeType);
        		if (null != brandId && brandId > -1L) {
        			paramMap.put("brandId", brandId);
        		}
        		//商品分类查询
        		if(null != cateLevel && cateLevel.equals(1) && null != categoryId && categoryId > -1L){	//第一级类目
        			paramMap.put("category1Id", categoryId);
                }else if(null != cateLevel && cateLevel.equals(2) && null != categoryId && categoryId > -1L){	//第二级类目
                	paramMap.put("category2Id", categoryId);
                }else if(null != cateLevel && null != categoryId && categoryId > -1L){	//第三级类目
                	paramMap.put("categoryId", categoryId);
                }
        		paramMap.put("orderByClause", "i."+orderByClause);
        		paramMap.put("offset", offset);
        		paramMap.put("limit", limit);
        		items = itemMapper.selectItemByTopic(paramMap);
        	}else{
        		items = fetchItemsByTypes(shopId, categoryId, brandId, offset, limit, types, orderByClause, cateLevel);
        	}
        	
        	tmpItemViews = combineItemTags(items);
        	if(BizConstants.QUERY_BROKERAGEFEE && isAgency == 1 && null != tmpItemViews && !tmpItemViews.isEmpty()){		//如果是代理设置商品佣金
            	for(ItemViewDO itemViewDO : tmpItemViews){
            		setBrokerageFeeInItemViewDO(itemViewDO);	//设置佣金
            	}
            }
            if (null != tmpItemViews && tmpItemViews.size() > 0) {
            	itemViewDOSetTopic(tmpItemViews, -1L);	//设置专题
                result.setSuccess(true);
                result.setResult(tmpItemViews);
                return result;
            }
            result.setCode(BizErrorEnum.NO_ITEMS_IN_CATEGORY.getCode());
            result.setMessage(BizErrorEnum.NO_ITEMS_IN_CATEGORY.getMsg());
            return result;
        }
    }

    /**
     * 下架商品
     *
     * @param offSaleItems
     */
    public boolean offlineItems(List<Long> offSaleItems) {
        //更新状态
        ItemExample iExample = new ItemExample();
        iExample.createCriteria().andIdIn(offSaleItems);
        Item tItem = new Item();
        tItem.setLastUpdated(new Date());
        tItem.setApproveStatus(BizConstants.ItemApproveStatus.OFF_SALE.getStatus());
        if (itemMapper.updateByExampleSelective(tItem, iExample) < 1) {
            log.error("update item approve status failed.");
            return false;
        }
        return true;
    }

    /**
     * 下架团购商品
     *
     * @param offSaleItems
     * @return
     */
    public boolean offlineGrouponItems(List<Long> offSaleItems) {
        //更新状态
        GrouponDOExample iExample = new GrouponDOExample();
        iExample.createCriteria().andItemIdIn(offSaleItems);
        GrouponDO grouponDO = new GrouponDO();
        grouponDO.setStatus(Constants.GrouponItemStatus.INVALID.getStatus());
        if (grouponDOMapper.updateByExampleSelective(grouponDO, iExample) < 1) {
            log.error("update groupon approve status failed.");
            return false;
        }
        return true;
    }

    /**
     * 下架团购商品
     *
     * @param offSaleItem
     * @param grouponId
     * @return
     */
    public boolean offlineGrouponItem(long offSaleItem, long grouponId) {
        //更新状态
        GrouponDOExample iExample = new GrouponDOExample();
        iExample.createCriteria().andItemIdEqualTo(offSaleItem).andIdEqualTo(grouponId);
        GrouponDO grouponDO = new GrouponDO();
        grouponDO.setStatus(Constants.GrouponItemStatus.INVALID.getStatus());
        if (grouponDOMapper.updateByExampleSelective(grouponDO, iExample) < 1) {
            log.error("update groupon approve status failed.");
            return false;
        }
        return true;
    }

    public boolean offlineGrouponItem(GrouponDO grouponDO) {
        //更新状态
        GrouponDOExample iExample = new GrouponDOExample();
        iExample.createCriteria().andItemIdEqualTo(grouponDO.getItemId()).andIdEqualTo(grouponDO.getId()).andVersionEqualTo(grouponDO.getVersion());
        GrouponDO grouponDOTmp = new GrouponDO();
        grouponDOTmp.setStatus(Constants.GrouponItemStatus.INVALID.getStatus());
        grouponDOTmp.setVersion(grouponDO.getVersion() + 1l);
        if (grouponDOMapper.updateByExampleSelective(grouponDOTmp, iExample) < 1) {
            log.error("update groupon approve status failed. grouponId:" + grouponDO.getId());
            return false;
        }
        return true;
    }


    public boolean noOnsalYet(List<Long> noOnsaleYetItems) {
        //更新状态
        ItemExample iExample = new ItemExample();
        iExample.createCriteria().andIdIn(noOnsaleYetItems);
        Item tItem = new Item();
        tItem.setLastUpdated(new Date());
        tItem.setApproveStatus(BizConstants.ItemApproveStatus.OFF_SALE.getStatus());
        if (itemMapper.updateByExampleSelective(tItem, iExample) < 1) {
            log.error("update item approve status failed.");
            return false;
        }
        return true;
    }

    public boolean updateItemStatus(Item item, ItemExample itemExample) {
        try {
            if (itemMapper.updateByExampleSelective(item, itemExample) < 1) {
                log.warn("offline groupon item failed. itemId:" + item.getId());
                return false;
            }
        } catch (Exception e) {
            log.warn("offline groupon item failed. itemId:" + item.getId());
            return false;
        }
        return true;
    }

    /**
     * 获取商品标签并组装至view do
     *
     * @param items
     * @return
     */
    public List<ItemViewDO> combineItemTags(List<Item> items) {
        List<ItemViewDO> itemViewDOList = new ArrayList<>();
        Map<Long, List<TagViewDO>> tagsMap = new HashMap<>();
        List<Long> tagIds = new ArrayList<>();
        Date nowDate = new Date();

        List<Long> itemIds = new ArrayList<>();
        if (null != items && !items.isEmpty()) {
            for (Item item : items) {
                itemIds.add(item.getId());
            }
            try {
                //获取所有标签
                ObjectTaggedDOExample qtExample = new ObjectTaggedDOExample();
                qtExample.createCriteria().andArtificialIdIn(itemIds).andStatusEqualTo((byte) 1);
                List<ObjectTaggedDO> objectTaggedDOs = objectTaggedDOMapper.selectByExample(qtExample);
                if (null != objectTaggedDOs && objectTaggedDOs.size() > 0) {
                    for (ObjectTaggedDO objectTaggedDO : objectTaggedDOs) {
                        tagIds.add(objectTaggedDO.getTagId());
                    }
                    //获取本次所有标
                    List<TagViewDO> tagViewDOs = fetchTagsViewViaTagIds(tagIds);
                    Map<Long, TagViewDO> tagViewMap = new HashMap<>();
                    if (null != tagViewDOs && tagViewDOs.size() > 0) {
                        for (TagViewDO tagViewDO : tagViewDOs) {
                            tagViewMap.put(tagViewDO.getId(), tagViewDO);
                        }
                    }
                    if (tagViewMap.size() > 0) {
                        //组装tag view do
                        for (ObjectTaggedDO objectTaggedDO : objectTaggedDOs) {
                            if (null == tagsMap.get(objectTaggedDO.getArtificialId()) && null != tagViewMap.get(objectTaggedDO.getTagId())) {
                            	List<TagViewDO> tmpTagViews = new ArrayList<>();
                                TagViewDO tagViewDO = tagViewMap.get(objectTaggedDO.getTagId());
                                if (null != tagViewDO) {
                                    tagViewDO.setOkv(objectTaggedDO.getKv());
                                    tagViewDO.setType(objectTaggedDO.getType());
                                }
                                //tmpTagViews.add(tagViewMap.get(objectTaggedDO.getTagId()));
                                tmpTagViews.add(tagViewDO);
                                tagsMap.put(objectTaggedDO.getArtificialId(), tmpTagViews);
                            } else if(null != tagsMap.get(objectTaggedDO.getArtificialId()) 
                            		&& null != tagViewMap.get(objectTaggedDO.getTagId())){
                                TagViewDO tagViewDO = new TagViewDO();
                                tagViewMap.get(objectTaggedDO.getTagId());
                                tagViewDO.setBit(tagViewMap.get(objectTaggedDO.getTagId()).getBit());
                                tagViewDO.setId(tagViewMap.get(objectTaggedDO.getTagId()).getId());
                                tagViewDO.setKv(tagViewMap.get(objectTaggedDO.getTagId()).getKv());
                                tagViewDO.setName(tagViewMap.get(objectTaggedDO.getTagId()).getName());
                                tagViewDO.setOkv(objectTaggedDO.getKv());
                                tagViewDO.setPic(tagViewMap.get(objectTaggedDO.getTagId()).getPic());
                                tagViewDO.setStatus(tagViewMap.get(objectTaggedDO.getTagId()).getStatus());
                                tagViewDO.setType(tagViewMap.get(objectTaggedDO.getTagId()).getType());
                                tagViewDO.setWeight(tagViewMap.get(objectTaggedDO.getTagId()).getWeight());
                                List<TagViewDO> tmpTagViews = tagsMap.get(objectTaggedDO.getArtificialId());
                                tmpTagViews.add(tagViewDO);
                                tagsMap.put(objectTaggedDO.getArtificialId(), tmpTagViews);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error("======获取商品标记失败：" + StringUtils.join(itemIds, ","));
            }

            //组装item view do
            for (Item item : items) {
                ItemViewDO itemViewDO = ViewDOCopy.buildItemViewDO(item);
                if(null != itemViewDO && null != item.getBrandId()){		//设置品牌
                	MikuBrandDO mikuBrandDO = mikuBrandDOMapper.selectByPrimaryKey(items.get(0).getBrandId());
                	if(null != mikuBrandDO){
                		itemViewDO.setBrandName(mikuBrandDO.getName());		//设置品牌
                	}
                }
                if (null != tagsMap.get(item.getId()) && tagsMap.get(item.getId()).size() > 0) {
                    try {
                        itemViewDO.setTags(ViewDOCopy.buildNewTagViewDOs(tagsMap.get(item.getId())));
                    } catch (Exception e) {
                        log.error("======商品设置标记失败");
                    }
                }
                itemViewDOList.add(itemViewDO);
            }
            Date activtyStartTime = null, activtyEndTime = null;
            for(ItemViewDO itemViewDO : itemViewDOList){
            	itemViewDO.setHasBrokerageFee(0); //是否有佣金(0=无佣金;1=有佣金)
            	itemViewDO.setMultiple(1);	//设置默认倍数
            	itemViewDO.setActivtyStatus(-1);	//活动状态	-1=无活动;0=未开始;1=已开始；2=已结束
            	itemViewDO.setIsNeedPostFee((byte)1);	//是否包邮(0=包邮;1=不包邮)
            	List<TagViewDO> tags = itemViewDO.getTags();
            	if(null !=tags && !tags.isEmpty()){
            		for(TagViewDO tag : tags){
            			//当为抢购标时设置价格
            			if(null != tag.getBit() && tag.getBit().equals(BigInteger.valueOf(BizConstants.SearchTagEnum.PANIC_BUYING.getTag()))){
            				//ObjectTaggedDO objectTaggedDO = fetchPanicBuyingTagViaItemId(itemViewDO.getItemId(), BizConstants.SearchTagEnum.PANIC_BUYING.getTag());
            				ObjectTaggedDO objectTaggedDO = null;
            				Date now = new Date();
            	            //查询限购标记的id
            	            TagsDOExample qtExample = new TagsDOExample();
            	            qtExample.createCriteria().andBitEqualTo(BigInteger.valueOf(BizConstants.SearchTagEnum.PANIC_BUYING.getTag())).andStatusNotEqualTo((byte)0);
            	            List<TagsDO> tagsDOs = new ArrayList<>();
            	            tagsDOs = tagsDOMapper.selectByExample(qtExample);
            	            if (null != tagsDOs && tagsDOs.size() > 0) {
            	                long tagId = tagsDOs.get(0).getId();
            	                ObjectTaggedDOExample qoExample = new ObjectTaggedDOExample();
            	                qoExample.createCriteria().andArtificialIdEqualTo(itemViewDO.getItemId()).andTagIdEqualTo(tagId)
            	            		.andStatusEqualTo((byte)1);
            	                List<ObjectTaggedDO> objectTaggedDOs = objectTaggedDOMapper.selectByExample(qoExample);
            	                if (!objectTaggedDOs.isEmpty()) {
            	                	objectTaggedDO = objectTaggedDOs.get(0);
            	                }
            	            }
            				
            				if(null != objectTaggedDO && null != objectTaggedDO.getKv() && !"".equals(objectTaggedDO.getKv().trim())){
            					/*if(null != objectTaggedDO.getStartTime() && objectTaggedDO.getStartTime().getTime() <= now.getTime() 
            							&& null != objectTaggedDO.getEndTime() && objectTaggedDO.getEndTime().getTime() > now.getTime()){
            					}*/
            					ItemActivityTag itemLimitTag = JSON.parseObject(objectTaggedDO.getKv(), ItemActivityTag.class);
            					itemViewDO.setActivtyMultiple(null == itemLimitTag.getMultiple() ? 1 : itemLimitTag.getMultiple());
            					itemViewDO.setActivtyBaseSoldQuantity(null == itemLimitTag.getBaseSoldNum() ? 0 : itemLimitTag.getBaseSoldNum());
            					//itemViewDO.setRefPrice(itemViewDO.getPrice());
            					itemViewDO.setActivtyPrice(itemLimitTag.getActivityPrice());
            					itemViewDO.setActivtyItemNum(Long.valueOf(null == objectTaggedDO.getActivityNum() ? 0 : objectTaggedDO.getActivityNum()));
            					itemViewDO.setActivtySoldCnt(Long.valueOf(null == objectTaggedDO.getActivitySoldNum() ? 0 :objectTaggedDO.getActivitySoldNum()));
            					activtyStartTime = objectTaggedDO.getStartTime();
            					activtyEndTime = objectTaggedDO.getEndTime();
            					itemViewDO.setActivtyStartTime(activtyStartTime);
            					itemViewDO.setActivtyEndTime(activtyEndTime);
            					
            					itemViewDO.setActivtyStatus(0);	//活动状态	-1=无活动;0=未开始;1=已开始；2=已结束
            					if(null != activtyStartTime && nowDate.getTime() < activtyStartTime.getTime()){
            						itemViewDO.setActivtyStatus(0);	//活动状态	-1=无活动;0=未开始;1=已开始；2=已结束
            					}else if(null != activtyStartTime && nowDate.getTime() >= activtyStartTime.getTime()){
            						itemViewDO.setActivtyStatus(1);	//活动状态	-1=无活动;0=未开始;1=已开始；2=已结束
            					}
            					if(null != activtyEndTime && nowDate.getTime() > activtyEndTime.getTime()){
            						itemViewDO.setActivtyStatus(2);	//活动状态	-1=无活动;0=未开始;1=已开始；2=已结束
            					}
            					
            				}
            			}else if(null != tag.getBit() && tag.getBit().equals(BigInteger.valueOf(BizConstants.SearchTagEnum.NON_POST_FEE.getTag()))){
            				itemViewDO.setIsNeedPostFee((byte)0);	//是否包邮(0=包邮;1=不包邮)
            			}
            		}
            	}
            	if(null != itemViewDO.getPrice() && itemViewDO.getPrice() >= Constants.POST_FEE_STEP ){
            		itemViewDO.setIsNeedPostFee((byte)0);	//是否包邮(0=包邮;1=不包邮)
            	}
            }
            return itemViewDOList;
        }
        return null;
    }
    
    /**
     * 根据tag ids获取tag的view do
     *
     * @param tagIds
     * @return
     */
    public List<TagViewDO> fetchTagsViewViaTagIds(List<Long> tagIds) {
        if (null != tagIds && tagIds.size() > 0) {
            TagsDOExample qtExample = new TagsDOExample();
            qtExample.createCriteria().andIdIn(tagIds).andStatusGreaterThan((byte) 0);
            //.andStatusEqualTo((byte) 1);
            List<TagsDO> tagsDOs = tagsDOMapper.selectByExample(qtExample);
            List<TagViewDO> tagViewDOs = new ArrayList<>();
            if (null != tagsDOs && tagsDOs.size() > 0) {
                for (TagsDO tagsDO : tagsDOs) {
                    tagViewDOs.add(ViewDOCopy.buildTagViewDO(tagsDO));
                }
            }
            return tagViewDOs;
        }
        return null;
    }

    /**
     * 根据item ids 获取限购标记
     *
     * @param itemIds
     * @return
     */
    public List<ObjectTaggedDO> fetchLimitTagsViewViaItemIds(List<Long> itemIds) {
        if (null != itemIds && itemIds.size() > 0) {
            //查询限购标记的id
            TagsDOExample qtExample = new TagsDOExample();
            qtExample.createCriteria().andBitEqualTo(BigInteger.valueOf(BizConstants.SearchTagEnum.LIMIT_BUY.getTag())).andStatusNotEqualTo((byte)0);
            List<TagsDO> tagsDOs = new ArrayList<>();
            tagsDOs = tagsDOMapper.selectByExample(qtExample);
            if (null != tagsDOs && tagsDOs.size() > 0) {
                long tagId = tagsDOs.get(0).getId();
                ObjectTaggedDOExample qoExample = new ObjectTaggedDOExample();
                qoExample.createCriteria().andArtificialIdIn(itemIds).andTagIdEqualTo(tagId).andStatusEqualTo((byte) 1);
                List<ObjectTaggedDO> objectTaggedDOs = objectTaggedDOMapper.selectByExample(qoExample);
                if (null != objectTaggedDOs) {
                    return objectTaggedDOs;
                }
            }
        }
        return null;
    }

    /**
     * 根据商品Id获取特定标记
     *
     * @param itemIds
     * @param tag
     * @return
     */
    public List<ObjectTaggedDO> fetchTagObjectsViaItemIds(List<Long> itemIds, long tag) {
        if (null != itemIds && itemIds.size() > 0) {
            //查询限购标记的id
            TagsDOExample qtExample = new TagsDOExample();
            qtExample.createCriteria().andBitEqualTo(BigInteger.valueOf(tag)).andStatusNotEqualTo((byte)0);
            List<TagsDO> tagsDOs = new ArrayList<>();
            tagsDOs = tagsDOMapper.selectByExample(qtExample);
            if (null != tagsDOs && tagsDOs.size() > 0) {
                long tagId = tagsDOs.get(0).getId();
                ObjectTaggedDOExample qoExample = new ObjectTaggedDOExample();
                qoExample.createCriteria().andArtificialIdIn(itemIds).andTagIdEqualTo(tagId).andStatusEqualTo((byte) 1);
                List<ObjectTaggedDO> objectTaggedDOs = objectTaggedDOMapper.selectByExample(qoExample);
                if (null != objectTaggedDOs) {
                    return objectTaggedDOs;
                }
            }
        }
        return null;
    }
    
    /**
     * 根据商品ids 获取有开始结束时间的特定标记DO
     *
     * @param itemIds
     * @param tag
     * @return
     */
    public List<ObjectTaggedDO> fetchPanicBuyingTagViaItemIds(List<Long> itemIds, Long tag) {
        if (null != itemIds && itemIds.size() > 0) {
        	Date now = new Date();
            //查询限购标记的id
            TagsDOExample qtExample = new TagsDOExample();
            qtExample.createCriteria().andBitEqualTo(BigInteger.valueOf(tag)).andStatusNotEqualTo((byte)0);
            List<TagsDO> tagsDOs = tagsDOMapper.selectByExample(qtExample);
            if (null != tagsDOs && tagsDOs.size() > 0 && !tagsDOs.isEmpty()) {
                Long tagId = tagsDOs.get(0).getId();
                ObjectTaggedDOExample qoExample = new ObjectTaggedDOExample();
                qoExample.createCriteria().andArtificialIdIn(itemIds).andTagIdEqualTo(tagId)
                	.andStartTimeLessThanOrEqualTo(now).andEndTimeGreaterThan(now);
                List<ObjectTaggedDO> objectTaggedDOs = objectTaggedDOMapper.selectByExample(qoExample);
                if (null != objectTaggedDOs && !objectTaggedDOs.isEmpty()) {
                    return objectTaggedDOs;
                }
            }
        }
        return null;
    }

    /**
     * 获取商品特定的标记 用于判断商品是否具有特定标记，并返回该商品的objectTaged对象
     *
     * @param itemId
     * @param tag
     * @return
     */
    public ObjectTaggedDO fetchTagObjectsViaItemId(Long itemId, long tag) {
        if (null != itemId) {
            //查询限购标记的id
            TagsDOExample qtExample = new TagsDOExample();
            qtExample.createCriteria().andBitEqualTo(BigInteger.valueOf(tag)).andStatusNotEqualTo((byte)0);
            List<TagsDO> tagsDOs = new ArrayList<>();
            tagsDOs = tagsDOMapper.selectByExample(qtExample);
            if (null != tagsDOs && tagsDOs.size() > 0) {
                long tagId = tagsDOs.get(0).getId();
                ObjectTaggedDOExample qoExample = new ObjectTaggedDOExample();
                qoExample.createCriteria().andArtificialIdEqualTo(itemId).andTagIdEqualTo(tagId).andStatusEqualTo((byte) 1);
                List<ObjectTaggedDO> objectTaggedDOs = objectTaggedDOMapper.selectByExample(qoExample);
                if (null != objectTaggedDOs && objectTaggedDOs.size() > 0) {
                    return objectTaggedDOs.get(0);
                }
            }
        }
        return null;
    }
    
    /**
     * 根据商品ids 获取有开始时间和结束时间的特定标记DO
     *
     * @param itemIds
     * @param tag
     * @return
     */
    public ObjectTaggedDO fetchPanicBuyingTagViaItemId(Long itemId, Long tag) {
        if (null != itemId && itemId > 0) {
        	Date now = new Date();
            //查询限购标记的id
            TagsDOExample qtExample = new TagsDOExample();
            qtExample.createCriteria().andBitEqualTo(BigInteger.valueOf(tag)).andStatusNotEqualTo((byte)0);
            List<TagsDO> tagsDOs = new ArrayList<>();
            tagsDOs = tagsDOMapper.selectByExample(qtExample);
            if (null != tagsDOs && tagsDOs.size() > 0) {
                long tagId = tagsDOs.get(0).getId();
                ObjectTaggedDOExample qoExample = new ObjectTaggedDOExample();
                qoExample.createCriteria().andArtificialIdEqualTo(itemId).andTagIdEqualTo(tagId)
            		.andStatusEqualTo((byte)1).andStartTimeLessThanOrEqualTo(now).andEndTimeGreaterThan(now);
                List<ObjectTaggedDO> objectTaggedDOs = objectTaggedDOMapper.selectByExample(qoExample);
                if (!objectTaggedDOs.isEmpty()) {
                    return objectTaggedDOs.get(0);
                }
            }
        }
        return null;
    }

    /**
     * 获取商品能购买数量
     *
     * @param itemIds
     * @return
     */
    public List<ItemCanBuy> fetchOutLimitItems(List<Long> itemIds, long profileId, boolean daily) {
        List<ItemCanBuy> itemCanBuys = new ArrayList<>();
        //获取限购标
        List<ObjectTaggedDO> limitItemTags = fetchLimitTagsViewViaItemIds(itemIds);
        if (null != limitItemTags && limitItemTags.size() > 0) {
            //获取用户购物车中的商品
            //Map<Long, Integer> cartCount = fetchCartItemCount(itemIds, profileId);
            //获取已经购买过的数量
            Map<Long, Integer> buyedCount = fetchBuyedCount(itemIds, profileId, daily);

            for (ObjectTaggedDO itemTag : limitItemTags) {
                int limit = fetchLimitCount(itemTag);
                if (limit > 0) {
                    ItemCanBuy itemCanBuy = new ItemCanBuy();
                    int buyed = 0;
                    if (null != buyedCount.get(itemTag.getArtificialId())) {
                        buyed = buyedCount.get(itemTag.getArtificialId());
                    }
                    int cap = limit - buyed;
                    if (cap < 0) {
                        itemCanBuy.setCap(0);
                    } else {
                        itemCanBuy.setCap(cap);
                    }
                    itemCanBuy.setRealCap(cap);
                    itemCanBuy.setItemId(itemTag.getArtificialId());
                    itemCanBuys.add(itemCanBuy);
                }
            }
        }
        
        //没有打标的设置
        for (Long itemId : itemIds) {
            boolean containItem = false;
            for (ItemCanBuy itemCanBuy : itemCanBuys) {
                if (itemId.equals(itemCanBuy.getItemId())) {
                    Item item = itemMapper.selectByPrimaryKey(itemCanBuy.getItemId());
                    if (itemCanBuy.getCap() > item.getNum()) {
                        itemCanBuy.setCap(item.getNum());
                    }
                    containItem = true;
                }
            }
            //该商品没有限购标记
            if (!containItem) {
                Item item = itemMapper.selectByPrimaryKey(itemId);
                ItemCanBuy itemCanBuy = new ItemCanBuy();
                itemCanBuy.setCap(item.getNum());
                itemCanBuy.setItemId(itemId);
                itemCanBuy.setRealCap(item.getNum());
                itemCanBuys.add(itemCanBuy);
            }
        }
        
        //获取抢购活动标
        List<ObjectTaggedDO> panicBuyingItemTags =  fetchPanicBuyingTagViaItemIds(itemIds, BizConstants.SearchTagEnum.PANIC_BUYING.getTag());
        if (null != panicBuyingItemTags && panicBuyingItemTags.size() > 0) {
        	List<ItemCanBuy> activityItemCanBuys = new ArrayList<ItemCanBuy>();
        	for (ObjectTaggedDO itemTag : panicBuyingItemTags) {
                int activityNum = (null == itemTag.getActivityNum() ? 0 : itemTag.getActivityNum());
                for (ItemCanBuy itemCanBuy : itemCanBuys) {
                	if (itemCanBuy.getItemId().equals(itemTag.getArtificialId())) {
                		if (itemCanBuy.getCap() > activityNum) {
                			itemCanBuy.setCap(activityNum);
                			itemCanBuy.setRealCap(itemTag.getActivityNum());
                		}
                	}
                }
            }
        }
        
        return itemCanBuys;
    }
    
    /**
     * 获取抢购和正常商品能购买数量
     *
     * @param itemIds
     * @return
     */
    public List<ItemCanBuy> fetchOutLimitItemsNoUser(List<Long> itemIds) {
        List<ItemCanBuy> itemCanBuys = new ArrayList<>();
        //没有打标的设置
        for (Long itemId : itemIds) {
            boolean containItem = false;
            for (ItemCanBuy itemCanBuy : itemCanBuys) {
                if (itemId.equals(itemCanBuy.getItemId())) {
                    Item item = itemMapper.selectByPrimaryKey(itemCanBuy.getItemId());
                    if (itemCanBuy.getCap() > item.getNum()) {
                        itemCanBuy.setCap(item.getNum());
                    }
                    containItem = true;
                }
            }
            //该商品没有限购标记
            if (!containItem) {
                Item item = itemMapper.selectByPrimaryKey(itemId);
                ItemCanBuy itemCanBuy = new ItemCanBuy();
                if(null != item){
                	itemCanBuy.setCap(item.getNum());
                	itemCanBuy.setItemId(itemId);
                	itemCanBuy.setRealCap(item.getNum());
                	itemCanBuys.add(itemCanBuy);
                }else{
                	itemCanBuy.setCap(0);
                	itemCanBuy.setItemId(itemId);
                	itemCanBuy.setRealCap(0);
                	itemCanBuys.add(itemCanBuy);
                }
            }
        }
        
        //获取抢购活动标
        List<ObjectTaggedDO> panicBuyingItemTags =  fetchPanicBuyingTagViaItemIds(itemIds, BizConstants.SearchTagEnum.PANIC_BUYING.getTag());
        if (null != panicBuyingItemTags && panicBuyingItemTags.size() > 0) {
        	List<ItemCanBuy> activityItemCanBuys = new ArrayList<ItemCanBuy>();
        	for (ObjectTaggedDO itemTag : panicBuyingItemTags) {
                int activityNum = (null == itemTag.getActivityNum() ? 0 : itemTag.getActivityNum());
                for (ItemCanBuy itemCanBuy : itemCanBuys) {
                	if (itemCanBuy.getItemId().equals(itemTag.getArtificialId())) {
                		if (itemCanBuy.getCap() > activityNum) {
                			itemCanBuy.setCap(activityNum);
                			itemCanBuy.setRealCap(itemTag.getActivityNum());
                		}
                	}
                }
            }
        }
        
        return itemCanBuys;
    }

    /**
     * 获取限购数量
     *
     * @param itemTag
     * @return
     */
    public int fetchLimitCount(ObjectTaggedDO itemTag) {
        int limit = -1;
        if (null != itemTag && StringUtils.isNotBlank(itemTag.getKv())) {
            ItemLimitTag itemLimitTag = JSON.parseObject(itemTag.getKv(), ItemLimitTag.class);
            if (null == itemLimitTag.getXgLimitNum()) {
                limit = 1;
            } else {
                log.error("========限购商品为设置xglimitnum  itemId:" + itemTag.getArtificialId());
                limit = itemLimitTag.getXgLimitNum();
            }
        }
        return limit;
    }

    /**
     * 获取某用户购买过的商品个数记录
     *
     * @param itemIds
     * @param profileId
     * @param daily
     * @return
     */
    public Map<Long, Integer> fetchBuyedCount(List<Long> itemIds, long profileId, boolean daily) {
    	daily = true;	//用于限购查询 true=只限制当天购买数量；false=一直限制购买数量
        BaseResult<List<Item>> result = fetchItemsByItemIds(itemIds);
        List<Item> items = Lists.newArrayList();
        if (null != result && result.isSuccess()) {
            items = result.getResult();
        }
        List<Long> globalLimitItems = Lists.newArrayList();
        /*for (Item item : items) {
            if (Long.compare(item.getCategoryId(), Constants.AppointmentServiceCategory.FirstInstallActive.getCategoryId()) == 0) {
                globalLimitItems.add(item.getBaseItemId());
            }
        }*/

        Map<Long, Integer> buyedCount = new HashMap<>();
        //分站限制数
        List<InstallActiveDO> installActiveDOs = Lists.newArrayList();//installActiveDOMapper.selectByExample(qiExample);
        for (Item item : items) {
            InstallActiveDOExample qiExample = new InstallActiveDOExample();
            List<ObjectTaggedDO> objectTaggedDOs = fetchTagObjectsViaItemIds(Lists.newArrayList(item.getId()), BizConstants.SearchTagEnum.HALF_PRICE.getTag());
            if (null != objectTaggedDOs && objectTaggedDOs.size() > 0) {
                daily = true;
            } 
            /*else {
                daily = false;
            }*/
            if (daily) {
                qiExample.createCriteria().andBuyerIdEqualTo(profileId).andItemIdEqualTo(item.getId()).andDateCreatedGreaterThan(TimeUtils.long2Date(TimeUtils.getStartTime()));
            } else {
                qiExample.createCriteria().andBuyerIdEqualTo(profileId).andItemIdEqualTo(item.getId());
            }
            List<InstallActiveDO> installActiveDOList = installActiveDOMapper.selectByExample(qiExample);
            if (null != installActiveDOList && installActiveDOList.size() > 0) {
                //installActiveDOs.add(installActiveDOList.get(0));
                installActiveDOs.addAll(installActiveDOs);
            }
        }

        //全站限制数
        List<InstallActiveDO> globalInstallActiveDOs = Lists.newArrayList();
        if (globalLimitItems.size() > 0) {
            InstallActiveDOExample gqiExample = new InstallActiveDOExample();
            gqiExample.createCriteria().andBuyerIdEqualTo(profileId).andBaseItemIdIn(globalLimitItems).andDateCreatedGreaterThan(TimeUtils.long2Date(TimeUtils.getStartTime()));
            globalInstallActiveDOs = installActiveDOMapper.selectByExample(gqiExample);
        }

        if (null != installActiveDOs && installActiveDOs.size() > 0) {
            for (InstallActiveDO installActiveDO : installActiveDOs) {
                if (null == buyedCount.get(installActiveDO.getItemId())) {
                    buyedCount.put(installActiveDO.getItemId(), installActiveDO.getCount());
                } else {
                    int count = buyedCount.get(installActiveDO.getItemId()) + installActiveDO.getCount();
                    buyedCount.put(installActiveDO.getItemId(), count);
                }

            }
        }
        //总站限制处理
        Map<Long, Integer> globalBuyedCount = new HashMap<>();
        if (globalInstallActiveDOs != null && globalInstallActiveDOs.size() > 0) {
            for (InstallActiveDO installActiveDO : globalInstallActiveDOs) {
                if (null == globalBuyedCount.get(installActiveDO.getBaseItemId())) {
                    globalBuyedCount.put(installActiveDO.getBaseItemId(), installActiveDO.getCount());
                } else {
                    int count = globalBuyedCount.get(installActiveDO.getBaseItemId()) + installActiveDO.getCount();
                    globalBuyedCount.put(installActiveDO.getBaseItemId(), count);
                }
            }
        }
        if (globalInstallActiveDOs != null && globalInstallActiveDOs.size() > 0) {
            for (InstallActiveDO installActiveDO : globalInstallActiveDOs) {
                for (Long id : buyedCount.keySet()) {
                    if (Long.compare(id, installActiveDO.getItemId()) == 0) {
                        buyedCount.put(id, globalBuyedCount.get(installActiveDO.getBaseItemId()));
                    }
                }
            }
        }
        return buyedCount;
    }

    /**
     * 获取用户购物车中商品数量
     *
     * @param itemIds
     * @param profileId
     * @return
     */
    public Map<Long, Integer> fetchCartItemCount(List<Long> itemIds, long profileId) {
        Map<Long, Integer> cartCount = new HashMap<>();
        CartDOExample qcExample = new CartDOExample();
        qcExample.createCriteria().andItemIdIn(itemIds).andUserIdEqualTo(profileId);
        List<CartDO> cartDOs = cartDOMapper.selectByExample(qcExample);
        if (null != cartDOs && cartDOs.size() > 0) {
            for (CartDO cartDO : cartDOs) {
                cartCount.put(cartDO.getItemId(), cartDO.getItem_count().intValue());
            }
        }
        return cartCount;
    }

    /**
     * 根据baseItemId获取商品
     *
     * @param baseItemId
     * @param shopId
     * @return
     */
    public Item fetchSubShopItem(long baseItemId, long shopId) {
        if (baseItemId < 0) {
            return null;
        }
        ItemExample iExample = new ItemExample();
        iExample.createCriteria().andShopIdEqualTo(shopId).andBaseItemIdEqualTo(baseItemId);
        List<Item> items = itemMapper.selectByExample(iExample);
        if (null != items && items.size() > 0) {
            return items.get(0);
        }
        return null;
    }

    /**
     * 记录商品购买记录
     *
     * @param itemCounts
     * @param profileId
     */
    public void recordBuy(Map<Long, Long> itemCounts, long profileId, Long tradeId) {
        List<Long> itemIds = new ArrayList<>();
        itemIds.addAll(itemCounts.keySet());
        if (itemIds.size() > 0) {
            //获取限购标
            List<ObjectTaggedDO> limitItemTags = fetchLimitTagsViewViaItemIds(itemIds);
            if (null != limitItemTags && limitItemTags.size() > 0) {
                for (ObjectTaggedDO limitTag : limitItemTags) {
                    InstallActiveDO installActiveDO = new InstallActiveDO();
                    installActiveDO.setBuyerId(profileId);
                    if (null != itemCounts.get(limitTag.getArtificialId())) {
                        installActiveDO.setCount(itemCounts.get(limitTag.getArtificialId()).intValue());
                    }
                    installActiveDO.setItemId(limitTag.getArtificialId());
                    installActiveDO.setDateCreated(new Date());
                    installActiveDO.setLastUpdated(new Date());
                    installActiveDO.setTradeId(tradeId);
                    installActiveDO.setBaseItemId(fetchBaseItemId(limitTag.getArtificialId()));
                    if (installActiveDOMapper.insertSelective(installActiveDO) < 0) {
                        log.error("insert limit item records failed. itemId:" + limitTag.getArtificialId() + ",profileId:" + profileId);
                    }
                }
            }
        }
    }

    /**
     * 根据itemId获取baseItemId
     *
     * @param artificialId
     * @return
     */
    private Long fetchBaseItemId(Long artificialId) {
        Item item = itemMapper.selectByPrimaryKey(artificialId);
        if (null != item) {
            return item.getBaseItemId();
        }
        return null;
    }

    /**
     * 删除购买记录
     *
     * @param trade
     */
    //public void deleteBuyRecord(Trade trade) {
    public boolean deleteBuyRecord(Trade trade) {
        if (null == trade) {
            return false;
        }
        InstallActiveDOExample installActiveDOExample = new InstallActiveDOExample();
        installActiveDOExample.createCriteria().andTradeIdEqualTo(trade.getTradeId());
        if(installActiveDOMapper.deleteByExample(installActiveDOExample) < 1){
        	return false;
        }
        return true;
    }

    public Item fetchItemByBaseId(Long itemId, Long shopId) {
        ItemExample qExample = new ItemExample();
        //qExample.createCriteria().andBaseItemIdEqualTo(itemId).andShopIdEqualTo(shopId).andApproveStatusEqualTo((byte) 1);
        qExample.createCriteria().andBaseItemIdEqualTo(itemId).andApproveStatusEqualTo((byte) 1);
        List<Item> items = itemMapper.selectByExample(qExample);
        if (items != null && items.size() > 0) {
            return items.get(0);
        }
        return null;
    }

    public Item fetchItemByBaseIdIgnoreStatus(Long itemId, Long shopId) {
        ItemExample qExample = new ItemExample();
        //qExample.createCriteria().andBaseItemIdEqualTo(itemId).andShopIdEqualTo(shopId);
        qExample.createCriteria().andBaseItemIdEqualTo(itemId);
        List<Item> items = itemMapper.selectByExample(qExample);
        if (items != null && items.size() > 0) {
            return items.get(0);
        }
        return null;
    }

    public void updateStock(Trade trade) {
        if (null == trade) {
            return;
        }
        List<Order> orders = new ArrayList<Order>();
        if (trade.getOrders().length() > 0) {
            for (String id : trade.getOrders().split(";")) {
                OrderExample orderExample = new OrderExample();
                orderExample.createCriteria().andIdEqualTo(Long.valueOf(id));
                List<Order> tempOrders = orderMapper.selectByExample(orderExample);
                if (null != tempOrders && tempOrders.size() > 0) {
                    orders.add(tempOrders.get(0));
                }
            }
            for (Order order : orders) {
                //非运费订单减库存
                if (Long.compare(order.getCategoryId(), Constants.AppointmentServiceCategory.PostFeeService.getCategoryId()) != 0) {
                    Long itemId = order.getArtificialId();
                    Item item = fetchItemByIdIgnoreStatus(itemId);
                    if (null != item) {
                        ItemExample uExample = new ItemExample();
                        uExample.createCriteria().andIdEqualTo(itemId);
                        Item uItem = new Item();
                        uItem.setVersion(item.getVersion() + 1L);
                        uItem.setLastUpdated(new Date());
                        if (item.getNum() < 1) {
                            uItem.setNum(0);
                        } else {
                            if (item.getNum() - order.getNum() < 1) {
                                uItem.setNum(0);
                            } else {
                                uItem.setNum(item.getNum() - order.getNum());
                            }
                        }
                        if (item.getSoldQuantity() == null) {
                            uItem.setSoldQuantity(order.getNum());
                        } else {
                            uItem.setSoldQuantity(item.getSoldQuantity() + order.getNum());
                        }
                        if (itemMapper.updateByExampleSelective(uItem, uExample) < 1) {
                            log.error("减库存失败，itemId:" + itemId + ",num:" + order.getNum());
                        }

                    }
                }

            }
        }
    }
    
    public List<ItemTagActivtyVO> fetchItemsByPageCache(Long shopId, BigInteger bit, Long startTime, Long startTime2, 
    		Integer startRow, Integer size){
    	List<ItemTagActivtyVO> itemTagActivtyVOList = null;
    	try {
    		itemTagActivtyVOList = ItemTagActivtyVOListCache.getUnchecked(ParametersStringMaker.parametersMake(shopId, bit, startTime, startTime2, startRow, size));
		} catch (Exception e) {
			// TODO: handle exception
		}
        if (null != itemTagActivtyVOList && !itemTagActivtyVOList.isEmpty()) {
            return itemTagActivtyVOList;

        } else {
        	itemTagActivtyVOList = fetchItemTagActivtyVOList(shopId, bit, startTime, startTime2, startRow, size);
            return combineItemTagActivtyVOTags(itemTagActivtyVOList);
        }
    }
    
    public List<ItemTagActivtyVO> fetchItemTagActivtyVOList(Long shopId, BigInteger bit, Long startTime, Long startTime2, 
    		Integer startRow, Integer size){
    	Map<String,Object> paramMap = new HashMap<String, Object>();
    	if(null != shopId && shopId > 0){
    		//paramMap.put("shopId", shopId);
    	}
    	if(null != bit){
    		paramMap.put("bit", bit);		//标签类型
    	}
    	if(null != startTime && startTime > 0){
    		paramMap.put("startTime", new Date(startTime));
    	}
    	if(null != startTime2 && startTime2 > 0){
    		paramMap.put("startTime2", new Date(startTime2));
    	}
    	if(null != size){
    		paramMap.put("limit", size);
    		if(null != startRow){
    			paramMap.put("offset", startRow);
        	}
    	}
        List<ItemTagActivtyVO> itemTagActivtys = itemMapper.selectItemTagByParams(paramMap);
        Item item = null;
        for(ItemTagActivtyVO itemTagActivtyVO : itemTagActivtys){
        	ItemActivityTag itemLimitTag = JSON.parseObject(itemTagActivtyVO.getKv(), ItemActivityTag.class);
        	itemTagActivtyVO.setMultiple(itemLimitTag.getMultiple());
        	itemTagActivtyVO.setBaseSoldQuantity(itemLimitTag.getBaseSoldNum());
        	itemTagActivtyVO.setRefPrice(itemTagActivtyVO.getPrice());
        	itemTagActivtyVO.setPrice(itemLimitTag.getActivityPrice());
        	
        	item = new Item();
        	item.setFeatures(itemTagActivtyVO.getFeatures());
        	if(null != item.getFeatures()){
        		Object obj = ItemUtil.getExtMap(item);
        		String features = JSON.toJSONString(obj);
        		if (StringUtils.isNotBlank(features) && !StringUtils.equals("null", features)) {
        			Map<String, String> map = JSON.parseObject(JSON.toJSONString(obj), Map.class);
        			if (StringUtils.isNotBlank(item.getAddress())) {
        				map.put("产地", item.getAddress());
        			}
        			features = JSON.toJSONString(map);
        			itemTagActivtyVO.setFeatures(features);
        		} else {
        			if (StringUtils.isNotBlank(item.getAddress())) {
        				Map<String, String> map = new HashMap<>();
        				map.put("产地", item.getAddress());
        				features = JSON.toJSONString(map);
        				itemTagActivtyVO.setFeatures(features);
        			}
        		}
        	}
        }
    	return itemTagActivtys;
    }
    
    /**
     * 获取商品标签并组装至view do
     *
     * @param items
     * @return
     */
    public List<ItemTagActivtyVO> combineItemTagActivtyVOTags(List<ItemTagActivtyVO> items) {
        List<ItemTagActivtyVO> itemTagActivtyVOList = new ArrayList<>();
        Map<Long, List<TagViewDO>> tagsMap = new HashMap<>();
        List<Long> tagIds = new ArrayList<>();

        List<Long> itemIds = new ArrayList<>();
        if (null != items) {
            for (ItemTagActivtyVO item : items) {
                itemIds.add(item.getItemId());
            }
            try {
                //获取所有标签
                ObjectTaggedDOExample qtExample = new ObjectTaggedDOExample();
                qtExample.createCriteria().andArtificialIdIn(itemIds).andStatusEqualTo((byte) 1);
                List<ObjectTaggedDO> objectTaggedDOs = objectTaggedDOMapper.selectByExample(qtExample);
                if (null != objectTaggedDOs && objectTaggedDOs.size() > 0) {
                    for (ObjectTaggedDO objectTaggedDO : objectTaggedDOs) {
                        tagIds.add(objectTaggedDO.getTagId());
                    }
                    //获取本次所有标
                    List<TagViewDO> tagViewDOs = fetchTagsViewViaTagIds(tagIds);
                    Map<Long, TagViewDO> tagViewMap = new HashMap<>();
                    if (null != tagViewDOs && tagViewDOs.size() > 0) {
                        for (TagViewDO tagViewDO : tagViewDOs) {
                            tagViewMap.put(tagViewDO.getId(), tagViewDO);
                        }
                    }
                    if (tagViewMap.size() > 0) {
                        //组装tag view do
                        for (ObjectTaggedDO objectTaggedDO : objectTaggedDOs) {
                            if (null == tagsMap.get(objectTaggedDO.getArtificialId()) && null != tagViewMap.get(objectTaggedDO.getTagId())) {
                                List<TagViewDO> tmpTagViews = new ArrayList<>();
                                TagViewDO tagViewDO = tagViewMap.get(objectTaggedDO.getTagId());
                                if (null != tagViewDO) {
                                    tagViewDO.setOkv(objectTaggedDO.getKv());
                                    tagViewDO.setType(objectTaggedDO.getType());
                                }
                                tmpTagViews.add(tagViewMap.get(objectTaggedDO.getTagId()));
                                tagsMap.put(objectTaggedDO.getArtificialId(), tmpTagViews);
                            } else if(null != tagViewMap.get(objectTaggedDO.getTagId())){
                                TagViewDO tagViewDO = new TagViewDO();
                                tagViewMap.get(objectTaggedDO.getTagId());
                                tagViewDO.setBit(tagViewMap.get(objectTaggedDO.getTagId()).getBit());
                                tagViewDO.setId(tagViewMap.get(objectTaggedDO.getTagId()).getId());
                                tagViewDO.setKv(tagViewMap.get(objectTaggedDO.getTagId()).getKv());
                                tagViewDO.setName(tagViewMap.get(objectTaggedDO.getTagId()).getName());
                                tagViewDO.setOkv(objectTaggedDO.getKv());
                                tagViewDO.setPic(tagViewMap.get(objectTaggedDO.getTagId()).getPic());
                                tagViewDO.setStatus(tagViewMap.get(objectTaggedDO.getTagId()).getStatus());
                                tagViewDO.setType(tagViewMap.get(objectTaggedDO.getTagId()).getType());
                                tagViewDO.setWeight(tagViewMap.get(objectTaggedDO.getTagId()).getWeight());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error("======获取商品标记失败：" + StringUtils.join(itemIds, ","));
            }

            //组装item view do
            for (ItemTagActivtyVO item : items) {
                //ItemViewDO itemViewDO = ViewDOCopy.buildItemViewDO(item);
                if (null != tagsMap.get(item.getItemId()) && tagsMap.get(item.getItemId()).size() > 0) {
                    try {
                    	item.setTags(ViewDOCopy.buildNewTagViewVOs(tagsMap.get(item.getItemId())));
                    } catch (Exception e) {
                        log.error("======商品设置标记失败");
                    }
                }
                itemTagActivtyVOList.add(item);
            }
            return itemTagActivtyVOList;
        }
        return null;
    }
    
    /**
     * 
     * getMikuItemShareParaDOByItemId:(根据商品Id获取商品分润信息). <br/>
     * TODO(这里描述这个方法适用条件 – 可选).<br/>
     *
     * @author LuoGuangChun
     * @param itemId
     * @return
     * @since JDK 1.6
     */
    public MikuItemShareParaDO getMikuItemShareParaDOByItemId(Long itemId){
    	MikuItemShareParaDOExample mikuItemShareParaDOExample = new MikuItemShareParaDOExample();
        mikuItemShareParaDOExample.createCriteria().andItemIdEqualTo(itemId);
        List<MikuItemShareParaDO> mikuItemShareParaDOList = mikuItemShareParaDOMapper.selectByExample(mikuItemShareParaDOExample);
        if(!mikuItemShareParaDOList.isEmpty()){
        	MikuItemShareParaDO mikuItemShareParaDO = mikuItemShareParaDOList.get(0);
        	return mikuItemShareParaDO;
        }
        return null;
    }
    
    /**
     * 根据商品ids 获取特定标记DO
     *
     * @param itemIds
     * @param tag
     * @return
     */
    public ObjectTaggedDO fetchTagViaItemId(Long itemId, Long tag) {
        if (null != itemId && itemId > 0) {
            //查询限购标记的id
            TagsDOExample qtExample = new TagsDOExample();
            qtExample.createCriteria().andBitEqualTo(BigInteger.valueOf(tag)).andStatusNotEqualTo((byte)0);
            List<TagsDO> tagsDOs = new ArrayList<>();
            tagsDOs = tagsDOMapper.selectByExample(qtExample);
            if (null != tagsDOs && tagsDOs.size() > 0) {
                long tagId = tagsDOs.get(0).getId();
                ObjectTaggedDOExample qoExample = new ObjectTaggedDOExample();
                qoExample.createCriteria().andArtificialIdEqualTo(itemId).andTagIdEqualTo(tagId).andStatusEqualTo((byte) 1);
                List<ObjectTaggedDO> objectTaggedDOs = objectTaggedDOMapper.selectByExample(qoExample);
                if (!objectTaggedDOs.isEmpty()) {
                    return objectTaggedDOs.get(0);
                }
            }
        }
        return null;
    }
    
    /**
     * 根据商品ids 获取特定标记DO
     *
     * @param itemIds
     * @param tag
     * @return
     */
    public ObjectTaggedDO fetchTagActivtyViaItemId(Long itemId, Long tag) {
        if (null != itemId && itemId > 0) {
            //查询限购标记的id
            TagsDOExample qtExample = new TagsDOExample();
            qtExample.createCriteria().andBitEqualTo(BigInteger.valueOf(tag)).andStatusNotEqualTo((byte)0);
            List<TagsDO> tagsDOs = new ArrayList<>();
            tagsDOs = tagsDOMapper.selectByExample(qtExample);
            if (null != tagsDOs && tagsDOs.size() > 0) {
            	Date now = new Date();
                long tagId = tagsDOs.get(0).getId();
                ObjectTaggedDOExample qoExample = new ObjectTaggedDOExample();
                qoExample.createCriteria().andArtificialIdEqualTo(itemId).andTagIdEqualTo(tagId)
                	.andStatusEqualTo((byte) 1)
                	.andStartTimeLessThanOrEqualTo(now).andEndTimeGreaterThan(now);
                List<ObjectTaggedDO> objectTaggedDOs = objectTaggedDOMapper.selectByExample(qoExample);
                if (!objectTaggedDOs.isEmpty()) {
                    return objectTaggedDOs.get(0);
                }
            }
        }
        return null;
    }
    
    /**
     * 
     * setBrokerageFeeInItemViewDO:(设置佣金). <br/>
     * TODO(这里描述这个方法适用条件 – 可选).<br/>
     *
     * @author LuoGuangChun
     * @param itemViewDO
     */
    public void setBrokerageFeeInItemViewDO(ItemViewDO itemViewDO) {
		
    	itemViewDO.setBrokerageFee(0L);		//设置代理分润佣金
    	if(null == itemViewDO || (null != itemViewDO && null == itemViewDO.getBaseItemId())){
    		return;
    	}
		MikuItemShareParaDOExample mikuItemShareParaDOExample = new MikuItemShareParaDOExample();
        mikuItemShareParaDOExample.createCriteria().andItemIdEqualTo(itemViewDO.getBaseItemId());
        List<MikuItemShareParaDO> mikuItemShareParaDOList = mikuItemShareParaDOMapper.selectByExample(mikuItemShareParaDOExample);
        if(!mikuItemShareParaDOList.isEmpty()){
        	MikuItemShareParaDO mikuItemShareParaDO = mikuItemShareParaDOList.get(0);
        	if(null != mikuItemShareParaDO && !"".equals(mikuItemShareParaDO.getParameter())){
        		List<LevelVO> levelVOList = JSON.parseArray(mikuItemShareParaDO.getParameter(), LevelVO.class);
        		if(!levelVOList.isEmpty() && levelVOList.size() > 0){
        			LevelVO levelVO = null;
        			for(LevelVO vo : levelVOList){
    					if(null != vo && BizConstants.AgencyLevel.hhh.getId().equals(vo.getId())){
    						levelVO = vo;
    						break;
    					}
        			}
        			//LevelVO levelVO = levelVOList.get(8);	//获取第3级代理分润参数
        			if(null != levelVO){
        				//商品可分润金额
        				Long itemProfitFee = (null == mikuItemShareParaDO.getItemProfitFee() ? 0L : mikuItemShareParaDO.getItemProfitFee());	//商品公司利润
        				Long itemCostFee = (null == mikuItemShareParaDO.getItemCostFee() ? 0L : mikuItemShareParaDO.getItemCostFee());		//商品成本
        				Long orderCanProfit = (itemViewDO.getPrice() - (itemProfitFee + itemCostFee) );		//订单可分润金额
        				
        				//抢购标设置价格
        				//ObjectTaggedDO pbOtag = fetchPanicBuyingTagViaItemId(itemViewDO.getItemId(), BizConstants.SearchTagEnum.PANIC_BUYING.getTag());
        				ObjectTaggedDO pbOtag = fetchTagObjectsViaItemId(itemViewDO.getItemId(), BizConstants.SearchTagEnum.PANIC_BUYING.getTag());
        				if(null != pbOtag && null != pbOtag.getKv() && !"".equals(pbOtag.getKv().trim())){
        					ItemActivityTag itemLimitTag = JSON.parseObject(pbOtag.getKv(), ItemActivityTag.class);
        					if(null != pbOtag.getActivityNum() && 
        							null != itemLimitTag && null != itemLimitTag.getActivityPrice()){
        						Long activityPrice = (null == itemLimitTag.getActivityPrice() ? 0L : itemLimitTag.getActivityPrice());
        						orderCanProfit = (activityPrice - (itemProfitFee + itemCostFee) );		//订单可分润金额
        						if(orderCanProfit < 0){
        							orderCanProfit = 0L;
        						}
        					}
        				}
        				//商品可分润如果小于1块钱，则按1块钱来计算分润
        				if(null == orderCanProfit || orderCanProfit < 100){
        					orderCanProfit = 100L;
        				}
        				if(null != levelVO.getValue() && levelVO.getValue().compareTo(BigDecimal.ZERO) > 0){
        					/*Long totalShareFee = (orderCanProfit *
        							levelVO.getValue() / 100);		//获取代理等级所对应的商品分润金额 */ 
        					Long totalShareFee = (long)levelVO.getValue().multiply(new BigDecimal(orderCanProfit))
        							.divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP).doubleValue();	//获取代理等级所对应的商品分润金额
        					if(null != totalShareFee && totalShareFee > 0){
        						itemViewDO.setHasBrokerageFee(1); //是否有佣金(0=无佣金;1=有佣金)
        						itemViewDO.setBrokerageFee(totalShareFee);
        						return;
        					}
        				}
        			}
        		}
        	}
        }
        itemViewDO.setBrokerageFee(1L);	//是否有佣金(0=无佣金;1=有佣金)
	}
    
    
    public List<MikuActiveTopicDO> getMikuActiveTopicList(Long topicId) {
    	List<MikuActiveTopicDO> activeTopicDOList = null;
    	try {
    		activeTopicDOList = activeTopicCache.getUnchecked(ParametersStringMaker.parametersMake(topicId));
		} catch (Exception e) {
			MikuActiveTopicDOExample mikuActiveTopicDOExample = new MikuActiveTopicDOExample();
            mikuActiveTopicDOExample.createCriteria().andIdEqualTo(topicId);
            activeTopicDOList = mikuActiveTopicDOMapper.selectByExample(mikuActiveTopicDOExample);
		}
    	return activeTopicDOList;
    }
    
    /**
     * 通过topicId设置itemViewDOs（商品列表）的topic参数
     * @param itemViewDOs
     * @param topicId
     */
    public void itemViewDOSetTopic(List<ItemViewDO> itemViewDOs, Long topicId){
    	if(null != itemViewDOs && !itemViewDOs.isEmpty()){
    		if(null != topicId && topicId > 0){
    			MikuActiveTopicDO mikuActiveTopicDO = null;
    			List<MikuActiveTopicDO> mikuActiveTopicList = getMikuActiveTopicList(topicId);
    			if(!mikuActiveTopicList.isEmpty()){
    				mikuActiveTopicDO = mikuActiveTopicList.get(0);
    				if(null != mikuActiveTopicDO && mikuActiveTopicDO.getStatus().equals((byte)1)){
    				}else{
    					mikuActiveTopicDO = null;
    				}
    			}
    			for(ItemViewDO itemViewDO : itemViewDOs){
        			if(null != mikuActiveTopicDO){	//设置专题
        				itemViewDO.setType(Constants.TradeType.topic_cut.getTradeTypeId());
        				itemViewDO.setTopicId(mikuActiveTopicDO.getId());
        				itemViewDO.setTopicName(mikuActiveTopicDO.getName());
        				itemViewDO.setTopicParameter(mikuActiveTopicDO.getParameter());
        				itemViewDO.setTopicStartTime(mikuActiveTopicDO.getStartTime());
        				itemViewDO.setTopicEndTime(mikuActiveTopicDO.getEndTime());
        			}
        		}
    		}else if(itemViewDOs.size() > 0 && (null == topicId || topicId <= 0)){
    			for(ItemViewDO itemViewDO : itemViewDOs){
    				if(itemViewDO.getItemId() > 0L){
    					//查找专题列表
    					Map<String, Object> topicParamMap = new HashMap<String, Object>();
    					topicParamMap.put("inActive", 1);	//活动中
    					topicParamMap.put("itemId", itemViewDO.getItemId());
    					List<MikuActiveTopicVO> mikuActiveTopicVOList = itemMapper.selectTopicVOsByItemIds(topicParamMap);
    					if(!mikuActiveTopicVOList.isEmpty()){
    						MikuActiveTopicVO mikuActiveTopicVO = mikuActiveTopicVOList.get(0);
							if(null != mikuActiveTopicVO){	//设置专题
								itemViewDO.setType(Constants.TradeType.topic_cut.getTradeTypeId());
								itemViewDO.setTopicId(mikuActiveTopicVO.getId());
								itemViewDO.setTopicName(mikuActiveTopicVO.getName());
								itemViewDO.setTopicParameter(mikuActiveTopicVO.getParameter());
								itemViewDO.setTopicStartTime(mikuActiveTopicVO.getStartTime());
								itemViewDO.setTopicEndTime(mikuActiveTopicVO.getEndTime());
							}
    					}
    				}
    			}
    		}
    		
    	}
    }
    
    
    public static void main(String[] args) {
    	Date date = new Date();
    	Long times = date.getTime();
    	System.out.println("times...."+new Date(times));
    	ParametersStringMaker.parametersMake(date, date);
    	//SimpleDateFormat sdf = new SimpleDateFormat("yyyy MM dd HH mm ss"); 
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
    	Date date2 = new Date(times);
    	System.out.println("111111------------------------------------");
    	System.out.println(sdf.format(date2));
    	
    	String[] params = StringUtils.split(ParametersStringMaker.parametersMake(date, date), ParametersStringMaker.SEPARATOR);
        Date start = null;
        if(null != params[0] && !"".equals(params[0]) ){
        	//shopId = Long.parseLong(params[0]);
        	Date date3 = new Date(params[0]);
        	System.out.println("222222------------------------------------");
        	System.out.println(params[0]);
        	System.out.println(sdf.format(date3));
        }
	}
}
