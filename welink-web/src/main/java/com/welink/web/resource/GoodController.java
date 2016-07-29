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
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by daniel on 15-5-5.
 */
@RestController
public class GoodController {
    //@NeedProfile
    @RequestMapping(value = {"/m/good.json", "/h/1.0/good.json"}, produces = "application/json;charset=utf-8")
//    @ResponseBody
    public String greeting(HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "name", defaultValue = "World") String name) throws Exception {
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
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

}
