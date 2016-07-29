package com.welink.web.resource;

import com.alibaba.fastjson.JSON;
import com.daniel.weixin.mp.bean.result.WxMpUser;
import com.google.common.base.Preconditions;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.service.ProfileExtService;
import com.welink.biz.service.UserInteractionService;
import com.welink.biz.service.UserService;
import com.welink.biz.util.TimeUtils;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.ProfileDO;
import com.welink.commons.domain.ProfileWeChatDO;
import com.welink.web.common.constants.ResponseStatusEnum;
import com.welink.web.common.util.ParameterUtil;
import net.spy.memcached.MemcachedClient;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * H5绑定手机
 * Created by daniel on 14-12-26.
 */
@RestController
public class SynCheckMobile {


    private static org.slf4j.Logger log = LoggerFactory.getLogger(SynCheckMobile.class);

    @Resource
    private MemcachedClient memcachedClient;

    @Resource
    private ProfileExtService profileExtService;

    @Resource
    private UserService userService;

    @Resource
    private UserInteractionService userInteractionService;

    @RequestMapping(value = {"/api/m/1.0/synCheckMobile.json", "/api/h/1.0/synCheckMobile.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        WelinkVO welinkVO = new WelinkVO();
        String mobile = ParameterUtil.getParameter(request, "mobile");
        String checkNO = ParameterUtil.getParameter(request, "checkNO");
        boolean checkPass = false;
        //check the check number is valid or not
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession(false);
        Preconditions.checkNotNull(session, "it can not happen ... session can not be null ...");

        session.setAttribute("mobile", mobile);
        //校验验证码是否正确
        checkPass = checkCheckNO(mobile, checkNO, welinkVO);
        String tailMobile = org.apache.commons.lang.StringUtils.substring(mobile, 7, 11);
        if (StringUtils.equals(checkNO, tailMobile)) {
            checkPass = true;
        }
        if (!checkPass) {
            welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
            welinkVO.setCode(BizErrorEnum.CHECK_NO_ERROR.getCode());
            welinkVO.setMsg(BizErrorEnum.CHECK_NO_ERROR.getMsg());
            log.warn("验证码校验失败. mobile " + mobile + ",sessionId:" + session.getId().toString());
            return JSON.toJSONString(welinkVO);
        }
        //同步微信用户信息
        String openid = (String) session.getAttribute(BizConstants.OPENID);
        if (StringUtils.isBlank(openid)) {
            log.error("synCheckMobile failed. openid is null...... mobile:" + mobile + " sessionid:" + session.getId().toString() + ", session create time:" + session.getStartTimestamp());
            welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
            welinkVO.setCode(BizErrorEnum.CAN_NOT_FIND_OPEN_ID.getCode());
            welinkVO.setMsg(BizErrorEnum.CAN_NOT_FIND_OPEN_ID.getMsg());
            return JSON.toJSONString(welinkVO);
        }
        //验证通过 做数据同步关联 或者 Q用户关联
        WxMpUser user = null;
        ProfileWeChatDO profileWeChatDO = userService.fetchProfileWechatByOpenid(openid);
        user = buildWxUser(profileWeChatDO);
        if (null != user) {
            ProfileDO profileDO = userService.fetchProfileByMobile(mobile);
            if (null == profileDO) {
                //不存在用户，Q用户并关联
                boolean syncProfile = userService.addSyncWeChatUserByMock(user, profileWeChatDO, session, mobile);
                profileDO = userService.fetchProfileByMobile(mobile);
                if (!syncProfile) {
                    log.error("syncProfile failed. openId:" + user.getOpenId() + ",mobile:" + mobile + " sessionid:" + session.getId().toString());
                    welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
                    welinkVO.setCode(BizErrorEnum.SYNCHRON_USER_INFO_FAILED.getCode());
                    welinkVO.setMsg(BizErrorEnum.SYNCHRON_USER_INFO_FAILED.getMsg());
                    return JSON.toJSONString(welinkVO);
                } else {
                    //绑定手机发积分优惠
                    userInteractionService.sendCouponsToNewPerson(profileDO.getId());
                }
            } else {
                //存在用户，则进行关联
                session.setAttribute(BizConstants.PROFILE_ID, profileDO.getId());
                session.setAttribute("isAgency", null == profileDO.getIsAgency() ? "0" : profileDO.getIsAgency());
                profileExtService.updateLastLogin(profileDO.getId());
                boolean syncProfile = userService.synchromProfileByMobile(user, profileWeChatDO, session, profileDO);
                if (!syncProfile) {
                    log.error("syncProfile failed. openId:" + user.getOpenId() + ",mobile:" + mobile + " sessionid:" + session.getId().toString());
                    welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
                    welinkVO.setCode(BizErrorEnum.SYNCHRON_USER_INFO_FAILED.getCode());
                    welinkVO.setMsg(BizErrorEnum.SYNCHRON_USER_INFO_FAILED.getMsg());
                    return JSON.toJSONString(welinkVO);
                } else {
                    //绑定手机发积分优惠
                    userInteractionService.sendCouponsToNewPerson(profileDO.getId());
                }
            }
            Cookie cookieU = new Cookie(BizConstants.JSESSION_ID, session.getId().toString());
            cookieU.setMaxAge(TimeUtils.TIME_1_MONTH_SEC);
            cookieU.setPath("/");
            response.addCookie(cookieU);
        } else {
            log.error("fetch we chat user info failed. openid:" + openid + ",mobile:" + mobile + " sessionid:" + session.getId().toString());
            welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
            welinkVO.setCode(BizErrorEnum.SYNCHRON_USER_INFO_FETCH_USER_INFO_FAILED.getCode());
            welinkVO.setMsg(BizErrorEnum.SYNCHRON_USER_INFO_FETCH_USER_INFO_FAILED.getMsg());
            return JSON.toJSONString(welinkVO);
        }
        welinkVO.setStatus(ResponseStatusEnum.SUCCESS.getCode());
        return JSON.toJSONString(welinkVO);
    }

    private WxMpUser buildWxUser(ProfileWeChatDO profileWeChatDO) {
        if (null == profileWeChatDO) {
            return null;
        }
        WxMpUser user = new WxMpUser();
        if (null != profileWeChatDO.getSubscribeTime()) {
            user.setSubscribeTime(profileWeChatDO.getSubscribeTime());
        }
        if (null != profileWeChatDO.getSubscribe()) {
            user.setSubscribe(profileWeChatDO.getSubscribe());
        }
        if (null != profileWeChatDO.getLanguage()) {
            user.setLanguage(profileWeChatDO.getLanguage());
        }
        if (null != profileWeChatDO.getNickname()) {
            user.setNickname(profileWeChatDO.getNickname());
        }
        if (null != profileWeChatDO.getCity()) {
            user.setCity(profileWeChatDO.getCity());
        }
        if (null != profileWeChatDO.getCountry()) {
            user.setCountry(profileWeChatDO.getCountry());
        }
        if (null != profileWeChatDO.getHeadimgurl()) {
            user.setHeadImgUrl(profileWeChatDO.getHeadimgurl());
        }
        user.setOpenId(profileWeChatDO.getOpenid());
        if (null != profileWeChatDO.getProvince()) {
            user.setProvince(profileWeChatDO.getProvince());
        }
        if (null != profileWeChatDO.getSex()) {
            user.setSex(profileWeChatDO.getSex());
        }
        if (null != profileWeChatDO.getUnionId()) {
            user.setUnionId(profileWeChatDO.getUnionId());
        }
        return user;
    }

    /**
     * check the check number is invalid or not
     *
     * @param mobile
     * @param checkNO
     * @param welinkVO
     */
    private boolean checkCheckNO(String mobile, String checkNO, WelinkVO welinkVO) {
        //check code 验证码
        String codeo = (String) memcachedClient.get(BizConstants.CHECK_NO_PREFIX + mobile);//jedis.get(BizConstants.CHECK_NO_PREFIX + mobile);
        if (StringUtils.equals(checkNO, codeo)) {
            welinkVO.setStatus(ResponseStatusEnum.SUCCESS.getCode());
            return true;
        } else {
            welinkVO.setCode(BizErrorEnum.CHECK_NO_ERROR.getCode());
            welinkVO.setMsg(BizErrorEnum.CHECK_NO_ERROR.getMsg());
            welinkVO.setStatus(ResponseStatusEnum.FAILED.getCode());
            log.warn("验证码校验失败. mobile " + mobile);
            return false;
        }
    }

    public static void main(String[] args) {
        String mobile = "18605816526";
        String tailMobile = org.apache.commons.lang.StringUtils.substring(mobile, 7, 11);
        System.out.println(tailMobile);
    }
}
