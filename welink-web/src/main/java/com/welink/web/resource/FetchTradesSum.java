package com.welink.web.resource;

import com.alibaba.fastjson.JSON;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.common.security.NeedProfile;
import com.welink.buy.utils.Constants;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.TradeExample;
import com.welink.commons.persistence.TradeMapper;
import com.welink.commons.utils.BigDecimalUtils;
import com.welink.web.common.constants.ResponseStatusEnum;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by daniel on 14-12-25.
 */
@RestController
public class FetchTradesSum {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(FetchTrades.class);

    @Resource
    private TradeMapper tradeMapper;

    @NeedProfile
    @RequestMapping(value = {"/api/m/1.0/fetchTradesSum.json", "/api/h/1.0/fetchTradesSum.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        WelinkVO welinkVO = new WelinkVO();
        Map resultMap = new HashMap();
        //get parameters
        long profileId = -1L;
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        profileId = (long) session.getAttribute("profileId");

        TradeExample tExample = new TradeExample();
        tExample.createCriteria().andBuyerIdEqualTo(profileId).andStatusEqualTo(Constants.TradeStatus.WAIT_BUYER_PAY.getTradeStatusId());
        int forPay = tradeMapper.countByExample(tExample);
        resultMap.put("forPay", forPay);

        TradeExample tExample1 = new TradeExample();
        tExample1.createCriteria().andBuyerIdEqualTo(profileId).andStatusEqualTo(Constants.TradeStatus.WAIT_SELLER_SEND_GOODS.getTradeStatusId());
        int forSend = tradeMapper.countByExample(tExample1);
        resultMap.put("forSend", forSend);

        TradeExample tExample2 = new TradeExample();
        tExample2.createCriteria().andBuyerIdEqualTo(profileId).andStatusEqualTo(Constants.TradeStatus.WAIT_BUYER_CONFIRM_GOODS.getTradeStatusId());
        int forReceive = tradeMapper.countByExample(tExample2);
        resultMap.put("forReceive", forReceive);
        welinkVO.setResult(resultMap);
        welinkVO.setStatus(ResponseStatusEnum.SUCCESS.getCode());
        return JSON.toJSONString(welinkVO);
    }
    
    @RequestMapping(value = {"/api/m/1.0/tradeSumFeeByBuyer.json", "/api/h/1.0/tradeSumFeeByBuyer.json"}, produces = "application/json;charset=utf-8")
    public String tradeSumFeeByBuyer(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	long profileId = -1l;
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        WelinkVO welinkVO = new WelinkVO();
        profileId = (long) session.getAttribute("profileId");
        if (profileId < 0) {
            welinkVO.setStatus(0);
            welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
            welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
            return JSON.toJSONString(welinkVO);
        }
        
        Map paramsMap = new HashMap();
        paramsMap.put("profileId", profileId);
        //paramsMap.put("status", Constants.TradeStatus.TRADE_FINISHED.getTradeStatusId());	//一完成的订单
        paramsMap.put("type", Constants.TradeType.join_agency.getTradeTypeId());	//成为代理
        Map<String, Object> resultMap = tradeMapper.sumByBuyer(paramsMap);
        if(null == resultMap){
        	resultMap = new HashMap<String, Object>();
        	resultMap.put("price", "0.00");
        	resultMap.put("totalFee", "0.00");
        }else{
        	resultMap.put("price", BigDecimalUtils.divFee100(resultMap.get("price").toString()));		//分转元
        	resultMap.put("totalFee", BigDecimalUtils.divFee100(resultMap.get("totalFee").toString())); //分转元
        }
        resultMap.put("canJoinAgencyFee",  BigDecimalUtils.divFee100(BizConstants.CAN_JOIN_AGENCY_FEE));
        welinkVO.setStatus(1);
        welinkVO.setResult(resultMap);
        return JSON.toJSONString(welinkVO);
    }
    
    /**
     * 
     * mineTradeCount:(我的订单统计). <br/>
     * TODO(统计待付款、待收货、待评价条数).<br/>
     *
     * @author LuoGuangChun
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = {"/api/m/1.0/mineTradeCount.json", "/api/h/1.0/mineTradeCount.json"}, produces = "application/json;charset=utf-8")
    public String mineTradeCount(HttpServletRequest request, HttpServletResponse response) throws Exception {
		long profileId = -1l;
		WelinkVO welinkVO = new WelinkVO();
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        profileId = (long) session.getAttribute("profileId");
		if (profileId < 0) {
			welinkVO.setStatus(0);
			welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
			welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
			return JSON.toJSONString(welinkVO);
		}
        
        Map resultMap = new HashMap();
        
        //待付款
        TradeExample tradeExample = new TradeExample();
        
        tradeExample.createCriteria().andBuyerIdEqualTo(profileId)
        	.andStatusEqualTo(Constants.TradeStatus.WAIT_BUYER_PAY.getTradeStatusId())
        	.andTypeNotEqualTo(Constants.TradeType.crowdfund_type.getTradeTypeId());
        int waitPayCount = tradeMapper.countByExample(tradeExample);
        resultMap.put("waitPayCount", waitPayCount);	//待付款
        
        //待收货
        List<Byte> wrTradeStatusList = new ArrayList<Byte>();
        wrTradeStatusList.add(Constants.TradeStatus.SELLER_CONSIGNED_PART.getTradeStatusId());
        wrTradeStatusList.add(Constants.TradeStatus.WAIT_SELLER_SEND_GOODS.getTradeStatusId());
        wrTradeStatusList.add(Constants.TradeStatus.WAIT_BUYER_CONFIRM_GOODS.getTradeStatusId());
        wrTradeStatusList.add(Constants.TradeStatus.TRADE_BUYER_SIGNED.getTradeStatusId());
        TradeExample tradeExample2 = new TradeExample();
        tradeExample2.createCriteria().andBuyerIdEqualTo(profileId)
        	.andTypeNotEqualTo(Constants.TradeType.crowdfund_type.getTradeTypeId())
        	.andStatusIn(wrTradeStatusList);
        int waitReceiptCount = tradeMapper.countByExample(tradeExample2);
        resultMap.put("waitReceiptCount", waitReceiptCount);	//待收货
        
        //待评价
        List<Byte> wReviewsTradeStatusList = new ArrayList<Byte>();
        wReviewsTradeStatusList.add(Constants.TradeStatus.TRADE_FINISHED.getTradeStatusId());
        wReviewsTradeStatusList.add(Constants.TradeStatus.TRADE_SUCCESSED.getTradeStatusId());
		TradeExample tradeExample3 = new TradeExample();
        tradeExample3.createCriteria().andBuyerIdEqualTo(profileId)
        	.andTypeNotEqualTo(Constants.TradeType.crowdfund_type.getTradeTypeId())
        	.andStatusIn(wReviewsTradeStatusList)
        	.andBuyerRateEqualTo((byte)0);
        int waitReviewsCount = tradeMapper.countByExample(tradeExample3);
        resultMap.put("waitReviewsCount", waitReviewsCount);	//待评价
        
        welinkVO.setStatus(1);
        welinkVO.setResult(resultMap);
        return JSON.toJSONString(welinkVO);
	}
    
}
