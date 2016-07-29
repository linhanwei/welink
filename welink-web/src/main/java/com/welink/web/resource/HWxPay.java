package com.welink.web.resource;

import com.alibaba.fastjson.JSON;
import com.daniel.weixin.mp.api.WxMpConfigStorage;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.common.security.NeedProfile;
import com.welink.biz.service.ShopService;
import com.welink.biz.util.TimeUtils;
import com.welink.biz.wx.tenpay.RequestHandler;
import com.welink.biz.wx.tenpay.util.ConstantUtil;
import com.welink.biz.wx.tenpay.util.PayCommonUtil;
import com.welink.biz.wx.tenpay.util.WXUtil;
import com.welink.buy.service.AppointmentTradeService;
import com.welink.buy.utils.BaseResult;
import com.welink.buy.utils.Constants;
import com.welink.commons.Env;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.MikuCrowdfundDOExample;
import com.welink.commons.domain.ShopDO;
import com.welink.commons.domain.Trade;
import com.welink.commons.domain.TradeExample;
import com.welink.commons.domain.MikuCrowdfundDOExample.Criteria;
import com.welink.commons.persistence.MikuCrowdfundDOMapper;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by daniel on 15-1-13.
 */
@RestController
public class HWxPay {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(HWxPay.class);

    @Resource
    private WxMpConfigStorage wxMpConfigStorage;

    @Resource
    private AppointmentTradeService appointmentService;

    @Resource
    private ShopService shopService;
    
    @Resource
    private MikuCrowdfundDOMapper mikuCrowdfundDOMapper;
    
    @Resource
    private Env env;

    @Resource
    private MemcachedClient memcachedClient;

    @NeedProfile
    @RequestMapping(value = {"/api/h/1.0/hwxPay.json"}, produces = "application/json;charset=utf-8")
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

        //接收财付通通知的URL TODO:更换成自己的回调地址
        RequestHandler prepayReqHandler = new RequestHandler();//获取prepayid的请求类
        SortedMap<Object, Object> prepayParams = new TreeMap<Object, Object>();
        
        String state = ConstantUtil.GUOGEGE_WX_CONF_KEY;
        if (env.isProd()) {
        	state = ConstantUtil.GUOGEGE_WX_CONF_KEY;
        } else {
        	state = ConstantUtil.XIAOWEILINJU_WX_CONF_KEY;
        }
        /*if (StringUtils.isNotBlank((String) session.getAttribute(BizConstants.WEIXIN_MP_STATE))) {
            state = (String) session.getAttribute(BizConstants.WEIXIN_MP_STATE);
        }*/
        //String state = (String) session.getAttribute(BizConstants.WEIXIN_MP_STATE);
        //获取token值
        String token = "";
        if (StringUtils.isNotBlank((String) memcachedClient.get(BizConstants.H5_TOKEN + state))) {
            token = (String) memcachedClient.get(BizConstants.H5_TOKEN + state);
        } else {
            token = prepayReqHandler.GetToken(ConstantUtil.mcMap.get(state).getAppId(), ConstantUtil.mcMap.get(state).getAppSecret());
            if (StringUtils.isNotBlank(token)) {
                memcachedClient.set(BizConstants.H5_TOKEN + state, TimeUtils.JS_API_TICKET_TIMEOUT, token);
            }
        }

        String noncestr = WXUtil.getNonceStr();
        String timestamp = WXUtil.getTimeStamp();
        String openid = (String) session.getAttribute(BizConstants.OPENID);

        System.out.println("HWxpay-----------------------------------------------------------------state:"+state);
        log.info("获取token------值 " + token + ",sessionId:" + session.getId()+"--------------state:"+state);

        if (StringUtils.isNotBlank(token)) {
            //设置package订单参数
            //prepayParams.put("appid", appId);//appid
            prepayParams.put("appid", wxMpConfigStorage.getAppId(state));//appid
            prepayParams.put("mch_id", ConstantUtil.mcMap.get(state).getMch_id()); //商户号
            prepayParams.put("body", title); //商品描述
            prepayParams.put("nonce_str", noncestr); //随机串
            prepayParams.put("trade_type", "JSAPI"); //调用类型
            prepayParams.put("notify_url", ConstantUtil.mcMap.get(state).getNotify_url()); //接收财付通通知的URL
            prepayParams.put("out_trade_no", String.valueOf(tradeId)); //商家订单号
            prepayParams.put("total_fee", String.valueOf(totalPrice)); //商品金额,以分为单位
            prepayParams.put("spbill_create_ip", request.getRemoteAddr()); //订单生成的机器IP，指用户浏览器端IP
            prepayParams.put("fee_type", "1"); //币种，1人民币   66
            prepayParams.put("openid", openid); //openid
            //根据以上参数生成sign，并加入到参数中
            String sign = PayCommonUtil.createSign("UTF-8", prepayParams, ConstantUtil.mcMap.get(state).getAppKey());
            prepayParams.put("sign", sign);

            //发送请求响应的XML字符串
            String prepayId = prepayReqHandler.sendPrepay(prepayParams, ConstantUtil.PREPAYURL);
            if (StringUtils.isNotBlank(prepayId)) {
                welinkVO.setStatus(1);
                SortedMap<Object, Object> params = new TreeMap<Object, Object>();
                params.put("appId", wxMpConfigStorage.getAppId(state));
                params.put("timeStamp", timestamp);
                params.put("nonceStr", noncestr);
                params.put("package", "prepay_id=" + prepayId);
                params.put("signType", "MD5");
//                params.put("pType", payType);
                String paySign = PayCommonUtil.createSign("UTF-8", params, ConstantUtil.mcMap.get(state).getAppKey());
                params.put("paySign", paySign);
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
    }

    private boolean sessionObject(Session session, String key) {
        if (null == session) {
            return false;
        }
        return null != session.getAttribute(key);
    }
}
