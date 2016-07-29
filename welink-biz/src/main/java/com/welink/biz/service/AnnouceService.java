package com.welink.biz.service;

import com.welink.commons.domain.AnnouceDO;
import com.welink.commons.domain.AnnouceDOExample;
import com.welink.commons.persistence.AnnouceDOMapper;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by daniel on 14-9-22.
 */
@Service
public class AnnouceService {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(AnnouceService.class);

    @Resource
    private AnnouceDOMapper annouceDOMapper;


    public List<AnnouceDO> fetchAnnouces(long communityId, int status, int startRow, int size) {
        AnnouceDOExample annouceDOExample = new AnnouceDOExample();
        annouceDOExample.setOrderByClause("id DESC");
        annouceDOExample.createCriteria() //
                //.andCommunityIdEqualTo(communityId) //
                .andStatusEqualTo(status);
        annouceDOExample.setOffset(startRow);
        annouceDOExample.setLimit(size);
        return annouceDOMapper.selectByExample(annouceDOExample);
    }

}
