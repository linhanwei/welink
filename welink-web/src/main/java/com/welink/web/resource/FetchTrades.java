package com.welink.web.resource;

import java.util.ArrayList;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.welink.biz.common.model.OrderViewDO;
import com.welink.biz.common.model.TradeViewDO;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.common.security.NeedProfile;
import com.welink.biz.service.ItemService;
import com.welink.biz.util.ViewDOCopy;
import com.welink.buy.service.AppointmentTradeService;
import com.welink.buy.utils.BaseResult;
import com.welink.buy.utils.Constants;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.CommunityDO;
import com.welink.commons.domain.Item;
import com.welink.commons.domain.Order;
import com.welink.commons.domain.OrderEvaluateDO;
import com.welink.commons.domain.OrderEvaluateDOExample;
import com.welink.commons.domain.OrderExample;
import com.welink.commons.domain.Trade;
import com.welink.commons.domain.TradeExample;
import com.welink.commons.domain.TradeExample.Criteria;
import com.welink.commons.persistence.CommunityDOMapper;
import com.welink.commons.persistence.OrderEvaluateDOMapper;
import com.welink.commons.persistence.OrderMapper;
import com.welink.commons.persistence.TradeMapper;
import com.welink.web.common.constants.ResponseStatusEnum;
import com.welink.web.common.filter.Profiler;
import com.welink.web.common.util.ParameterUtil;

/**
 * Created by daniel on 14-9-18.
 */
@RestController
public class FetchTrades {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(FetchTrades.class);

    @Resource
    private AppointmentTradeService appointmentService;

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private OrderEvaluateDOMapper orderEvaluateDOMapper;

    @Resource
    private ItemService itemService;
    
    @Resource
    private TradeMapper tradeMapper;

    @Resource
    private CommunityDOMapper communityDOMapper;

