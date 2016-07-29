package com.welink.web.resource;

import com.alibaba.fastjson.JSON;
import com.welink.biz.common.model.CommunityViewDO;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.util.ViewDOCopy;
import com.welink.commons.domain.CommunityDO;
import com.welink.commons.domain.CommunityDOExample;
import com.welink.commons.persistence.CommunityDOMapper;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class FetchCommunity {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(FetchCommunity.class);

    @Resource
    private CommunityDOMapper communityDOMapper;

    @RequestMapping(value = {"/api/m/1.0/fetchCommunity.json", "/api/h/1.0/fetchCommunity.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        WelinkVO welinkVO = new WelinkVO();
        List<CommunityViewDO> comtDOs = fetchCommunityViewDOs();
        welinkVO.setStatus(1);
        Map resultMap = new HashMap();
        resultMap.put("comtDos", comtDOs);
        welinkVO.setResult(resultMap);
        return JSON.toJSONString(welinkVO);
    }

    /**
     * 获取自提点
     *
     * @return
     */
    private List<CommunityViewDO> fetchCommunityViewDOs() {
        CommunityDOExample communityDOExample = new CommunityDOExample();
        communityDOExample.createCriteria().andStatusEqualTo((byte) 1);
        communityDOExample.setOrderByClause("id DESC");
        List<CommunityDO> comtDOs = communityDOMapper.selectByExample(communityDOExample);
        List<CommunityViewDO> communityViewDOs = new ArrayList<>();
        if (null != comtDOs && comtDOs.size() > 0) {
            for (CommunityDO communityDO : comtDOs) {
                communityViewDOs.add(ViewDOCopy.buildCommunityViewDO(communityDO));
            }
        }
        return communityViewDOs;
    }

}
