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
import com.welink.buy.utils.BaseResult;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.Item;
import com.welink.commons.domain.MikuBrandDO;
import com.welink.commons.domain.ProfileDO;
import com.welink.commons.persistence.MikuBrandDOMapper;
import com.welink.commons.persistence.ProfileDOMapper;
import com.welink.web.common.constants.ResponseStatusEnum;
import com.welink.web.common.filter.Profiler;

/**
 * 根据商品ids 获取商品信息，包含标信息
 * Created by daniel on 15-4-6.
 */
@RestController
public class FetchItemsInfo {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(FetchItemsInfo.class);

    @Resource
    private ItemService itemService;
    
    @Resource
    private ProfileDOMapper profileDOMapper;
    
    @Resource
    private MikuBrandDOMapper mikuBrandDOMapper;

    @RequestMapping(value = {"/api/m/1.0/fetchItemsInfo2.json", "/api/h/1.0/fetchItemsInfo2.json"}, produces = "application/json;charset=utf-8")
    public String execute2(HttpServletRequest request, HttpServletResponse response, @RequestParam String ids) throws Exception {
        WelinkVO welinkVO = new WelinkVO();
        Map resultMap = new HashMap();
        List<Long> itemIds = new ArrayList<>();
        if (StringUtils.isNotBlank(ids)) {
            for (String s : ids.split(";")) {
                itemIds.add(Long.valueOf(s));
            }
        }

        if (itemIds.size() > 0) {
            List<com.welink.commons.domain.Item> items = null;
            BaseResult<List<Item>> itemResult = itemService.fetchItemsByItemIds(itemIds);
            if (null != itemResult && itemResult.isSuccess() && null != itemResult.getResult()) {
                items = itemResult.getResult();
            }
            if (null != items) {
                List<ItemViewDO> itemViewDOs = itemService.combineItemTags(items);
                for(ItemViewDO itemViewDO : itemViewDOs){
                	for(Item item : items){
                		if(null != item.getBrandId() && item.getBrandId() > 0){ 
                			if(item.getId() == itemViewDO.getItemId()){
                				MikuBrandDO mikuBrandDO = mikuBrandDOMapper.selectByPrimaryKey(item.getBrandId());
                				itemViewDO.setBrandName(mikuBrandDO.getName());
                				break;
                			}
                		}
                	}
                }
                resultMap.put("items", itemViewDOs);
            }
        } else {
            welinkVO.setStatus(0);
            welinkVO.setCode(BizErrorEnum.PARAMS_ERROR.getCode());
            welinkVO.setMsg(BizErrorEnum.PARAMS_ERROR.getMsg());
        }
        welinkVO.setStatus(1);
        welinkVO.setResult(resultMap);
        return JSON.toJSONString(welinkVO);
    }
    
    @RequestMapping(value = {"/api/m/1.0/fetchItemsInfo.json", "/api/h/1.0/fetchItemsInfo.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response, @RequestParam String ids) throws Exception {
        WelinkVO welinkVO = new WelinkVO();
        Map resultMap = new HashMap();
        Integer isAgency = 0;
        List<ItemViewDO> itemViewDOs = new ArrayList<ItemViewDO>();
        List<Long> itemIds = new ArrayList<>();
        if (StringUtils.isNotBlank(ids)) {
            for (String s : ids.split(";")) {
                itemIds.add(Long.valueOf(s));
            }
        }
        if(null==itemIds || itemIds.isEmpty()){
        	welinkVO.setStatus(0);
            welinkVO.setCode(BizErrorEnum.PARAMS_ERROR.getCode());
            welinkVO.setMsg(BizErrorEnum.PARAMS_ERROR.getMsg());
            return JSON.toJSONString(welinkVO);
        }
        for(int i=0; i<itemIds.size(); i++) {
        	if (null == itemIds.get(i) || itemIds.get(i) < 0) {
                log.error("fetch item failed .itemId:" + itemIds.get(i));
                welinkVO.setStatus(0);
                welinkVO.setCode(ResponseStatusEnum.FAILED.getCode());
                welinkVO.setMsg(ResponseStatusEnum.FAILED.getMsg());
                Profiler.release();
                return JSON.toJSONString(welinkVO);
            }
            org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
            Session session = currentUser.getSession();//boolean create
            try {
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
            Profiler.release();
            Profiler.enter("query item detail");
            ItemViewDO itemViewDO = itemService.fetchItemViewDOByIdWithoutUpdateCache(itemIds.get(i),isAgency);
            if (null != itemViewDO) {
            	itemViewDOs.add(itemViewDO);
            } else {
                welinkVO.setStatus(0);
                welinkVO.setCode(BizErrorEnum.NO_ITEMS_IN_CATEGORY.getCode());
                welinkVO.setMsg(BizErrorEnum.NO_ITEMS_IN_CATEGORY.getMsg());
                Profiler.release();
                return JSON.toJSONString(welinkVO);
            }
        }
        if(!itemViewDOs.isEmpty() && itemViewDOs.size() > 0){
        	resultMap.put("items", itemViewDOs);
        	welinkVO.setStatus(1);
        }else{
        	welinkVO.setStatus(0);
            welinkVO.setCode(BizErrorEnum.NO_ITEMS_IN_CATEGORY.getCode());
            welinkVO.setMsg(BizErrorEnum.NO_ITEMS_IN_CATEGORY.getMsg());
        }
        resultMap.put("nowDate", new Date());
        welinkVO.setResult(resultMap);
        return JSON.toJSONString(welinkVO);
    }
    
}
