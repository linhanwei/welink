package com.welink.web.search.index.item;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.welink.commons.Env;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.*;
import com.welink.commons.persistence.ItemMapper;
import com.welink.commons.persistence.ObjectTaggedDOMapper;
import com.welink.commons.persistence.SearchItemMapper;
import com.welink.commons.persistence.TagsDOMapper;
import com.welink.web.ons.MessageProcessFacade;
import com.welink.web.ons.SystemSignal;
import com.welink.web.ons.SystemSignalConstants;
import com.welink.web.ons.config.ONSTopic;
import com.welink.web.ons.process.ItemUpdateMessageProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;

/**
 * Created by saarixx on 13/12/14.
 */
@Service("itemIndexService")
public class ItemIndexServiceImpl implements ItemIndexService {

    static Logger logger = LoggerFactory.getLogger(ItemIndexServiceImpl.class);


    private final static int MAX_STEP = 500;

    @Resource
    private Env env;

    @Resource
    private ItemMapper itemMapper;

    @Resource
    private SearchItemMapper searchItemMapper;

    @Resource
    private MessageProcessFacade messageProcessFacade;

    @Resource
    private ObjectTaggedDOMapper objectTaggedDOMapper;

    @Resource
    private TagsDOMapper tagsDOMapper;


    @Override
    public void doIndex(List<SearchItem> searchItems, String type) {
        for (SearchItem searchItem : searchItems) {
            if (searchItemMapper.selectByPrimaryKey(searchItem.getId()) != null) {
                searchItemMapper.updateByPrimaryKeySelective(searchItem);
            } else {
                searchItemMapper.insert(searchItem);
            }
        }
    }

    @Override
    public void refreshAll() {
        List<Item> itemList = Lists.newArrayList();
        int offset = 0;
        while (!(itemList = itemMapper.selectByExample(makeItemExample(offset, MAX_STEP))).isEmpty()) {
            try {
                doIndex(Lists.newArrayList(Collections2.transform(itemList, new Function<Item, SearchItem>() {
                    @Nullable
                    @Override
                    public SearchItem apply(Item item) {
                        ObjectTaggedDOExample objectTaggedDOExample = new ObjectTaggedDOExample();
                        objectTaggedDOExample.createCriteria() //
                                .andTypeIn(Lists.newArrayList(BizConstants.TagTypeEnum.ITEM_EXHIBITION.getAction(), BizConstants.TagTypeEnum.ITEM_LOGIC.getAction()))
                                .andArtificialIdEqualTo(item.getId())
                                .andStatusEqualTo((byte) 1);

                        List<ObjectTaggedDO> objectTaggedDOs = objectTaggedDOMapper.selectByExample(objectTaggedDOExample);

                        List<TagsDO> tagsDOs = Lists.newArrayList(Collections2.transform(objectTaggedDOs, new Function<ObjectTaggedDO, TagsDO>() {
                            @Nullable
                            @Override
                            public TagsDO apply(ObjectTaggedDO objectTaggedDO) {
                                return tagsDOMapper.selectByPrimaryKey(objectTaggedDO.getTagId());
                            }
                        }));

                        return ItemUpdateMessageProcess.transform(item, tagsDOs);
                    }
                })), "update");
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            offset = offset + MAX_STEP;
        }
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void scheduleRefresh() {
        // 发送一个数据同步信号。同步评率最小5分钟

        SystemSignal systemSignal = new SystemSignal();
        systemSignal.setCode(SystemSignalConstants.Signal.SEARCH_ITEM_REFRESH.getCode());

        String message = JSON.toJSONString(systemSignal);

        if (env.isProd()) {
            messageProcessFacade.sendMessage(ONSTopic.SYSTEM_SIGNAL.toString(), "system", systemSignal.getCode(), message);
        } else {
            messageProcessFacade.sendMessage(ONSTopic.SYSTEM_SIGNAL_TEST.toString(), "system", systemSignal.getCode(), message);
        }
    }

    private ItemExample makeItemExample(int offset, int limit) {
        ItemExample itemExample = new ItemExample();
        itemExample.setOrderByClause("id DESC");
        itemExample.setOffset(offset);
        itemExample.setLimit(limit);
        return itemExample;
    }

    @PostConstruct
    public final void init() {
    }
}
