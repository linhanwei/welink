package com.welink.web.resource;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.MessageSumViewDO;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.util.ViewDOCopy;
import com.welink.commons.domain.MessageSummaryDO;
import com.welink.commons.domain.MessageSummaryDOExample;
import com.welink.commons.persistence.MessageSummaryDOMapper;
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
 * 获取消息页面的最
 * Created by daniel on 14-10-9.
 */
@RestController
public class MsgSum {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(MsgSum.class);

    @Resource
    private MessageSummaryDOMapper messageSummaryDOMapper;

    @RequestMapping(value = {"/api/m/1.0/msgSum.json", "/api/h/1.0/msgSum.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        long profileId = -1;
        String op = request.getParameter("op");
        //check session
        WelinkVO welinkVO = new WelinkVO();
        Map resultMap = new HashMap();
        int page = ParameterUtil.getParameterAsIntForSpringMVC(request, "pg", 0);
        int size = ParameterUtil.getParameterAsIntForSpringMVC(request, "sz", 8);

        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();//boolean create
        if (checkSession(welinkVO, session)) {
            return JSON.toJSONString(welinkVO);
        }
        profileId = (long) session.getAttribute("profileId");
        //更新？
        if (StringUtils.equalsIgnoreCase(op, "up")) {
            Long id = ParameterUtil.getParameterAslongForSpringMVC(request, "id", -1);
            if (id > 0) {
                MessageSummaryDO messageSummaryDO = messageSummaryDOMapper.selectByPrimaryKey(id);
                if (Long.compare(messageSummaryDO.getProfileId(), profileId) == 0) {
                    //update
                    MessageSummaryDO uMessageSummaryDO = new MessageSummaryDO();
                    uMessageSummaryDO.setStatus((byte) 0);
                    MessageSummaryDOExample uExample = new MessageSummaryDOExample();
                    uExample.createCriteria().andIdEqualTo(id);
                    messageSummaryDOMapper.updateByExampleSelective(uMessageSummaryDO, uExample);
                    welinkVO.setStatus(1);
                }
            }
            return JSON.toJSONString(welinkVO);
        }

        //fetch message summary
        int startRow = 0;
        startRow = (page) * size;
        MessageSummaryDOExample mExample = new MessageSummaryDOExample();
        mExample.createCriteria().andProfileIdEqualTo(profileId);
        mExample.setOffset(startRow);
        mExample.setLimit(size);
        List<MessageSummaryDO> messageSummaryDOs = messageSummaryDOMapper.selectByExample(mExample);
        resultMap.put("hasNext", false);
        if (null != messageSummaryDOs && messageSummaryDOs.size() > 0) {
            List<MessageSumViewDO> messageSumViewDOs = Lists.newArrayList();
            for (MessageSummaryDO messageSummaryDO : messageSummaryDOs) {
                messageSumViewDOs.add(ViewDOCopy.buildMsgSumViewDO(messageSummaryDO));
            }
            if (messageSummaryDOs.size() < size) {
                resultMap.put("hasNext", false);
                resultMap.put("messages", messageSumViewDOs);
            } else {
                if (messageSummaryDOs.size() == size) {
                    resultMap.put("messages", messageSumViewDOs);
                }
                resultMap.put("hasNext", true);
            }
        }
        welinkVO.setResult(resultMap);
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
