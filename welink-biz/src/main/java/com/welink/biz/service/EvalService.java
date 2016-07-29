package com.welink.biz.service;

import com.welink.biz.common.model.ItemEvalStatistics;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.OrderEvaluateDO;
import com.welink.commons.domain.OrderEvaluateDOExample;
import com.welink.commons.persistence.OrderEvaluateDOMapper;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * Created by daniel on 14-11-11.
 */
@Service
public class EvalService {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(EvalService.class);

    @Resource
    private OrderEvaluateDOMapper orderEvaluateDOMapper;

    /**
     * 获取商品统计信息
     *
     * @return
     */
    public ItemEvalStatistics sumEval(long itemId) {
        ItemEvalStatistics itemEvalStatistics = new ItemEvalStatistics();


        return itemEvalStatistics;
    }

    /**
     * 分页查找商品的评价信息
     *
     * @param limit
     * @param offset
     * @param itemId
     * @return
     */
    public List<OrderEvaluateDO> fetchEvalsByPage(int limit, int offset, long itemId) {
        OrderEvaluateDOExample oExample = new OrderEvaluateDOExample();
        oExample.setOrderByClause("id DESC");
        OrderEvaluateDOExample.Criteria criteria = oExample.createCriteria();
        criteria.andItemIdEqualTo(itemId).andRoleEqualTo(BizConstants.EvalRoleType.BUYER.getType()).andRateTypeEqualTo((byte) 1);
        oExample.setOffset(offset);
        oExample.setLimit(limit);
        return orderEvaluateDOMapper.selectByExample(oExample);
    }

    /**
     * 添加订单评论
     *
     * @param userId
     * @param tradeId
     * @param orderId
     * @param itemId
     * @param evalCode
     * @param pics
     * @param content
     * @param ratedMobile
     * @param ratedNick
     * @param itemTitle
     * @return
     */
    public boolean addEval(long userId, long tradeId, long orderId, long itemId, byte evalCode, String pics, String content, String ratedMobile,
                           String ratedNick, String itemTitle, byte evalType) {
        OrderEvaluateDO orderEvaluateDO = new OrderEvaluateDO();
        orderEvaluateDO.setAnonymous(BizConstants.OrderEvalAnonymousType.ANONYMOUS.getType());
        orderEvaluateDO.setContent(content);
        orderEvaluateDO.setDateCreated(new Date());
        orderEvaluateDO.setEvalCode(evalCode);
        orderEvaluateDO.setItemId(itemId);
        orderEvaluateDO.setItemTitle(itemTitle);
        orderEvaluateDO.setLastUpdated(new Date());
        orderEvaluateDO.setOrderId(orderId);
        orderEvaluateDO.setPics(pics);
        orderEvaluateDO.setRated_mobile(ratedMobile);
        orderEvaluateDO.setRatedNick(ratedNick);
        orderEvaluateDO.setRateType(evalType);
        orderEvaluateDO.setRole(evalType);
        orderEvaluateDO.setTradeId(tradeId);
        orderEvaluateDO.setUserId(userId);
        if (orderEvaluateDOMapper.insert(orderEvaluateDO) < 0) {
            log.error("insert order eval failed. itemId:" + itemId + ",userId:" + userId);
            return false;
        }
        return true;
    }
}
