package com.welink.commons.persistence

import com.welink.commons.domain.Item
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import javax.annotation.Resource

/**
 * Created by saarixx on 16/9/14.
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(locations = ["classpath:dataSource-buy.xml", "classpath:mapper-buy.xml"])
@ActiveProfiles("test")
class Mock extends Specification {

    @Resource
    ItemMapper itemMapper

    @Resource
    OrderMapper orderMapper

    @Resource
    TradeMapper tradeMapper

    void cleanup() {
//        itemMapper.deleteByExample(null)
    }


    def "insert 4 item for unit test"() {

        given:

        println itemMapper.insert(new Item(approveStatus: 1 as byte,
                categoryId: 100010L,
                dateCreated: new Date(),
                description: "家政服务，预约说明",
                num: 9999,
                subStock: 0 as byte,
                title: "家政服务"
        ))

        itemMapper.insert(new Item(approveStatus: 1 as byte,
                categoryId: 100011L,
                dateCreated: new Date(),
                description: "维修服务，预约说明",
                num: 9999,
                subStock: 0 as byte,
                title: "维修服务"
        ))

        itemMapper.insert(new Item(approveStatus: 1 as byte,
                categoryId: 100012L,
                dateCreated: new Date(),
                description: "送水服务，预约说明",
                num: 9999,
                price: 26125L,
                picUrls: "http://img10.360buyimg.com/n0/g14/M04/00/19/rBEhVlNeKvQIAAAAAAIUDgWN7-MAAMu6gPfpPsAAhQm624.jpg",
                subStock: 0 as byte,
                title: "法国进口 巴黎水青柠味天然含汽矿泉水750ml*12 整箱（Perrier） 矿泉水"
        ))

        itemMapper.insert(new Item(approveStatus: 1 as byte,
                categoryId: 100012L,
                dateCreated: new Date(),
                description: "送水服务，预约说明",
                num: 9999,
                price: 2790L,
                picUrls: "http://img10.360buyimg.com/n0/g14/M03/0A/1D/rBEhVlIMr2YIAAAAAAHH_1OLEKQAACIbQIi3uwAAcgX256.jpg",
                subStock: 0 as byte,
                title: "农夫山泉 天然饮用水4L*4桶 整箱"
        ))

        when: "insert"

        then:
        itemMapper.selectByExample().size() == 4

    }
}
