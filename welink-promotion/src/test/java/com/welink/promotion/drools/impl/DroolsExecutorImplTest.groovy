package com.welink.promotion.drools.impl

import com.welink.commons.domain.PointAccountDO
import com.welink.commons.domain.PointRecordDO
import com.welink.commons.domain.UserInteractionRecordsDO
import com.welink.promotion.PromotionErrorEnum
import com.welink.promotion.PromotionType
import com.welink.promotion.reactive.PromotionResult
import org.joda.time.DateTime
import spock.lang.Shared
import spock.lang.Specification

/**
 * Created by saarixx on 8/3/15.
 */
class DroolsExecutorImplTest extends Specification {

    @Shared
    DroolsExecutorImpl droolsExecutor = new DroolsExecutorImpl()

    static Long DEFAULT_USER_ID = 999999L

    def setupSpec() {
        droolsExecutor.afterPropertiesSet()
    }

    def setup() {
    }

    def cleanup() {
    }

    def "用户第一次签到"() {
        when:
        PointAccountDO pointAccountDO = makePointAccountDO()
        UserInteractionRecordsDO thisUserInteractionRecordsDO = makeThisUserInteractionRecordsDO(PromotionType.POINT_SIGN_IN.code, null)
        PointRecordDO pointRecordDO = makePointRecordDO()
        PromotionResult promotionResult = makePromotionResult(PromotionType.POINT_SIGN_IN.code)

        droolsExecutor.execute(promotionResult, pointAccountDO, thisUserInteractionRecordsDO, pointRecordDO)

        then:

        promotionResult.reward == true
        thisUserInteractionRecordsDO.value == 1L
        pointAccountDO.availableBalance == 10L
        pointAccountDO.version == 2L;
        pointRecordDO.amount == 10L

    }

    def "用户同一天签到两次"() {
        when:
        PointAccountDO pointAccountDO = makePointAccountDO()
        UserInteractionRecordsDO thisUserInteractionRecordsDO = makeThisUserInteractionRecordsDO(PromotionType.POINT_SIGN_IN.code, null)
        UserInteractionRecordsDO lastUserInteractionRecordsDO = makeLatestUserInteractionRecordsDO(PromotionType.POINT_SIGN_IN.code, 1, new Date())
        PointRecordDO pointRecordDO = makePointRecordDO()
        PromotionResult promotionResult = makePromotionResult(PromotionType.POINT_SIGN_IN.code)

        droolsExecutor.execute(promotionResult, pointAccountDO, thisUserInteractionRecordsDO, pointRecordDO, lastUserInteractionRecordsDO)

        then:
        promotionResult.reward == false

    }

    def "用户昨天第1次签到，今天再次签到"() {
        when:
        PointAccountDO pointAccountDO = makePointAccountDO()
        UserInteractionRecordsDO thisUserInteractionRecordsDO = makeThisUserInteractionRecordsDO(PromotionType.POINT_SIGN_IN.code, null)
        UserInteractionRecordsDO lastUserInteractionRecordsDO = makeLatestUserInteractionRecordsDO(PromotionType.POINT_SIGN_IN.code, 1, new DateTime().minusDays(1).toDate())
        PointRecordDO pointRecordDO = makePointRecordDO()
        PromotionResult promotionResult = makePromotionResult(PromotionType.POINT_SIGN_IN.code)

        droolsExecutor.execute(promotionResult, pointAccountDO, thisUserInteractionRecordsDO, pointRecordDO, lastUserInteractionRecordsDO)

        then:
        promotionResult.reward == true
        thisUserInteractionRecordsDO.value == 2L
        pointAccountDO.availableBalance == 15L
        pointAccountDO.version == 2L;
        pointRecordDO.amount == 15L
    }

