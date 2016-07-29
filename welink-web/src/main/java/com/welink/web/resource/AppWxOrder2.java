package com.welink.web.resource;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.ItemCanBuy;
import com.welink.biz.common.model.ItemJson;
import com.welink.biz.common.model.WelinkAgent;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.service.AddressService;
import com.welink.biz.service.CartService;
import com.welink.biz.service.ItemService;
import com.welink.biz.service.ShopService;
import com.welink.biz.util.TimeUtils;
import com.welink.biz.util.UserUtils;
import com.welink.biz.wx.tenpay.RequestHandler;
import com.welink.biz.wx.tenpay.util.ConstantUtil;
import com.welink.biz.wx.tenpay.util.Sha1Util;
import com.welink.biz.wx.tenpay.util.WXUtil;
import com.welink.buy.service.AppointmentTradeService;
import com.welink.buy.utils.BaseResult;
import com.welink.buy.utils.Constants;
import com.welink.commons.Env;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.*;
import com.welink.commons.persistence.*;
import com.welink.commons.tacker.EventTracker;
import com.welink.web.common.constants.ResponseStatusEnum;
import com.welink.web.common.filter.Profiler;
import com.welink.web.common.model.ResponseResult;
import com.welink.web.common.util.ParameterUtil;
import net.spy.memcached.MemcachedClient;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
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
public class AppWxOrder2 {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(AppWxOrder2.class);

    @Resource
    private AppointmentTradeService appointmentService;

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private ProfileDOMapper profileDOMapper;

    @Resource
    private ItemMapper itemMapper;

    @Resource
    private AddressService addressService;

    @Resource
    private TradeMapper tradeMapper;

    @Resource
    private ShopService shopService;

    @Resource
    private CartService cartService;

    @Resource
    private ItemService itemService;

    @Resource
    private Env env;

    @Resource
    private MemcachedClient memcachedClient;

