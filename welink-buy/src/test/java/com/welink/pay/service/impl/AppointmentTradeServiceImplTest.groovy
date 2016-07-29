package com.welink.pay.service.impl

import com.google.common.collect.Lists
import com.welink.buy.service.AppointmentTradeService
import com.welink.buy.utils.BaseResult
import com.welink.buy.utils.Constants
import com.welink.commons.domain.Item
import com.welink.commons.domain.ItemExample
import com.welink.commons.domain.Trade
import com.welink.commons.domain.TradeExample
import com.welink.commons.persistence.ItemMapper
import com.welink.commons.persistence.OrderMapper
import org.apache.commons.lang3.tuple.ImmutablePair
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import javax.annotation.Resource

/**
 * Created by saarixx on 16/9/14.
 */

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(locations = ["classpath:buy-applicationContext.xml"])
@ActiveProfiles("dev")
class AppointmentTradeServiceImplTest extends Specification {

    @Resource
    ItemMapper itemMapper;

    @Resource
    OrderMapper orderMapper;

    @Resource
    AppointmentTradeService appointmentTradeService;

    void cleanup() {

    }

    def "maximum of two numbers"(int a, int b, int c) {
        expect:
        Math.max(a, b) == c

        where:
        a | b | c
        1 | 3 | 3
        7 | 4 | 7
        0 | 0 | 0
    }

    def "insert new household management appointment"() {

        given:
        ItemExample itemExample = new ItemExample();
        itemExample.createCriteria() //
                .andCategoryIdEqualTo(Constants.AppointmentServiceCategory.BottledWaterService.getCategoryId());
        List<Item> itemList = itemMapper.selectByExample(itemExample)

        List<ImmutablePair> immutablePairList = Lists.newArrayList()
        itemList.each {
            Item item ->
                immutablePairList.add(ImmutablePair.of(item.id, new Random().nextInt(10)))

        }

        when: "create new trade"
        BaseResult<Trade> tradeBaseResult =
                appointmentTradeService.createNewAppointment(199L, 911L, 4343L, immutablePairList, new Date(), null, "那就这样吧，都去死吧", (byte) 1)

        then:
        tradeBaseResult.isSuccess() == true
        tradeBaseResult.getResult().getStatus() == 2
        TradeExample tradeExample = new TradeExample()
        tradeExample.createCriteria() //
                .andCommunityIdEqualTo(199L) //
                .andBuyerIdEqualTo(911L);

        appointmentTradeService.findByExample(tradeExample).getResult().size() > 0


    }
}
