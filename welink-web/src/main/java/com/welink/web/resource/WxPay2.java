package com.welink.web.resource;

import com.alibaba.fastjson.JSON;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.WelinkAgent;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.service.ShopService;
import com.welink.biz.util.TimeUtils;
import com.welink.biz.wx.tenpay.RequestHandler;
import com.welink.biz.wx.tenpay.util.ConstantUtil;
import com.welink.biz.wx.tenpay.util.Sha1Util;
import com.welink.biz.wx.tenpay.util.WXUtil;
import com.welink.buy.service.AppointmentTradeService;
import com.welink.buy.utils.BaseResult;
import com.welink.buy.utils.Constants;
import com.welink.commons.Env;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.ShopDO;
import com.welink.commons.domain.Trade;
import com.welink.commons.domain.TradeExample;
import com.welink.commons.tacker.EventTracker;
import com.welink.web.common.filter.Profiler;
import com.welink.web.common.util.ParameterUtil;
import net.spy.memcached.MemcachedClient;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * 微信重新付款，针对单笔订单付款接口
 * totalFee是用户需要付款的钱数,包含运费,减去优惠和积分
 * price是商品的钱数
 * Created by daniel on 15-1-13.
 */
@RestController
public class WxPay2 {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(WxPay2.class);

    @Resource
    private AppointmentTradeService appointmentService;

    @Resource
    private ShopService shopService;

    @Resource
    private MemcachedClient memcachedClient;

    @Resource
    private Env env;

    @RequestMapping(value = {"/api/m/1.0/wxPay2.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        //get parameters
        long tradeId = ParameterUtil.getParameterAslongForSpringMVC(request, "tradeId", -1l);
        long profileId = -1;
        long shopId = -1;
        long totalPrice = 0l;
        String title = "";
        String appwxToken = "";
        WelinkVO welinkVO = new WelinkVO();
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        try {
            if (session == null || !sessionObject(session, BizConstants.PROFILE_ID)) {
                welinkVO.setStatus(0);
                welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
                welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
                return JSON.toJSONString(welinkVO);
            }
            profileId = (long) session.getAttribute("profileId");
        } catch (Exception e) {
            log.error("fetch paras from session failed . exp:" + e.getMessage() + ",sessionId:" + session.getId().toString());
            welinkVO.setStatus(0);
            welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
            welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
            return JSON.toJSONString(welinkVO);
        }
        Profiler.enter("h5 order action " + profileId);
        EventTracker.track(String.valueOf(profileId), "horder", "horder-action", "horder-pre", 1L);
        if (tradeId < 0) {
            welinkVO.setStatus(0);
            welinkVO.setMsg(BizErrorEnum.PARAMS_ERROR.getMsg());
            welinkVO.setCode(BizErrorEnum.PARAMS_ERROR.getCode());
            Profiler.release();
            return JSON.toJSONString(welinkVO);
        }
        //fetch order info
        byte tradeStatus = -1;
        TradeExample tradeExample = new TradeExample();
        tradeExample.createCriteria().andTradeIdEqualTo(tradeId).andStatusEqualTo(Constants.TradeStatus.WAIT_BUYER_PAY.getTradeStatusId());
        BaseResult<List<Trade>> tradeList = appointmentService.findByExample(tradeExample);
        if (tradeList.isSuccess() && tradeList != null && tradeList.getResult() != null && tradeList.getResult().size() > 0) {
            Trade trade = tradeList.getResult().get(0);
            tradeStatus = trade.getStatus();
            shopId = trade.getShopId();
            totalPrice = trade.getTotalFee();
            title = trade.getTitle();
        } else {
            welinkVO.setStatus(0);
            welinkVO.setMsg(BizErrorEnum.TRADE_NOT_FOUND.getMsg());
            welinkVO.setCode(BizErrorEnum.TRADE_NOT_FOUND.getCode());
            Profiler.release();
            return JSON.toJSONString(welinkVO);
        }

        //如果订单未付款，构建订单微信付款参数
        if (tradeStatus == Constants.TradeStatus.WAIT_BUYER_PAY.getTradeStatusId()) {
            //支付相关参数
            ShopDO shop = shopService.fetchShopByShopId(shopId);
            if (null == shop) {
                welinkVO.setStatus(0);
                welinkVO.setMsg(BizErrorEnum.COMMUNITY_ALIPAY_NOT_CONFIGURATED.getMsg());
                welinkVO.setCode(BizErrorEnum.COMMUNITY_ALIPAY_NOT_CONFIGURATED.getCode());
                Profiler.release();
                return JSON.toJSONString(welinkVO);
            }
            //接收财付通通知的URL
            String notify_url = "http://m.7nb.com.cn/api/m/1.0/weiXinCallBack.htm";
            if (env.isProd()) {
            	notify_url = "http://" + BizConstants.ONLINE_DOMAIN + "/api/m/1.0/weiXinCallBack.htm";
            } else {
                notify_url = "http://" + BizConstants.ONLINE_DOMAIN_TEST + "/api/m/1.0/weiXinCallBack.htm";
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
                log.error("record client diploma failed. profileId:" + profileId + ",sessionId:" + session.getId().toString());
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

            log.info("获取token------值 " + token + ",sessionId:" + session.getId().toString());

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
                signParams.put("appkey", appKey);
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
                    params.put("appkey", appKey);
                    params.put("noncestr", noncestr);
                    params.put("package", "Sign=WXPay");
                    params.put("partnerid", partnerId);
                    params.put("prepayid", prepayId);
                    params.put("timestamp", timestamp);
                    //生成签名
                    sign = Sha1Util.createSHA1Sign(params);
                    params.put("sign", sign);
                    welinkVO.setResult(params);
                    return JSON.toJSONString(welinkVO);

                } else {
                    welinkVO.setStatus(0);
                    welinkVO.setMsg(BizErrorEnum.WEIXIN_PREPAY_FAILED.getMsg());
                    welinkVO.setCode(BizErrorEnum.WEIXIN_PREPAY_FAILED.getCode());
                    Profiler.release();
                    return JSON.toJSONString(welinkVO);
                }
            } else {
                welinkVO.setStatus(0);
                welinkVO.setMsg(BizErrorEnum.WEIXIN_AUTH_FAILED.getMsg());
                welinkVO.setCode(BizErrorEnum.WEIXIN_AUTH_FAILED.getCode());
                Profiler.release();
                return JSON.toJSONString(welinkVO);
            }
        } else {
            welinkVO.setStatus(0);
            welinkVO.setCode(BizErrorEnum.TRADE_PAYED_ALREADY.getCode());
            welinkVO.setMsg(BizErrorEnum.TRADE_PAYED_ALREADY.getMsg());
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
