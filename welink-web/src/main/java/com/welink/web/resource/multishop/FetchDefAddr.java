package com.welink.web.resource.multishop;

import com.alibaba.fastjson.JSON;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.WelinkVO;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.ConsigneeAddrDO;
import com.welink.commons.domain.ConsigneeAddrDOExample;
import com.welink.commons.persistence.ConsigneeAddrDOMapper;
import com.welink.web.common.constants.ResponseStatusEnum;
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
 * Created by daniel on 15-4-20.
 */
@RestController
public class FetchDefAddr {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(FetchDefAddr.class);

    @Resource
    private ConsigneeAddrDOMapper consigneeAddrDOMapper;

    @RequestMapping(value = {"/api/m/1.0/fetchDefAddr.json", "/api/h/1.0/fetchDefAddr.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        WelinkVO welinkVO = new WelinkVO();
        Map resultMap = new HashMap();
        if (checkSession(welinkVO, session)) {
            welinkVO.setStatus(1);
            welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
            welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
            return JSON.toJSONString(welinkVO);
        }
        long profileId = (long) session.getAttribute(BizConstants.PROFILE_ID);
        ConsigneeAddrDOExample qExample = new ConsigneeAddrDOExample();
        qExample.createCriteria().andUserIdEqualTo(profileId).andGetDefEqualTo((byte) 1);
        List<ConsigneeAddrDO> consigneeAddrDOs = consigneeAddrDOMapper.selectByExample(qExample);
        if (null != consigneeAddrDOs && consigneeAddrDOs.size() > 0 && consigneeAddrDOs.get(0).getCommunityId() > 0) {
            resultMap.put("consignee", consigneeAddrDOs.get(0));
            welinkVO.setStatus(1);
            welinkVO.setResult(resultMap);//前端根据communityId是否大于0进行处理
            return JSON.toJSONString(welinkVO);
        }
        welinkVO.setStatus(1);
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
