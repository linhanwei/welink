package com.welink.web.resource.multishop;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.ItemViewDO;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.service.ItemService;
import com.welink.biz.service.ShopService;
import com.welink.biz.service.UserService;
import com.welink.buy.utils.BaseResult;
import com.welink.buy.utils.Constants;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.Item;
import com.welink.commons.domain.MikuActiveTopicDO;
import com.welink.commons.domain.ProfileDO;
import com.welink.commons.persistence.MikuActiveTopicDOMapper;
import com.welink.commons.persistence.ProfileDOMapper;
import com.welink.commons.tacker.EventTracker;
import com.welink.web.common.constants.ResponseMSGConstans;
import com.welink.web.common.constants.ResponseStatusEnum;

/**
 * Created by daniel on 15-4-1.
 */
@RestController
public class FetchItems {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(FetchItems.class);

    @Resource
    private ItemService itemService;

    @Resource
    private UserService userService;

    @Resource
    private ShopService shopService;
    
    @Resource
    private ProfileDOMapper profileDOMapper;
    
    @Resource
    private MikuActiveTopicDOMapper mikuActiveTopicDOMapper;
    
    /**
     * 
     * execute:(这里用一句话描述这个方法的作用). <br/>
     * TODO(这里描述这个方法适用条件 – 可选).<br/>
     * TODO(这里描述这个方法的执行流程 – 可选).<br/>
     * TODO(这里描述这个方法的使用方法 – 可选).<br/>
     * TODO(这里描述这个方法的注意事项 – 可选).<br/>
     *
     * @author LuoGuangChun
     * @param request
     * @param response
     * @param cateId
     * @param cateLevel			（1=一级；2=二级；3=三级）
     * @param orderColumn		//排序字段（1=weight权重；2=price价格；3=sold_quantity销售数量）
     * @param sortType 			排序类型（1=降序desc；2=升序asc）
     * @param brandId
     * @param itemTypes
     * @param pg
     * @param sz
     * @return
     */
    //@NeedShop
    @RequestMapping(value = {"/api/m/1.0/listItems.json", "/api/m/1.0/fetchItems.json", "/api/h/1.0/listItems.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response, 
    					@RequestParam(value="cateId", required = false, defaultValue="-2")  Long cateId,  
    					@RequestParam(value="cateLevel", required = false, defaultValue="2")  Integer cateLevel, 
    					@RequestParam(value="orderColumn", required = false, defaultValue="1")  Integer orderColumn,
    					@RequestParam(value="sortType", required = false, defaultValue="2")  Integer sortType,
    					@RequestParam(value="brandId", required = false)  Long brandId,
    					@RequestParam(value="itemTypes", required = false)  String itemTypes,
    					@RequestParam(value="topicId", required = false, defaultValue="-1")  Long topicId,
    					@RequestParam(value="pg", required = false, defaultValue = "0") Integer pg,
    		    		@RequestParam(value="sz", required = false, defaultValue = "7") Integer sz) throws Exception {
        WelinkVO welinkVO = new WelinkVO();
        String mobile = "";
        Integer isAgency = 0;
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();//boolean create
        try {
            mobile = (String) session.getAttribute("mobile");
            Long profileId = (Long) session.getAttribute("profileId");
        	if(BizConstants.QUERY_BROKERAGEFEE && profileId > 0){
        		ProfileDO profileDO = profileDOMapper.selectByPrimaryKey(profileId);
        		if(null != profileDO){
        			isAgency = (null == profileDO.getIsAgency() ? 0 :  Integer.valueOf(profileDO.getIsAgency()));
        		}
        	}
        } catch (Exception e) {
        	isAgency = 0;
            log.info("user not login");
        }
        EventTracker.track(mobile, "items", "fetch", "pre", 1L);
        int page = pg;
        int size = sz;
        if (size < 1) {
            size = 7;
        }
        if (page < 0) {
            page = 0;
        }
        int startRow = 0;
        startRow = (page) * size;
        //1.查找商品，走搜索
        long shopId = userService.fetchLastLoginShop(session);
        shopId = shopService.fetchIdByShopId(shopId);
        log.info("根据shop_id获取id:" + shopId);
        if (null == cateId || cateId <= 0) {
            cateId = null;
        }
        if (null == brandId || brandId <= 0) {
        	brandId = null;
        }
        String sortTypeStr = " ASC";	//排序类型
		if(sortType.equals(2)){	//2=升序asc
			sortTypeStr = " ASC";
		}else{	//降序desc
			sortTypeStr = " DESC";
		}
		
		String orderByClause = " weight ASC ";
		if(orderColumn.equals(2)){	//2=价格
			orderByClause = " price " + sortTypeStr;
		}else if(orderColumn.equals(3)){	//3=销售数量
			orderByClause = " sold_quantity " + sortTypeStr;
		}else{	//1=权重
			orderByClause = " weight " + sortTypeStr;
		}
        try {
            //List<Item> itemList = itemService.searchOpenSearchItems(shopId, null, cateId, brandId, startRow, size, OpenSearchType.WEIGHT_ASC.getType(), null, true);
        	List<Item> itemList = null;
        	if (null != itemList && itemList.size() > 0) {
                boolean hasNext = true;
                if (null != itemList && itemList.size() < size) {
                    hasNext = false;
                } else {
                    hasNext = true;
                }
                welinkVO.setStatus(1);
                Map resultMap = new HashMap();
                resultMap.put("threshold", BizConstants.THRESHOLD);
                List<Long> itemIds = new ArrayList<Long>();
                for(Item item : itemList){
                	itemIds.add(item.getId());
                }
                List<ItemViewDO> itemViewDOs = itemService.combineItemTags(itemList);
                resultMap.put("items", itemViewDOs);
                resultMap.put("hasNext", hasNext);
                resultMap.put("nowDate", new Date());
                welinkVO.setResult(resultMap);
                EventTracker.track(mobile, "items", "fetch", "success", 1L);
                return JSON.toJSONString(welinkVO);
            }
        } catch (Exception e) {
            log.error("do search error. exp:" + e.getMessage());
        }
        //2. 如果搜索失败，则走数据库查询查找商品
        List<ItemViewDO> items = null;
        byte excludeType = 3;
        List<Byte> itemTypesList = new ArrayList<Byte>();
        if (StringUtils.isNotBlank(itemTypes)) {
            String statuses[] = itemTypes.split(",");
            //List<Byte> tradeStatusList = new ArrayList<Byte>();
            for (String status : statuses) {
            	itemTypesList.add(Byte.valueOf(status));
            }
        }else{
        	itemTypesList.add(Constants.TradeType.fixed.getTradeTypeId());
        	//itemTypesList.add(Constants.TradeType.cod.getTradeTypeId());
        }
        //BaseResult itemResult = itemService.fetchItemsByPageAndCateId(shopId, cateId, brandId, startRow, size, excludeType);
        BaseResult<List<ItemViewDO>> itemResult = itemService.fetchItemViewDOsByPageAndCateId(shopId, cateId, brandId, startRow, size, itemTypesList, isAgency, orderByClause, cateLevel, topicId);
        if (null != itemResult && itemResult.isSuccess() && null != itemResult.getResult()) {
            items = (List<ItemViewDO>) itemResult.getResult();
        } else {
            if (itemResult != null && itemResult.getCode() == 24) {
                welinkVO.setStatus(1);
                Map resultMap = new HashMap();
                resultMap.put("hasNext", false);
                welinkVO.setResult(resultMap);
                welinkVO.setCode(BizErrorEnum.NO_MORE_ITEMS.getCode());
                welinkVO.setMsg(BizErrorEnum.NO_MORE_ITEMS.getMsg());
                return JSON.toJSONString(welinkVO);
            } else {
                welinkVO.setStatus(0);
                welinkVO.setCode(itemResult.getCode());
                welinkVO.setMsg(itemResult.getMessage());
                return JSON.toJSONString(welinkVO);
            }
        }
        Map resultMap = new HashMap();
        MikuActiveTopicDO mikuActiveTopicDO = null;
		if(null != topicId && topicId > 0){
			List<MikuActiveTopicDO> mikuActiveTopicList = itemService.getMikuActiveTopicList(topicId);
			if(null != mikuActiveTopicList && !mikuActiveTopicList.isEmpty()){
				mikuActiveTopicDO = mikuActiveTopicList.get(0);
			}
		}
		resultMap.put("topicDO", mikuActiveTopicDO);	//专题
        if (null != items && items.size() > 0) {
            boolean hasNext = true;
            if (null != items && items.size() < size) {
                hasNext = false;
            } else {
                hasNext = true;
            }
            welinkVO.setStatus(1);
            //List<ItemViewDO> itemViewDOs = itemService.combineItemTags(items);
            resultMap.put("items", items);
            resultMap.put("hasNext", hasNext);
            resultMap.put("nowDate", new Date());
            welinkVO.setResult(resultMap);
            EventTracker.track(mobile, "items", "fetch", "success", 1L);
            return JSON.toJSONString(welinkVO);
        } else {
            log.warn("fetch items  no items here .");
            welinkVO.setStatus(0);
            welinkVO.setCode(ResponseStatusEnum.FAILED.getCode());
            welinkVO.setMsg(ResponseMSGConstans.ITEM_NO_STOCK);
            return JSON.toJSONString(welinkVO);
        }
    }
}
