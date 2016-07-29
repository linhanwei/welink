package com.welink.web.resource;

import com.alibaba.fastjson.JSON;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.common.security.NeedProfile;
import com.welink.biz.service.PointService;
import com.welink.biz.service.UserInteractionService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by daniel on 15-3-11.
 */
@RestController
public class FetchPoint {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(FetchPoint.class);

    @Resource
    private UserInteractionService userInteractionService;

    @Resource
    private PointService pointService;

    @NeedProfile
    @RequestMapping(value = {"/api/m/1.0/fetchPoint.json", "/api/h/1.0/fetchPoint.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        long profileId = -1;
        //今天是否签到
        boolean hasSignToday = false;
        //连续签到天数
        long signDay = 0;
        //总积分个数
        long totalScore = 0;
        //今天签到可领积分数
        long todayScore = 10;
        //明天可领积分数
        long tomorrowScore = 15;
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        profileId = (long) session.getAttribute("profileId");
        WelinkVO welinkVO = new WelinkVO();
        if (profileId < 0) {
            welinkVO.setStatus(0);
            welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
            welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
            return JSON.toJSONString(welinkVO);
        }

        hasSignToday = userInteractionService.hasSignToday(profileId);
        signDay = userInteractionService.findContinueSignDays(profileId);

        if (hasSignToday) {
            todayScore = 10 + (signDay - 1) * 5;
        } else {
            todayScore = 10 + signDay * 5;
        }

        if (todayScore < 40) {
            tomorrowScore = todayScore + 5;
        } else {
            todayScore = 40L;
            tomorrowScore = 40L;
        }

        totalScore = pointService.findAvailablePointByUserId(profileId);

        welinkVO.setStatus(1);
        Map resultMap = new HashMap();
        resultMap.put("hasSignToday", hasSignToday);
        resultMap.put("signDay", signDay);
        resultMap.put("totalScore", totalScore);
        resultMap.put("todayScore", todayScore);
        resultMap.put("tomorrowScore", tomorrowScore);
        welinkVO.setResult(resultMap);
        return JSON.toJSONString(welinkVO);
    }
}