    def "用户累积已连续第2次签到，今天再次签到"() {
        when:
        PointAccountDO pointAccountDO = makePointAccountDO()
        UserInteractionRecordsDO thisUserInteractionRecordsDO = makeThisUserInteractionRecordsDO(PromotionType.POINT_SIGN_IN.code, null)
        UserInteractionRecordsDO lastUserInteractionRecordsDO = makeLatestUserInteractionRecordsDO(PromotionType.POINT_SIGN_IN.code, 2, new DateTime().minusDays(1).toDate())
        PointRecordDO pointRecordDO = makePointRecordDO()
        PromotionResult promotionResult = makePromotionResult(PromotionType.POINT_SIGN_IN.code)

        droolsExecutor.execute(promotionResult, pointAccountDO, thisUserInteractionRecordsDO, pointRecordDO, lastUserInteractionRecordsDO)

        then:
        promotionResult.reward == true
        thisUserInteractionRecordsDO.value == 3L
        pointRecordDO.amount == 20L
        pointRecordDO.availableBalance == 20L
        pointAccountDO.availableBalance == 20L
        pointAccountDO.version == 2L;
    }

    def "用户累积已连续第5次签到，今天再次签到"() {
        when:
        PointAccountDO pointAccountDO = makePointAccountDO()
        UserInteractionRecordsDO thisUserInteractionRecordsDO = makeThisUserInteractionRecordsDO(PromotionType.POINT_SIGN_IN.code, null)
        UserInteractionRecordsDO lastUserInteractionRecordsDO = makeLatestUserInteractionRecordsDO(PromotionType.POINT_SIGN_IN.code, 5, new DateTime().minusDays(1).toDate())
        PointRecordDO pointRecordDO = makePointRecordDO()
        PromotionResult promotionResult = makePromotionResult(PromotionType.POINT_SIGN_IN.code)

        droolsExecutor.execute(promotionResult, pointAccountDO, thisUserInteractionRecordsDO, pointRecordDO, lastUserInteractionRecordsDO)

        then:
        promotionResult.reward == true
        thisUserInteractionRecordsDO.value == 6L
        pointAccountDO.availableBalance == 35L
        pointAccountDO.version == 2L;
        pointRecordDO.amount == 35L
        pointRecordDO.availableBalance == 35L
    }

    def "用户累积已连续第6次签到，今天再次签到"() {
        when:
        PointAccountDO pointAccountDO = makePointAccountDO()
        UserInteractionRecordsDO thisUserInteractionRecordsDO = makeThisUserInteractionRecordsDO(PromotionType.POINT_SIGN_IN.code, null)
        UserInteractionRecordsDO lastUserInteractionRecordsDO = makeLatestUserInteractionRecordsDO(PromotionType.POINT_SIGN_IN.code, 6, new DateTime().minusDays(1).toDate())
        PointRecordDO pointRecordDO = makePointRecordDO()
        PromotionResult promotionResult = makePromotionResult(PromotionType.POINT_SIGN_IN.code)

        droolsExecutor.execute(promotionResult, pointAccountDO, thisUserInteractionRecordsDO, pointRecordDO, lastUserInteractionRecordsDO)

        then:
        promotionResult.reward == true
        thisUserInteractionRecordsDO.value == 7L
        pointAccountDO.availableBalance == 40L
        pointAccountDO.version == 2L;
        pointRecordDO.amount == 40L
        pointRecordDO.availableBalance == 40L
    }

    def "用户累积已连续第7次签到，今天再次签到"() {
        when:
        PointAccountDO pointAccountDO = makePointAccountDO()
        UserInteractionRecordsDO thisUserInteractionRecordsDO = makeThisUserInteractionRecordsDO(PromotionType.POINT_SIGN_IN.code, null)
        UserInteractionRecordsDO lastUserInteractionRecordsDO = makeLatestUserInteractionRecordsDO(PromotionType.POINT_SIGN_IN.code, 7, new DateTime().minusDays(1).toDate())
        PointRecordDO pointRecordDO = makePointRecordDO()
        PromotionResult promotionResult = makePromotionResult(PromotionType.POINT_SIGN_IN.code)

        droolsExecutor.execute(promotionResult, pointAccountDO, thisUserInteractionRecordsDO, pointRecordDO, lastUserInteractionRecordsDO)

        then:
        promotionResult.reward == true
        thisUserInteractionRecordsDO.value == 8L
        pointAccountDO.availableBalance == 40L
        pointAccountDO.version == 2L;
        pointRecordDO.amount == 40L
        pointRecordDO.availableBalance == 40L
    }

