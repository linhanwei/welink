package com.welink.web.resource;

import com.alibaba.fastjson.JSON;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.CouponViewDO;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.common.security.NeedProfile;
import com.welink.biz.service.CouponService;
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
import java.util.List;
import java.util.Map;

/**
 * Created by daniel on 15-3-11.
 */
@RestController
public class FetchCouponList {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(FetchCouponList.class);

    @Resource
    private CouponService couponService;

    @Resource
    private Env env;

    @NeedProfile
    @RequestMapping(value = {"/api/m/1.0/fetchCouponList.json", "/api/h/1.0/fetchCouponList.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        long profileId = -1;
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        WelinkVO welinkVO = new WelinkVO();
        profileId = (long) session.getAttribute("profileId");
        if (profileId == -1) {
            welinkVO.setStatus(0);
            welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
            welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
            return JSON.toJSONString(welinkVO);
        }
        List<CouponViewDO> unUsedCoupons = couponService.findUnUsedCouponsByUserId(profileId);
        List<CouponViewDO> usedCoupons = couponService.findUsedCouponsByUserId(profileId);
        welinkVO.setStatus(1);

        Map resultMap = new HashMap();
        resultMap.put("unUsedCoupons", unUsedCoupons);
        resultMap.put("usedCoupons", usedCoupons);
        String couponRule = "http://h5.unesmall.com/h5/html/coupon-guide.html?m=c";
        if (env.isProd()) {
            //couponRule = "http://h5.unesmall.com/h5/html/coupon-guide.html?m=c";
        	couponRule = BizConstants.H5_SEVEN_NB_HOST+"/h5/html/coupon-guide.html?m=c";
        } else {
            couponRule = BizConstants.H5_WEILINJIA_HOST+"/h5/html/coupon-guide.html?m=c";
        }
        resultMap.put("couponRuleUrl", couponRule);
        welinkVO.setResult(resultMap);
        return JSON.toJSONString(welinkVO);
    }
}
