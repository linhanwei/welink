package com.welink.web.resource;

import static com.google.common.base.Preconditions.checkNotNull;

import com.alibaba.fastjson.JSON;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.service.ItemService;
import com.welink.biz.service.TradeService;
import com.welink.biz.service.UsePromotionService;
import com.welink.buy.service.AppointmentTradeService;
import com.welink.buy.utils.Constants;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.GrouponDO;
import com.welink.commons.domain.GrouponDOExample;
import com.welink.commons.domain.ObjectTaggedDO;
import com.welink.commons.domain.Order;
import com.welink.commons.domain.OrderExample;
import com.welink.commons.domain.Trade;
import com.welink.commons.domain.TradeExample;
import com.welink.commons.persistence.GrouponDOMapper;
import com.welink.commons.persistence.ObjectTaggedDOMapper;
import com.welink.commons.persistence.OrderMapper;
import com.welink.commons.persistence.TradeMapper;
import com.welink.web.common.filter.Profiler;
import com.welink.web.common.util.ParameterUtil;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.bind.annotation.RequestMapping;
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
 * Created by daniel on 14-9-18.
 */
@RestController
public class CancelOrder {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(CancelOrder.class);

    @Resource
    private TradeMapper tradeMapper;

    @Resource
    private UsePromotionService usePromotionService;

    @Resource
    private ItemService itemService;
    
    @Resource
    private TradeService tradeService;
    
    @Resource
    private GrouponDOMapper grouponDOMapper;
    
    @Resource
    private OrderMapper orderMapper;
    
    @Resource
    private AppointmentTradeService appointmentService;
    
    @Resource
    private ObjectTaggedDOMapper objectTaggedDOMapper;

    @RequestMapping(value = {"/api/m/1.0/cancelOrder2.json", "/api/h/1.0/cancelOrder2.json"}, produces = "application/json;charset=utf-8")
    public String cancelOrder2(HttpServletRequest request, HttpServletResponse response) throws Exception {
        long tradeId = ParameterUtil.getParameterAslongForSpringMVC(request, "tradeId", -1l);
        long profileId = -1;

        WelinkVO welinkVO = new WelinkVO();
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        try {
            if (session == null || !sessionObject(session, "profileId")) {
                welinkVO.setStatus(0);
                welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
                welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
                return JSON.toJSONString(welinkVO);
            }
            profileId = (long) session.getAttribute("profileId");
        } catch (Exception e) {
            log.error("fetch paras from session failed . exp:" + e.getMessage() + ",sessionId:" + session.getId());
            welinkVO.setStatus(0);
            welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
            welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
            return JSON.toJSONString(welinkVO);
        }

        if (tradeId < 0) {
            welinkVO.setStatus(0);
            welinkVO.setMsg(BizErrorEnum.PARAMS_ERROR.getMsg());
            welinkVO.setCode(BizErrorEnum.PARAMS_ERROR.getCode());
            Profiler.release();
            return JSON.toJSONString(welinkVO);
        }

        TradeExample tradeExample = new TradeExample();
        tradeExample.createCriteria().andTradeIdEqualTo(tradeId);
        List<Trade> tradeList = tradeMapper.selectByExample(tradeExample);
        if (tradeList != null && tradeList.size() > 0) {
            Trade trade = tradeList.get(0);
            if (trade.getStatus() == Constants.TradeStatus.WAIT_BUYER_PAY.getTradeStatusId() && trade.getBuyerId() == profileId) {
                trade.setStatus(Constants.TradeStatus.TRADE_CLOSED_BY_TAOBAO.getTradeStatusId());
                trade.setVersion(trade.getVersion() + 1);
                trade.setLastUpdated(new Date());
                trade.setEndTime(new Date());
                if (tradeMapper.updateByExampleSelective(trade, tradeExample) < 1) {
                    log.error("cancel trade failed. tradeId:" + trade.getTradeId() + ",sessionId:" + session.getId());
                }
                usePromotionService.changePromotionFrozenToUnUsed(trade.getTradeId(), trade.getBuyerId());
                //删除购买记录
                itemService.deleteBuyRecord(trade);
                
                List<Order> orderList = findOrdersByTradeId(tradeId);	//根据交易号查找订单列表
                if(null != orderList && !orderList.isEmpty()){
                	for(Order order : orderList){
                		if(null != order){
                			updateStock2(trade, order, null, 2);		//更新库存(2=加库存)
                		}
                	}
                }
                
                welinkVO.setStatus(1);
                return JSON.toJSONString(welinkVO);
            } else if (trade.getStatus() == Constants.TradeStatus.TRADE_CLOSED_BY_TAOBAO.getTradeStatusId() && trade.getBuyerId() == profileId) {
                itemService.deleteBuyRecord(trade);
                welinkVO.setStatus(1);
                return JSON.toJSONString(welinkVO);
            } else {
                welinkVO.setStatus(0);
                welinkVO.setMsg(BizErrorEnum.PARAMS_ERROR.getMsg());
                welinkVO.setCode(BizErrorEnum.PARAMS_ERROR.getCode());
                Profiler.release();
                return JSON.toJSONString(welinkVO);
            }
        } else {
            welinkVO.setStatus(0);
            welinkVO.setMsg(BizErrorEnum.PARAMS_ERROR.getMsg());
            welinkVO.setCode(BizErrorEnum.PARAMS_ERROR.getCode());
            Profiler.release();
            return JSON.toJSONString(welinkVO);
        }
    }
    
