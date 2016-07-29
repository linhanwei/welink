package com.welink.biz.service;

import com.welink.commons.domain.ShopDO;
import com.welink.commons.domain.ShopDOExample;
import com.welink.commons.persistence.ShopDOMapper;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by daniel on 15-4-8.
 */
@Service
public class ShopService {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(ShopService.class);

    @Resource
    private ShopDOMapper shopDOMapper;

    /**
     * 根据shop_id获取shop do
     *
     * @param shopId
     * @return
     */
    public ShopDO fetchShopByShopId(long shopId) {
        return shopDOMapper.selectByPrimaryKey(shopId);
    }

    /**
     * 获取shop的id
     *
     * @param shopId
     * @return
     */
    public long fetchIdByShopId(long shopId) {
        log.info("根据shop_id-communityId 获取shopId:" + shopId);
        ShopDOExample shopDOExample = new ShopDOExample();
        shopDOExample.createCriteria().andShopIdEqualTo(shopId);
        List<ShopDO> shopDOList = shopDOMapper.selectByExample(shopDOExample);
        if (null != shopDOList && shopDOList.size() > 0) {
            return shopDOList.get(0).getId();
        }
        return -1;
    }
}
