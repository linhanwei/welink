package com.welink.web.resource;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.common.pay.AlipayConfig;
import com.welink.biz.common.pay.AlipaySubmit;
import com.welink.biz.common.pay.XmlConverUtil;
import com.welink.biz.common.security.NeedProfile;
import com.welink.biz.service.ShopService;
import com.welink.biz.util.StringUtil;
import com.welink.buy.service.AppointmentTradeService;
import com.welink.buy.utils.BaseResult;
import com.welink.buy.utils.Constants;
import com.welink.commons.Env;
import com.welink.commons.domain.MikuCrowdfundDOExample;
import com.welink.commons.domain.MikuCrowdfundDOExample.Criteria;
import com.welink.commons.domain.Order;
import com.welink.commons.domain.ShopDO;
import com.welink.commons.domain.Trade;
import com.welink.commons.domain.TradeExample;
import com.welink.commons.persistence.MikuCrowdfundDOMapper;
import com.welink.commons.persistence.TradeMapper;
import com.welink.commons.tacker.EventTracker;
import com.welink.web.common.filter.Profiler;
import com.welink.web.common.util.ParameterUtil;

/**
 * 此接口是在 待付款列表/订单详情页 进行付款提供的
 * Created by daniel on 14-12-11.
 */
@RestController
public class HPay {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(HPay.class);

    @Resource
    private AppointmentTradeService appointmentService;

    @Resource
    private TradeMapper tradeMapper;
    
    @Resource
    private MikuCrowdfundDOMapper mikuCrowdfundDOMapper;

    @Resource
    private Env env;

    @Resource
    private ShopService shopService;

