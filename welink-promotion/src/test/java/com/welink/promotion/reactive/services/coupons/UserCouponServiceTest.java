package com.welink.promotion.reactive.services.coupons;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.welink.commons.domain.*;
import com.welink.commons.persistence.CouponDOMapper;
import com.welink.commons.persistence.UserCouponDOMapper;
import com.welink.commons.persistence.UserInteractionRecordsDOMapper;
import com.welink.promotion.CouponType;
import com.welink.promotion.PromotionType;
import com.welink.promotion.reactive.PromotionResult;
import com.welink.promotion.reactive.UserInteractionEffect;
import com.welink.promotion.reactive.UserInteractionRequest;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hamcrest.MatcherAssert;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:promotion-applicationContext.xml"})
@ActiveProfiles("test")
public class UserCouponServiceTest {

    @Resource
    private UserInteractionEffect userInteractionEffect;

    @Resource
    private CouponDOMapper couponDOMapper;

    @Resource
    private UserCouponDOMapper userCouponDOMapper;

    @Resource
    private UserInteractionRecordsDOMapper userInteractionRecordsDOMapper;

    public List<Long> couponIds = Lists.newArrayList();

    @Before
    public void setUp() throws Exception {
        CouponDO c1 = new CouponDO();
        c1.setDateCreated(new Date());
        c1.setEndTime(new DateTime().plusDays(30).toDate());
        c1.setStartTime(new DateTime().plusDays(30).toDate());
        c1.setLimitNum(200L);
        c1.setMinValue(10000);
        c1.setValue(500);
        c1.setProbability("0.99");
        c1.setShopId(999L);
        c1.setStatus((byte) 1);
        c1.setType(CouponType.OFF_FOR_CONDITION.getCode());
        c1.setVersion(1L);

        couponDOMapper.insert(c1);

        couponIds.add(c1.getId());
    }

    @After
    public void tearDown() throws Exception {
        CouponDOExample couponDOExample = new CouponDOExample();
        couponDOExample.createCriteria() //
                .andIdIn(couponIds);

        couponDOMapper.deleteByExample(couponDOExample);


        UserInteractionRecordsDOExample userInteractionRecordsDOExample = new UserInteractionRecordsDOExample();
        userInteractionRecordsDOExample.createCriteria() //
                .andUserIdEqualTo(199L);

        userInteractionRecordsDOMapper.deleteByExample(userInteractionRecordsDOExample);

        UserCouponDOExample userCouponDOExample = new UserCouponDOExample();
        userCouponDOExample.createCriteria() //
                .andUserIdEqualTo(199L);

        userCouponDOMapper.deleteByExample(userCouponDOExample);

    }

    @Test
    public void testGetCouponAccuracy() throws Exception {

        // 摇一摇获取优惠券
        UserInteractionRequest userInteractionRequest = new UserInteractionRequest();
        userInteractionRequest.setUserId(199L);
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
        userCouponDO.setUserId(199L);

        userInteractionRequest.getParams().putAll(ImmutableMap.of("user_coupon", userCouponDO, "couponDOs", filter));

        Optional<PromotionResult> promotionResultOptional = userInteractionEffect.interactive(userInteractionRequest);
        promotionResultOptional = userInteractionEffect.interactive(userInteractionRequest);
        promotionResultOptional = userInteractionEffect.interactive(userInteractionRequest);

        MatcherAssert.assertThat(promotionResultOptional.isPresent(), is(true));

        System.out.println(ToStringBuilder.reflectionToString(promotionResultOptional.get()));
    }

    @Test
    public void testUserCouponAccuracy() throws Exception {
        UserInteractionRequest userInteractionRequest = new UserInteractionRequest();
        userInteractionRequest.setUserId(199L);
        userInteractionRequest.setTargetId("12323232");

        userInteractionRequest.setType(PromotionType.COUPON_USE_IN_TRADE_FROZEN_ELIMINATE.getCode());


        UserCouponDO userCouponDO = new UserCouponDO();
        userCouponDO.setStatus((byte) 0);
        userCouponDO.setUserId(199L);

        userInteractionRequest.getParams().putAll(ImmutableMap.of("user_coupon", userCouponDO));
        userInteractionEffect.interactive(userInteractionRequest);
    }

}