    def "用户累积已连续第8次签到，今天再次签到"() {
        when:
        PointAccountDO pointAccountDO = makePointAccountDO()
        UserInteractionRecordsDO thisUserInteractionRecordsDO = makeThisUserInteractionRecordsDO(PromotionType.POINT_SIGN_IN.code, null)
        UserInteractionRecordsDO lastUserInteractionRecordsDO = makeLatestUserInteractionRecordsDO(PromotionType.POINT_SIGN_IN.code, 8, new DateTime().minusDays(1).toDate())
        PointRecordDO pointRecordDO = makePointRecordDO()
        PromotionResult promotionResult = makePromotionResult(PromotionType.POINT_SIGN_IN.code)

        droolsExecutor.execute(promotionResult, pointAccountDO, thisUserInteractionRecordsDO, pointRecordDO, lastUserInteractionRecordsDO)

        then:
        promotionResult.reward == true
        thisUserInteractionRecordsDO.value == 9L
        pointAccountDO.availableBalance == 40L
        pointAccountDO.version == 2L;
        pointRecordDO.amount == 40L
        pointRecordDO.availableBalance == 40L
    }

    def "交易中使用积分，未传入tradeId"() {
        when:
        PointAccountDO pointAccountDO = makePointAccountDO()
        UserInteractionRecordsDO thisUserInteractionRecordsDO = makeThisUserInteractionRecordsDO(PromotionType.POINT_USE_IN_TRADE_DIRECT.code, null)
        PointRecordDO pointRecordDO = makePointRecordDO()
        PromotionResult promotionResult = makePromotionResult(PromotionType.POINT_SIGN_IN.code)

        droolsExecutor.execute(promotionResult, pointAccountDO, thisUserInteractionRecordsDO, pointRecordDO)

        then:
        promotionResult.reward == false
    }

    def "交易中使用积分，余额不足"() {
        when:
        PointAccountDO pointAccountDO = makePointAccountDO()
        pointAccountDO.availableBalance = 100L;
        UserInteractionRecordsDO thisUserInteractionRecordsDO = makeThisUserInteractionRecordsDO(PromotionType.POINT_USE_IN_TRADE_DIRECT.code, "1234567")
        thisUserInteractionRecordsDO.value = 101L;
        PointRecordDO pointRecordDO = makePointRecordDO()
        PromotionResult promotionResult = makePromotionResult(PromotionType.POINT_SIGN_IN.code)

        droolsExecutor.execute(promotionResult, pointAccountDO, thisUserInteractionRecordsDO, pointRecordDO)

        then:
        promotionResult.reward == false
    }

    def "交易中使用积分"() {
        when:
        PointAccountDO pointAccountDO = makePointAccountDO()
        pointAccountDO.availableBalance = 100L;
        UserInteractionRecordsDO thisUserInteractionRecordsDO = makeThisUserInteractionRecordsDO(PromotionType.POINT_USE_IN_TRADE_DIRECT.code, "1234567")
        thisUserInteractionRecordsDO.value = 99L;
        PointRecordDO pointRecordDO = makePointRecordDO()
        PromotionResult promotionResult = makePromotionResult(PromotionType.POINT_USE_IN_TRADE_DIRECT.code)

        droolsExecutor.execute(promotionResult, pointAccountDO, thisUserInteractionRecordsDO, pointRecordDO)

        then:
        promotionResult.reward == true
        pointRecordDO.amount == 99L
        pointAccountDO.availableBalance == 1L;
        pointAccountDO.version == 2L;
        pointAccountDO.frozenBalance == null || pointAccountDO.frozenBalance == 0
    }

