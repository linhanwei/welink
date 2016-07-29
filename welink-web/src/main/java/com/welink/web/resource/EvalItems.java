package com.welink.web.resource;

import com.welink.buy.service.AppointmentTradeService;
import com.welink.commons.persistence.OrderEvaluateDOMapper;
import com.welink.commons.persistence.OrderMapper;
import org.apache.shiro.session.Session;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by daniel on 14-11-14.
 */
@RestController
public class EvalItems {

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(EvalItems.class);

    @Resource
    private AppointmentTradeService appointmentService;

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private OrderEvaluateDOMapper orderEvaluateDOMapper;

    @RequestMapping(value = {"/api/m/1.0/evalItems.json", "/api/h/1.0/evalItems.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        //get parameters
//        long tradeId = ParameterUtil.getParameterAslong("tradeId");
//        long profileId = -1;
//        ActionContext context = ActionContext.getContext();
//        ResponseResult result = new ResponseResult();
//        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
//        Session session = currentUser.getSession();
//        try {
//            if (session == null || !sessionObject(session, "profileId") || !sessionObject(session, "mobile")) {
//                result.setStatus(ResponseStatusEnum.FAILED.getCode());
//                result.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
//                result.setErrorCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
//                context.put("result", result);
//                return SUCCESS;
//            }
//            profileId = (long) session.getAttribute("profileId");
//        } catch (Exception e) {
//            logger.error("fetch paras from session failed . exp:" + e.getMessage());
//        }
//        //fetch order info
//        TradeExample tradeExample = new TradeExample();
//        tradeExample.createCriteria().andTradeIdEqualTo(tradeId);
//        BaseResult<List<Trade>> tradeList = appointmentService.findByExample(tradeExample);
//        if (null != tradeList && tradeList.getResult() != null && tradeList.getResult().size() > 0) {
//            StringUtil stringUtil = new StringUtil();
//            context.put("stringUtil", stringUtil);
//            Trade trade = tradeList.getResult().get(0);
//            context.put("trade", trade);
//            List<Order> orders = new ArrayList<Order>();
//            if (trade.getOrders().length() > 0) {
//                for (String id : trade.getOrders().split(";")) {
//                    OrderExample orderExample = new OrderExample();
//                    orderExample.createCriteria().andIdEqualTo(Long.valueOf(id));
//                    List<Order> tempOrders = orderMapper.selectByExample(orderExample);
//                    if (null != tempOrders && tempOrders.size() > 0) {
//                        if (Long.compare(tempOrders.get(0).getCategoryId(), Constants.AppointmentServiceCategory.PostFeeService.getCategoryId()) != 0) {
//                            orders.add(tempOrders.get(0));
//                        }
//                    }
//                }
//            }
//            context.put("orders", orders);
//            //是否评论过
//            List<Long> items = new ArrayList<>();
//            List<Long> ratedItemIds = new ArrayList<>();
//            for (Order o : orders) {
//                if (Long.compare(o.getCategoryId(), Constants.AppointmentServiceCategory.PostFeeService.getCategoryId()) == 0) {
//                    context.put("postFee", o.getPrice());
//                } else {
//                    items.add(o.getArtificialId());
//                }
//            }
//            if (items.size() > 0) {
//                OrderEvaluateDOExample oExample = new OrderEvaluateDOExample();
//                oExample.createCriteria().andItemIdIn(items).andUserIdEqualTo(profileId).andTradeIdEqualTo(tradeId);
//                List<OrderEvaluateDO> orderEvaluateDOs = orderEvaluateDOMapper.selectByExample(oExample);
//                if (null != orderEvaluateDOs && orderEvaluateDOs.size() > 0) {
//                    for (OrderEvaluateDO o : orderEvaluateDOs) {
//                        ratedItemIds.add(o.getItemId());
//                    }
//                }
//                context.put("ratedItemIds", ratedItemIds);
//            }
//        } else {
//            result.setStatus(ResponseStatusEnum.FAILED.getCode());
//            result.setErrorCode(BizErrorEnum.TRADE_NOT_FOUND.getCode());
//            result.setMsg(BizErrorEnum.TRADE_NOT_FOUND.getMsg());
//            context.put("result", result);
//            return SUCCESS;
//        }
//        StringUtil stringUtil = new StringUtil();
//        context.put("stringUtil", stringUtil);
//        result.setStatus(ResponseStatusEnum.SUCCESS.getCode());
//        context.put("result", result);
        return "";
    }

    private boolean sessionObject(Session session, String key) {
        if (null == session) {
            return false;
        }
        return null != session.getAttribute(key);
    }
}