    @RequestMapping(value = {"/api/m/1.0/appWxOrder2.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String from = ParameterUtil.getParameter(request, "from");
        byte shippingType = ParameterUtil.getParameterAsByteForSpringMVC(request, "sType");
        long shippingId = ParameterUtil.getParameterAslongForSpringMVC(request, "spId", -1l);
        long point = ParameterUtil.getParameterAslongForSpringMVC(request, "point", -1l);
        long couponId = ParameterUtil.getParameterAslongForSpringMVC(request, "couponId", -1l);
        Map<Long, Long> itemCounts = new HashMap<>();
        Date appointDeliveryTime = null;
        String appwxToken = "";
        ResponseResult result = new ResponseResult();
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        WelinkVO welinkVO = new WelinkVO();
        if (shippingType == 1) {
            welinkVO.setStatus(0);
            welinkVO.setCode(BizErrorEnum.SELF_PICK_NOT_SUPPORTED.getCode());
            welinkVO.setMsg(BizErrorEnum.SELF_PICK_NOT_SUPPORTED.getMsg());
            return JSON.toJSONString(welinkVO);
        }
        if (shippingId < 1) {//非自提
            if (StringUtils.isBlank(ParameterUtil.getParameter(request, BizConstants.APPOINTMENT_DELIVERY_TIME))) {
                welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
                welinkVO.setCode(BizErrorEnum.PARAMS_ERROR.getCode());
                welinkVO.setMsg(BizErrorEnum.PARAMS_ERROR.getMsg());
                return JSON.toJSONString(welinkVO);
            }
            appointDeliveryTime = ParameterUtil.getParameterAsDateTimeFromLongForSpringMVC(request, BizConstants.APPOINTMENT_DELIVERY_TIME);
        }
        long profileId = -1;
        long itemId = -1l;
        String mobile = "";
        String title = "";
        String body = "";
        long tradeId = -1;
        long sellerId = -1;
        long shopId = -1;
        long totalPrice = 0;
        try {
            if (session == null || !sessionObject(session, "profileId")) {
                result.setStatus(ResponseStatusEnum.FAILED.getCode());
                result.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
                result.setErrorCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
                welinkVO.setStatus(0);
                welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
                welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
                return JSON.toJSONString(welinkVO);
            }
            profileId = (long) session.getAttribute("profileId");
            mobile = (String) session.getAttribute("mobile");
        } catch (Exception e) {
            log.error("fetch paras from session failed . exp:" + e.getMessage() + ",sessionId:" + session.getId());
            result.setStatus(ResponseStatusEnum.FAILED.getCode());
            result.setErrorCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
            result.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
            welinkVO.setStatus(0);
            welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
            welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
            return JSON.toJSONString(welinkVO);
        }
        if (profileId == -1) {
            result.setStatus(ResponseStatusEnum.FAILED.getCode());
            result.setErrorCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
            result.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
            welinkVO.setStatus(0);
            welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
            welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
            return JSON.toJSONString(welinkVO);
        }
        EventTracker.track(mobile, "order", "order-action", "order-pre", 1L);
        String message = ParameterUtil.getParameter(request, "msg");
        //fetch user info via profile id
        ProfileDOExample profileDOExample = new ProfileDOExample();
        profileDOExample.createCriteria().andIdEqualTo(profileId).andStatusEqualTo((byte) 1);
        List<ProfileDO> profiles = profileDOMapper.selectByExample(profileDOExample);
        long consigneeId = -1l;
        long communityId = -1l;
        long logisticsId = -1l;
        if (null != profiles && profiles.size() > 0) {
            ConsigneeAddrDO consigneeAddrDO = addressService.fetchDefConsignee(profileId);
            if (shippingId > 0) {
                communityId = shippingId;
                if (consigneeAddrDO != null) {
                    logisticsId = addressService.addLogistics(consigneeAddrDO, shippingId, (byte)0);
                    if (logisticsId < 0) {
                        welinkVO.setStatus(0);
                        welinkVO.setMsg(BizErrorEnum.SAVE_TRADE_LOGISTICS_ERROR.getMsg());
                        welinkVO.setCode(BizErrorEnum.SAVE_TRADE_LOGISTICS_ERROR.getCode());
                        Profiler.release();
                        return JSON.toJSONString(welinkVO);
                    }
                } else {
                    consigneeAddrDO = new ConsigneeAddrDO();
                    consigneeAddrDO.setReceiverMobile(profiles.get(0).getMobile());
                    consigneeAddrDO.setReceiverPhone(profiles.get(0).getMobile());
                    consigneeAddrDO.setUserId(profileId);
                    consigneeAddrDO.setReceiverName(profiles.get(0).getRealName());
                    logisticsId = addressService.addLogistics(consigneeAddrDO, shippingId, (byte)0);
                    if (logisticsId < 0) {
                        welinkVO.setStatus(0);
                        welinkVO.setMsg(BizErrorEnum.SAVE_TRADE_LOGISTICS_ERROR.getMsg());
                        welinkVO.setCode(BizErrorEnum.SAVE_TRADE_LOGISTICS_ERROR.getCode());
                        Profiler.release();
                        return JSON.toJSONString(welinkVO);
                    }
                }
            } else if (consigneeAddrDO != null) {
                communityId = consigneeAddrDO.getCommunityId();
                logisticsId = addressService.addLogistics(consigneeAddrDO, shippingId, (byte)0);
                if (logisticsId < 0) {
                    welinkVO.setStatus(0);
                    welinkVO.setMsg(BizErrorEnum.SAVE_TRADE_LOGISTICS_ERROR.getMsg());
                    welinkVO.setCode(BizErrorEnum.SAVE_TRADE_LOGISTICS_ERROR.getCode());
                    Profiler.release();
                    return JSON.toJSONString(welinkVO);
                }
            } else {
                welinkVO.setStatus(0);
                welinkVO.setMsg(BizErrorEnum.NO_CONSIGNEE_ADDRESS_ERROR.getMsg());
                welinkVO.setCode(BizErrorEnum.NO_CONSIGNEE_ADDRESS_ERROR.getCode());
                return JSON.toJSONString(welinkVO);
            }
        } else {
            welinkVO.setStatus(0);
            session.setAttribute(BizConstants.PROFILE_ID, -1l);
            try {
                currentUser.logout();
            } catch (Exception e) {
                log.error("logout failed");
            }
            welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
            welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
            Profiler.release();
            return JSON.toJSONString(welinkVO);
        }

        //校验communityId
        Long cachedCommunityId = (Long) session.getAttribute(BizConstants.SHOP_ID);
        log.error("========cached c id :" + cachedCommunityId);
        log.error("======== c id :" + communityId);
        if (Long.compare(communityId, cachedCommunityId) != 0) {
            welinkVO.setStatus(0);
            welinkVO.setCode(BizErrorEnum.NO_EQUAL_COMMUNITY.getCode());
            welinkVO.setMsg(BizErrorEnum.NO_EQUAL_COMMUNITY.getMsg());
            return JSON.toJSONString(welinkVO);
        }

        BaseResult<Trade> tradeResult = null;
        List<ImmutablePair<Long, Integer>> itemIdNumPairList = new ArrayList<ImmutablePair<Long, Integer>>();
        List<ItemJson> itemList = new ArrayList<ItemJson>();

        String items = ParameterUtil.getParameter(request, "items");
        log.error("items :" + items + ",sessionId:" + session.getId());
        com.alibaba.fastjson.JSONArray array = com.alibaba.fastjson.JSONArray.parseArray(items);
        for (int i = 0; i < array.size(); i++) {
            com.alibaba.fastjson.JSONObject jobj = (com.alibaba.fastjson.JSONObject) array.get(i);
            ItemJson item = JSON.toJavaObject(jobj, ItemJson.class);
            itemList.add(item);
            itemId = item.getItem_id();
            itemCounts.put(item.getItem_id(), (long) item.getNum());
        }

        if (itemList.size() < 0) {
            result.setStatus(ResponseStatusEnum.FAILED.getCode());
            result.setErrorCode(BizErrorEnum.PARAMS_ERROR.getCode());
            result.setMsg(BizErrorEnum.PARAMS_ERROR.getMsg());
            welinkVO.setStatus(0);
            welinkVO.setMsg(BizErrorEnum.PARAMS_ERROR.getMsg());
            welinkVO.setCode(BizErrorEnum.PARAMS_ERROR.getCode());
            Profiler.release();
            return JSON.toJSONString(welinkVO);
        }

        List<Long> itemIds = Lists.newArrayList();
        for (ItemJson item : itemList) {
            long id = item.getItem_id();
            int quantity = item.getNum();
            itemIds.add(item.getItem_id());
            itemIdNumPairList.add(ImmutablePair.of(id, quantity));
        }

        if (itemId < 0) {
            result.setStatus(ResponseStatusEnum.FAILED.getCode());
            result.setErrorCode(BizErrorEnum.CAN_NOT_FOUND_ITEM.getCode());
            result.setMsg(BizErrorEnum.CAN_NOT_FOUND_ITEM.getMsg());
            welinkVO.setStatus(0);
            welinkVO.setMsg(BizErrorEnum.CAN_NOT_FOUND_ITEM.getMsg());
            welinkVO.setCode(BizErrorEnum.CAN_NOT_FOUND_ITEM.getCode());
            Profiler.release();
            return JSON.toJSONString(welinkVO);
        }
        Map resultMap = new HashMap();
        //限购检查
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
            Profiler.release();
            return JSON.toJSONString(welinkVO);
        }


        com.welink.commons.domain.Item item = itemMapper.selectByPrimaryKey(itemId);
        if (null != item) {
            sellerId = item.getSellerId();
            shopId = item.getShopId();
        }

        Constants.TradeFrom tradeFrom = UserUtils.differentiateOS(request);
        Profiler.enter("create trade transaction userId:" + profileId + " create trade");
        tradeResult = appointmentService.createNewAppointment(sellerId, shopId, communityId, profileId, -1l, itemIdNumPairList, new Date(), null, message,
                tradeFrom.getTypeId(), Constants.PayType.ONLINE_WXPAY.getPayTypeId(), logisticsId, shippingType, point, couponId, appointDeliveryTime, null, null, null, null, null);
        Profiler.release();
        //query new trade
        if (tradeResult.isSuccess() && tradeResult.getResult() != null) {
            long tid = tradeResult.getResult().getId();
            Trade trade = tradeMapper.selectByPrimaryKey(tid);
            if (null != trade) {
                //记录购买记录
                itemService.recordBuy(itemCounts, profileId, tradeResult.getResult().getTradeId());

                if (StringUtils.equals(from, "cart")) {
                    //清除购物车--购物车已支持部分购买，所以这里是更新购物车数据
                    Map<Long, Long> toUpdateItemCounts = new HashMap<>();
                    for (Long id : itemCounts.keySet()) {
                        toUpdateItemCounts.put(id, 0 - itemCounts.get(id));
                    }
                    cartService.updateItem2Cart(profileId, toUpdateItemCounts);
                }
                tradeId = trade.getTradeId();
                totalPrice = trade.getTotalFee();
                title = trade.getTitle();
                List<Order> orders = new ArrayList<Order>();
                Profiler.enter("fetch orders of trade ." + profileId + " orders");
                if (trade.getOrders().length() > 0) {
                    for (String id : trade.getOrders().split(";")) {
                        OrderExample orderExample = new OrderExample();
                        orderExample.createCriteria().andIdEqualTo(Long.valueOf(id));
                        List<Order> tmpOrders = new ArrayList<Order>();
                        tmpOrders = orderMapper.selectByExample(orderExample);
                        if (null != tmpOrders && tmpOrders.size() > 0) {
                            orders.add(tmpOrders.get(0));
                            body += tmpOrders.get(0).getTitle() + "-" + tmpOrders.get(0).getId() + "-";
                        }
                    }
                }
                Profiler.release();
            } else {
                result.setStatus(ResponseStatusEnum.FAILED.getCode());
                result.setErrorCode(BizErrorEnum.CREATE_TRADE_FAILED.getCode());
                result.setMsg(BizErrorEnum.CREATE_TRADE_FAILED.getMsg());
                welinkVO.setStatus(0);
                welinkVO.setMsg(BizErrorEnum.CREATE_TRADE_FAILED.getMsg());
                welinkVO.setCode(BizErrorEnum.CREATE_TRADE_FAILED.getCode());
                return JSON.toJSONString(welinkVO);
            }
        } else {
            result.setStatus(ResponseStatusEnum.FAILED.getCode());
            result.setMsg(tradeResult.getMessage());
            result.setErrorCode(tradeResult.getCode());
            log.error("create trade failed. itemId:" + itemId + ",profileId:" + profileId + ",sessionId:" + session.getId());
            welinkVO.setStatus(0);
            welinkVO.setMsg(tradeResult.getMessage());
            welinkVO.setCode(tradeResult.getCode());
            return JSON.toJSONString(welinkVO);
        }
        //生成支付相关 获取店铺支付宝账户
        ShopDO shop = shopService.fetchShopByShopId(shopId);
        if (null == shop) {
            result.setErrorCode(BizErrorEnum.COMMUNITY_ALIPAY_NOT_CONFIGURATED.getCode());
            result.setMsg(BizErrorEnum.COMMUNITY_ALIPAY_NOT_CONFIGURATED.getMsg());
            result.setStatus(ResponseStatusEnum.FAILED.getCode());
            welinkVO.setStatus(0);
            welinkVO.setMsg(BizErrorEnum.COMMUNITY_ALIPAY_NOT_CONFIGURATED.getMsg());
            welinkVO.setCode(BizErrorEnum.COMMUNITY_ALIPAY_NOT_CONFIGURATED.getCode());
            return JSON.toJSONString(welinkVO);
        }
        //微信支付参数
        //接收财付通通知的URL TODO:更换成自己的回调地址
        String notify_url = "http://m.7nb.com.cn/api/m/1.0/weiXinCallBack.htm";
        if (env.isDev()) {
        	notify_url = "http://m." + BizConstants.ONLINE_DOMAIN + "/api/m/1.0/weiXinCallBack.htm";
        } else {
            notify_url = "http://m." + BizConstants.ONLINE_DOMAIN + "/api/m/1.0/weiXinCallBack.htm";
        }
        RequestHandler prepayReqHandler = new RequestHandler();//获取prepayid的请求类

        //IOS和IOSPRE的参数不同默认设置为来自IOS
        String appId = ConstantUtil.APP_APP_ID;
        String appSecret = ConstantUtil.APP_APP_SECRET;
        String partnerId = ConstantUtil.PARTNER_ID;
        String partnerKey = ConstantUtil.PARTNER_KEY;
        String appKey = ConstantUtil.APPKEY;
        appwxToken = BizConstants.APP_TOKEN;
        WelinkAgent welinkAgent = new WelinkAgent();
        String userAgent = request.getHeader(BizConstants.USER_AGENT);
        try {
            if (org.apache.commons.lang.StringUtils.isNotBlank(userAgent)) {
                welinkAgent = JSON.parseObject(userAgent, WelinkAgent.class);
            }
        } catch (Exception e) {
            log.error("record client diploma failed. profileId:" + profileId + ",sessionId:" + session.getId());
        }

        //获取token值
        String token = "";
        if (StringUtils.isNotBlank((String) memcachedClient.get(appwxToken))) {
            token = (String) memcachedClient.get(appwxToken);
        } else {
            token = prepayReqHandler.GetToken(appId, appSecret);
            if (StringUtils.isNotBlank(token)) {
                memcachedClient.set(appwxToken, TimeUtils.JS_API_TICKET_TIMEOUT, token);
            }
        }
        String noncestr = WXUtil.getNonceStr();
        String timestamp = WXUtil.getTimeStamp();

        log.info("获取token------值 " + token + ",sessionId:" + session.getId());

        if (StringUtils.isNotBlank(token)) {
            //设置package订单参数
            SortedMap<Object, Object> packageParams = new TreeMap<Object, Object>();
            packageParams.put("bank_type", "WX"); //商品描述
            packageParams.put("body", title); //商品描述
            packageParams.put("notify_url", notify_url); //接收财付通通知的URL
            packageParams.put("partner", partnerId); //商户号
            packageParams.put("out_trade_no", String.valueOf(tradeId)); //商家订单号
            packageParams.put("total_fee", String.valueOf(totalPrice)); //商品金额,以分为单位
            packageParams.put("spbill_create_ip", request.getRemoteAddr()); //订单生成的机器IP，指用户浏览器端IP
            packageParams.put("fee_type", "1"); //币种，1人民币   66
            packageParams.put("input_charset", "UTF-8"); //字符编码

            //获取package包
            String packageValue = prepayReqHandler.genPackage(packageParams, partnerKey);

            String traceid = "mytestid_001";

            //设置支付参数
            SortedMap<String, String> signParams = new TreeMap<String, String>();
            signParams.put("appid", appId);
            //signParams.put("appkey", appKey);
            signParams.put("noncestr", noncestr);
            signParams.put("package", packageValue);
            signParams.put("timestamp", timestamp);
            signParams.put("traceid", traceid);

            //生成支付签名，要采用URLENCODER的原始值进行SHA1算法！
            String sign = Sha1Util.createSHA1Sign(signParams);
            //增加非参与签名的额外参数
            signParams.put("app_signature", sign);
            signParams.put("sign_method", "sha1");

            //获取prepayId
            String prepayId = prepayReqHandler.appSendPrepay(signParams, ConstantUtil.APPPREPAYURL, token);
            if (StringUtils.isNotBlank(prepayId)) {
                welinkVO.setStatus(1);
                SortedMap<String, String> params = new TreeMap<String, String>();
                params.put("appid", appId);
                //params.put("appkey", appKey);
                params.put("noncestr", noncestr);
                params.put("package", "Sign=WXPay");
                params.put("partnerid", partnerId);
                params.put("prepayid", prepayId);
                params.put("timestamp", timestamp);
                //生成签名
                sign = Sha1Util.createSHA1Sign(params);
                params.put("sign", sign);
                params.put("tradeid", String.valueOf(tradeId));
                welinkVO.setResult(params);
                return JSON.toJSONString(welinkVO);

            } else {
                result.setStatus(ResponseStatusEnum.FAILED.getCode());
                result.setErrorCode(BizErrorEnum.WEIXIN_PREPAY_FAILED.getCode());
                result.setMsg(BizErrorEnum.WEIXIN_PREPAY_FAILED.getMsg());
                welinkVO.setStatus(0);
                welinkVO.setMsg(BizErrorEnum.WEIXIN_PREPAY_FAILED.getMsg());
                welinkVO.setCode(BizErrorEnum.WEIXIN_PREPAY_FAILED.getCode());
                Profiler.release();
                EventTracker.track(mobile, "hwxOrder", "hwxOrder-action", "hwxOrder", 1L);
                Profiler.release();
                return JSON.toJSONString(welinkVO);
            }
        } else {
            result.setStatus(ResponseStatusEnum.FAILED.getCode());
            result.setErrorCode(BizErrorEnum.WEIXIN_AUTH_FAILED.getCode());
            result.setMsg(BizErrorEnum.WEIXIN_AUTH_FAILED.getMsg());
            welinkVO.setStatus(0);
            welinkVO.setMsg(BizErrorEnum.WEIXIN_AUTH_FAILED.getMsg());
            welinkVO.setCode(BizErrorEnum.WEIXIN_AUTH_FAILED.getCode());
            Profiler.release();
            EventTracker.track(mobile, "hwxOrder", "hwxOrder-action", "hwxOrder", 1L);
            Profiler.release();
            return JSON.toJSONString(welinkVO);
        }
    }

    private boolean sessionObject(Session session, String key) {
        if (null == session) {
            return false;
        }
        return null != session.getAttribute(key);
    }
}
