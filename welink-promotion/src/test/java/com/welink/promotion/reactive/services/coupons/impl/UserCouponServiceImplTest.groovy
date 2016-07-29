package com.welink.promotion.reactive.services.coupons.impl

import com.google.common.collect.Lists
import com.welink.commons.domain.CouponDO
import com.welink.commons.domain.UserCouponDO
import com.welink.commons.domain.UserInteractionRecordsDO
import com.welink.promotion.CouponType
import com.welink.promotion.PromotionErrorEnum
import com.welink.promotion.PromotionType
import com.welink.promotion.drools.Utils
import com.welink.promotion.drools.impl.DroolsExecutorImpl
import com.welink.promotion.reactive.PromotionResult
import org.joda.time.DateTime
import spock.lang.Shared
import spock.lang.Specification

/**
 * Created by saarixx on 10/3/15.
 */
class UserCouponServiceImplTest extends Specification {

    @Shared
    DroolsExecutorImpl droolsExecutor = new DroolsExecutorImpl()

    static Long DEFAULT_USER_ID = 999999L

    static long DEFAULT_ID = 4347332;

    def setupSpec() {
        droolsExecutor.afterPropertiesSet()
    }

    def setup() {

    }

    def cleanup() {

    }

    def "如果今天摇一摇已经3次，不能给奖励"() {
        when:
        PromotionResult promotionResult = makePromotionResult(PromotionType.COUPON_SHAKE.getCode())
        droolsExecutor.execute(promotionResult, getUserInteractionRecordsDO1(), getUserInteractionRecordsDO2(), getUserInteractionRecordsDO3())
        then:
        promotionResult.reward == false
    }

    def "如果今天已经中奖了，不能给奖励"() {
        when:
        PromotionResult promotionResult = makePromotionResult(PromotionType.COUPON_SHAKE.getCode())
        UserInteractionRecordsDO userInteractionRecordsDO = getUserInteractionRecordsDO1()
        userInteractionRecordsDO.destination = "121323"

        droolsExecutor.execute(promotionResult, userInteractionRecordsDO)
        then:
        promotionResult.reward == false
    }

    def "放入一个Coupon，一定中奖"() {
        when:
        PromotionResult promotionResult = makePromotionResult(PromotionType.COUPON_SHAKE.getCode())
        UserInteractionRecordsDO userInteractionRecordsDO = getUserInteractionRecordsDO1()
        userInteractionRecordsDO.id = null
        CouponDO couponDO = makeCouponDO(CouponType.OFF_FOR_CONDITION.code, "0.99")

        UserCouponDO userCouponDO = new UserCouponDO();
        userCouponDO.setStartTime(couponDO.getStartTime())
        userCouponDO.setEndTime(couponDO.getEndTime())


        droolsExecutor.execute(promotionResult, userInteractionRecordsDO, getUserInteractionRecordsDO2(), couponDO, userCouponDO)
        then:
        promotionResult.reward == true
        userCouponDO.couponType == CouponType.OFF_FOR_CONDITION.code
        userCouponDO.endTime != null
        userCouponDO.startTime != null
    }

    def "放入三个Coupon，一个一定不能中，两个随机中，但是只有一个能中奖"() {
        when:
        PromotionResult promotionResult = makePromotionResult(PromotionType.COUPON_SHAKE.getCode())
        UserInteractionRecordsDO userInteractionRecordsDO = getUserInteractionRecordsDO1()
        userInteractionRecordsDO.id = null
        CouponDO c1 = makeCouponDO(CouponType.OFF_FOR_CONDITION.code, "0.99")
        CouponDO c2 = makeCouponDO(CouponType.OFF_FOR_CONDITION.code, "0.01")
        CouponDO c3 = makeCouponDO(CouponType.OFF_FOR_CONDITION.code, "0.99")

        UserCouponDO userCouponDO = new UserCouponDO();
        userCouponDO.setStartTime(c1.getStartTime())
        userCouponDO.setEndTime(c1.getEndTime())

        droolsExecutor.execute(promotionResult, userInteractionRecordsDO, getUserInteractionRecordsDO2(), c1, c2, c3, userCouponDO)
        then:
        promotionResult.reward == true
        userCouponDO.couponType == CouponType.OFF_FOR_CONDITION.code
        userCouponDO.endTime != null
        userCouponDO.startTime != null
    }

