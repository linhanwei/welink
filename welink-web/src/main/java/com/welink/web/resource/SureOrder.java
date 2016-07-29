package com.welink.web.resource;

import com.alibaba.fastjson.JSON;
import com.google.common.eventbus.AsyncEventBus;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.ItemJson;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.common.security.NeedProfile;
import com.welink.biz.service.CartService;
import com.welink.biz.service.ProfitService;
import com.welink.buy.utils.BaseResult;
import com.welink.buy.utils.Constants;
import com.welink.commons.Env;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.Trade;
import com.welink.commons.domain.TradeExample;
import com.welink.commons.events.ProfitEvent;
import com.welink.commons.persistence.TradeMapper;
import com.welink.commons.tacker.EventTracker;
import com.welink.web.common.constants.ResponseStatusEnum;
import com.welink.web.common.model.ResponseResult;
import com.welink.web.common.util.ParameterUtil;
import com.welink.web.ons.MessageProcessFacade;
import com.welink.web.ons.TradeMessageProcessFacade;
import com.welink.web.ons.config.ONSTopic;
import com.welink.web.ons.events.TradeEvent;
import com.welink.web.ons.events.TradeEventType;

import org.apache.commons.lang.StringUtils;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by daniel on 14-11-17.
 */
@RestController
public class SureOrder {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(SureOrder.class);

    @Resource
    private ProfitService profitService;
    
    @Resource
	private TradeMapper tradeMapper;
    
    /*@Resource
    private MessageProcessFacade messageProcessFacade;*/
    
    @Resource
    private TradeMessageProcessFacade tradeMessageProcessFacade;
    
    @Resource
    private AsyncEventBus asyncEventBus;
    
    @Resource
    private Env env;

    //@NeedProfile
    @RequestMapping(value = {"/api/m/1.0/sureOrder.json", "/api/h/1.0/sureOrder.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response,
    		@RequestParam(value="tradeId", required = true) Long tradeId) throws Exception {
        long profileId = -1;
        ResponseResult result = new ResponseResult();
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        WelinkVO welinkVO = new WelinkVO();
        //EventTracker.track(BizConstants.CART_OP, "cart", "update", "pre", 1L);
        profileId = (long) session.getAttribute("profileId");
        if (profileId < 0) {
			welinkVO.setStatus(0);
			welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
			welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
			return JSON.toJSONString(welinkVO);
		}
        
        Map resultMap = new HashMap();
        
        Trade trade = null;
        
        TradeExample tradeExample = new TradeExample(); 
        tradeExample.createCriteria().andTradeIdEqualTo(tradeId);
        List<Trade> tradeList = tradeMapper.selectByExample(tradeExample);
        if(!tradeList.isEmpty()){
        	trade = tradeList.get(0);
        }else{
        	welinkVO.setStatus(0);
        	welinkVO.setMsg("阿哦~没有此订单，不能进行确认订单操作");
        	return JSON.toJSONString(welinkVO);
        }
        
        if(Constants.TradeStatus.WAIT_BUYER_CONFIRM_GOODS.getTradeStatusId() != trade.getStatus()){
        	welinkVO.setStatus(0);
        	welinkVO.setMsg("阿哦~不是等待买家确认收货状态，不能进行订单确认收货操作");
        	return JSON.toJSONString(welinkVO);
        }
        
        if(null != trade.getReturnStatus() && trade.getReturnStatus().equals(Constants.TradeReturnStatus.RETURNED.getStatusId())){
        	welinkVO.setStatus(0);
        	welinkVO.setMsg("阿哦~订单已退货，不能进行订单确认收货操作");
        	return JSON.toJSONString(welinkVO);
        }
        
        if(profitService.sureTrade(trade.getId())){		//确认订单
        	tradeMessageProcessFacade.sendMessage(trade.getTradeId(), TradeEventType.TRADE_SUCCESS.getTopic());	//送积分
        	
        	result.setStatus(ResponseStatusEnum.SUCCESS.getCode());
        	//EventTracker.track(BizConstants.CART_OP, "cart", "update", "success", 1L);
        	welinkVO.setStatus(1);
        }else{
        	welinkVO.setStatus(0);
        	welinkVO.setCode(BizErrorEnum.SYSTEM_BUSY.getCode());
        	welinkVO.setMsg(BizErrorEnum.SYSTEM_BUSY.getMsg());
        }
        
        /*if (null != tradeId && tradeId > 0) {
        	asyncEventBus.post(new ProfitEvent(tradeId));	//通过事件总线 交易分润
        	//profitService.profitByTradeId(tradeId);
		}*/
        return JSON.toJSONString(welinkVO);
    }
    
}
