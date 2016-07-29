package com.welink.web.resource;

import com.alibaba.fastjson.JSON;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.common.security.NeedProfile;
import com.welink.biz.service.CouponService;
import com.welink.biz.service.PointService;
import com.welink.biz.service.UserService;
import com.welink.buy.utils.PhenixBase64;
import com.welink.commons.Env;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.ProfileDO;
import com.welink.commons.domain.WeChatProfileDOExample;
import com.welink.commons.persistence.WeChatProfileDOMapper;
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
 * 获取用户信息，判断用户是否登录
 * Created by daniel on 14-12-9.
 */
@RestController
public class FetchUser {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(FetchUser.class);

    @Resource
    private UserService userService;

    @Resource
    private Env env;

    @Resource
    private PointService pointService;

    @Resource
    private CouponService couponService;

    @Resource
    private WeChatProfileDOMapper weChatProfileDOMapper;

    @NeedProfile
    @RequestMapping(value = {"/api/m/1.0/fetchUser.json", "/api/h/1.0/fetchUser.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        WelinkVO welinkVO = new WelinkVO();
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();//boolean create
        long profileId = (long) session.getAttribute(BizConstants.PROFILE_ID);
        //测试用，绑定关系是否存在
        if (env.isDev() && profileId < 0) {
            WeChatProfileDOExample qwExample = new WeChatProfileDOExample();
            qwExample.createCriteria().andProfileIdEqualTo(profileId);
            if (weChatProfileDOMapper.countByExample(qwExample) < 1) {
                welinkVO.setStatus(0);
                welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
                welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
                return JSON.toJSONString(welinkVO);
            }
        }
        //缓存中存在profileId
        ProfileDO profileDO = userService.fetchProfileById(profileId);
        welinkVO.setStatus(1);
        Map resultMap = new HashMap();
        if (null != profileDO) {
            resultMap.put("mobile", profileDO.getMobile());
            resultMap.put("uid", PhenixBase64.encode(profileDO.getId().toString().getBytes()));
            resultMap.put("nick", profileDO.getNickname());
            resultMap.put("isAgency", profileDO.getIsAgency());
            /**积分&优惠券*/
            long point = pointService.findAvailablePointByUserId(profileId);
            long couponCount = couponService.findUserCouponCountByUserId(profileId);
            resultMap.put("point", point);
            resultMap.put("couponCount", couponCount);
        } else {
            welinkVO.setStatus(0);
            welinkVO.setCode(BizErrorEnum.CAN_NOT_FIND_USER_IN_CACHE.getCode());
            welinkVO.setMsg(BizErrorEnum.CAN_NOT_FIND_USER_IN_CACHE.getMsg());
            return JSON.toJSONString(welinkVO);
        }
        welinkVO.setResult(resultMap);
        return JSON.toJSONString(welinkVO);
    }
}
