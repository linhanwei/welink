package com.welink.buy.service;

import com.welink.commons.domain.ObjectTaggedDO;
import com.welink.commons.domain.ObjectTaggedDOExample;
import com.welink.commons.domain.TagsDO;
import com.welink.commons.domain.TagsDOExample;
import com.welink.commons.persistence.ObjectTaggedDOMapper;
import com.welink.commons.persistence.TagsDOMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by daniel on 15-4-16.
 */
@Service
public class BuyItemService {

    @Resource
    private TagsDOMapper tagsDOMapper;

    @Resource
    private ObjectTaggedDOMapper objectTaggedDOMapper;

    /**
     * 获取商品特定的标记 用于判断商品是否具有特定标记，并返回该商品的objectTaged对象
     *
     * @param itemId
     * @param tag
     * @return
     */
    public ObjectTaggedDO fetchTagObjectsViaItemId(Long itemId, Long tag) {
        if (null != itemId) {
            //查询限购标记的id
            TagsDOExample qtExample = new TagsDOExample();
            qtExample.createCriteria().andBitEqualTo(BigInteger.valueOf(tag)).andStatusEqualTo((byte) 1);
            List<TagsDO> tagsDOs = new ArrayList<>();
            tagsDOs = tagsDOMapper.selectByExample(qtExample);
            if (null != tagsDOs && tagsDOs.size() > 0) {
                long tagId = tagsDOs.get(0).getId();
                ObjectTaggedDOExample qoExample = new ObjectTaggedDOExample();
                qoExample.createCriteria().andArtificialIdEqualTo(itemId).andTagIdEqualTo(tagId).andStatusEqualTo((byte) 1);
                List<ObjectTaggedDO> objectTaggedDOs = objectTaggedDOMapper.selectByExample(qoExample);
                if (null != objectTaggedDOs && objectTaggedDOs.size() > 0) {
                    return objectTaggedDOs.get(0);
                }
            }
        }
        return null;
    }

    /**
     * 是否属于不收运费商品(标签)
     *
     * @param itemId
     * @param tag
     * @return
     */
    public boolean nonPostFee(Long itemId, Long tag) {
        ObjectTaggedDO objectTaggedDO = fetchTagObjectsViaItemId(itemId, tag);
        if (null != objectTaggedDO) {
            return true;
        }
        return false;
    }
}
