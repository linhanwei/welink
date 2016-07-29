package com.welink.promotion.reactive.services.points;

import com.welink.commons.domain.PointAccountDO;
import com.welink.commons.domain.PointRecordDO;
import com.welink.commons.domain.UserInteractionRecordsDO;
import com.welink.promotion.reactive.PromotionResult;

/**
 * 用户积分接口
 * <p/>
 * Created by saarixx on 6/3/15.
 */
public interface UserPointService {

    /**
     * 一次互动动作，依靠规则引擎生成不同的 pointRecordDO 以及 pointAccountDO
     *
     * @param userInteractionRecordsDO 最新的一次有效交互动作，有效依靠规则引擎判断
     * @param pointRecordDO            用户积分变更纪录
     * @param pointAccountDO           用户账户变更纪录
     * @return
     */
    public boolean updateUserPoint(UserInteractionRecordsDO userInteractionRecordsDO, PointRecordDO pointRecordDO, PointAccountDO pointAccountDO);
}
