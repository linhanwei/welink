package com.welink.web.resource;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.common.security.NeedProfile;
import com.welink.commons.domain.CouponDO;
import com.welink.commons.domain.CouponDOExample;
import com.welink.commons.domain.UserCouponDO;
import com.welink.commons.persistence.CouponDOMapper;
import com.welink.commons.persistence.UserCouponDOMapper;
import com.welink.promotion.CouponType;
import com.welink.promotion.PromotionType;
import com.welink.promotion.reactive.PromotionResult;
import com.welink.promotion.reactive.UserInteractionEffect;
import com.welink.promotion.reactive.UserInteractionRequest;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by XUTIAN on 2015/3/7.
 */
@RestController
public class ObtainCoupon {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(ObtainCoupon.class);

    @Resource
    private UserInteractionEffect userInteractionEffect;

    @Resource
    private CouponDOMapper couponDOMapper;

    @Resource
    private UserCouponDOMapper userCouponDOMapper;

    @NeedProfile
    @RequestMapping(value = {"/api/m/1.0/obtainCoupon.json", "/api/h/1.0/obtainCoupon.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        long profileId = -1;
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        profileId = (long) session.getAttribute("profileId");
        WelinkVO welinkVO = new WelinkVO();
        Map resultMap = new HashMap();
        if (profileId < 0) {
            welinkVO.setStatus(0);
            welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
            welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
            return JSON.toJSONString(welinkVO);
        }
        // 摇一摇获取优惠券
        UserInteractionRequest userInteractionRequest = new UserInteractionRequest();
        userInteractionRequest.setUserId(profileId);
        userInteractionRequest.setType(PromotionType.COUPON_SHAKE.getCode());

        CouponDOExample couponDOExample = new CouponDOExample();
        couponDOExample.createCriteria() //
                .andTypeEqualTo(CouponType.OFF_FOR_CONDITION.getCode()) //
                .andStatusEqualTo((byte) 1);

        List<CouponDO> couponDOs = couponDOMapper.selectByExample(couponDOExample);

        List<CouponDO> filter = Lists.newArrayList(Collections2.filter(couponDOs, new Predicate<CouponDO>() {
            @Override
            public boolean apply(CouponDO couponDO) {

                Date startTime = couponDO.getStartTime();
                Date endTime = couponDO.getEndTime();
                Date now = new Date();

                boolean s = true;
                boolean e = true;

                if (startTime != null) {
                    s = now.after(startTime);
                }

                if (endTime != null) {
                    s = now.before(endTime);
                }


                return s && e;
            }
        }));

        UserCouponDO userCouponDO = new UserCouponDO();
        userCouponDO.setUserId(profileId);

        userInteractionRequest.getParams().putAll(ImmutableMap.of("user_coupon", userCouponDO, "couponDOs", filter));

        Optional<PromotionResult> promotionResultOptional = userInteractionEffect.interactive(userInteractionRequest);

        // 如果数据库挂了
        if (!promotionResultOptional.isPresent()) {
            // TODO:
        } else {
            //正常返回
            PromotionResult promotionResult = promotionResultOptional.get();
            // 领取优惠券成功
            if (promotionResult.getReward()) {
                // 如果领取了优惠
                Long userCouponDOId = promotionResult.getPromotionId();
                UserCouponDO userCouponDO1 = userCouponDOMapper.selectByPrimaryKey(userCouponDOId);
                CouponDO couponDO = couponDOMapper.selectByPrimaryKey(userCouponDO1.getCouponId());
                welinkVO.setStatus(1);
                resultMap.put("picUrl", couponDO.getPicUrl());
                resultMap.put("value", couponDO.getValue());
            } else {
                welinkVO.setStatus(1);
                resultMap.put("value", 0);
                resultMap.put("msg", promotionResult.getMessage());
            }
        }
        welinkVO.setResult(resultMap);
        return JSON.toJSONString(welinkVO);
    }
}
