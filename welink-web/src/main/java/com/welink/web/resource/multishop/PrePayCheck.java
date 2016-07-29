package com.welink.web.resource.multishop;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.ItemCanBuy;
import com.welink.biz.common.model.ItemJson;
import com.welink.biz.common.model.ItemViewDO;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.common.security.NeedProfile;
import com.welink.biz.service.CartService;
import com.welink.biz.service.ItemService;
import com.welink.buy.service.AppointmentTradeService;
import com.welink.buy.utils.BaseResult;
import com.welink.buy.utils.Constants;
import com.welink.commons.domain.Item;
import com.welink.commons.domain.Order;
import com.welink.commons.domain.Trade;
import com.welink.commons.domain.TradeExample;
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
 * 支付前检查
 * Created by daniel on 15-4-6.
 */
@RestController
public class PrePayCheck {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(PrePayCheck.class);

    @Resource
    private AppointmentTradeService appointmentService;

    @Resource
    private CartService cartService;

    @Resource
    private ItemService itemService;

    @NeedProfile
    @RequestMapping(value = {"/api/m/1.0/prePayCheck.json", "/api/h/1.0/prePayCheck.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        long tradeId = ParameterUtil.getParameterAslongForSpringMVC(request, "tradeId", -1l);
        WelinkVO welinkVO = new WelinkVO();
        List<Long> itemIds = new ArrayList<>();
        List<Byte> payTypes = new ArrayList<>();
        //先去掉货到付款
        //payTypes.add(Constants.PayType.OFF_LINE.getPayTypeId());
        payTypes.add(Constants.PayType.ONLINE_ALIPAY.getPayTypeId());
        payTypes.add(Constants.PayType.ONLINE_WXPAY.getPayTypeId());
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        Long profileId = (long) session.getAttribute("profileId");

        Map resultMap = new HashMap();
        List<Long> outItems = new ArrayList<>();
        resultMap.put("degrade", true);

        //针对订单详情/订单list支付前检查
        if (tradeId > 0) {
            TradeExample tradeExample = new TradeExample();
            tradeExample.createCriteria().andTradeIdEqualTo(tradeId).andStatusEqualTo(Constants.TradeStatus.WAIT_BUYER_PAY.getTradeStatusId());
            BaseResult<List<Trade>> tradeList = appointmentService.findByExample(tradeExample);
            if (tradeList.isSuccess() && tradeList != null && tradeList.getResult() != null && tradeList.getResult().size() > 0) {
                List<Order> orders = new ArrayList<Order>();
                for (Order order : orders) {
                    if (order.getArtificialId() > 0) {
                        itemIds.add(order.getArtificialId());
                    }
                }
                List<Item> items = new ArrayList<>();
                BaseResult<List<com.welink.commons.domain.Item>> itemResult = itemService.fetchItemsByItemIds(itemIds);
                if (null != itemResult && itemResult.isSuccess() && null != itemResult.getResult()) {
                    items = itemResult.getResult();
                }
                List<ItemViewDO> itemViewDOs = itemService.combineItemTags(items);
                if (null != itemViewDOs && itemViewDOs.size() > 0) {
                    boolean containsActive = false;
                    for (ItemViewDO itemViewDO : itemViewDOs) {
                        if (Long.compare(itemViewDO.getCateId(), Constants.AppointmentServiceCategory.FirstInstallActive.getCategoryId()) == 0) {
                            containsActive = true;
                        }
                        if (itemViewDO.getItemNum() < 1) {
                            outItems.add(itemViewDO.getItemId());
                        }
                    }
                    if (containsActive) {
                        payTypes.remove(0);
                    }
                }
                resultMap.put("payTypes", payTypes);
                resultMap.put("outItems", outItems);	//已卖完的Item
                welinkVO.setStatus(1);
                welinkVO.setResult(resultMap);
                return JSON.toJSONString(welinkVO);
            } else {
                welinkVO.setStatus(0);
                welinkVO.setMsg(BizErrorEnum.TRADE_NOT_FOUND.getMsg());
                welinkVO.setCode(BizErrorEnum.TRADE_NOT_FOUND.getCode());
                return JSON.toJSONString(welinkVO);
            }
        }
        //针对购物车支付前检查 传递商品Ids
        else {
            String ids = ParameterUtil.getParameter(request, "ids");
            if (StringUtils.isBlank(ids)) {
                welinkVO.setStatus(0);
                welinkVO.setMsg(BizErrorEnum.PARAMS_ERROR.getMsg());
                welinkVO.setCode(BizErrorEnum.PARAMS_ERROR.getCode());
                return JSON.toJSONString(welinkVO);
            }
            com.alibaba.fastjson.JSONArray array = com.alibaba.fastjson.JSONArray.parseArray(ids);
            if (array.size() < 1) {
                welinkVO.setStatus(0);
                welinkVO.setMsg(BizErrorEnum.PARAMS_ERROR.getMsg());
                welinkVO.setCode(BizErrorEnum.PARAMS_ERROR.getCode());
                return JSON.toJSONString(welinkVO);
            }

            Map<Long, Integer> itemCounts = new HashMap<>();
            List<ItemJson> itemList = new ArrayList<ItemJson>();
            for (int i = 0; i < array.size(); i++) {
                com.alibaba.fastjson.JSONObject jobj = (com.alibaba.fastjson.JSONObject) array.get(i);
                ItemJson item = JSON.toJavaObject(jobj, ItemJson.class);
                itemList.add(item);
            }

            for (ItemJson itemJson : itemList) {
                long itemId = itemJson.getItem_id();
                itemIds.add(itemId);
                itemCounts.put(itemId, itemJson.getNum());
            }

            List<Item> items = new ArrayList<>();
            BaseResult<List<com.welink.commons.domain.Item>> itemResult = itemService.fetchItemsByItemIds(itemIds);
            if (null != itemResult && itemResult.isSuccess() && null != itemResult.getResult()) {
                items = itemResult.getResult();
            }
            List<ItemViewDO> itemViewDOs = itemService.combineItemTags(items);	//获取商品所对应的标签
            if (null != itemViewDOs && itemViewDOs.size() > 0) {
                boolean containsActive = false;
                for (ItemViewDO itemViewDO : itemViewDOs) {
                    if (null != itemViewDO.getCateId() && Long.compare(itemViewDO.getCateId(), Constants.AppointmentServiceCategory.FirstInstallActive.getCategoryId()) == 0) {
                        containsActive = true;
                    }
                    //if (itemViewDO.getItemId() < 1) {		//lgc认为有问题，应该是商品数量小于一
                    if (itemViewDO.getItemNum() < 1 || itemViewDO.getItemId() < 1) {		//lgc认为有问题，应该是商品数量小于一
                        outItems.add(itemViewDO.getItemId());	
                    }
                }
                if (containsActive) {
                    payTypes.remove(0);
                }
            }
            //购买前检查 - 检查限购数
            List<ItemCanBuy> outItemCanBuyes = Lists.newArrayList();
            List<ItemCanBuy> itemCanBuys = itemService.fetchOutLimitItems(itemIds, profileId, false);
            for (Long id : itemCounts.keySet()) {
                for (ItemCanBuy itemCanBuy : itemCanBuys) {
                    if (Long.compare(id, itemCanBuy.getItemId()) == 0 && itemCounts.get(id) > itemCanBuy.getCap()) {
                        outItemCanBuyes.add(itemCanBuy);
                    }
                }
            }
            if (outItemCanBuyes.size() > 0) {
                welinkVO.setStatus(0);
                welinkVO.setMsg(BizErrorEnum.ITEM_LIMIT_BUY_COUNTS.getMsg());
                welinkVO.setCode(BizErrorEnum.ITEM_LIMIT_BUY_COUNTS.getCode());
                resultMap.put("outItems", outItemCanBuyes);
                welinkVO.setResult(resultMap);
            }
            resultMap.put("payTypes", payTypes);
            resultMap.put("outItems", outItemCanBuyes);		//超出可买数量的商品
            welinkVO.setStatus(1);
            welinkVO.setResult(resultMap);
            return JSON.toJSONString(welinkVO);
        }
    }
}
