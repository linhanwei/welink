package com.welink.web.resource;

import com.alibaba.fastjson.JSON;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.EvalJson;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.service.EvalService;
import com.welink.buy.utils.Constants;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.*;
import com.welink.commons.persistence.OrderMapper;
import com.welink.commons.persistence.ProfileDOMapper;
import com.welink.commons.persistence.TradeMapper;
import com.welink.web.common.constants.ResponseStatusEnum;
import com.welink.web.common.util.ParameterUtil;
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

/**
 * Created by daniel on 14-11-14.
 */
@RestController
public class AddEval {

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(AddEval.class);

    @Resource
    private EvalService evalService;

    @Resource
    private TradeMapper tradeMapper;

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private ProfileDOMapper profileDOMapper;

    @RequestMapping(value = {"/api/m/1.0/addEval.json", "/api/h/1.0/addEval.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        WelinkVO welinkVO = new WelinkVO();
        String evals = ParameterUtil.getParameter(request, "evals");
        long profileId = -1;
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        try {
            if (session == null || !sessionObject(session, "profileId") || !sessionObject(session, "mobile")) {
                welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
                welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
                welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
                return JSON.toJSONString(welinkVO);
            }
            profileId = (long) session.getAttribute("profileId");
        } catch (Exception e) {
            logger.error("fetch paras from session failed . exp:" + e.getMessage());
        }
        long tradeId = -1;
        com.alibaba.fastjson.JSONArray array = com.alibaba.fastjson.JSONArray.parseArray(evals);
        List<EvalJson> eevals = new ArrayList<EvalJson>();
        for (int i = 0; i < array.size(); i++) {
            com.alibaba.fastjson.JSONObject jobj = (com.alibaba.fastjson.JSONObject) array.get(i);
            EvalJson evalJson = JSON.toJavaObject(jobj, EvalJson.class);
            eevals.add(evalJson);
            tradeId = evalJson.getTradeId();
        }

        TradeExample tExample = new TradeExample();
        tExample.createCriteria().andTradeIdEqualTo(tradeId);
        List<Trade> trades = tradeMapper.selectByExample(tExample);
        if (null == trades || (null != trades && trades.size() < 1)) {
            welinkVO.setCode(BizErrorEnum.CAN_NOT_FIND_TRADE.getCode());
            welinkVO.setMsg(BizErrorEnum.CAN_NOT_FIND_TRADE.getMsg());
            welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
            return JSON.toJSONString(welinkVO);
        }
        Trade trade = trades.get(0);
        List<Order> orders = new ArrayList<Order>();
        List<Order> tmpOrders = new ArrayList<Order>();
        if (trade.getOrders().length() > 0) {
            for (String id : trade.getOrders().split(";")) {
                OrderExample orderExample = new OrderExample();
                orderExample.createCriteria().andIdEqualTo(Long.valueOf(id));
                orders = orderMapper.selectByExample(orderExample);
                if (null != orders && orders.size() > 0) {
                    tmpOrders.add(orders.get(0));
                }
            }
        }
        //针对每条评论进行处理
        String mobile = null;
        String nick = null;
        ProfileDO profileDO = profileDOMapper.selectByPrimaryKey(profileId);
        if (profileDO != null) {
            mobile = profileDO.getMobile();
            nick = profileDO.getNickname();
        }
        for (Order o : tmpOrders) {
            for (EvalJson evl : eevals) {
                if (Long.compare(evl.getItemId(), o.getArtificialId()) == 0) {
                    if (!evalService.addEval(profileId, tradeId, o.getId(), evl.getItemId(), evl.getEvalCode(),
                            evl.getPics(), evl.getContent(), mobile, nick, null, BizConstants.OrderEvalType.BUYER_EVAL.getType())) {
                        logger.error("add eval failed. tradeId:" + tradeId + ",itemId:" + evl.getItemId());
                    }
                }
            }
        }
        //更新trade
        Trade trade1 = new Trade();
        trade1.setBuyerRate(Constants.RateType.RATED.getRateTypeId());
        trade1.setCanRate((byte) 0);
        trade1.setLastUpdated(new Date());
        TradeExample tradeExample = new TradeExample();
        tExample.createCriteria().andTradeIdEqualTo(trade.getTradeId());
        List<Trade> tradeList = tradeMapper.selectByExample(tradeExample);
        if (tradeList != null && tradeList.size() > 0) {
            trade1.setVersion(tradeList.get(0).getVersion() + 1L);
        }
        if (tradeMapper.updateByExampleSelective(trade1, tradeExample) < 1) {
            logger.error("update trade rate status failed. tradeId:" + tradeId);
        }
        welinkVO.setStatus(ResponseStatusEnum.SUCCESS.getCode());
        return JSON.toJSONString(welinkVO);
    }

    private boolean sessionObject(Session session, String key) {
        if (null == session) {
            return false;
        }
        return null != session.getAttribute(key);
    }
}
