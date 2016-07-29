package com.welink.commons.mybaits.plugins;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.Subscribe;
import com.welink.commons.domain.Item;
import com.welink.commons.domain.ItemExample;
import com.welink.commons.events.ItemUpdateEvent;
import com.welink.commons.persistence.ItemMapper;
import com.welink.commons.persistence.ProfileExtDOMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:commons-applicationContext.xml"})
@ActiveProfiles("test")
public class ItemUpdateMapperInterceptorTest {

    @Resource
    private ItemMapper itemMapper;

    @Resource
    private ProfileExtDOMapper profileExtDOMapper;

    @Resource
    private AsyncEventBus asyncEventBus;

    private List<Long> itemList = Lists.newArrayList();

    @Before
    public void setUp() throws Exception {
        Preconditions.checkNotNull(itemMapper);
        Preconditions.checkNotNull(profileExtDOMapper);

        asyncEventBus.register(new ItemUpdateEventHandle());
    }

    @After
    public void tearDown() throws Exception {
        for (Long itemId : itemList) {
            itemMapper.deleteByPrimaryKey(itemId);
        }
    }

    @Test
    public void test_accuracy() throws InterruptedException {
        Item item = new Item();
        item.setDescription("desc");

        itemMapper.insert(item);

        itemList.add(item.getId());

        itemMapper.insertSelective(item);

        itemList.add(item.getId());

        ItemExample itemExample = new ItemExample();
        itemExample.createCriteria().andIdEqualTo(item.getId());

        item.setTitle("hello world");
        itemMapper.updateByExample(item, itemExample);

        itemMapper.updateByPrimaryKeySelective(item);

        itemMapper.updateByExampleSelective(item, itemExample);

        itemMapper.updateByPrimaryKey(item);


        Thread.sleep(2000);
    }

    static class ItemUpdateEventHandle {

        @Subscribe
        public void handle(ItemUpdateEvent event) {
            System.out.println(event);
        }
    }
}