package com.welink.promotion.reactive.services.coupons;

import com.welink.commons.domain.CouponDO;
import com.welink.commons.domain.UserCouponDO;
import com.welink.commons.domain.UserInteractionRecordsDO;

/**
 * Created by saarixx on 9/3/15.
 */
public interface UserCouponService {

    public boolean updateUserCoupon(UserInteractionRecordsDO userInteractionRecordsDO, UserCouponDO userCouponDO);
}
