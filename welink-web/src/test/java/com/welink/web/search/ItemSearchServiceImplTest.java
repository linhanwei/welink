package com.welink.web.search;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.welink.biz.common.model.SearchResult;
import com.welink.biz.service.ItemSearchService;
import com.welink.biz.service.ItemService;
import com.welink.biz.util.OpenSearchType;
import com.welink.commons.domain.Item;
import com.welink.commons.domain.SearchItem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
@ActiveProfiles("dev")
public class ItemSearchServiceImplTest {

    static Logger logger = LoggerFactory.getLogger(ItemSearchServiceImplTest.class);

    @Resource
    private ItemSearchService itemSearchService;

    @Resource
    private ItemService itemService;

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void defaultSearch() {
        SearchResult<Item> itemSearchResult = itemSearchService.defaultSearch(999L, null, 20000026L, null, 0, 10, OpenSearchType.RECOMMEND_DESC, Collections.<Long>emptyList(), false);
        logger.info(" ----> {}", JSON.toJSONString(itemSearchResult, true));
        assertThat(itemSearchResult.isSuccess(), is(true));
        assertThat(itemSearchResult.getResult().getSearchTime(), greaterThan(0f));
        assertThat(itemSearchResult.getResult().getResultList().size(), is(10));
    }

    @Test
    public void defaultSearchWithQuery() {
        SearchResult<Item> itemSearchResult = itemSearchService.defaultSearch(999L, "苹果", 20000011L, null, 0, 10, OpenSearchType.SOLD_DESC, Collections.<Long>emptyList(), false);
        logger.info(" ----> {}", JSON.toJSONString(itemSearchResult, true));
        assertThat(itemSearchResult.isSuccess(), is(true));
        assertThat(itemSearchResult.getResult().getSearchTime(), greaterThan(0f));
        assertThat(itemSearchResult.getResult().getResultList().size(), lessThanOrEqualTo(10));
    }

    @Test
    public void listSearchWithCache() {
//        List<Item> itemList = itemService.fetchOpenSearchItemsWithCache(BizConstants.WELINK_ID, null, 20000027L, 0, 10, OpenSearchType.RECOMMEND_DESC.getType());
//        System.out.println(itemList.size());
//        logger.info(" ----> {}", JSON.toJSONString(itemList, true));
//        assertThat(itemList.size() > 0, is(true));
    }

    @Test
    public void testTransform() throws Exception {
        SearchItem searchItem = new SearchItem();
        searchItem.setId(1L);
        searchItem.setTitle("你知道的");
        searchItem.setDateCreated(new Date());
        searchItem.setLastUpdated(new Date());
        searchItem.setTagId(199L);
        searchItem.setCategoryId(1999L);
        searchItem.setOnlineEndTime(System.currentTimeMillis());
        searchItem.setOnlineStartTime(System.currentTimeMillis());
        searchItem.setPromotionPrice(100L);
        searchItem.setPrice(0L);
        searchItem.setStatus((byte) 3);

        System.out.println("====================================");
        System.out.println(JSON.toJSONString(searchItem,
                        SerializerFeature.WriteMapNullValue,
                        SerializerFeature.WriteNullNumberAsZero,
                        SerializerFeature.WriteNullStringAsEmpty,
                        SerializerFeature.WriteNullBooleanAsFalse)
        );
        System.out.println("====================================");
    }
}