    def "如果优惠券不是本人1"() {
        when:
        PromotionResult promotionResult = makePromotionResult(PromotionType.COUPON_SHAKE.getCode())
        UserInteractionRecordsDO userInteractionRecordsDO = getUserInteractionRecordsDO1()
        userInteractionRecordsDO.userId = DEFAULT_ID
        userInteractionRecordsDO.type = PromotionType.COUPON_USE_IN_TRADE_DIRECT.getCode()
        userInteractionRecordsDO.id = null
        CouponDO c1 = makeCouponDO(CouponType.OFF_FOR_CONDITION.code, "0.99")

        UserCouponDO userCouponDO = new UserCouponDO();
        userCouponDO.setUserId(1L)
        userCouponDO.setStartTime(c1.getStartTime())
        userCouponDO.setEndTime(c1.getEndTime())


        droolsExecutor.execute(promotionResult, userInteractionRecordsDO, getUserInteractionRecordsDO2(), c1, userCouponDO)
        then:
        promotionResult.reward == false
    }

    def "如果优惠券不是本人2"() {
        when:
        PromotionResult promotionResult = makePromotionResult(PromotionType.COUPON_SHAKE.getCode())
        UserInteractionRecordsDO userInteractionRecordsDO = getUserInteractionRecordsDO1()
        userInteractionRecordsDO.userId = DEFAULT_ID
        userInteractionRecordsDO.type = PromotionType.COUPON_USE_IN_TRADE_FROZEN.getCode()
        userInteractionRecordsDO.id = null
        CouponDO c1 = makeCouponDO(CouponType.OFF_FOR_CONDITION.code, "0.99")

        UserCouponDO userCouponDO = new UserCouponDO();
        userCouponDO.setUserId(1L)
        userCouponDO.setStartTime(c1.getStartTime())
        userCouponDO.setEndTime(c1.getEndTime())


        droolsExecutor.execute(promotionResult, userInteractionRecordsDO, getUserInteractionRecordsDO2(), c1, userCouponDO)
        then:
        promotionResult.reward == false
    }

    def "如果优惠券不是本人3"() {
        when:
        PromotionResult promotionResult = makePromotionResult(PromotionType.COUPON_SHAKE.getCode())
        UserInteractionRecordsDO userInteractionRecordsDO = getUserInteractionRecordsDO1()
        userInteractionRecordsDO.userId = DEFAULT_ID
        userInteractionRecordsDO.type = PromotionType.COUPON_USE_IN_TRADE_FROZEN_ELIMINATE.getCode()
        userInteractionRecordsDO.id = null
        CouponDO c1 = makeCouponDO(CouponType.OFF_FOR_CONDITION.code, "0.99")

        UserCouponDO userCouponDO = new UserCouponDO();
        userCouponDO.setUserId(1L)
        userCouponDO.setStartTime(c1.getStartTime())
        userCouponDO.setEndTime(c1.getEndTime())


        droolsExecutor.execute(promotionResult, userInteractionRecordsDO, getUserInteractionRecordsDO2(), c1, userCouponDO)
        then:
        promotionResult.reward == false
    }

    def "测试状态对不对1"() {
        when:
        PromotionResult promotionResult = makePromotionResult(PromotionType.COUPON_SHAKE.getCode())
        UserInteractionRecordsDO userInteractionRecordsDO = getUserInteractionRecordsDO1()
        userInteractionRecordsDO.userId = DEFAULT_USER_ID
        userInteractionRecordsDO.type = PromotionType.COUPON_USE_IN_TRADE_DIRECT.getCode()
        userInteractionRecordsDO.id = null
        CouponDO c1 = makeCouponDO(CouponType.OFF_FOR_CONDITION.code, "0.99")

        UserCouponDO userCouponDO = new UserCouponDO();
        userCouponDO.setStatus((byte) 0)
        userCouponDO.setUserId(DEFAULT_USER_ID)
        userCouponDO.setStartTime(c1.getStartTime())
        userCouponDO.setEndTime(c1.getEndTime())


        droolsExecutor.execute(promotionResult, userInteractionRecordsDO, getUserInteractionRecordsDO2(), c1, userCouponDO)
        then:
        promotionResult.reward == false
    }

