package com.welink.biz.service;

import com.google.common.base.Function;
import com.google.common.cache.*;
import com.welink.biz.lbs.PointInPolygonService;
import com.welink.commons.domain.CommunityDO;
import com.welink.commons.domain.CommunityDOExample;
import com.welink.commons.persistence.CommunityDOMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang3.StringUtils.isNoneBlank;
import static org.apache.commons.lang3.StringUtils.trim;

/**
 * Created by saarixx on 13/1/15.
 */
@Service
public class CommunityService {

    static Logger logger = LoggerFactory.getLogger(CommunityService.class);

    static final String DEFAULT_KEY = "$$COMMUNITY_KEY_ALL$$";

    @Resource
    private CommunityDOMapper communityDOMapper;

    @Resource
    private PointInPolygonService pointInPolygonService;

    private LoadingCache<String, List<CommunityDO>> communityDOLoadingCache = CacheBuilder.newBuilder() //
            .recordStats()
            .concurrencyLevel(128)
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .removalListener(new RemovalListener<String, List<CommunityDO>>() {
                @Override
                public void onRemoval(RemovalNotification<String, List<CommunityDO>> objectObjectRemovalNotification) {
                    logger.info("remove listener: {}", objectObjectRemovalNotification);
                }
            })
            .build(CacheLoader.from(new Function<String, List<CommunityDO>>() {
                @Override
                public List<CommunityDO> apply(@Nullable String key) {
                    CommunityDOExample communityDOExample = new CommunityDOExample();
                    communityDOExample.createCriteria().andStatusEqualTo((byte) 1);

                    List<CommunityDO> communityDOs = communityDOMapper.selectByExample(communityDOExample);

                    return communityDOs;
                }
            }));


    public long queryCommunityIdByCoordinates(String point) {
        checkArgument(isNoneBlank(point));
        checkArgument(StringUtils.split(point, ',').length == 2);

        List<CommunityDO> communityDOs = communityDOLoadingCache.getUnchecked(DEFAULT_KEY);

        try {
        	for (CommunityDO communityDO : communityDOs) {
        		if (StringUtils.isNoneBlank(communityDO.getDeliveryArea())) {
        			if (pointInPolygonService.isIn(point, trim(communityDO.getDeliveryArea()))) {
        				return communityDO.getId();
        			}
        		} else {
        			logger.error("the delivery area is black, the community id is [%s]", communityDO.getId());
        		}
        	}
		} catch (Exception e) {
		}

        return -1L;
    }

}
