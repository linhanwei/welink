package com.welink.biz.service;

import com.welink.commons.domain.BuildingDO;
import com.welink.commons.domain.TenantDO;
import com.welink.commons.domain.TenantDOExample;
import com.welink.commons.persistence.BuildingDOMapper;
import com.welink.commons.persistence.TenantDOMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by daniel on 14-9-22.
 */
@Service
public class BuildingService {

    private static Logger logger = LoggerFactory.getLogger(BuildingService.class);

    @Resource
    private TenantDOMapper tenantDOMapper;

    @Resource
    private BuildingDOMapper buildingDOMapper;

    /**
     * 获取个人的房屋信息 identity为用户的角色，如果不区分业主和游客，请传递0值
     *
     * @param profileId
     * @param identity
     * @return
     */
    public List<BuildingDO> fetchBuildings(long profileId, byte identity) {
        List<BuildingDO> buildingDOs = new ArrayList<BuildingDO>();
        //1. fetch tenants
        TenantDOExample tenantDOExample = new TenantDOExample();
        if (identity > 0) {
            tenantDOExample.createCriteria().andProfileIdEqualTo(profileId).andIdentityEqualTo(identity);
        } else {
            tenantDOExample.createCriteria().andProfileIdEqualTo(profileId);
        }
        List<TenantDO> tenantDOs = tenantDOMapper.selectByExample(tenantDOExample);
        //2. fetch buildings
        if (null != tenantDOs && tenantDOs.size() > 0) {
            for (TenantDO t : tenantDOs) {
                BuildingDO buildingDO = buildingDOMapper.selectByPrimaryKey(t.getBuildingId());
                if (null != buildingDO) {
                    buildingDOs.add(buildingDO);
                }
            }
        }
        return buildingDOs;
    }

}
