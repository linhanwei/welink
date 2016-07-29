package com.welink.commons.persistence

import com.welink.commons.domain.Item
import com.welink.commons.domain.ItemExample
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import javax.annotation.Resource

/**
 * Created by saarixx on 14/9/14.
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(locations = ["classpath:commons-dataSource.xml", "classpath:commons-mapper.xml"])
@ActiveProfiles("test")
class ItemMapperTest extends Specification {

    @Resource
    ItemMapper itemMapper

    List<Item> itemList = []

    void setup() {

    }

    void cleanup() {
        itemList.each {
            Item item ->
                itemMapper.deleteByPrimaryKey(item.id)
        }

    }

    def "test insert new item and assert it exist"() {
        given: "new item"


        when: "insert"
        ItemExample itemExample = new ItemExample();
        itemExample.limit = 1
        itemExample.offset = 1
        int row = itemMapper.selectByExample(itemExample)

        then:
        row == 1
    }
}