    @RequestMapping(value = {"/api/m/1.0/cancelOrder.json", "/api/h/1.0/cancelOrder.json"}, produces = "application/json;charset=utf-8")
    public String cancelOrder(HttpServletRequest request, HttpServletResponse response) throws Exception {
        long tradeId = ParameterUtil.getParameterAslongForSpringMVC(request, "tradeId", -1l);
        long profileId = -1;

        WelinkVO welinkVO = new WelinkVO();
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        try {
            if (session == null || !sessionObject(session, "profileId")) {
                welinkVO.setStatus(0);
                welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
                welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
                return JSON.toJSONString(welinkVO);
            }
            profileId = (long) session.getAttribute("profileId");
        } catch (Exception e) {
            log.error("fetch paras from session failed . exp:" + e.getMessage() + ",sessionId:" + session.getId());
            welinkVO.setStatus(0);
            welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
            welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
            return JSON.toJSONString(welinkVO);
        }

        if (tradeId < 0) {
            welinkVO.setStatus(0);
            welinkVO.setMsg(BizErrorEnum.PARAMS_ERROR.getMsg());
            welinkVO.setCode(BizErrorEnum.PARAMS_ERROR.getCode());
            Profiler.release();
            return JSON.toJSONString(welinkVO);
        }

        TradeExample tradeExample = new TradeExample();
        tradeExample.createCriteria().andTradeIdEqualTo(tradeId);
        List<Trade> tradeList = tradeMapper.selectByExample(tradeExample);
        if (tradeList != null && tradeList.size() > 0) {
            Trade trade = tradeList.get(0);
            if (trade.getStatus() == Constants.TradeStatus.WAIT_BUYER_PAY.getTradeStatusId() && trade.getBuyerId() == profileId) {
                tradeService.cancelOrder(trade);
                welinkVO.setStatus(1);
                return JSON.toJSONString(welinkVO);
            } else if (trade.getStatus() == Constants.TradeStatus.TRADE_CLOSED_BY_TAOBAO.getTradeStatusId() && trade.getBuyerId() == profileId) {
                itemService.deleteBuyRecord(trade);
                welinkVO.setStatus(1);
                return JSON.toJSONString(welinkVO);
            } else {
                welinkVO.setStatus(0);
                welinkVO.setMsg(BizErrorEnum.PARAMS_ERROR.getMsg());
                welinkVO.setCode(BizErrorEnum.PARAMS_ERROR.getCode());
                Profiler.release();
                return JSON.toJSONString(welinkVO);
            }
        } else {
            welinkVO.setStatus(0);
            welinkVO.setMsg(BizErrorEnum.PARAMS_ERROR.getMsg());
            welinkVO.setCode(BizErrorEnum.PARAMS_ERROR.getCode());
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

    public void setUsePromotionService(UsePromotionService usePromotionService) {
        this.usePromotionService = usePromotionService;
    }
    
    public List<Order> findOrdersByTradeId(Long tradeId){
    	checkNotNull(tradeId);
    	OrderExample orderExample = new OrderExample();
    	orderExample.createCriteria().andTradeIdEqualTo(tradeId).andArtificialIdGreaterThan(0L);
    	List<Order> orderList = orderMapper.selectByExample(orderExample);
    	return orderList;
    }
    
    /**
     * 更新库存2
     *
     * @param type 如果type=2加库存,type=1或其它减库存
     * @param order
     * @return
     */
    private boolean updateStock2(Trade trade, Order order, TransactionStatus transactionStatus, Integer type) {
        if (Long.compare(order.getCategoryId(), Constants.AppointmentServiceCategory.PostFeeService.getCategoryId()) != 0) {
            long itemId = order.getArtificialId();
            int num = order.getNum();
            if(itemId < 1){
            	return false;
            }
            GrouponDOExample gExample = new GrouponDOExample();
            gExample.createCriteria().andItemIdEqualTo(itemId).andStatusEqualTo(BizConstants.ItemApproveStatus.ON_SALE.getStatus());
            gExample.setOrderByClause("online_end_time DESC");
            List<GrouponDO> grouponDOs = grouponDOMapper.selectByExample(gExample);
            Map<String,Object> map = new HashMap<String, Object>();
            if(2 == type){		//如果type=2加库存,type=1或其它减库存
            	map.put("num", -num);
            }else{			//其它减库存
            	map.put("num", num);
            }
            map.put("itemId", itemId);
            //是团购商品
            if (null != grouponDOs && grouponDOs.size() > 0) {
        		if(tradeMapper.updateGrouponItemNum(map) < 1){
        			log.error("update groupon stock quantity failed. itemId:" + itemId + ",num:" + num);
        			return false;
        		}else{
        			if (tradeMapper.updateItemNum(map) < 1) {
        				log.error("update item sold quantity and stock failed. itemId:" + itemId + ",num:" + num);
                        //com.welink.web.common.filter.Profiler.release();
                        return false;
                    }
        		}
            }else{
            	
            	if (tradeMapper.updateItemNum(map) < 1) {
            		log.error("update item sold quantity and stock failed. itemId:" + itemId + ",num:" + num);
                    //com.welink.web.common.filter.Profiler.release();
                    return false;
                }
            }
            List<Long> itemIds = new ArrayList<Long>();
            itemIds.add(itemId);
            //获取抢购活动标
            List<ObjectTaggedDO> panicBuyingItemTags =  itemService.fetchTagObjectsViaItemIds(itemIds, BizConstants.SearchTagEnum.PANIC_BUYING.getTag());
            if(null != panicBuyingItemTags && !panicBuyingItemTags.isEmpty()){
            	ObjectTaggedDO objectTaggedDO = panicBuyingItemTags.get(0);
            	Integer activityNum = (null == objectTaggedDO.getActivityNum() ? 0 : objectTaggedDO.getActivityNum());
            	Integer activitySoldNum = (null == objectTaggedDO.getActivitySoldNum() ? 0 : objectTaggedDO.getActivitySoldNum());
            	if(2 == type){		//如果type=2加库存,type=1或其它减库存
                	objectTaggedDO.setActivityNum(activityNum+num);
                	objectTaggedDO.setActivitySoldNum(activitySoldNum-num);
                }else{			//其它减库存
                	objectTaggedDO.setActivityNum(activityNum-num);
                	objectTaggedDO.setActivitySoldNum(activitySoldNum+num);
                }
            	if(objectTaggedDOMapper.updateByPrimaryKeySelective(objectTaggedDO) < 1){
            		log.error("update item sold quantity and stock failed. itemId:" + itemId + ",num:" + num);
                    //com.welink.web.common.filter.Profiler.release();
                    return false;
            	}
            }
        }
        return true;
    }
    
}
