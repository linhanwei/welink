package com.welink.biz.service;


import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.AroundNumberDO;
import com.welink.commons.domain.AroundNumberDOExample;
import com.welink.commons.persistence.AroundNumberDOMapper;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * Created by daniel on 14-10-15.
 */
@Service
public class AroundNumberService {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(AroundNumberService.class);

    @Resource
    private AroundNumberDOMapper aroundNumberDOMapper;

    /**
     * 添加
     *
     * @param communityId
     * @param type
     * @param lat
     * @param lng
     * @param address
     * @param tel
     * @param distance
     * @param tags
     * @param name
     * @param price
     * @return
     */
    public boolean addArdNumber(long communityId, byte type, double lat, double lng, String address,
                                String tel, long distance, String tags, String name, long price) {
        AroundNumberDO aroundNumberDO = new AroundNumberDO();
        aroundNumberDO.setLastUpdated(new Date());
        aroundNumberDO.setAddress(address);
        aroundNumberDO.setCommunityId(communityId);
        aroundNumberDO.setCount(0l);
        aroundNumberDO.setDateCreated(new Date());
        aroundNumberDO.setDistance(distance);
        aroundNumberDO.setLat(lat);
        aroundNumberDO.setLng(lng);
        aroundNumberDO.setName(name);
        aroundNumberDO.setPrice(price);
        aroundNumberDO.setTags(tags);
        aroundNumberDO.setTel(tel);
        aroundNumberDO.setType(type);
        aroundNumberDO.setTypeName(BizConstants.NumberType.getNumberTypeName(type));
        if (aroundNumberDOMapper.insertSelective(aroundNumberDO) > 0) {
            return true;
        }
        log.error("add around number failed. tel:" + tel + ",communityId:" + communityId);
        return false;
    }

    /**
     * 获取某个小区不同类别的周边电话信息
     *
     * @param communityId
     * @param type
     * @return
     */
    public List<AroundNumberDO> fetchNumbersByCommunityAndType(long communityId, byte type) {
        AroundNumberDOExample aExample = new AroundNumberDOExample();
        aExample.createCriteria().andCommunityIdEqualTo(communityId).andTypeEqualTo(type);
        aExample.setOrderByClause("distance ASC");
        return aroundNumberDOMapper.selectByExample(aExample);
    }
}
