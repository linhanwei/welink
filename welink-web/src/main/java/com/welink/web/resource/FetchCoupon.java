package com.welink.web.resource;

import com.alibaba.fastjson.JSON;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.common.security.NeedProfile;
import com.welink.biz.service.UserInteractionService;
import com.welink.buy.utils.Constants;
import com.welink.commons.Env;
import com.welink.commons.commons.BizConstants;

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
public class FetchCoupon {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(FetchCoupon.class);

    @Resource
    private UserInteractionService userInteractionService;

    @Resource
    private Env env;

    @NeedProfile
    @RequestMapping(value = {"/api/m/1.0/fetchCoupon.json", "/api/h/1.0/fetchCoupon.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        long profileId = -1;
        int totalCount = Constants.TOTAL_SHAKE_COUPON_COUNT;
        int remainCount = 0;
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
        remainCount = userInteractionService.findRemainShakeCouponCountBy(profileId);
        if (remainCount < 0) {
            remainCount = 0;
        }
        String couponRule = "http://h5.unesmall.com/h5/html/coupon-guide.html?m=c";
        if (env.isProd()) {
            //couponRule = "http://h5.unesmall.com/h5/html/coupon-guide.html?m=c";
        	couponRule = BizConstants.H5_SEVEN_NB_HOST+"/h5/html/coupon-guide.html?m=c";
        } else {
            couponRule = BizConstants.H5_WEILINJIA_HOST+"/h5/html/coupon-guide.html?m=c";
        }
        Map resultMap = new HashMap();
        resultMap.put("couponRuleUrl", couponRule);
        welinkVO.setStatus(1);
        resultMap.put("totalCount", totalCount);
        resultMap.put("remainCount", remainCount);
        welinkVO.setResult(resultMap);
        return JSON.toJSONString(welinkVO);
    }
}
