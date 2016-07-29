package com.welink.web.resource.multishop;

import com.alibaba.fastjson.JSON;
import com.welink.biz.common.model.ItemViewDO;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.common.security.NeedProfile;
import com.welink.biz.common.security.NeedShop;
import com.welink.biz.service.CartService;
import com.welink.biz.service.ItemService;
import com.welink.biz.service.ShopService;
import com.welink.buy.utils.BaseResult;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.MikuActiveTopicDO;
import com.welink.commons.persistence.ItemMapper;
import com.welink.commons.tacker.EventTracker;
import com.welink.commons.vo.MikuActiveTopicVO;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 多站点支持 获取购物车商品 增加商品判断
 * Created by daniel on 15-3-26.
 */
@RestController
public class CartItems {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(CartItems.class);

    @Resource
    private ItemService itemService;

    @Resource
    private CartService cartService;

    @Resource
    private ShopService shopService;
    
    @Resource
    private ItemMapper itemMapper;
    
    @NeedProfile
    //@NeedShop
    @RequestMapping(value = {"/api/m/1.0/oCartItems.json", "/api/h/1.0/oCartItems.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        long profileId = -1;
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();//boolean create
        /*long shopId = (long) session.getAttribute(BizConstants.SHOP_ID);
        shopId = shopService.fetchIdByShopId(shopId);
        log.info("根据shop_id获取id:" + shopId);*/
        long shopId = -1L;
        String mobile = "";
        WelinkVO welinkVO = new WelinkVO();
        Map resultMap = new HashMap();
        try {
            mobile = (String) session.getAttribute("mobile");
            profileId = (long) session.getAttribute("profileId");
            Map<String, Integer> itemCountMap = new HashMap<>();
            List<com.welink.commons.domain.Item> itemsInCart = cartService.fetchCartItems(profileId, shopId);
            List<Long> itemIdsInCart = new ArrayList<>();
            if (null != itemsInCart) {
                for (com.welink.commons.domain.Item item : itemsInCart) {
                    itemIdsInCart.add(item.getId());
                    itemCountMap.put(String.valueOf(item.getId()), item.getNum());
                }
            }
            if (itemIdsInCart.size() > 0) {
                List<com.welink.commons.domain.Item> items = null;
                BaseResult<List<com.welink.commons.domain.Item>> itemResult = itemService.fetchItemsByItemIds(itemIdsInCart);
                if (null != itemResult && itemResult.isSuccess() && null != itemResult.getResult()) {
                    items = itemResult.getResult();
                }
                if (null != items) {
                    List<ItemViewDO> itemViewDOs = itemService.combineItemTags(items);
                    for (String id : itemCountMap.keySet()) {
                        for (ItemViewDO item : itemViewDOs) {
                            if (StringUtils.equals(String.valueOf(item.getItemId()), id)) {
                                item.setCartCount(itemCountMap.get(id));
                            }
                        }
                    }
                    for (ItemViewDO item : itemViewDOs) {
                    	if(null != item){
                    		Map<String, Object> paramMap = new HashMap<String, Object>();
                    		paramMap.put("itemId", item.getItemId());
                    		List<MikuActiveTopicVO> mikuActiveTopicDOList = itemMapper.selectTopicVOsByItemIds(paramMap);
                    		//给商品设置专题
                    		if(null != mikuActiveTopicDOList && !mikuActiveTopicDOList.isEmpty()){
                    			item.setTopicId(mikuActiveTopicDOList.get(0).getId());
                    			item.setTopicName(mikuActiveTopicDOList.get(0).getName());
                    			item.setTopicParameter(mikuActiveTopicDOList.get(0).getParameter());
                    			item.setTopicStartTime(mikuActiveTopicDOList.get(0).getStartTime());
                    			item.setTopicEndTime(mikuActiveTopicDOList.get(0).getEndTime());
                    		}
                    	}
                    }
                    resultMap.put("items", itemViewDOs);
                    resultMap.put("uid", profileId);
                }
            }
        } catch (Exception e) {
            log.error("fetch paras from session failed . exp:" + e.getMessage());
        }
        welinkVO.setStatus(1);
        welinkVO.setResult(resultMap);
        EventTracker.track(mobile, "cart", "cart", "success", 1L);
        return JSON.toJSONString(welinkVO);
    }
}