    def "测试状态对不对2"() {
        when:
        PromotionResult promotionResult = makePromotionResult(PromotionType.COUPON_SHAKE.getCode())
        UserInteractionRecordsDO userInteractionRecordsDO = getUserInteractionRecordsDO1()
        userInteractionRecordsDO.userId = DEFAULT_USER_ID
        userInteractionRecordsDO.type = PromotionType.COUPON_USE_IN_TRADE_DIRECT.getCode()
        userInteractionRecordsDO.id = null
        CouponDO c1 = makeCouponDO(CouponType.OFF_FOR_CONDITION.code, "0.99")

        UserCouponDO userCouponDO = new UserCouponDO();
        userCouponDO.setStatus((byte) -1)
        userCouponDO.setUserId(DEFAULT_USER_ID)
        userCouponDO.setStartTime(c1.getStartTime())
        userCouponDO.setEndTime(c1.getEndTime())


        droolsExecutor.execute(promotionResult, userInteractionRecordsDO, getUserInteractionRecordsDO2(), c1, userCouponDO)
        then:
        promotionResult.reward == false
    }

    def "测试验证优惠券有没有过期1"() {
        when:
        PromotionResult promotionResult = makePromotionResult(PromotionType.COUPON_SHAKE.getCode())
        UserInteractionRecordsDO userInteractionRecordsDO = getUserInteractionRecordsDO1()
        userInteractionRecordsDO.userId = DEFAULT_USER_ID
        userInteractionRecordsDO.type = PromotionType.COUPON_USE_IN_TRADE_FROZEN.getCode()
        userInteractionRecordsDO.id = null
        CouponDO c1 = makeCouponDO(CouponType.OFF_FOR_CONDITION.code, "0.99")

        UserCouponDO userCouponDO = new UserCouponDO();
        userCouponDO.setStatus((byte) 1)
        userCouponDO.setUserId(DEFAULT_USER_ID)
        userCouponDO.setStartTime(new DateTime().minusDays(2).toDate())
        userCouponDO.setEndTime(new DateTime().minusDays(1).toDate())


        droolsExecutor.execute(promotionResult, userInteractionRecordsDO, getUserInteractionRecordsDO2(), c1, userCouponDO)
        then:
        promotionResult.reward == false
    }

    def "测试验证优惠券有没有过期2"() {
        when:
        PromotionResult promotionResult = makePromotionResult(PromotionType.COUPON_USE_IN_TRADE_DIRECT.getCode())
        UserInteractionRecordsDO userInteractionRecordsDO = getUserInteractionRecordsDO1()
        userInteractionRecordsDO.userId = DEFAULT_USER_ID
        userInteractionRecordsDO.type = PromotionType.COUPON_USE_IN_TRADE_FROZEN.getCode()
        userInteractionRecordsDO.id = null
        CouponDO c1 = makeCouponDO(CouponType.OFF_FOR_CONDITION.code, "0.99")

        UserCouponDO userCouponDO = new UserCouponDO();
        userCouponDO.setStatus((byte) 1)
        userCouponDO.setUserId(DEFAULT_USER_ID)
        userCouponDO.setStartTime(new DateTime().plusDays(1).toDate())
        userCouponDO.setEndTime(new DateTime().plusDays(2).toDate())


        droolsExecutor.execute(promotionResult, userInteractionRecordsDO, getUserInteractionRecordsDO2(), c1, userCouponDO)
        then:
        promotionResult.reward == false
    }

    def "测试验证交易有没有到优惠券的最小金额1"() {
        when:
        PromotionResult promotionResult = makePromotionResult(PromotionType.COUPON_USE_IN_TRADE_DIRECT.getCode())
        UserInteractionRecordsDO userInteractionRecordsDO = getUserInteractionRecordsDO1()
        userInteractionRecordsDO.userId = DEFAULT_USER_ID
        userInteractionRecordsDO.type = PromotionType.COUPON_USE_IN_TRADE_DIRECT.getCode()
        userInteractionRecordsDO.id = null
        userInteractionRecordsDO.value = 999
        CouponDO c1 = makeCouponDO(CouponType.OFF_FOR_CONDITION.code, "0.99")
        c1.id = 929302L
        c1.minValue = 1000

        UserCouponDO userCouponDO = new UserCouponDO();
        userCouponDO.setCouponId(c1.getId())
        userCouponDO.setStatus((byte) 1)
        userCouponDO.setUserId(DEFAULT_USER_ID)
        userCouponDO.setStartTime(new DateTime().minusDays(1).toDate())
        userCouponDO.setEndTime(new DateTime().plusDays(2).toDate())

        droolsExecutor.execute(promotionResult, userInteractionRecordsDO,
                getUserInteractionRecordsDO1(), getUserInteractionRecordsDO3(), getUserInteractionRecordsDO2(),
                c1, userCouponDO)

        then:
        promotionResult.reward == false
    }

