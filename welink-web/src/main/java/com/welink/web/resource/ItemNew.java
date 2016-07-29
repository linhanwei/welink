package com.welink.web.resource;

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
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.ItemViewDO;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.service.ItemService;
import com.welink.buy.utils.BaseResult;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.Item;
import com.welink.commons.domain.ProfileDO;
import com.welink.commons.persistence.MikuBrandDOMapper;
import com.welink.commons.persistence.MikuItemShareParaDOMapper;
import com.welink.commons.persistence.ProfileDOMapper;
import com.welink.commons.tacker.EventTracker;
import com.welink.web.common.constants.ResponseStatusEnum;
import com.welink.web.common.filter.Profiler;
import com.welink.web.common.util.ParameterUtil;

/**
 * Created by daniel on 15-1-14.
 */
@RestController
public class ItemNew {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(ItemNew.class);

    @Resource
    private ItemService itemService;
    
    @Resource
    private MikuBrandDOMapper mikuBrandDOMapper;
    
    @Resource
    private ProfileDOMapper profileDOMapper;
    
    @Resource
    private MikuItemShareParaDOMapper mikuItemShareParaDOMapper;

    @RequestMapping(value = {"/api/m/1.0/item2.json", "/api/h/1.0/item2.json"}, produces = "application/json;charset=utf-8")
    public String item2(HttpServletRequest request, HttpServletResponse response) throws Exception {
        WelinkVO welinkVO = new WelinkVO();
        Integer isAgency = 0;
        long itemId = ParameterUtil.getParameterAslongForSpringMVC(request, "itemId", -1L);
        Profiler.enter("fetch item with item id " + itemId);
        Profiler.enter("track item");
        EventTracker.track(BizConstants.FETCH_ITEM, "item", "fetch", "pre", 1L);
        Profiler.release();
        if (itemId < 0) {
            log.error("fetch item failed .itemId:" + itemId);
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
        	if(profileId > 0){
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
        BaseResult<com.welink.commons.domain.Item> itemBaseResult = itemService.fetchItemByIdWithoutUpdate(itemId);
        if (null != itemBaseResult && itemBaseResult.isSuccess() && null != itemBaseResult.getResult()) {
            com.welink.commons.domain.Item item = itemBaseResult.getResult();
            List<Item> items = Lists.newArrayList(item);
            List<ItemViewDO> itemViewDOs = itemService.combineItemTags(items);
            welinkVO.setStatus(1);
            Map resultMap = new HashMap();
            ItemViewDO itemViewDO = new ItemViewDO();
            if (null != itemViewDOs && itemViewDOs.size() > 0) {
                itemViewDO = itemViewDOs.get(0);
                if(isAgency == 1){		//如果是代理设置商品佣金
                	itemService.setBrokerageFeeInItemViewDO(itemViewDO);
            		/*MikuItemShareParaDOExample mikuItemShareParaDOExample = new MikuItemShareParaDOExample();
                    mikuItemShareParaDOExample.createCriteria().andItemIdEqualTo(itemViewDO.getItemId());
                    List<MikuItemShareParaDO> mikuItemShareParaDOList = mikuItemShareParaDOMapper.selectByExample(mikuItemShareParaDOExample);
                    if(mikuItemShareParaDOList.isEmpty()){
                    	MikuItemShareParaDO mikuItemShareParaDO = mikuItemShareParaDOList.get(0);
                    	if(null != mikuItemShareParaDO && !"".equals(mikuItemShareParaDO.getParameter())){
                    		List<LevelVO> levelVOList = JSON.parseArray(mikuItemShareParaDO.getParameter(), LevelVO.class);
                    		LevelVO levelVO = levelVOList.get(levelVOList.size()-1);
                    		if(null != levelVO){
                    			//商品可分润金额
                    			Long itemProfitFee = (null == mikuItemShareParaDO.getItemProfitFee() ? 0L : mikuItemShareParaDO.getItemProfitFee());	//商品公司利润
                    			Long itemCostFee = (null == mikuItemShareParaDO.getItemCostFee() ? 0L : mikuItemShareParaDO.getItemCostFee());		//商品成本
                    			Long orderCanProfit = (itemViewDO.getPrice() - (itemProfitFee + itemCostFee) );		//订单可分润金额
                    			if(null != levelVO.getValue()){
                    				Long totalShareFee = (orderCanProfit *
                    						levelVO.getValue() / 100);		//获取代理等级所对应的商品分润金额 
                    				itemViewDO.setBrokerageFee(totalShareFee);
                    			}else{
                    				itemViewDO.setBrokerageFee(0L);
                    			}
                    		}
                    	}
                    }else{
                    	itemViewDO.setBrokerageFee(0L);
                    }*/
                }
            }
            resultMap.put("item", itemViewDO);
            resultMap.put("nowDate", new Date());
            welinkVO.setResult(resultMap);
            Profiler.release();
            Profiler.enter("track item");
            return JSON.toJSONString(welinkVO);
        } else {
            welinkVO.setStatus(0);
            welinkVO.setCode(BizErrorEnum.NO_ITEMS_IN_CATEGORY.getCode());
            welinkVO.setMsg(BizErrorEnum.NO_ITEMS_IN_CATEGORY.getMsg());
            Profiler.release();
            return JSON.toJSONString(welinkVO);
        }
    }
    
    @RequestMapping(value = {"/api/m/1.0/item.json", "/api/h/1.0/item.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        WelinkVO welinkVO = new WelinkVO();
        Integer isAgency = 0;
        long itemId = ParameterUtil.getParameterAslongForSpringMVC(request, "itemId", -1L);
        Profiler.enter("fetch item with item id " + itemId);
        Profiler.enter("track item");
        EventTracker.track(BizConstants.FETCH_ITEM, "item", "fetch", "pre", 1L);
        Profiler.release();
        if (itemId < 0) {
            log.error("fetch item failed .itemId:" + itemId);
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
        	if(profileId > 0){
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
        ItemViewDO itemViewDO = itemService.fetchItemViewDOByIdWithoutUpdateCache(itemId,isAgency);
        if (null != itemViewDO) {
            welinkVO.setStatus(1);
            Map resultMap = new HashMap();
            resultMap.put("item", itemViewDO);
            resultMap.put("nowDate", new Date());
            welinkVO.setResult(resultMap);
            Profiler.release();
            Profiler.enter("track item");
            return JSON.toJSONString(welinkVO);
        } else {
            welinkVO.setStatus(0);
            welinkVO.setCode(BizErrorEnum.NO_ITEMS_IN_CATEGORY.getCode());
            welinkVO.setMsg(BizErrorEnum.NO_ITEMS_IN_CATEGORY.getMsg());
            Profiler.release();
            return JSON.toJSONString(welinkVO);
        }
    }
}