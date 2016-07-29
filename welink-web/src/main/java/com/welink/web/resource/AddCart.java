package com.welink.web.resource;

import com.alibaba.fastjson.JSON;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.ItemJson;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.common.security.NeedProfile;
import com.welink.biz.service.CartService;
import com.welink.buy.utils.BaseResult;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.tacker.EventTracker;
import com.welink.web.common.constants.ResponseStatusEnum;
import com.welink.web.common.model.ResponseResult;
import com.welink.web.common.util.ParameterUtil;
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
 * Created by daniel on 14-11-17.
 */
@RestController
public class AddCart {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(AddCart.class);

    @Resource
    private CartService cartService;

    @NeedProfile
    @RequestMapping(value = {"/api/m/1.0/addCart.json", "/api/h/1.0/addCart.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        long profileId = -1;
        ResponseResult result = new ResponseResult();
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        WelinkVO welinkVO = new WelinkVO();
        EventTracker.track(BizConstants.CART_OP, "cart", "update", "pre", 1L);
        String items = ParameterUtil.getParameter(request, "items");
        if (StringUtils.isBlank(items)) {
            welinkVO.setStatus(0);
            welinkVO.setCode(BizErrorEnum.PARAMS_ERROR.getCode());
            welinkVO.setMsg(BizErrorEnum.PARAMS_ERROR.getMsg());
            return JSON.toJSONString(welinkVO);
        }
        profileId = (long) session.getAttribute("profileId");
        com.alibaba.fastjson.JSONArray array = com.alibaba.fastjson.JSONArray.parseArray(items);
        List<ItemJson> itemList = new ArrayList<ItemJson>();
        for (int i = 0; i < array.size(); i++) {
            com.alibaba.fastjson.JSONObject jobj = (com.alibaba.fastjson.JSONObject) array.get(i);
            ItemJson item = JSON.toJavaObject(jobj, ItemJson.class);
            itemList.add(item);
        }
        Map<Long, Long> itemCounts = new HashMap<>();
        for (ItemJson it : itemList) {
            itemCounts.put(it.getItem_id(), (long) it.getNum());
        }
        result.setStatus(ResponseStatusEnum.SUCCESS.getCode());
        if (itemCounts.size() < 1) {
            welinkVO.setStatus(0);
            welinkVO.setCode(BizErrorEnum.PARAMS_ERROR.getCode());
            welinkVO.setMsg(BizErrorEnum.PARAMS_ERROR.getMsg());
            return JSON.toJSONString(welinkVO);
        }
        Map resultMap = new HashMap();
        BaseResult<List<Long>> addResult = cartService.updateItem2Cart(profileId, itemCounts);
        if (!addResult.isSuccess()) {
            welinkVO.setCode(0);
            welinkVO.setCode(BizErrorEnum.ADD_CART_FAILED_ITEM_COUNT.getCode());
            welinkVO.setMsg(BizErrorEnum.ADD_CART_FAILED_ITEM_COUNT.getMsg());
            resultMap.put("failedItems", addResult.getResult());
            welinkVO.setResult(resultMap);
            return JSON.toJSONString(welinkVO);
        }
        //获取购物车中的商品种类
        int itemCount = cartService.fetchItemKindsInCart(profileId);

        resultMap.put("item_kinds", itemCount);
        result.setStatus(ResponseStatusEnum.SUCCESS.getCode());
        EventTracker.track(BizConstants.CART_OP, "cart", "update", "success", 1L);
        welinkVO.setStatus(1);
        welinkVO.setResult(resultMap);
        return JSON.toJSONString(welinkVO);
    }
}
