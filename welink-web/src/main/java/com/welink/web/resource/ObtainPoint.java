package com.welink.web.resource;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Optional;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.common.security.NeedProfile;
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

/**
 * Created by XUTIAN on 2015/3/7.
 */
@RestController
public class ObtainPoint {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(ObtainPoint.class);

    @Resource
    private UserInteractionEffect userInteractionEffect;

    @NeedProfile
    @RequestMapping(value = {"/api/m/1.0/obtainPoint.json", "/api/h/1.0/obtainPoint.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        long profileId = -1;
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

        UserInteractionRequest userInteractionRequest = new UserInteractionRequest();
        userInteractionRequest.setType(PromotionType.POINT_SIGN_IN.getCode());
        userInteractionRequest.setUserId(profileId);
        Optional<PromotionResult> promotionResultOptional = userInteractionEffect.interactive(userInteractionRequest);

        // 如果数据库挂了
        if (!promotionResultOptional.isPresent()) {

        } else {
            //正常返回
            PromotionResult promotionResult = promotionResultOptional.get();
            // 领取优惠券成功
            if (promotionResult.getReward()) {
                // 如果领取了优惠
                Long pointRecordId = promotionResult.getPromotionId();
                welinkVO.setStatus(1);

            } else {
                // 领取优惠失败，触犯了某个规则
                welinkVO.setStatus(0);
                welinkVO.setCode(promotionResult.getCode());
                welinkVO.setMsg(promotionResult.getMessage());
            }
        }

        return JSON.toJSONString(welinkVO);
    }
}
