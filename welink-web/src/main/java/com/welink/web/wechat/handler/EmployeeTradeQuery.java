package com.welink.web.wechat.handler;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.welink.commons.domain.*;
import com.welink.commons.persistence.*;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by saarixx on 27/12/14.
 */
@Service
public class EmployeeTradeQuery {

    static DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");

    static Logger logger = LoggerFactory.getLogger(EmployeeTradeQuery.class);

    @Resource
    private ProfileDOMapper profileDOMapper;

    @Resource
    private TradeMapper tradeMapper;

    @Resource
    private LogisticsDOMapper logisticsDOMapper;

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private ProfileWeChatDOMapper profileWeChatDOMapper;

    @Resource
    private EmployeeDOMapper employeeDOMapper;

    @Resource
    private WeChatProfileDOMapper weChatProfileDOMapper;

    public boolean isEmployee(String openId) {
        ProfileWeChatDOExample profileWeChatDOExample = new ProfileWeChatDOExample();
        profileWeChatDOExample.createCriteria().andOpenidEqualTo(openId);
        List<ProfileWeChatDO> profileWeChatDOs = profileWeChatDOMapper.selectByExample(profileWeChatDOExample);

        if (profileWeChatDOs.isEmpty()) {
            return false;
        }

        ProfileWeChatDO profileWeChatDO = profileWeChatDOs.get(0);

        String unionId = profileWeChatDO.getUnionId();

        WeChatProfileDOExample weChatProfileDOExample = new WeChatProfileDOExample();
        weChatProfileDOExample.createCriteria().andUnionIdEqualTo(unionId);
        List<WeChatProfileDO> weChatProfileDOs = weChatProfileDOMapper.selectByExample(weChatProfileDOExample);

        if (weChatProfileDOs.isEmpty()) {
            return false;
        }

        WeChatProfileDO weChatProfileDO = weChatProfileDOs.get(0);

        ProfileDO profileDO = profileDOMapper.selectByPrimaryKey(weChatProfileDO.getProfileId());

        EmployeeDOExample employeeDOExample = new EmployeeDOExample();
        employeeDOExample.createCriteria().andMobileEqualTo(profileDO.getMobile()) //
                .andStatusEqualTo((byte) 1);

        return !employeeDOMapper.selectByExample(employeeDOExample).isEmpty();
    }


    public Optional<String> query(String mobile) {

        if (StringUtils.isBlank(mobile)) {
            return Optional.absent();
        }

        try {

            ProfileDOExample profileDOExample = new ProfileDOExample();
            profileDOExample.createCriteria().andMobileEqualTo(mobile);

            List<ProfileDO> profileDOs = profileDOMapper.selectByExample(profileDOExample);

            DateTime dateTime = new DateTime();
            dateTime = dateTime.minusDays(3);

            LogisticsDOExample logisticsDOExample = new LogisticsDOExample();
            logisticsDOExample.createCriteria().andMobileEqualTo(mobile) //
                    .andDateCreatedGreaterThan(dateTime.toDate());

            List<LogisticsDO> logisticsDOs = logisticsDOMapper.selectByExample(logisticsDOExample);

            TradeExample tradeExample = new TradeExample();

            if (!profileDOs.isEmpty()) {
                tradeExample.or().andBuyerIdIn(Lists.<Long>newArrayList(Collections2.transform(profileDOs, new Function<ProfileDO, Long>() {
                    @Nullable
                    @Override
                    public Long apply(ProfileDO profileDO) {
                        return profileDO.getId();
                    }
                })));
            }

            if (!logisticsDOs.isEmpty()) {
                tradeExample.or().andConsigneeIdIn(Lists.<Long>newArrayList(Collections2.transform(logisticsDOs, new Function<LogisticsDO, Long>() {
                    @Nullable
                    @Override
                    public Long apply(LogisticsDO logisticsDO) {
                        return logisticsDO.getId();
                    }
                })));
            }

            List<Trade> trades = Lists.newArrayList();

            if (!tradeExample.getOredCriteria().isEmpty()) {
                tradeExample.setOffset(0);
                tradeExample.setLimit(3);

                tradeExample.setOrderByClause("id DESC");

                trades = tradeMapper.selectByExample(tradeExample);
            }

            String ret = make(trades, mobile);

            return Optional.of(ret);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Optional.absent();
        }
    }

    protected String make(List<Trade> trades, String mobile) {
        StringBuilder stringBuilder = new StringBuilder("手机号[" + mobile + "]最近3笔相关订单：\n");

        // 如果没有订单
        if (trades.isEmpty()) {
            return stringBuilder.append("未找到关联订单，用户既不是购买者也不是收货者。").toString();
        }


        for (Trade trade : trades) {
            String dateCreated = new DateTime(trade.getDateCreated()).toString(formatter);
            stringBuilder.append("  下单时间：" + dateCreated + "\n");
            stringBuilder.append("  订单状态： " + getTradeStatus(trade.getStatus()) + "\n");
            stringBuilder.append("  订单总计： " + trade.getTotalFee() / 100f + "\n");

            LogisticsDO logisticsDO = logisticsDOMapper.selectByPrimaryKey(trade.getConsigneeId());

            stringBuilder.append("  收货地址： " + logisticsDO.getAddr() + "\n");
            stringBuilder.append("  收货人： " + logisticsDO.getContactName() + " " + logisticsDO.getMobile() + "\n");

            OrderExample orderExample = new OrderExample();
            List<Long> orderIds = Lists.newArrayList();
            String[] orderStringIds = StringUtils.split(trade.getOrders(), ';');
            for (String orderStringId : orderStringIds) {
                orderIds.add(Long.parseLong(orderStringId));
            }
            orderExample.createCriteria().andIdIn(orderIds);
            List<Order> orders = orderMapper.selectByExample(orderExample);
            for (Order order : orders) {
                stringBuilder.append("    " + order.getTitle() + " " + order.getNum() + " 总计：" + order.getTotalFee() / 100f + "\n");
            }

            ProfileDO profileDO = profileDOMapper.selectByPrimaryKey(trade.getBuyerId());


            stringBuilder.append("  购买人：" + profileDO.getMobile() + "\n");
            stringBuilder.append("\n");
            stringBuilder.append("\n");

        }

        return stringBuilder.toString();

    }

    protected String getTradeStatus(Byte status) {
        switch (status) {
            case 2:
                return "已下单，待付款";

            case 4:
                return "已付款，待发货";

            case 5:
                return "已发货，待收货";

            case 7:
                return "交易成功";

            case 9:
                return "交易关闭";
        }

        throw new UnsupportedOperationException();
    }

    public static <T> List<T> subList(@Nonnull List<T> list,
                                      @Nonnegative int offset,
                                      @Nonnegative int limit) {
        Iterator<T> iterator = list.iterator();
        if (offset == Iterators.advance(iterator, offset)) {
            return Lists.newArrayList(Iterators.limit(iterator, limit));
        }
        return Collections.emptyList();
    }

}
