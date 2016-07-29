package com.welink.web.resource.multishop;

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
import com.daniel.weixin.common.util.StringUtils;
import com.google.common.collect.Lists;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.ItemViewDO;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.service.ItemService;
import com.welink.biz.service.ShopService;
import com.welink.biz.service.UserService;
import com.welink.biz.util.OpenSearchType;
import com.welink.commons.domain.Item;
import com.welink.web.common.util.ParameterUtil;

/**
 * Created by daniel on 15-4-29.
 */
@RestController
public class TagItems {
    private static org.slf4j.Logger log = LoggerFactory.getLogger(TagItems.class);

    @Resource
    private ItemService itemService;

    @Resource
    private UserService userService;

    @Resource
    private ShopService shopService;

    @RequestMapping(value = {"/api/m/1.0/tagItems.json", "/api/h/1.0/tagItems.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Long tag = ParameterUtil.getParameterAslongForSpringMVC(request, "tag", -1L);
        String q = ParameterUtil.getParameter(request, "q");
        if (StringUtils.isBlank(q)) {
            q = null;
        }

        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        long shopId = userService.fetchLastLoginShop(session);
        shopId = shopService.fetchIdByShopId(shopId);
        WelinkVO welinkVO = new WelinkVO();
        Map resultMap = new HashMap();
        if (tag < 0) {
            welinkVO.setStatus(0);
            welinkVO.setCode(BizErrorEnum.PARAMS_ERROR.getCode());
            welinkVO.setMsg(BizErrorEnum.PARAMS_ERROR.getMsg());
            return JSON.toJSONString(welinkVO);
        }
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

        List<Item> itemList = Lists.newArrayList();
        List<Long> tags = Lists.newArrayList();
        tags.add(tag);
        itemList = itemService.searchOpenSearchItems(shopId, null, null, null, startRow, size, OpenSearchType.RECOMMEND_DESC.getType(), tags, true);

        List<ItemViewDO> itemViewDOs = Lists.newArrayList();

        if (null != itemList && itemList.size() > 0) {
            itemViewDOs = itemService.combineItemTags(itemList);
        }
        boolean hasNext = true;
        if (null != itemList && itemList.size() < size) {
            hasNext = false;
        } else {
            hasNext = true;
        }
        resultMap.put("hasNext", hasNext);
        resultMap.put("items", itemViewDOs);
        welinkVO.setStatus(1);
        welinkVO.setResult(resultMap);
        return JSON.toJSONString(welinkVO);
    }
}
