package com.welink.biz.service;

import com.welink.biz.common.constants.ProfileEnum;
import com.welink.commons.domain.ProfileDO;
import com.welink.commons.domain.ProfileDOExample;
import com.welink.commons.domain.TenantDO;
import com.welink.commons.domain.TenantDOExample;
import com.welink.commons.persistence.ProfileDOMapper;
import com.welink.commons.persistence.TenantDOMapper;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by daniel on 14-11-3.
 */
@Service
public class PermissionService {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(PermissionService.class);

    @Resource
    private TenantDOMapper tenantDOMapper;

    @Resource
    private ProfileDOMapper profileDOMapper;

    /**
     * 判断用户是否具有业务权限的操作资格
     *
     * @param profileDO
     * @param buildingId
     * @return
     */
    public boolean checkPermission(ProfileDO profileDO, long buildingId) {
        List<Byte> permissionTypes = new ArrayList<>();
        permissionTypes.add(ProfileEnum.proprietor.getCode());
        permissionTypes.add(ProfileEnum.member.getCode());
        permissionTypes.add(ProfileEnum.renter.getCode());
        //根据profile获取其与房屋关系
        if (null != profileDO) {//profle表有记录
            TenantDOExample tenantDOExample = new TenantDOExample();
            tenantDOExample.createCriteria().andProfileIdEqualTo(profileDO.getId()).andBuildingIdEqualTo(buildingId);
            List<TenantDO> tenants = tenantDOMapper.selectByExample(tenantDOExample);
            for (TenantDO t : tenants) {
                if (Long.compare(t.getProfileId(), profileDO.getId()) == 0) {
                    if (Long.compare(t.getProfileId(), profileDO.getId()) == 0) {
                        //是业主
                        if (permissionTypes.contains((Byte) t.getIdentity())) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * 判断用户是否具有业务权限的操作资格
     *
     * @param profileId
     * @param buildingId
     * @return
     */
    public boolean checkPermission(long profileId, long buildingId) {
        ProfileDOExample profileDOExample = new ProfileDOExample();
        profileDOExample.createCriteria().andIdEqualTo(profileId).andStatusEqualTo((byte) 1);
        List<ProfileDO> thisNOprofiles = profileDOMapper.selectByExample(profileDOExample);
        List<Byte> permissionTypes = new ArrayList<>();
        permissionTypes.add(ProfileEnum.proprietor.getCode());
        permissionTypes.add(ProfileEnum.member.getCode());
        permissionTypes.add(ProfileEnum.renter.getCode());
        //根据profile获取其与房屋关系
        if (null != thisNOprofiles && thisNOprofiles.size() > 0) {//profle表有记录
            TenantDOExample tenantDOExample = new TenantDOExample();
            tenantDOExample.createCriteria().andProfileIdEqualTo(profileId).andBuildingIdEqualTo(buildingId);
            List<TenantDO> tenants = tenantDOMapper.selectByExample(tenantDOExample);
            for (TenantDO t : tenants) {
                if (Long.compare(t.getProfileId(), profileId) == 0) {
                    //是业主
                    if (permissionTypes.contains((Byte) t.getIdentity())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