    def "测试验证交易有没有到优惠券的最小金额2"() {
        when:
        PromotionResult promotionResult = makePromotionResult(PromotionType.COUPON_USE_IN_TRADE_DIRECT.getCode())
        UserInteractionRecordsDO userInteractionRecordsDO = getUserInteractionRecordsDO1()
        userInteractionRecordsDO.userId = DEFAULT_USER_ID
        userInteractionRecordsDO.type = PromotionType.COUPON_USE_IN_TRADE_DIRECT.getCode()
        userInteractionRecordsDO.id = null
        userInteractionRecordsDO.value = 999
        CouponDO c1 = makeCouponDO(CouponType.OFF_FOR_CONDITION.code, "0.99")
        c1.id = 929302L
        c1.value = 20
        c1.minValue = 1000

        UserCouponDO userCouponDO = new UserCouponDO();
        userCouponDO.setCouponId(c1.getId())
        userCouponDO.setStatus((byte) 1)
        userCouponDO.setUserId(DEFAULT_USER_ID)
        userCouponDO.setStartTime(new DateTime().minusDays(1).toDate())
        userCouponDO.setEndTime(new DateTime().plusDays(2).toDate())

        droolsExecutor.execute(promotionResult, userInteractionRecordsDO, c1, userCouponDO)

        then:
        promotionResult.reward == false
    }


    def "直接使用优惠券"() {
        when:
        PromotionResult promotionResult = makePromotionResult(PromotionType.COUPON_USE_IN_TRADE_DIRECT.getCode())
        UserInteractionRecordsDO userInteractionRecordsDO = getUserInteractionRecordsDO1()
        userInteractionRecordsDO.userId = DEFAULT_USER_ID
        userInteractionRecordsDO.type = PromotionType.COUPON_USE_IN_TRADE_DIRECT.getCode()
        userInteractionRecordsDO.id = null
        userInteractionRecordsDO.value = 2001
        CouponDO c1 = makeCouponDO(CouponType.OFF_FOR_CONDITION.code, "0.99")
        c1.id = 929302L
        c1.value = 20
        c1.minValue = 1000

        UserCouponDO userCouponDO = new UserCouponDO();
        userCouponDO.setCouponId(c1.getId())
        userCouponDO.setStatus((byte) 1)
        userCouponDO.setUserId(DEFAULT_USER_ID)
        userCouponDO.setStartTime(new DateTime().minusDays(1).toDate())
        userCouponDO.setEndTime(new DateTime().plusDays(2).toDate())

        droolsExecutor.execute(promotionResult, userInteractionRecordsDO, c1, userCouponDO,
                getUserInteractionRecordsDO1(), getUserInteractionRecordsDO3())

        then:
        promotionResult.reward == true
        userCouponDO.status == -1
    }

    def "先冻结"() {
        when:
        PromotionResult promotionResult = makePromotionResult(PromotionType.COUPON_USE_IN_TRADE_FROZEN.getCode())
        UserInteractionRecordsDO userInteractionRecordsDO = getUserInteractionRecordsDO1()
        userInteractionRecordsDO.userId = DEFAULT_USER_ID
        userInteractionRecordsDO.type = PromotionType.COUPON_USE_IN_TRADE_FROZEN.getCode()
        userInteractionRecordsDO.id = null
        userInteractionRecordsDO.value = 2001
        CouponDO c1 = makeCouponDO(CouponType.OFF_FOR_CONDITION.code, "0.99")
        c1.id = 929302L
        c1.value = 20
        c1.minValue = 1000

        UserCouponDO userCouponDO = new UserCouponDO();
        userCouponDO.setCouponId(c1.getId())
        userCouponDO.setStatus((byte) 1)
        userCouponDO.setUserId(DEFAULT_USER_ID)
        userCouponDO.setStartTime(new DateTime().minusDays(1).toDate())
        userCouponDO.setEndTime(new DateTime().plusDays(2).toDate())

        droolsExecutor.execute(promotionResult, userInteractionRecordsDO, c1, userCouponDO)

        then:
        promotionResult.reward == true
        userCouponDO.status == 0
    }

