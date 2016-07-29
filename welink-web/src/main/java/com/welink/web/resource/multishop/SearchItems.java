package com.welink.web.resource.multishop;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.daniel.weixin.common.util.StringUtils;
import com.google.common.collect.Lists;
import com.welink.biz.common.model.ItemViewDO;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.service.ItemService;
import com.welink.biz.service.ShopService;
import com.welink.biz.service.UserService;
import com.welink.biz.util.OpenSearchType;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.Item;
import com.welink.commons.domain.ProfileDO;
import com.welink.commons.persistence.MikuItemShareParaDOMapper;
import com.welink.commons.persistence.ProfileDOMapper;
import com.welink.web.common.util.ParameterUtil;

/**
 * Created by daniel on 15-4-30.
 */
@RestController
public class SearchItems {
    private static org.slf4j.Logger log = LoggerFactory.getLogger(SearchItems.class);

    @Resource
    private ItemService itemService;

    @Resource
    private UserService userService;

    @Resource
    private ShopService shopService;
    
    @Resource
    private MikuItemShareParaDOMapper mikuItemShareParaDOMapper;
    
    @Resource
    private ProfileDOMapper profileDOMapper;

    /**
     * 
     * execute:(这里用一句话描述这个方法的作用). <br/>
     * TODO(这里描述这个方法适用条件 – 可选).<br/>
     *
     * @author LuoGuangChun
     * @param request
     * @param response
     * @param orderColumn		//排序字段（1=weight权重；2=price价格；3=sold_quantity销售数量）
     * @param sortType 			排序类型（1=降序desc；2=升序asc）
     * @param cateId
     * @param brandId
     * @return
     */
    @RequestMapping(value = {"/api/m/1.0/searchItems.json", "/api/h/1.0/searchItems.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response,
    		@RequestParam(value="orderColumn", required = false, defaultValue="1")  Integer orderColumn,
			@RequestParam(value="sortType", required = false, defaultValue="2")  Integer sortType,
    		@RequestParam(value="cateId", required=false) Long cateId,
    		@RequestParam(value="brandId", required = false)  Long brandId) throws Exception {
        String q = ParameterUtil.getParameter(request, "q");
        if (StringUtils.isBlank(q)) {
            q = null;
        }
        if (null == cateId || cateId <= 0) {
            cateId = null;
        }
        if (null == brandId || brandId <= 0) {
        	brandId = null;
        }

        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        Integer isAgency = 0;
        try {
            //isAgency = (Integer) session.getAttribute("isAgency");
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
        long shopId = userService.fetchLastLoginShop(session);
        shopId = shopService.fetchIdByShopId(shopId);
        WelinkVO welinkVO = new WelinkVO();
        Map resultMap = new HashMap();
        int page = ParameterUtil.getParameterAsIntForSpringMVC(request, "pg");
        int size = ParameterUtil.getParameterAsIntForSpringMVC(request, "sz");
        if (size < 1) {
            size = 7;
        }
        if (page < 0) {
            page = 0;
        }
        int startRow = 0;
        startRow = (page) * size;
        
		if(!sortType.equals(2)){	//2=升序asc
			sortType = 1;
		}
		
		int openSearchType = 10;	//权重升序
		if(orderColumn.equals(2) && sortType.equals(2)){	//2=价格升序
			openSearchType = OpenSearchType.PRICE_ASC.getType();
		}else if(orderColumn.equals(2) && sortType.equals(1)){	//2=价格降序
			openSearchType = OpenSearchType.PRICE_DESC.getType();
		}else if(orderColumn.equals(3) && sortType.equals(2)){	//3=销售升序
			openSearchType = OpenSearchType.SOLD_ASC.getType();
		}else if(orderColumn.equals(3) && sortType.equals(1)){	//3=销售降序
			openSearchType = OpenSearchType.SOLD_DESC.getType();
		}else if(orderColumn.equals(1) && sortType.equals(1)){	//1=权重降序
			openSearchType = OpenSearchType.WEIGHT_DESC.getType();
		}else{	//1=权重
			openSearchType = OpenSearchType.WEIGHT_ASC.getType();	//1=权重升序
		}

        //List<Item> itemList = itemService.searchOpenSearchItems(shopId, q, cateId, brandId, startRow, size, OpenSearchType.RECOMMEND_DESC.getType(), null, true);
		List<Item> itemList = itemService.searchOpenSearchItems(shopId, q, cateId, brandId, startRow, size, openSearchType, null, true);
		
        List<ItemViewDO> itemViewDOs = Lists.newArrayList();

        if (null != itemList && itemList.size() > 0) {
            itemViewDOs = itemService.combineItemTags(itemList);
            if(BizConstants.QUERY_BROKERAGEFEE && isAgency == 1){		//如果是代理设置商品佣金
            	for(ItemViewDO itemViewDO : itemViewDOs){
            		itemService.setBrokerageFeeInItemViewDO(itemViewDO);
            	}
            }
        }
        boolean hasNext = true;
        if (itemList == null || (null != itemList && itemList.size() < size)) {
            hasNext = false;
        } else {
            hasNext = true;
        }
        resultMap.put("nowDate", new Date());
        resultMap.put("hasNext", hasNext);
        resultMap.put("items", itemViewDOs);
        welinkVO.setStatus(1);
        welinkVO.setResult(resultMap);
        return JSON.toJSONString(welinkVO);
    }
}
