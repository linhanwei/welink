package com.welink.promotion.reactive.impl;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.welink.commons.domain.CouponDO;
import com.welink.commons.domain.CouponDOExample;
import com.welink.commons.domain.UserCouponDO;
import com.welink.commons.persistence.CouponDOMapper;
import com.welink.promotion.CouponType;
import com.welink.promotion.PromotionType;
import com.welink.promotion.reactive.PromotionResult;
import com.welink.promotion.reactive.UserInteractionEffect;
import com.welink.promotion.reactive.UserInteractionRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:promotion-applicationContext.xml"})
@ActiveProfiles("dev")
public class DefaultUserInteractionEffectTest {

    @Resource
    private DefaultUserInteractionEffect defaultUserInteractionEffect;

    @Resource
    private CouponDOMapper couponDOMapper;

    @Resource
    private UserInteractionEffect userInteractionEffect;

    @Test
    public void testInteractive() throws Exception {

        UserInteractionRequest userInteractionRequest = new UserInteractionRequest();
        userInteractionRequest.setType(PromotionType.POINT_SIGN_IN.getCode());
        userInteractionRequest.setUserId(2046L);

        Optional<PromotionResult> promotionResultOptional
                = defaultUserInteractionEffect.interactive(userInteractionRequest);

        assertThat(promotionResultOptional.isPresent(), is(true));
    }

    @Test
    public void testRookieGetCoupons() throws Exception {
        //新人发优惠券
        CouponDOExample couponDOExample = new CouponDOExample();
        couponDOExample.createCriteria().andTypeEqualTo(CouponType.ROOKIE.getCode()).andStatusEqualTo((byte) 1);
        List<CouponDO> couponDOs = couponDOMapper.selectByExample(couponDOExample);


        for (CouponDO couponDO : couponDOs) {
            UserInteractionRequest couponRequest = new UserInteractionRequest();
            couponRequest.setUserId(199L);
            couponRequest.setType(PromotionType.COUPON_NEW_USER.getCode());
            couponRequest.getParams().putAll(ImmutableMap.of("user_coupon", new UserCouponDO(), "couponDOs", Lists.newArrayList(couponDO)));
            Optional<PromotionResult> promotionResultOptional = userInteractionEffect.interactive(couponRequest);
        }
    }
}