package com.welink.biz.service;

import com.welink.commons.domain.ConfigureDO;
import com.welink.commons.domain.ConfigureDOExample;
import com.welink.commons.persistence.ConfigureDOMapper;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by daniel on 14-10-31.
 */
@Service
public class ConfigureService {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(ConfigureService.class);

    @Resource
    private ConfigureDOMapper configureDOMapper;

    /**
     * 根据communityId查找小区配置
     *
     * @param communityId
     * @return
     */
    public ConfigureDO fetchConfigureByCid(long communityId) {
        if (communityId < 0) {
            log.warn("fetch configuration error. communityId < 0. communityId:" + communityId);
            return null;
        }
        ConfigureDOExample cExample = new ConfigureDOExample();
        cExample.createCriteria().andCommunityIdEqualTo(communityId);
        List<ConfigureDO> configureDOs = configureDOMapper.selectByExample(cExample);
        if (null != configureDOs && configureDOs.size() > 0) {
            return configureDOs.get(0);
        }
        log.warn("fetch configuration error. configuration not found . communityId:" + communityId);
        return null;
    }
}
