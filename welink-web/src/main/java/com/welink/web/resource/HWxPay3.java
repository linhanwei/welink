package com.welink.web.resource;

import com.alibaba.fastjson.JSON;
import com.daniel.weixin.mp.api.WxMpConfigStorage;
import com.google.gson.Gson;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.WelinkAgent;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.common.security.NeedProfile;
import com.welink.biz.service.ShopService;
import com.welink.biz.util.TimeUtils;
import com.welink.biz.wx.tenpay.RequestHandler;
import com.welink.biz.wx.tenpay.util.ConstantUtil;
import com.welink.biz.wx.tenpay.util.MD5;
import com.welink.biz.wx.tenpay.util.PayCommonUtil;
import com.welink.biz.wx.tenpay.util.WXUtil;
import com.welink.biz.wx.tenpay.util.XMLUtil;
import com.welink.buy.service.AppointmentTradeService;
import com.welink.buy.utils.BaseResult;
import com.welink.buy.utils.Constants;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.ShopDO;
import com.welink.commons.domain.Trade;
import com.welink.commons.domain.TradeExample;
import com.welink.commons.tacker.EventTracker;
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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by daniel on 15-1-13.
 */
@RestController
public class HWxPay3 {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(HWxPay.class);

    @Resource
    private WxMpConfigStorage wxMpConfigStorage;

    @Resource
    private AppointmentTradeService appointmentService;

    @Resource
    private ShopService shopService;

    @Resource
    private MemcachedClient memcachedClient;

    @NeedProfile
    @RequestMapping(value = {"/api/h/1.0/hwxPay3.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        //get parameters
        long tradeId = ParameterUtil.getParameterAslongForSpringMVC(request, "tradeId", -1l);
        byte payType = ParameterUtil.getParameterAsByteForSpringMVC(request, "pType");
        long profileId = -1;
        long shopId = -1;
        long totalPrice = 0l;
        String title = "";
        WelinkVO welinkVO = new WelinkVO();
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        profileId = (long) session.getAttribute("profileId");
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
        TradeExample tradeExample = new TradeExample();
        tradeExample.createCriteria().andTradeIdEqualTo(tradeId).andStatusEqualTo(Constants.TradeStatus.WAIT_BUYER_PAY.getTradeStatusId());
        BaseResult<List<Trade>> tradeList = appointmentService.findByExample(tradeExample);
        if (tradeList.isSuccess() && tradeList != null && tradeList.getResult() != null && tradeList.getResult().size() > 0) {
            Trade trade = tradeList.getResult().get(0);
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

        //支付相关参数
        ShopDO shop = shopService.fetchShopByShopId(shopId);
        if (null == shop) {
            welinkVO.setStatus(0);
            welinkVO.setMsg(BizErrorEnum.COMMUNITY_ALIPAY_NOT_CONFIGURATED.getMsg());
            welinkVO.setCode(BizErrorEnum.COMMUNITY_ALIPAY_NOT_CONFIGURATED.getCode());
            Profiler.release();
            return JSON.toJSONString(welinkVO);
        }

        //IOS和IOSPRE的参数不同默认设置为来自IOS
        /*String appId = ConstantUtil.APP_APP_ID;
        String appSecret = ConstantUtil.APP_APP_SECRET;
        String partnerId = ConstantUtil.PARTNER_ID;
        String partnerKey = ConstantUtil.PARTNER_KEY;
        String appKey = ConstantUtil.APPKEY;*/
        WelinkAgent welinkAgent = new WelinkAgent();
        String userAgent = request.getHeader(BizConstants.USER_AGENT);
        try {
            if (org.apache.commons.lang.StringUtils.isNotBlank(userAgent)) {
                welinkAgent = JSON.parseObject(userAgent, WelinkAgent.class);
            }
        } catch (Exception e) {
            log.error("record client diploma failed. profileId:" + profileId + ",sessionId:" + session.getId().toString());
        }
        String state = (String) session.getAttribute(BizConstants.WEIXIN_MP_STATE);
        
        String noncestr = WXUtil.getNonceStr();
        String timestamp = WXUtil.getTimeStamp();
        String openid = (String) session.getAttribute(BizConstants.OPENID);
        
        List<NameValuePair> packageParams2 = new LinkedList<NameValuePair>();
        packageParams2.add(new BasicNameValuePair("appid", wxMpConfigStorage.getAppId(state)));	
        packageParams2.add(new BasicNameValuePair("mch_id", ConstantUtil.mcMap.get(state).getMch_id()));
        //packageParams2.add(new BasicNameValuePair("body", title));
        packageParams2.add(new BasicNameValuePair("body", "weixin ykd"));	//商品描述
        packageParams2.add(new BasicNameValuePair("nonce_str", noncestr));
        //packageParams2.add(new BasicNameValuePair("notify_url", URLEncoder.encode(notify_url)));	//接收财付通通知的URL
        packageParams2.add(new BasicNameValuePair("notify_url", ConstantUtil.mcMap.get(state).getNotify_url()));	//接收财付通通知的URL
        packageParams2.add(new BasicNameValuePair("out_trade_no",String.valueOf(tradeId+"_"+(int)((Math.random()*9+1)*10000))));	//商家订单号
        packageParams2.add(new BasicNameValuePair("spbill_create_ip",request.getRemoteAddr()));	//订单生成的机器IP，指用户浏览器端IP
        packageParams2.add(new BasicNameValuePair("total_fee", String.valueOf(totalPrice)));	//商品金额,以分为单位
        //packageParams.put("fee_type", "1"); //币种，1人民币   66
        packageParams2.add(new BasicNameValuePair("openid", openid));
        packageParams2.add(new BasicNameValuePair("trade_type", "JSAPI"));
        
		//String sign = WXUtil.genPackageSign(packageParams2);
        String sign = genPackageSign(packageParams2);
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
            params.put("appid", wxMpConfigStorage.getAppId(state));
            //params.put("appkey", appKey);
            params.put("noncestr", noncestr);
            params.put("package", "prepay_id=" + prepayId);
            params.put("timestamp", timestamp);
            params.put("signType", "MD5");

    		sign = WXUtil.genAppSign(params);
            
            //生成签名
            /*sign = Sha1Util.createSHA1Sign(params);
            params.put("sign", sign);*/
    		params.put("paySign", sign);
            params.put("tradeid", String.valueOf(tradeId));
            
            /*String paySign = PayCommonUtil.createSign("UTF-8", params, ConstantUtil.mcMap.get(state).getAppKey());
            params.put("paySign", paySign);*/
            
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

    }

    private boolean sessionObject(Session session, String key) {
        if (null == session) {
            return false;
        }
        return null != session.getAttribute(key);
    }
    
    public String genPackageSign(List<NameValuePair> params) {
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < params.size(); i++) {
			sb.append(params.get(i).getName());
			sb.append('=');
			sb.append(params.get(i).getValue());
			sb.append('&');
		}
		sb.append("key=");
		sb.append(ConstantUtil.PARTNER_KEY);
		String packageSign = MD5.getMessageDigest(sb.toString().getBytes()).toUpperCase();
		return packageSign;
	}
    
}