    def "交易中使用积分，先冻结"() {
        when:
        PointAccountDO pointAccountDO = makePointAccountDO()
        pointAccountDO.availableBalance = 100L;
        UserInteractionRecordsDO thisUserInteractionRecordsDO = makeThisUserInteractionRecordsDO(PromotionType.POINT_USE_IN_TRADE_FROZEN.code, "1234567")
        thisUserInteractionRecordsDO.value = 99L;
        PointRecordDO pointRecordDO = makePointRecordDO()
        PromotionResult promotionResult = makePromotionResult(PromotionType.POINT_USE_IN_TRADE_FROZEN.code)

        droolsExecutor.execute(promotionResult, pointAccountDO, thisUserInteractionRecordsDO, pointRecordDO)

        then:
        promotionResult.reward == true
        pointRecordDO.availableBalance == 1L
        pointRecordDO.amount == 99L
        pointAccountDO.availableBalance == 1L;
        pointAccountDO.version == 2L;
        pointAccountDO.frozenBalance == 99L
    }

    def "交易中使用积分，冻结积分清除 > 冻结积分"() {
        when:
        PointAccountDO pointAccountDO = makePointAccountDO()
        pointAccountDO.availableBalance = 100L;
        pointAccountDO.frozenBalance = 100L
        UserInteractionRecordsDO thisUserInteractionRecordsDO = makeThisUserInteractionRecordsDO(PromotionType.POINT_USE_IN_TRADE_FROZEN_ELIMINATE.code, "1234567")
        thisUserInteractionRecordsDO.value = 101;
        PointRecordDO pointRecordDO = makePointRecordDO()
        PromotionResult promotionResult = makePromotionResult(PromotionType.POINT_USE_IN_TRADE_FROZEN_ELIMINATE.code)

        droolsExecutor.execute(promotionResult, pointAccountDO, thisUserInteractionRecordsDO, pointRecordDO)

        then:
        promotionResult.reward == false
    }

    def "交易中使用积分，冻结积分清除"() {
        when:
        PointAccountDO pointAccountDO = makePointAccountDO()
        pointAccountDO.availableBalance = 100L;
        pointAccountDO.frozenBalance = 200L
        UserInteractionRecordsDO thisUserInteractionRecordsDO = makeThisUserInteractionRecordsDO(PromotionType.POINT_USE_IN_TRADE_FROZEN_ELIMINATE.code, "1234567")
        thisUserInteractionRecordsDO.value = 100L;
        PointRecordDO pointRecordDO = makePointRecordDO()
        PromotionResult promotionResult = makePromotionResult(PromotionType.POINT_USE_IN_TRADE_FROZEN_ELIMINATE.code)

        droolsExecutor.execute(promotionResult, pointAccountDO, thisUserInteractionRecordsDO, pointRecordDO)

        then:
        promotionResult.reward == true
        pointAccountDO.availableBalance == 100L;
        pointAccountDO.frozenBalance == 100L;
    }

    def "交易获得用户积分"() {
        when:
        PointAccountDO pointAccountDO = makePointAccountDO()
        pointAccountDO.availableBalance = 100L;
        UserInteractionRecordsDO thisUserInteractionRecordsDO = makeThisUserInteractionRecordsDO(PromotionType.POINT_TRADE_SUCCESS.code, "1234567")
        thisUserInteractionRecordsDO.value = 10000L;
        PointRecordDO pointRecordDO = makePointRecordDO()
        PromotionResult promotionResult = makePromotionResult(PromotionType.POINT_USE_IN_TRADE_FROZEN_ELIMINATE.code)

        droolsExecutor.execute(promotionResult, pointAccountDO, thisUserInteractionRecordsDO, pointRecordDO)

        then:
        promotionResult.reward == true
        pointAccountDO.availableBalance == 200L;
    }

