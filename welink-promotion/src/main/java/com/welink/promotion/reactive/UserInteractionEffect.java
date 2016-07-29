package com.welink.promotion.reactive;

import com.google.common.base.Optional;

import java.util.List;

/**
 * 用户互动以获取优惠接口
 * <p/>
 * Created by saarixx on 5/3/15.
 */
public interface UserInteractionEffect {

    /**
     * 通过用户行为来获取积分或者优惠券，等等；
     *
     * @param userInteractionRequest
     * @return
     */
    Optional<PromotionResult> interactive(UserInteractionRequest userInteractionRequest);
}
