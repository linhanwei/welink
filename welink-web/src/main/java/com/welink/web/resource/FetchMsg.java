package com.welink.web.resource;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.common.security.NeedProfile;
import com.welink.biz.service.MessageService;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.BizMessage;
import com.welink.commons.domain.BizMessageExample;
import com.welink.commons.domain.MessageSummaryDO;
import com.welink.commons.domain.MessageSummaryDOExample;
import com.welink.commons.persistence.BizMessageMapper;
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
import java.util.*;

/**
 * Created by daniel on 15-6-2.
 */
@RestController
public class FetchMsg {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(FetchMsg.class);

    @Resource
    private BizMessageMapper bizMessageMapper;

    @Resource
    private MessageSummaryDOMapper messageSummaryDOMapper;

    @Resource
    private MessageService messageService;

    @NeedProfile
    @RequestMapping(value = {"/api/m/1.0/fetchMsg.json", "/api/h/1.0/fetchMsg.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        long profileId = -1;
        long bizType = ParameterUtil.getParameterAslongForSpringMVC(request, "biz", -1l);
        long page = ParameterUtil.getParameterAslongForSpringMVC(request, "pg", 0l);
        long size = ParameterUtil.getParameterAslongForSpringMVC(request, "sz", 8l);
        WelinkVO welinkVO = new WelinkVO();
        Map resultMap = new HashMap();

        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();//boolean create
        profileId = (long) session.getAttribute("profileId");
        long startRow = 0;
        startRow = (page) * size;
        List<BizMessage> messages = Lists.newArrayList();
        if (null != messages) {
            if (messages.size() < size) {
                resultMap.put("hasNext", false);
                resultMap.put("messages", messages);
            } else {
                if (messages.size() == size) {
                    resultMap.put("messages", messages.subList(0, 6));
                }
                resultMap.put("hasNext", true);
            }
        }
        //清除消息
        MessageSummaryDO messageSummaryDO = new MessageSummaryDO();
        messageSummaryDO.setBizType(bizType);
        messageSummaryDO.setLastUpdated(new Date());//标识读取时间
        messageSummaryDO.setNonReadCount(0);
        messageSummaryDO.setLastMessage("");
        MessageSummaryDOExample mExample = new MessageSummaryDOExample();
        mExample.createCriteria().andProfileIdEqualTo(profileId).andBizTypeEqualTo(bizType);
        int upCnt = messageSummaryDOMapper.updateByExampleSelective(messageSummaryDO, mExample);
        if (upCnt < 1) {
            log.warn("更新消息读取状态数量失败. update message nonReadCount failed. bizType:" + bizType + ",profileId:" + profileId);
        }
        //消息本身业务状态更新 : 2 标记已读
        messageService.updateMsgs(profileId, (byte) BizConstants.UserMsgBizStatus.VALID.getStatus(), bizType);

        //获取其他业务未读消息数
        List<Long> otherBizType = new ArrayList<>();
        otherBizType.add(BizConstants.UserNotifyTagEnum.ANNOUNCEMENT.getUserNotifyTagId());
        otherBizType.add(bizType);
        BizMessageExample bExample = new BizMessageExample();
        bExample.createCriteria().andProfileIdEqualTo(profileId).andStatusEqualTo(BizConstants.UserNotifyMsgStatus.NON_READ.getStatus())
                .andBizTypeNotIn(otherBizType);
        welinkVO.setStatus(ResponseStatusEnum.SUCCESS.getCode());
        welinkVO.setResult(resultMap);
        return JSON.toJSONString(welinkVO);
    }
}