    /**
     * 
     * execute:(这里用一句话描述这个方法的作用). <br/>
     *
     * @author LuoGuangChun
     * @param request
     * @param response
     * @param buyerRate	0=未评价;1=已评价;
     */
    @NeedProfile
    @RequestMapping(value = {"/api/m/1.0/fetchTrades.json", "/api/h/1.0/fetchTrades.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response,
    		@RequestParam(value="buyerRate", required = false) Byte buyerRate) throws Exception {
        //get parameters
        long profileId = -1l;
        String tradeStatus = request.getParameter("tradeStatus");
        WelinkVO welinkVO = new WelinkVO();
        Map resultMap = new HashMap();

        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        profileId = (long) session.getAttribute("profileId");
        int page = ParameterUtil.getParameterAsIntForSpringMVC(request, "pg");
        int size = ParameterUtil.getParameterAsIntForSpringMVC(request, "sz");
        if (size < 1) {
            size = 7;
        }
        if (page < 0) {
            page = 0;
        }
        int startRow = (page) * size;
        List<Trade> tradeList = null;
        resultMap.put("hasNext", false);
        
        Trade tradeParam = new Trade();
        tradeParam.setBuyerId(profileId);
        if(null != buyerRate){
        	tradeParam.setBuyerRate(buyerRate);
        }
        
        if (StringUtils.isNotBlank(tradeStatus)) {
            Profiler.enter("fetch trades with status " + tradeStatus);
            String statuses[] = tradeStatus.split(",");
            List<Byte> tradeStatusList = new ArrayList<Byte>();
            for (String status : statuses) {
                tradeStatusList.add(Byte.valueOf(status));
            }
            if (StringUtils.equalsIgnoreCase("4,5", tradeStatus)) {
                tradeStatusList.add((byte) 6);
            }
            //BaseResult<List<Trade>> tradeResult = appointmentService.findByBuyerTradesWithStatusAndType(profileId, tradeStatusList, startRow, size);
            BaseResult<List<Trade>> tradeResult = findTradesByParams(tradeParam, tradeStatusList, startRow, size);
            if (null != tradeResult && tradeResult.isSuccess()) {
                tradeList = tradeResult.getResult();
            }
            if (null != tradeList && tradeList.size() < size) {
                resultMap.put("hasNext", false);
            } else {
                resultMap.put("hasNext", true);
            }
            Profiler.release();
        } else {
            Profiler.enter("fetch trades with status 0");
            //BaseResult<List<Trade>> tradeResult = appointmentService.findByBuyerTrades(profileId, startRow, size);
            BaseResult<List<Trade>> tradeResult = findTradesByParams(tradeParam, null, startRow, size);
            if (null != tradeResult && tradeResult.isSuccess() && tradeResult.getResult() != null) {
                tradeList = tradeResult.getResult();
            } else {
                welinkVO.setStatus(ResponseStatusEnum.SUCCESS.getCode());
                return JSON.toJSONString(welinkVO);
            }
            if (null != tradeList && tradeList.size() < size) {
                resultMap.put("hasNext", false);
            } else {
                resultMap.put("hasNext", true);
            }
            Profiler.release();
        }
        List<TradeViewDO> tradeViewDOs = Lists.newArrayList();
        Map<String, List<Order>> tradeOrders = new HashMap<String, List<Order>>();
        if (null != tradeList) {
            List<Long> ratedTradeIds = new ArrayList<>();
            for (Trade trade : tradeList) {
                //评价
                Profiler.enter("fetch trade rate");
                if (trade.getStatus() == Constants.TradeStatus.TRADE_FINISHED.getTradeStatusId()) {
                    OrderEvaluateDOExample oExample = new OrderEvaluateDOExample();
                    List<Long> rateUsers = new ArrayList<>();
                    rateUsers.add(profileId);
                    rateUsers.add(BizConstants.WELINK_RATE_ID);
                    oExample.createCriteria().andTradeIdEqualTo(trade.getTradeId()).andUserIdIn(rateUsers);//
                    List<OrderEvaluateDO> orderEvaluateDOs = orderEvaluateDOMapper.selectByExample(oExample);
                    if (null != orderEvaluateDOs && orderEvaluateDOs.size() > 0) {
                        ratedTradeIds.add(trade.getTradeId());
                    }
                }
                Profiler.release();
                List<Order> tmpOrders = new ArrayList<Order>();
                List<Long> itemIds = Lists.newArrayList();
                if (trade.getOrders().length() > 0) {
                    for (String id : trade.getOrders().split(";")) {
                        Profiler.enter("fetch trade order with " + id);
                        OrderExample orderExample = new OrderExample();
                        orderExample.createCriteria().andIdEqualTo(Long.valueOf(id));
                        List<Order> orders = orderMapper.selectByExample(orderExample);
                        if (null != orders && orders.size() > 0) {
                            if (null == orders.get(0).getCategoryId() ||
                            		(null != orders.get(0).getCategoryId() && 
                            		Long.compare(orders.get(0).getCategoryId(), Constants.AppointmentServiceCategory.PostFeeService.getCategoryId()) != 0)) {
                            	tmpOrders.add(orders.get(0));
                            	itemIds.add(orders.get(0).getArtificialId());
                            }
                        }
                        Profiler.release();
                    }
                }
                tradeOrders.put(trade.getTradeId().toString(), tmpOrders);
                //查找站點
                Long communityId = trade.getCommunityId();
                CommunityDO communityDO = communityDOMapper.selectByPrimaryKey(communityId);

                TradeViewDO tradeViewDO = ViewDOCopy.buildTradeViewDO(trade, communityDO);
                Map<Long, Item> itemMap = new HashMap<>();
                BaseResult<List<Item>> itemsResult = itemService.fetchItemsByItemIds(itemIds);
                if (null != itemsResult && itemsResult.isSuccess() && itemsResult.getResult() != null) {
                    for (Item item : itemsResult.getResult()) {
                        itemMap.put(item.getId(), item);
                    }
                }
                List<OrderViewDO> orderViewDOs = Lists.newArrayList();
                for (Order order : tmpOrders) {
                    OrderViewDO orderViewDO = ViewDOCopy.buildOrderViewDO(order);
                    if (null != itemMap.get(order.getArtificialId())) {
                        orderViewDO.setSpecification(itemMap.get(order.getArtificialId()).getSpecification());
                    }
                    orderViewDOs.add(orderViewDO);
                }
                tradeViewDO.setOrderViewDOs(orderViewDOs);
                tradeViewDOs.add(tradeViewDO);
            }
            welinkVO.setStatus(1);
            resultMap.put("trades", tradeViewDOs);
            welinkVO.setResult(resultMap);
        }

        return JSON.toJSONString(welinkVO);
    }
    
    public BaseResult<List<Trade>> findTradesByParams(Trade trade, List<Byte> tradeStatusList, int offset, int limit){
    	TradeExample tradeExample = new TradeExample();
        Criteria createCriteria = tradeExample.createCriteria(); //
        createCriteria.andBuyerIdEqualTo(trade.getBuyerId()); //
        if(null != trade.getBuyerRate()){
        	createCriteria.andBuyerRateEqualTo(trade.getBuyerRate());
        }
        if(null != tradeStatusList && !tradeStatusList.isEmpty()){
        	createCriteria.andStatusIn(tradeStatusList);
        }
        createCriteria.andTypeNotEqualTo(Constants.TradeType.crowdfund_type.getTradeTypeId());	//不为众筹订单
        tradeExample.setOrderByClause("id DESC");
        tradeExample.setOffset(offset);
        tradeExample.setLimit(limit);
        List<Trade> trades = tradeMapper.selectByExample(tradeExample);
        return new BaseResult<>(trades).putExternal("total", trades.size());
    }
    
    //setters
    public void setAppointmentService(AppointmentTradeService appointmentService) {
        this.appointmentService = appointmentService;
    }

    public void setOrderMapper(OrderMapper orderMapper) {
        this.orderMapper = orderMapper;
    }
}
