package com.welink.web.resource;

import com.alibaba.fastjson.JSON;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.util.TimeUtils;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.ProfileDO;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by daniel on 15-4-24.
 */
@RestController
public class GreetingController {

    //@NeedProfile
    @RequestMapping(value = {"/m/greeting.json", "/h/1.0/greeting.json"}, produces = "application/json;charset=utf-8")
//    @ResponseBody
    public String greeting(HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "name", defaultValue = "World") String name) throws Exception {
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        if (true) {
            throw new Exception();
        }
        Session session = currentUser.getSession();
        session.setAttribute("hello", name + "hello session via shiro......");
        Cookie cookieU = new Cookie(BizConstants.JSESSION_ID, session.getId().toString());
        cookieU.setMaxAge(TimeUtils.TIME_1_MONTH_SEC);
        cookieU.setPath("/");
        response.addCookie(cookieU);
        String para = request.getParameter("para");
        ProfileDO model = new ProfileDO();
        model.setId(100l);
        model.setNickname("王坤");
        WelinkVO welinkVO = new WelinkVO();
        welinkVO.setStatus(1);
        Map<String, Object> resultMap = new HashMap<>();
        ProfileDO user = new ProfileDO();
        user.setId(100l);
        user.setNickname("yonder");
        resultMap.put("user", user);
        resultMap.put("point", 1001);
        resultMap.put("para", para);
        welinkVO.setResult(resultMap);
        return JSON.toJSONString(welinkVO);
    }

    @RequestMapping(value = {"/m/fetch.json", "/h/1.0/fetch.json"}, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String fetch(@RequestParam(value = "name", defaultValue = "World") String name) {
        ProfileDO model = new ProfileDO();
        model.setId(100L);
        model.setNickname(name);
        WelinkVO welinkVO = new WelinkVO();
        welinkVO.setStatus(1);
        Map<String, Object> resultMap = new HashMap<>();
        ProfileDO user = new ProfileDO();
        user.setId(100L);
        user.setNickname(name);
        resultMap.put("fetch", user);
        resultMap.put("point", 1001);
        welinkVO.setResult(resultMap);
        return JSON.toJSONString(welinkVO);
    }
}