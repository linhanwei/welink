package com.welink.web.resource;

import com.alibaba.fastjson.JSON;
import com.welink.biz.common.model.ItemViewDO;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.common.security.NeedShop;
import com.welink.biz.service.ItemService;
import com.welink.biz.service.ShopService;
import com.welink.biz.util.OpenSearchType;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.Item;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Created by daniel on 15-1-13.
 */
@RestController
public class HHalfActiveSnapshot {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(HHalfActiveSnapshot.class);

    @Resource
    private ItemService itemService;

    @Resource
    private ShopService shopService;

    //@NeedShop
    @RequestMapping(value = {"/api/m/1.0/halfActiveSnapshot.json", "/api/h/1.0/halfActiveSnapshot.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();//boolean create
        /*long communityId = (long) session.getAttribute(BizConstants.SHOP_ID);
        long shopId = shopService.fetchIdByShopId(communityId);
        log.info("根据shop_id获取id:" + shopId);*/
        long communityId = -1L;
        long shopId = -1L;
        WelinkVO welinkVO = new WelinkVO();
        //已经结束的活动
       /*
        ItemAtHalfDOExample endExample = new ItemAtHalfDOExample();
        endExample.createCriteria().andEndTimeLessThan(now).andActiveStatusEqualTo(BizConstants.HalfItemStatus.ACTIVE_ENDS.getStatus()).andStatusEqualTo((byte) 1);
        endExample.setOrderByClause("start_time DESC");
        endExample.setLimit(2);
        List<ItemAtHalfDO> endItemAtHalfList = itemAtHalfDOMapper.selectByExample(endExample);
        Collections.reverse(endItemAtHalfList);
        List<ActiveSnapViewDO> endActiveList = transferActiveSnapViewDOList(endItemAtHalfList);


        //还未开始的活动
        ItemAtHalfDOExample notStartExample = new ItemAtHalfDOExample();
        notStartExample.createCriteria().andStartTimeGreaterThan(now).andActiveStatusEqualTo(BizConstants.HalfItemStatus.DONT_ANNOUNCE.getStatus()).andStatusEqualTo((byte) 1);
        notStartExample.setOrderByClause("start_time");
        List<ItemAtHalfDO> notStartItemAtHalfList = itemAtHalfDOMapper.selectByExample(notStartExample);
        List<ActiveSnapViewDO> notStartActiveList = transferActiveSnapViewDOList(notStartItemAtHalfList);
        */

        //正在进行中的活动
//        ItemAtHalfDOExample excutiveExample = new ItemAtHalfDOExample();
//        excutiveExample.createCriteria().andStartTimeLessThan(now).andEndTimeGreaterThan(now).andActiveStatusEqualTo(BizConstants.HalfItemStatus.ACTIVING_IN_STOCK.getStatus()).andStatusEqualTo((byte) 1);
//        excutiveExample.setOrderByClause("start_time");
//        List<ItemAtHalfDO> excutiveItemAtHalfList = itemAtHalfDOMapper.selectByExample(excutiveExample);
        List<ItemViewDO> itemViewDOs = fetchHalfPriceItems(shopId);

        welinkVO.setStatus(1);
        Map resultMap = new HashMap();
        //resultMap.put("endList", endActiveList);
        //resultMap.put("notStartList", notStartActiveList);
        resultMap.put("items", itemViewDOs);
        welinkVO.setResult(resultMap);
        return JSON.toJSONString(welinkVO);

    }

    /**
     * 获取特价活动商品
     *
     * @param shopId
     * @return
     */
    private List<ItemViewDO> fetchHalfPriceItems(long shopId) {
        List<Long> searchTags = new ArrayList<>();
        searchTags.add(BizConstants.SearchTagEnum.HALF_PRICE.getTag());
        List<com.welink.commons.domain.Item> items = itemService.searchOpenSearchItems(shopId, null, null, null, 0, 10, OpenSearchType.DATA_CREATED_DESC.getType(), searchTags, true);

        List<ItemViewDO> itemViewDOs = new ArrayList<>();
        if (null != items && items.size() > 0) {
            Collections.sort(items, new Comparator<Item>() {
                public int compare(Item arg0, Item arg1) {
                    return arg1.getLastUpdated().compareTo(arg0.getLastUpdated());
                }
            });
            itemViewDOs = itemService.combineItemTags(items);
        }
        return itemViewDOs;
    }
}
