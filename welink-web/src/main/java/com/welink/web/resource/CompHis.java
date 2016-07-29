package com.welink.web.resource;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.CompViewDO;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.util.ViewDOCopy;
import com.welink.commons.domain.ComplainDO;
import com.welink.commons.domain.ComplainDOExample;
import com.welink.commons.persistence.ComplainDOMapper;
import com.welink.web.common.constants.ResponseStatusEnum;
import com.welink.web.common.util.ParameterUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by daniel on 14-10-14.
 */
@RestController
public class CompHis {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(CompHis.class);

    @Resource
    private ComplainDOMapper complainDOMapper;

    @RequestMapping(value = {"/api/m/1.0/compHis.json", "/api/h/1.0/compHis.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        WelinkVO welinkVO = new WelinkVO();
        Map resultMap = new HashMap();
        //para
        long profileId = -1;
        byte status = ParameterUtil.getParameterAsByteForSpringMVC(request, "status");
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();//boolean create
        if (checkSession(welinkVO, session)) {
            return JSON.toJSONString(welinkVO);
        }

        profileId = (long) session.getAttribute("profileId");
        int page = ParameterUtil.getParameterAsIntForSpringMVC(request, "pg");
        int size = ParameterUtil.getParameterAsIntForSpringMVC(request, "sz");
        if (size < 1) {
            size = 7;
        }
        if (page < 0) {
            page = 0;
        }
        int startRow = 0;
        startRow = (page) * size;

        ComplainDOExample complainDOExample = new ComplainDOExample();
        complainDOExample.setOrderByClause("id DESC");

        ComplainDOExample.Criteria criteria = complainDOExample.createCriteria();
        criteria.andProfileIdEqualTo(profileId);

        complainDOExample.setLimit(size);
        complainDOExample.setOffset(startRow);

        List<ComplainDO> complainDOList = Lists.newArrayList();
        resultMap.put("hasNext", true);
        //fetch history
        if (status > 0) {
            criteria.andStatusEqualTo(status);
            complainDOList = complainDOMapper.selectByExample(complainDOExample);
            if (null != complainDOList && complainDOList.size() >= size) {
                resultMap.put("hasNext", true);
            }
        } else {
            complainDOList = complainDOMapper.selectByExample(complainDOExample);
            if (null != complainDOList && complainDOList.size() >= size) {
                resultMap.put("hasNext", true);
            }
        }
        List<CompViewDO> compViewDOs = Lists.newArrayList();
        for (ComplainDO complainDO : complainDOList) {
            compViewDOs.add(ViewDOCopy.buildCompViewDO(complainDO));
        }
        resultMap.put("complains", compViewDOs);
        welinkVO.setStatus(ResponseStatusEnum.SUCCESS.getCode());
        return JSON.toJSONString(welinkVO);
    }

    private boolean checkSession(WelinkVO welinkVO, Session session) {
        if (null == session) {
            welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
            welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
            welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
            return true;
        }
        if (null != session.getAttribute("profileId")) {
            return false;
        } else {
            welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
            welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
            welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
            return true;
        }
    }
}
