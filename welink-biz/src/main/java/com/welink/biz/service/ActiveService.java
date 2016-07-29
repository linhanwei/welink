package com.welink.biz.service;

import com.welink.commons.domain.InstallActiveDOExample;
import com.welink.commons.persistence.InstallActiveDOMapper;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by daniel on 15-3-18.
 */
@Service
public class ActiveService {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(ActiveService.class);

    @Resource
    private InstallActiveDOMapper installActiveDOMapper;

    //判断某件商品是否已经购买过
    public boolean checkActive(long itemId, long profileId) {
        InstallActiveDOExample iExample = new InstallActiveDOExample();
        iExample.createCriteria().andBuyerIdEqualTo(profileId).andItemIdEqualTo(itemId);
        int cnt = 0;
        cnt = installActiveDOMapper.countByExample(iExample);
        return cnt > 0;
    }

}