    @NeedProfile
    @RequestMapping(value = {"/api/h/1.0/hPay.json"}, produces = "application/json;charset=utf-8")
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
        tradeExample.createCriteria().andTradeIdEqualTo(tradeId);
        BaseResult<List<Trade>> tradeList = appointmentService.findByExample(tradeExample);
        if (null != tradeList && tradeList.isSuccess()) {
            StringUtil stringUtil = new StringUtil();
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
                    return JSON.toJSONString(welinkVO);
         		}
            }
            shopId = trade.getShopId();
            totalPrice = trade.getTotalFee();
            title = trade.getTitle();
            List<Order> orders = new ArrayList<Order>();
        } else {
            welinkVO.setStatus(0);
            welinkVO.setMsg(BizErrorEnum.TRADE_NOT_FOUND.getMsg());
            welinkVO.setCode(BizErrorEnum.TRADE_NOT_FOUND.getCode());
            Profiler.release();
            return JSON.toJSONString(welinkVO);
        }
        //货到付款处理
        if (Byte.compare(payType, Constants.PayType.OFF_LINE.getPayTypeId()) == 0) {
            return CodPay(tradeId, welinkVO, response);
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
        BigDecimal t = new BigDecimal(totalPrice);
        String priceStr = t.divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP).toString();
        //获取授权
        Map<String, String> paraMap = new HashMap<>();
        Map<String, String> reqMap = new HashMap<>();
        reqMap.put("total_fee", priceStr);
        reqMap.put("subject", title);
        reqMap.put("out_trade_no", String.valueOf(tradeId));
        reqMap.put("seller_account_name", shop.getAlipayAccount());
        if (env.isProd()) {
            reqMap.put("call_back_url", AlipayConfig.ONLINE_H5_GUIDE_URL);//会跳
            reqMap.put("notify_url", AlipayConfig.ONLINE_H5_NOTIFY_URL);
            reqMap.put("merchant_url", AlipayConfig.ONLINE_H5_GUIDE_URL);
        } else {
            reqMap.put("call_back_url", AlipayConfig.DAILY_H5_GUIDE_URL);//会跳
            reqMap.put("notify_url", AlipayConfig.DEV_H5_NOTIFY_URL);
            reqMap.put("merchant_url", AlipayConfig.DAILY_H5_GUIDE_URL);
        }

        reqMap.put("out_user", AlipayConfig.partner);
        reqMap.put("pay_expire", AlipayConfig.ALIPAY_OUT_TIME_24_HOUR);
        String reqData = XmlConverUtil.maptoXml(reqMap);
        paraMap.put("service", AlipayConfig.ALIPAY_AUTH_INTERFACE);
        paraMap.put("format", AlipayConfig.FORMAT);
        paraMap.put("v", AlipayConfig.V);
        paraMap.put("partner", AlipayConfig.partner);
        paraMap.put("req_id", String.valueOf(new Date().getTime()));
        paraMap.put("sec_id", AlipayConfig.wap_sign_type);
        paraMap.put("_input_charset", AlipayConfig.input_charset);
        paraMap.put("req_data", reqData);
        // ---> 授权建立请求
        String sHtmlTextToken = AlipaySubmit.buildRequest(AlipayConfig.ALIPAY_AUTH_URL, "", "", paraMap);
        //URLDECODE返回的信息
        sHtmlTextToken = URLDecoder.decode(sHtmlTextToken, AlipayConfig.input_charset);
        //获取token
        String request_token = null;
        try {
            request_token = AlipaySubmit.getRequestToken(sHtmlTextToken);
        } catch (Exception e) {
            log.error("fetch request token failed.  sHtmlText:" + sHtmlTextToken + ",sessionId:" + session.getId().toString());
        }
        ////////////////////////////////////根据授权码token调用交易接口alipay.wap.auth.authAndExecute//////////////////////////////////////
        if (StringUtils.isNotBlank(request_token)) {
            String req_data = "<auth_and_execute_req><request_token>" + request_token + "</request_token></auth_and_execute_req>";
            Map<String, String> sParaTemp = new HashMap<String, String>();
            sParaTemp.put("req_data", req_data);
            sParaTemp.put("service", AlipayConfig.ALIPAY_AUTH_EXECUTE);
            sParaTemp.put("partner", AlipayConfig.partner);
            sParaTemp.put("_input_charset", AlipayConfig.input_charset);
            sParaTemp.put("sec_id", AlipayConfig.wap_sign_type);
            sParaTemp.put("v", AlipayConfig.V);
            sParaTemp.put("format", AlipayConfig.FORMAT);
            String sHtmlText = AlipaySubmit.buildRequestForPayUrl(AlipayConfig.ALIPAY_AUTH_URL, sParaTemp, "get", "确认");
            if (StringUtils.isNotBlank(sHtmlText)) {
                welinkVO.setStatus(1);
                Map resultMap = new HashMap();
                resultMap.put("pType", payType);
                resultMap.put("fm", URLEncoder.encode(sHtmlText, "utf-8"));
                welinkVO.setResult(resultMap);
                Profiler.release();
                return JSON.toJSONString(welinkVO);
            } else {
                welinkVO.setStatus(0);
                welinkVO.setMsg(BizErrorEnum.SYSTEM_BUSY.getMsg());
                welinkVO.setCode(BizErrorEnum.SYSTEM_BUSY.getCode());
                Profiler.release();
                EventTracker.track(String.valueOf(profileId), "horder", "horder-action", "horder", 1L);
                return JSON.toJSONString(welinkVO);
            }
        } else {
            welinkVO.setStatus(0);
            welinkVO.setMsg(BizErrorEnum.ALIPAY_AUTH_FAILED.getMsg());
            welinkVO.setCode(BizErrorEnum.ALIPAY_AUTH_FAILED.getCode());
            Profiler.release();
            return JSON.toJSONString(welinkVO);
        }
    }

    private String CodPay(long tradeId, WelinkVO welinkVO, HttpServletResponse response) throws IOException {
        //更改trade 信息
        Trade uTrade = new Trade();
        uTrade.setLastUpdated(new Date());
        uTrade.setPayType(Constants.PayType.OFF_LINE.getPayTypeId());
        uTrade.setType(Constants.TradeType.cod.getTradeTypeId());
        uTrade.setCodStatus(Constants.CodStatus.ACCEPTED_BY_COMPANY.getCodStatusId());
        uTrade.setStatus(Constants.TradeStatus.WAIT_SELLER_SEND_GOODS.getTradeStatusId());
        TradeExample uExample = new TradeExample();
        uExample.createCriteria().andTradeIdEqualTo(tradeId);
        List<Trade> tradeList1 = tradeMapper.selectByExample(uExample);
        if (tradeList1 != null && tradeList1.size() > 0) {
            uTrade.setVersion(tradeList1.get(0).getVersion() + 1L);
        }
        tradeMapper.updateByExampleSelective(uTrade, uExample);
        TradeExample qExample = new TradeExample();
        qExample.createCriteria().andTradeIdEqualTo(tradeId);
        BaseResult<List<Trade>> qTradeList = appointmentService.findByExample(qExample);
        Map resultMap = new HashMap();
        resultMap.put("tradeStatus", qTradeList.getResult().get(0).getStatus());
        resultMap.put("tradeId", tradeId);
        welinkVO.setStatus(1);
        welinkVO.setResult(resultMap);
        return JSON.toJSONString(welinkVO);
    }

    /**
     * check session
     *
     * @param session
     * @param key
     * @return
     */
    private boolean sessionObject(Session session, String key) {
        if (null == session) {
            return false;
        }
        return null != session.getAttribute(key);
    }

    public static void main(String[] args) {
        WelinkVO welinkVO = new WelinkVO();
        welinkVO.setStatus(1);
        Map mp = new HashMap();
        mp.put("fm", new StringUtil().escapeJson("eldfld;f;d"));
        welinkVO.setResult(mp);
        System.out.println(JSON.toJSONString(welinkVO));
    }
}