    def "冻结变成已使用"() {
        when:
        PromotionResult promotionResult = makePromotionResult(PromotionType.COUPON_USE_IN_TRADE_FROZEN_ELIMINATE.getCode())
        UserInteractionRecordsDO userInteractionRecordsDO = getUserInteractionRecordsDO1()
        userInteractionRecordsDO.userId = DEFAULT_USER_ID
        userInteractionRecordsDO.type = PromotionType.COUPON_USE_IN_TRADE_FROZEN_ELIMINATE.getCode()
        userInteractionRecordsDO.id = null
        CouponDO c1 = makeCouponDO(CouponType.OFF_FOR_CONDITION.code, "0.99")
        c1.id = 929302L
        c1.value = 20
        c1.minValue = 1000

        UserCouponDO userCouponDO = new UserCouponDO();
        userCouponDO.setCouponId(c1.getId())
        userCouponDO.setStatus((byte) 0)
        userCouponDO.setUserId(DEFAULT_USER_ID)
        userCouponDO.setStartTime(new DateTime().minusDays(1).toDate())
        userCouponDO.setEndTime(new DateTime().plusDays(2).toDate())

        droolsExecutor.execute(promotionResult, userInteractionRecordsDO, c1, userCouponDO,
                getUserInteractionRecordsDO1(), getUserInteractionRecordsDO2())

        then:
        promotionResult.reward == true
        userCouponDO.status == -1
    }

    def "新人优惠已领取"() {
        when:
        PromotionResult promotionResult = makePromotionResult(PromotionType.COUPON_NEW_USER.getCode())
        UserInteractionRecordsDO userInteractionRecordsDO = getUserInteractionRecordsDO1()
        userInteractionRecordsDO.userId = DEFAULT_USER_ID
        userInteractionRecordsDO.type = PromotionType.COUPON_NEW_USER.getCode()

        UserInteractionRecordsDO userInteractionRecordsDO1 = getUserInteractionRecordsDO1()
        userInteractionRecordsDO1.userId = DEFAULT_USER_ID
        userInteractionRecordsDO1.type = PromotionType.COUPON_NEW_USER.getCode()

        UserInteractionRecordsDO userInteractionRecordsDO2 = getUserInteractionRecordsDO1()
        userInteractionRecordsDO2.userId = DEFAULT_USER_ID
        userInteractionRecordsDO2.type = PromotionType.COUPON_NEW_USER.getCode()


        UserInteractionRecordsDO userInteractionRecordsDO3 = getUserInteractionRecordsDO1()
        userInteractionRecordsDO3.userId = DEFAULT_USER_ID
        userInteractionRecordsDO3.type = PromotionType.COUPON_NEW_USER.getCode()
        userInteractionRecordsDO3.id = null

        CouponDO c1 = makeCouponDO(CouponType.ROOKIE.code, "1")
        c1.id = 929302L
        c1.value = 20
        c1.minValue = 1000

        UserCouponDO userCouponDO = new UserCouponDO();


        droolsExecutor.execute(promotionResult, userInteractionRecordsDO, c1, userCouponDO, userInteractionRecordsDO1, userInteractionRecordsDO2,
                userInteractionRecordsDO3)

        then:
        promotionResult.reward == false
        promotionResult.code == PromotionErrorEnum.COUPON_ROOKIE_PRESENT_ALREADY_RECEIVED.code
    }

