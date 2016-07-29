package com.welink.web.resource.multishop;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.ItemViewDO;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.common.security.NeedShop;
import com.welink.biz.service.ItemService;
import com.welink.biz.service.ShopService;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.Item;
import com.welink.commons.tacker.EventTracker;
import com.welink.web.common.constants.ResponseStatusEnum;
import com.welink.web.common.filter.Profiler;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by daniel on 15-4-13.
 */
@RestController
public class FetchGiftItem {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(FetchGiftItem.class);

    @Resource
    private ItemService itemService;

    @Resource
    private ShopService shopService;

    //@NeedShop
    @RequestMapping(value = {"/api/m/1.0/fetchGiftItem.json", "/api/h/1.0/fetchGiftItem.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();//boolean create
        /*long communityId = (long) session.getAttribute(BizConstants.SHOP_ID);
        long shopId = shopService.fetchIdByShopId(communityId);
        log.info("根据shop_id获取id:" + shopId);*/
        long communityId = -1L;
        long shopId = -1L;
        WelinkVO welinkVO = new WelinkVO();
        long itemId = BizConstants.ACTIVE_ITEM_ID;
        Profiler.enter("fetch active item with base item id " + itemId);
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
        Profiler.release();
        Profiler.enter("query item detail");
        Item activeItem = itemService.fetchItemByBaseIdIgnoreStatus(itemId, shopId);
        if (null != activeItem) {
            List<Item> itemList = Lists.newArrayList(activeItem);
            List<ItemViewDO> itemViewDOs = itemService.combineItemTags(itemList);
            welinkVO.setStatus(1);
            Map resultMap = new HashMap();
            ItemViewDO itemViewDO = new ItemViewDO();
            if (null != itemViewDOs && itemViewDOs.size() > 0) {
                itemViewDO = itemViewDOs.get(0);
            }
            resultMap.put("item", itemViewDO);
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