    def "新用户送积分"() {
        when:
        PointAccountDO pointAccountDO = makePointAccountDO()
        pointAccountDO.availableBalance = 100L;
        UserInteractionRecordsDO thisUserInteractionRecordsDO = makeThisUserInteractionRecordsDO(PromotionType.POINT_NEW_USER.code, "1234567")
        PointRecordDO pointRecordDO = makePointRecordDO()
        PromotionResult promotionResult = makePromotionResult(PromotionType.POINT_NEW_USER.code)

        droolsExecutor.execute(promotionResult, pointAccountDO, thisUserInteractionRecordsDO, pointRecordDO)

        then:
        promotionResult.reward == true
        pointAccountDO.availableBalance == 600L;
    }

    def "新用户送积分两次"() {
        when:
        PointAccountDO pointAccountDO = makePointAccountDO()
        pointAccountDO.availableBalance = 100L;
        UserInteractionRecordsDO thisUserInteractionRecordsDO = makeThisUserInteractionRecordsDO(PromotionType.POINT_NEW_USER.code, "1234567")
        UserInteractionRecordsDO lastUserInteractionRecordsDO = makeLatestUserInteractionRecordsDO(PromotionType.POINT_NEW_USER.code, 0, new DateTime().minusDays(1).toDate())
        PointRecordDO pointRecordDO = makePointRecordDO()
        PromotionResult promotionResult = makePromotionResult(PromotionType.POINT_NEW_USER.code)

        droolsExecutor.execute(promotionResult, pointAccountDO, thisUserInteractionRecordsDO, pointRecordDO, lastUserInteractionRecordsDO)

        then:
        promotionResult.reward == false
        promotionResult.code == PromotionErrorEnum.POINT_ROOKIE_PRESENT_ALREADY_RECEIVED.getCode()
    }

    private PointAccountDO makePointAccountDO() {
        PointAccountDO pointAccountDO = new PointAccountDO()
        pointAccountDO.id = 2046L
        pointAccountDO.userId = DEFAULT_USER_ID
        pointAccountDO.availableBalance = 0
        pointAccountDO.dateCreated = new Date()
        pointAccountDO.status = 1 as Byte
        pointAccountDO.version = 1L
        pointAccountDO
    }

    private UserInteractionRecordsDO makeThisUserInteractionRecordsDO(Integer type, String targetId) {
        UserInteractionRecordsDO userInteractionRecordsDO = new UserInteractionRecordsDO()
        userInteractionRecordsDO.dateCreated = new Date()
        userInteractionRecordsDO.status = 1 as Byte
        userInteractionRecordsDO.type = type
        userInteractionRecordsDO.targetId = targetId
        userInteractionRecordsDO.userId = DEFAULT_USER_ID
        userInteractionRecordsDO
    }

    private UserInteractionRecordsDO makeLatestUserInteractionRecordsDO(Integer type, Long value, Date dateCreated) {
        UserInteractionRecordsDO userInteractionRecordsDO = new UserInteractionRecordsDO()
        userInteractionRecordsDO.id = 2046L
        userInteractionRecordsDO.dateCreated = dateCreated
        userInteractionRecordsDO.status = 1 as Byte
        userInteractionRecordsDO.type = type
        userInteractionRecordsDO.value = value
        userInteractionRecordsDO.userId = DEFAULT_USER_ID
        userInteractionRecordsDO
    }

    private PromotionResult makePromotionResult(Long type) {
        PromotionResult promotionResult = new PromotionResult()
        promotionResult.type = type
        promotionResult
    }

    private PointRecordDO makePointRecordDO() {
        PointRecordDO pointRecordDO = new PointRecordDO()
        pointRecordDO.dateCreated = new Date()
        pointRecordDO.userId = DEFAULT_USER_ID
        pointRecordDO
    }
}
