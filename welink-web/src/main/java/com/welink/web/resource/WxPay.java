package com.welink.web.resource;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.WelinkAgent;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.service.ShopService;
import com.welink.biz.util.TimeUtils;
import com.welink.biz.wx.tenpay.RequestHandler;
import com.welink.biz.wx.tenpay.util.ConstantUtil;
import com.welink.biz.wx.tenpay.util.Sha1Util;
import com.welink.biz.wx.tenpay.util.WXUtil;
import com.welink.biz.wx.tenpay.util.XMLUtil;
import com.welink.buy.service.AppointmentTradeService;
import com.welink.buy.utils.BaseResult;
import com.welink.buy.utils.Constants;
import com.welink.commons.Env;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.MikuCrowdfundDO;
import com.welink.commons.domain.MikuCrowdfundDOExample;
import com.welink.commons.domain.ShopDO;
import com.welink.commons.domain.Trade;
import com.welink.commons.domain.TradeExample;
import com.welink.commons.domain.MikuCrowdfundDOExample.Criteria;
import com.welink.commons.persistence.MikuCrowdfundDOMapper;
import com.welink.commons.tacker.EventTracker;
import com.welink.web.common.constants.ResponseStatusEnum;
import com.welink.web.common.filter.Profiler;
import com.welink.web.common.util.ParameterUtil;

import net.spy.memcached.MemcachedClient;

import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * 微信重新付款，针对单笔订单付款接口
 * totalFee是用户需要付款的钱数,包含运费,减去优惠和积分
 * price是商品的钱数
 * Created by daniel on 15-1-13.
 */
@RestController
public class WxPay {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(WxPay.class);

    @Resource
    private AppointmentTradeService appointmentService;

    @Resource
    private ShopService shopService;
    
    @Resource
    private MikuCrowdfundDOMapper mikuCrowdfundDOMapper;

    @Resource
    private MemcachedClient memcachedClient;

    @Resource
    private Env env;

    @RequestMapping(value = {"/api/m/1.0/wxPay.json"}, produces = "application/json;charset=utf-8")
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
            if(trade.getType().equals(Constants.TradeType.crowdfund_type.getTradeTypeId())){
            	Date nowDate = new Date();
            	//MikuCrowdfundDO mikuCrowdfundDO = mikuCrowdfundDOMapper.selectByPrimaryKey(trade.getCrowdfundId());
            	List<Byte> statusList = new ArrayList<Byte>();
            	statusList.add(Constants.CrowdfundStatus.NORMAL.getStatusId());
            	statusList.add(Constants.CrowdfundStatus.SUCCESS.getStatusId());
            	MikuCrowdfundDOExample mikuCrowdfundDOExample = new MikuCrowdfundDOExample();
    			Criteria createCriteria = mikuCrowdfundDOExample.createCriteria();
    			createCriteria.andIdEqualTo(trade.getCrowdfundId());
				createCriteria.andStatusIn(statusList);
         		//createCriteria.andStartTimeLessThan(nowDate).andEndTimeGreaterThan(nowDate);
         		if(mikuCrowdfundDOMapper.countByExample(mikuCrowdfundDOExample) < 1){
         			welinkVO.setStatus(0);
                    welinkVO.setMsg("亲~众筹已结束不能进行付款啦~");
                    Profiler.release();
                    return JSON.toJSONString(welinkVO);
         		}
            }
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
                //notify_url = "http://m.7nb.com.cn/api/m/1.0/weiXinCallBack.htm";
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
            
            String noncestr = WXUtil.getNonceStr();
            String timestamp = WXUtil.getTimeStamp();
            
            List<NameValuePair> packageParams2 = new LinkedList<NameValuePair>();
            packageParams2.add(new BasicNameValuePair("appid", appId));	
            //packageParams2.add(new BasicNameValuePair("body", title));
            title = (title.length() > 10) ? title + "……" : title.substring(0, title.length());
            packageParams2.add(new BasicNameValuePair("body", title));	//商品描述
            packageParams2.add(new BasicNameValuePair("mch_id", partnerId));	//商户号
            packageParams2.add(new BasicNameValuePair("nonce_str", noncestr));
            //packageParams2.add(new BasicNameValuePair("notify_url", URLEncoder.encode(notify_url)));	//接收财付通通知的URL
            packageParams2.add(new BasicNameValuePair("notify_url", notify_url));	//接收财付通通知的URL
            packageParams2.add(new BasicNameValuePair("out_trade_no",String.valueOf(tradeId+"_"+(int)((Math.random()*9+1)*10000))));	//商家订单号
            packageParams2.add(new BasicNameValuePair("spbill_create_ip",request.getRemoteAddr()));	//订单生成的机器IP，指用户浏览器端IP
            packageParams2.add(new BasicNameValuePair("total_fee", String.valueOf(totalPrice)));	//商品金额,以分为单位
            //packageParams.put("fee_type", "1"); //币种，1人民币   66
            packageParams2.add(new BasicNameValuePair("trade_type", "APP"));
            
			String sign = WXUtil.genPackageSign(packageParams2);
			//packageParams.put("sign", sign); //签名
			packageParams2.add(new BasicNameValuePair("sign", sign)); //签名
			
			String entity = WXUtil.toXml(packageParams2);
            String traceid = "mytestid_001";

            //获取prepayId
            //String prepayId = prepayReqHandler.appSendPrepay(signParams, ConstantUtil.APPPREPAYURL, token);
            String appprepayUrl = String.format("https://api.mch.weixin.qq.com/pay/unifiedorder");
            String prepayId = null;				//预支付id
            byte[] buf = WXUtil.httpPost(appprepayUrl, entity);		//生成预支付订单
            String resContent = new String(buf);
            Gson gson = new Gson();
            Map<String, String> map = XMLUtil.doXMLParse(resContent);
            if(null != map && "SUCCESS".equals(map.get("return_code")) && "SUCCESS".equals(map.get("result_code"))){
            	prepayId = map.get("prepay_id");
            }else{
            	 System.out.println("get prepayId err ,info =" + map.get("return_msg"));
            }
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

        		sign = WXUtil.genAppSign(params);
                
                //生成签名
                /*sign = Sha1Util.createSHA1Sign(params);
                params.put("sign", sign);*/
        		params.put("sign", sign);
                params.put("tradeid", String.valueOf(tradeId));
                welinkVO.setResult(params);
                return JSON.toJSONString(welinkVO);

            } else {
                welinkVO.setStatus(0);
                welinkVO.setMsg(BizErrorEnum.WEIXIN_PREPAY_FAILED.getMsg());
                welinkVO.setCode(BizErrorEnum.WEIXIN_PREPAY_FAILED.getCode());
                Profiler.release();
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
