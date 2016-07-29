/**
 * Project Name:welink-web
 * File Name:CouponAction.java
 * Package Name:com.welink.web.resource
 * Date:2016年3月30日下午2:37:46
 * Copyright (c) 2016, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.web.resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.service.CouponService;
import com.welink.commons.domain.CouponDO;
import com.welink.commons.domain.CouponDOExample;
import com.welink.commons.domain.UserInteractionRecordsDO;
import com.welink.commons.domain.UserInteractionRecordsDOExample;
import com.welink.commons.persistence.CouponDOMapper;
import com.welink.commons.persistence.UserCouponDOMapper;
import com.welink.commons.persistence.UserInteractionRecordsDOMapper;
import com.welink.promotion.PromotionType;
import com.welink.promotion.reactive.UserInteractionRequest;
import com.welink.web.common.constants.ResponseStatusEnum;
import com.welink.web.common.util.ParameterUtil;

/**
 * ClassName:CouponAction <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2016年3月30日 下午2:37:46 <br/>
 * @author   LuoGuangChun
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
@RestController
public class CouponAction {
	
	@Resource
    private CouponService couponService;
	
	@Resource
    private UserCouponDOMapper userCouponDOMapper;
	
	@Resource
    private CouponDOMapper couponDOMapper;
    
    @Resource
    private UserInteractionRecordsDOMapper userInteractionRecordsDOMapper;
	
	private static org.slf4j.Logger log = LoggerFactory.getLogger(CouponAction.class);
	
	
	@RequestMapping(value = {"/api/m/1.0/sendActiveCoupon.json", "/api/h/1.0/sendActiveCoupon.json"}, produces = "application/json;charset=utf-8")
    public String sendActiveCoupon(HttpServletRequest request, HttpServletResponse response) throws Exception {
		WelinkVO welinkVO = new WelinkVO();
		welinkVO.setStatus(1);
        Map resultMap = new HashMap();
		org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        long profileId = -1;
        profileId = (long) session.getAttribute("profileId");
        if (profileId < 0) {
            welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
            welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
            welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
            return JSON.toJSONString(welinkVO);
        }
		welinkVO.setResult(resultMap);
		return JSON.toJSONString(welinkVO);
	}
	
	@RequestMapping(value = {"/api/m/1.0/receiveActiveCoupon.json", "/api/h/1.0/receiveActiveCoupon.json"}, produces = "application/json;charset=utf-8")
    public String receiveActiveCoupon(HttpServletRequest request, HttpServletResponse response) throws Exception {
		//long couponId = ParameterUtil.getParameterAslongForSpringMVC(request, "couponId", 0);
		WelinkVO welinkVO = new WelinkVO();
		welinkVO.setStatus(1);
        Map resultMap = new HashMap();
		org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        long profileId = -1;
        profileId = (long) session.getAttribute("profileId");
        if (profileId < 0) {
            welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
            welinkVO.setCode(BizErrorEnum.SESSION_TIME_OUT.getCode());
            welinkVO.setMsg(BizErrorEnum.SESSION_TIME_OUT.getMsg());
            return JSON.toJSONString(welinkVO);
        }
        
        //优惠券交互类型
        int interactionType = PromotionType.COUPON_SEND_ACTIVE_900001.getCode();	//国足
        UserInteractionRecordsDOExample userInteractionRecordsDOExample = new UserInteractionRecordsDOExample();
        userInteractionRecordsDOExample.setOrderByClause("id DESC");
        userInteractionRecordsDOExample.createCriteria() //
                .andUserIdEqualTo(profileId) //
                .andTypeEqualTo(interactionType);

        UserInteractionRequest userInteractionRequest = new UserInteractionRequest();
        userInteractionRequest.setUserId(profileId);
        userInteractionRequest.setType(interactionType);		
        welinkVO.setResult(resultMap);
        List<UserInteractionRecordsDO> userInteractionRecordsDOList = userInteractionRecordsDOMapper.selectByExample(userInteractionRecordsDOExample);
        if(null == userInteractionRecordsDOList || userInteractionRecordsDOList.isEmpty()){
        	long couponId = 0L;	//优惠券id
        	CouponDOExample couponDOExample = new CouponDOExample();
        	couponDOExample.createCriteria().andShopIdEqualTo(Long.valueOf(PromotionType.COUPON_SEND_ACTIVE_900001.getCode()))
        		.andStatusEqualTo((byte)1);
        	List<CouponDO> couponDOList = couponDOMapper.selectByExample(couponDOExample);
        	CouponDO couponDO = null;
        	if(!couponDOList.isEmpty()){
        		couponDO = couponDOList.get(0);
        	}else{
        		welinkVO.setStatus(ResponseStatusEnum.SUCCESS.getCode());
        		resultMap.put("status", 2);	//status(0=已领取不能再领取;1=领取成功；2=无此优惠券)
                welinkVO.setMsg("亲~此优惠券已过期或不存在~");
                return JSON.toJSONString(welinkVO);
        	}
        	if(null != couponDO && couponDO.getStatus().equals((byte)1)){
        		welinkVO = couponService.giveCoupon(userInteractionRequest, profileId, couponDO.getId());
        		resultMap.put("status", 1);		//status(0=已领取不能再领取;1=领取成功；2=无此优惠券)
        		resultMap.put("name", couponDO.getName());		//优惠券名称
        		resultMap.put("picUrl", couponDO.getPicUrl());	//优惠券图片
        		welinkVO.setResult(resultMap);
        		return JSON.toJSONString(welinkVO);
        	}else{
        		welinkVO.setStatus(ResponseStatusEnum.SUCCESS.getCode());
        		resultMap.put("status", 2);		//status(0=已领取不能再领取;1=领取成功；2=无此优惠券)
                welinkVO.setMsg("亲~此优惠券已过期或不存在~");
                return JSON.toJSONString(welinkVO);
        	}
        }else{
        	welinkVO.setStatus(ResponseStatusEnum.SUCCESS.getCode());
        	resultMap.put("status", 0);		//status(0=已领取不能再领取;1=领取成功；2=无此优惠券)
            welinkVO.setMsg("亲~您已领取过优惠券，不能再领取啦~");
            return JSON.toJSONString(welinkVO);
        }
	}
	
}