    def "新人优惠领取"() {
        when:
        PromotionResult promotionResult = makePromotionResult(PromotionType.COUPON_NEW_USER.getCode())
        UserInteractionRecordsDO userInteractionRecordsDO = getUserInteractionRecordsDO1()
        userInteractionRecordsDO.userId = DEFAULT_USER_ID
        userInteractionRecordsDO.id = null
        userInteractionRecordsDO.type = PromotionType.COUPON_NEW_USER.getCode()

        UserInteractionRecordsDO userInteractionRecordsDO1 = getUserInteractionRecordsDO1()
        userInteractionRecordsDO1.userId = DEFAULT_USER_ID
        userInteractionRecordsDO1.type = PromotionType.COUPON_NEW_USER.getCode()

        UserInteractionRecordsDO userInteractionRecordsDO2 = getUserInteractionRecordsDO1()
        userInteractionRecordsDO2.userId = DEFAULT_USER_ID
        userInteractionRecordsDO2.type = PromotionType.COUPON_NEW_USER.getCode()

        CouponDO c1 = makeCouponDO(CouponType.ROOKIE.code, "1")
        c1.id = 929302L
        c1.value = 20
        c1.minValue = 1000

        UserCouponDO userCouponDO = new UserCouponDO();


        droolsExecutor.execute(promotionResult, userInteractionRecordsDO, c1, userCouponDO, userInteractionRecordsDO1, userInteractionRecordsDO2)

        then:
        promotionResult.reward == true
        Utils.isToday(userCouponDO.startTime) == true
        userCouponDO.endTime != null
    }


    List<CouponDO> createCouponDO() {

        Date startTime = new DateTime().minusDays(10)
        Date endTime = new DateTime().plusDays(10)

        CouponDO c1 = new CouponDO(shopId: 999L, startTime: startTime, endTime: endTime, name: "满100减20", value: 20, status: 1, minValue: 100, probability: 0.5, version: 1, dateCreated: new Date(), lastUpdated: new Date());
        CouponDO c2 = new CouponDO(shopId: 999L, startTime: startTime, endTime: endTime, name: "满50减10", value: 10, status: 1, minValue: 50, probability: 0.5, version: 1, dateCreated: new Date(), lastUpdated: new Date());
        CouponDO c3 = new CouponDO(shopId: 999L, startTime: startTime, endTime: endTime, name: "满1000减200", value: 200, status: 1, minValue: 1000, probability: 0.5, version: 1, dateCreated: new Date(), lastUpdated: new Date());

        return Lists.newArrayList(c1, c2, c3);
    }


    UserInteractionRecordsDO getUserInteractionRecordsDO1() {
        UserInteractionRecordsDO userInteractionRecordsDO = new UserInteractionRecordsDO();
        userInteractionRecordsDO.id = 12
        userInteractionRecordsDO.dateCreated = new Date()
        userInteractionRecordsDO.lastUpdated = new Date()
        userInteractionRecordsDO.setStatus((byte) 1)
        userInteractionRecordsDO.setType(PromotionType.COUPON_SHAKE.getCode())
        userInteractionRecordsDO.setVersion(1)
        userInteractionRecordsDO
    }

    UserInteractionRecordsDO getUserInteractionRecordsDO2() {
        UserInteractionRecordsDO userInteractionRecordsDO = new UserInteractionRecordsDO();
        userInteractionRecordsDO.id = 23
        userInteractionRecordsDO.dateCreated = new Date()
        userInteractionRecordsDO.lastUpdated = new Date()
        userInteractionRecordsDO.setStatus((byte) 1)
        userInteractionRecordsDO.setType(PromotionType.COUPON_SHAKE.getCode())
        userInteractionRecordsDO.setVersion(1)
        userInteractionRecordsDO
    }

    UserInteractionRecordsDO getUserInteractionRecordsDO3() {
        UserInteractionRecordsDO userInteractionRecordsDO = new UserInteractionRecordsDO();
        userInteractionRecordsDO.id = 44
        userInteractionRecordsDO.dateCreated = new Date()
        userInteractionRecordsDO.lastUpdated = new Date()
        userInteractionRecordsDO.setStatus((byte) 1)
        userInteractionRecordsDO.setType(PromotionType.COUPON_SHAKE.getCode())
        userInteractionRecordsDO.setVersion(1)
        userInteractionRecordsDO
    }

    private PromotionResult makePromotionResult(Long type) {
        PromotionResult promotionResult = new PromotionResult()
        promotionResult.type = type
        promotionResult
    }


    CouponDO makeCouponDO(int type, String probability) {
        CouponDO couponDO = new CouponDO()
        couponDO.id = DEFAULT_ID++
        couponDO.type = type
        couponDO.probability = probability
        return couponDO
    }


}
