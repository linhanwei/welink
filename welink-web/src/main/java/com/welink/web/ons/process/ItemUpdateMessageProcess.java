package com.welink.web.ons.process;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.welink.commons.Env;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.*;
import com.welink.commons.persistence.ItemMapper;
import com.welink.commons.persistence.MikuBrandDOMapper;
import com.welink.commons.persistence.ObjectTaggedDOMapper;
import com.welink.commons.persistence.SearchItemMapper;
import com.welink.commons.persistence.TagsDOMapper;
import com.welink.web.ons.ConsumeProcess;
import com.welink.web.ons.MessageProcess;
import com.welink.web.ons.MessageProcessFacade;
import com.welink.web.ons.config.AliKeys;
import com.welink.web.ons.config.ONSPublish;
import com.welink.web.ons.config.ONSSubscribe;
import com.welink.web.ons.config.ONSTopic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by saarixx on 13/12/14.
 */
@Service
public class ItemUpdateMessageProcess {

    static final Logger logger = LoggerFactory.getLogger(ItemUpdateMessageProcess.class);

    @Resource
    private MessageProcessFacade messageProcessFacade;

    @Resource
    private ItemMapper itemMapper;

    @Resource
    private SearchItemMapper searchItemMapper;

    @Resource
    private ObjectTaggedDOMapper objectTaggedDOMapper;

    @Resource
    private TagsDOMapper tagsDOMapper;
    
    @Resource
    private MikuBrandDOMapper mikuBrandDOMapper;

    @Resource
    private Env env;

    public void doConsume(Message message) {
        Map<String, String> params = JSON.parseObject(new String(message.getBody()), new TypeReference<Map<String, String>>() {
        });

        Long itemId = Long.valueOf(checkNotNull(params).get("item_id"));
        String type = params.get("type");

        Item item = itemMapper.selectByPrimaryKey(itemId);
        // 这里是不可能的
        checkNotNull(item);
        // 存到 db 里面去
        updateSearchItem(item, type);
    }

    public void updateSearchItem(Item item, String type) {

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


        SearchItem searchItem = transform(item, tagsDOs);
        if(null != item && null != item.getBrandId()){
        	MikuBrandDO mikuBrandDO = mikuBrandDOMapper.selectByPrimaryKey(item.getBrandId());
        	if(null != mikuBrandDO){
        		searchItem.setBrandName(mikuBrandDO.getName());	//品牌名
        	}
        }
        switch (type.toLowerCase()) {
            case "insert": {
                checkArgument(1 == searchItemMapper.insert(searchItem));
                break;
            }
            case "update": {
                if (searchItemMapper.selectByPrimaryKey(item.getId()) == null) {
                    checkArgument(1 == searchItemMapper.insert(searchItem));
                } else {
                    checkArgument(1 == searchItemMapper.updateByPrimaryKeySelective(searchItem));
                }
                break;
            }
            case "delete": {
                if (searchItemMapper.selectByPrimaryKey(item.getId()) != null) {
                    checkArgument(1 == searchItemMapper.deleteByPrimaryKey(item.getId()));
                }
                break;
            }
            default:
                throw new IllegalStateException("the invalid index type [" + type + "] ...");
        }

    }

    public static SearchItem transform(Item item, List<TagsDO> tags) {
        SearchItem searchItem = new SearchItem();
        searchItem.setVersion(item.getVersion());
        searchItem.setId(item.getId());
        searchItem.setCategoryId(item.getCategoryId());
        searchItem.setCategory1Id(item.getCategory1Id());
        searchItem.setCategory2Id(item.getCategory2Id());
        searchItem.setPrice(item.getPrice());
        searchItem.setPromotionPrice(item.getPrice());
        searchItem.setTitle(item.getTitle());
        searchItem.setOnlineStartTime(item.getOnlineStartTime());
        searchItem.setOnlineEndTime(item.getOnlineEndTime());
        searchItem.setSoldCount(item.getSoldQuantity());
        searchItem.setStatus(item.getApproveStatus());
        searchItem.setShopId(item.getShopId());
        searchItem.setFeatures(item.getFeatures());
        searchItem.setRank(null);
        searchItem.setType(Integer.valueOf(item.getType() == null ? 1 : item.getType()));
        searchItem.setTitle(item.getTitle());
        searchItem.setBrandId(item.getBrandId());
        searchItem.setBaseSoldQuantity(item.getBaseSoldQuantity());
        searchItem.setNum(item.getNum());
        searchItem.setWeight(item.getWeight());
        searchItem.setIsrefund(item.getIsrefund());
        searchItem.setKeyWord(item.getKeyWord());

        // fffffffffffffff
        BigInteger zero = BigInteger.ZERO;

        for (TagsDO tagsDO : tags) {
            BigInteger bit = checkNotNull(tagsDO.getBit(), "the bit of tag with id [%s] should not be null ... ", tagsDO.getId());
            zero = zero.xor(bit); // 不是OR的原因是我觉得一个tag就应该是一个bit位置
        }

        searchItem.setTagId(zero.longValue());
        searchItem.setLastUpdated(item.getLastUpdated());
        searchItem.setItemLastUpdated(item.getLastUpdated().getTime());
        searchItem.setDateCreated(item.getDateCreated());
        searchItem.setItemDateCreated(item.getDateCreated().getTime());
        return searchItem;
    }

    @PostConstruct
    public void init() {
        if (env.isProd()) { // prod
            MessageProcess messageProcess = MessageProcess.newBuilder() //
            		.setAccessKey(AliKeys.ACCESS_KEY.toString())
            		.setSecretKey(AliKeys.SECRET_KEY.toString()) //
            		.setTopic(ONSTopic.ITEM_UPDATE.toString()) //
                    .setProducerId(ONSPublish.ITEM_UPDATE.toString()) //
                    .setConsumerId(ONSSubscribe.ITEM_UPDATE.toString()) //
            		.setConsumeProcess(new ConsumeProcess() {
                        @Override
                        public void consume(Message message, ConsumeContext consumeContext) {
                            doConsume(message);
                        }
                    }) //
                    .build();

            messageProcessFacade.register(messageProcess);
        } else {
            // dev or test
//            MessageProcess messageProcess = MessageProcess.newBuilder() //
//                    .setTopic(ONSTopic.ITEM_UPDATE_TEST.toString()) //
//                    .setProducerId("PID2218978803-104") //
//                    .setConsumerId("CID2218978803-104") //
//                    .setConsumeProcess(new ConsumeProcess() {
//                        @Override
//                        public void consume(Message message, ConsumeContext consumeContext) {
//                            doConsume(message);
//                        }
//                    }) //
//                    .build();
//
//            messageProcessFacade.register(messageProcess);
        	MessageProcess messageProcess = MessageProcess.newBuilder() //
        			.setAccessKey(AliKeys.ACCESS_KEY_TEST.toString())
            		.setSecretKey(AliKeys.SECRET_KEY_TEST.toString()) 
                    .setTopic(ONSTopic.ITEM_UPDATE_TEST.toString()) //
                    .setProducerId(ONSPublish.ITEM_UPDATE_TEST.toString()) //
                    .setConsumerId(ONSSubscribe.ITEM_UPDATE_TEST.toString()) //
                    .setConsumeProcess(new ConsumeProcess() {
                        @Override
                        public void consume(Message message, ConsumeContext consumeContext) {
                            doConsume(message);
                        }
                    }) //
                    .build();

            messageProcessFacade.register(messageProcess);
        }
    }
}
