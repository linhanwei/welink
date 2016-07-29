package com.welink.web.resource.hpage;

import com.daniel.weixin.mp.api.WxMpService;
import com.daniel.weixin.mp.bean.result.WxMpOAuth2AccessToken;
import com.daniel.weixin.mp.bean.result.WxMpUser;
import com.google.common.base.Preconditions;
import com.welink.biz.service.CouponService;
import com.welink.biz.service.PointService;
import com.welink.biz.service.ProfileExtService;
import com.welink.biz.service.UserService;
import com.welink.biz.util.TimeUtils;
import com.welink.buy.utils.PhenixUserHander;
import com.welink.commons.Env;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.ProfileDO;
import com.welink.web.common.constants.ResponseStatusEnum;
import com.welink.web.common.filter.Profiler;
import com.welink.web.common.model.ResponseResult;
import com.welink.web.common.util.ParameterUtil;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.joda.time.DateTime;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.net.URLEncoder;
import java.util.Date;

/**
 * Created by daniel on 15-1-4.
 */
@Controller
public class UserCenter {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(UserCenter.class);

    @Resource
    private UserService userService;

    @Resource
    private WxMpService wxMpService;

    @Resource
    private ProfileExtService profileExtService;

    @Resource
    private PointService pointService;

    @Resource
    private CouponService couponService;
    
    @Resource
    private DetailPage detailPage;

    @Resource
    private Env env;

    @RequestMapping(value = {"/api/h/1.0/userCenter.htm"}, produces = "text/html;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
        log.info("query string : " + request.getQueryString());
        String code = ParameterUtil.getParameter(request, BizConstants.WX_PARAM_CODE);
        String state = ParameterUtil.getParameter(request, BizConstants.WX_PARAM_STATE);
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        
        String pUserId = ParameterUtil.getParameter(request, "pUserId");	//上级用户id
        if(null == pUserId || !StringUtils.isNumeric(pUserId) || StringUtils.isBlank(pUserId)){
        	pUserId = "0";
        }
        model.addAttribute("pUserId", pUserId);	//上级userId
        
        Session session = currentUser.getSession();
        if (StringUtils.isNotBlank(state) && !StringUtils.equalsIgnoreCase("null", state)) {
            session.setAttribute(BizConstants.WEIXIN_MP_STATE, state);
        }
        session.setTimeout(TimeUtils.TIME_1_MONTH_MILi);
        long profileId = -1l;
        long point = 0;
        long couponCount = 0;
        if (null != session.getAttribute(BizConstants.PROFILE_ID)) {
        	detailPage.setUserAgency(model, session, request);	//设置代理关系
            profileId = (long) session.getAttribute(BizConstants.PROFILE_ID);
            model.addAttribute(BizConstants.PROFILE_ID, session.getAttribute(BizConstants.PROFILE_ID));
            model.addAttribute(BizConstants.P_USER_ID, PhenixUserHander.encodeUserId((Long) session.getAttribute(BizConstants.PROFILE_ID)));
        }
        if (profileId < 0) {
        	response.sendRedirect("/api/h/1.0/vLoginPage.htm?refurl=" 
        			+ URLEncoder.encode("userCenter.htm"+(null==request.getQueryString() ? "" : "?"+request.getQueryString()), "utf-8"));
            return null;
        }
        
        ResponseResult result = new ResponseResult();
        //设置Host参数
        setHost(model);
        model.addAttribute("ver", new Date().getTime() / (1000 * 60 * 60 * 24));
        //openId 检测并进行缓存处理
        String openId = null;
        Profiler.enter(" fetch wechat user info.  ");
        log.info("user enter fetch wechat page ... sesionid:" + session.getId()+".......profileId:"+profileId);
        try {
            if (StringUtils.isNotBlank(code)) {
                log.info("user enter fetch wechat page has code ... sesionid:" + session.getId());
                Profiler.enter(" fetch wechat user info  wechat oauth....  ");
                WxMpOAuth2AccessToken wxMpOAuth2AccessToken = wxMpService.oauth2getAccessToken(code);
                Profiler.release();
                if (null != wxMpOAuth2AccessToken) {
                    log.info("user enter fetch wechat page  wxMpOAuth access token not null ... sesionid:" + session.getId());
                    WxMpUser user = null;
                    openId = wxMpOAuth2AccessToken.getOpenId();
                    Preconditions.checkArgument(StringUtils.isNotBlank(openId), "只能在微信环境下使用，无法获得openId");
                    if (StringUtils.isNotEmpty(openId)) {
                        model.addAttribute("openid", openId);
                        if (profileId > 0) {
                            profileExtService.updateOpenId(openId, profileId);
                        }
                        log.info("user enter fetch wechat page open id is not null  ... sesionid:" + session.getId() + ",openid:" + openId);
                        user = wxMpService.userInfo(openId, BizConstants.WX_LANG, state);
                        String unionId = null;
                        if (null != user) {
                            unionId = user.getUnionId();
                        }
                        //update微信存储信息
                        userService.updateProfileWechatInfo(user);
                        //有存储过微信信息且已经获取过union_id
                        boolean hasFetchedWxInfo = userService.isOauthed(openId);
                        if (!hasFetchedWxInfo) {//未存储过微信用户信息profile wechat
                            if (null != user) {
                                if (!userService.addWechatInfoByToken(user, wxMpOAuth2AccessToken)) {
                                    log.error("insert wechat user info failed. openId:" + wxMpOAuth2AccessToken.getOpenId() + "sessionid:" + session.getId() + ",openid:" + openId);
                                } else {
                                    session.setAttribute(BizConstants.OPENID, openId);
                                    Cookie cookie = new Cookie(BizConstants.JSESSION_ID, session.getId().toString());
                                    cookie.setMaxAge(TimeUtils.TIME_1_MONTH_SEC);
                                    cookie.setPath("/");
                                    response.addCookie(cookie);
                                    log.info("user enter fetch wechat page ... sesionid:" + session.getId() + ",openid:" + openId);
                                }
                            } else {
                                log.error("fetch wechat user info failed. maybe user refresh home page..." + "sessionid:" + session.getId().toString() + ",openid:" + openId);
                            }
                        } else {//存储过profile wechat 用户微信信息
                            if (profileId < 0) {
                                profileId = userService.fetchProfileIdByOpenid(openId);
                            }
                            if (profileId > 0) {
                                profileExtService.updateLastLogin(profileId);
                                session.setAttribute(BizConstants.PROFILE_ID, profileId);
                                ProfileDO profileDO = userService.fetchProfileById(profileId);
                                profileExtService.updateLastLogin(profileId);
                                if(null != profileDO){
                                	 session.setAttribute("isAgency", null == profileDO.getIsAgency() ? "0" : profileDO.getIsAgency());
                                }
                                model.addAttribute(BizConstants.PROFILE_ID, profileId);
                                session.setAttribute(BizConstants.OPENID, wxMpOAuth2AccessToken.getOpenId());
                                Cookie cookie = new Cookie(BizConstants.JSESSION_ID, session.getId().toString());
                                cookie.setMaxAge(TimeUtils.TIME_1_MONTH_SEC);
                                cookie.setPath("/");
                                response.addCookie(cookie);
                                log.info("user enter set sessionid and openid has profile ... sesionid:" + session.getId() + ",openid:" + openId);
                            } else {
                                session.setAttribute(BizConstants.OPENID, wxMpOAuth2AccessToken.getOpenId());
                                Cookie cookie = new Cookie(BizConstants.JSESSION_ID, session.getId().toString());
                                cookie.setMaxAge(TimeUtils.TIME_1_MONTH_SEC);
                                cookie.setPath("/");
                                response.addCookie(cookie);
                                log.info("user enter set sessionid and openid  no profile... sesionid:" + session.getId() + ",openid:" + openId);
                            }
                        }
                        if (profileId < 0) {
                            profileId = userService.checkWxMpBinded(unionId);
                        }
                        if (profileId > 0) {
                            session.setAttribute(BizConstants.PROFILE_ID, profileId);
                            profileExtService.updateLastLogin(profileId);
                            ProfileDO profileDO = userService.fetchProfileById(profileId);
                            if(null != profileDO){
                            	 session.setAttribute("isAgency", null == profileDO.getIsAgency() ? "0" : profileDO.getIsAgency());
                            }
                            model.addAttribute(BizConstants.PROFILE_ID, profileId);
                            //积分 & 优惠券
                            setUmp(model, profileId);
                        }
                    } else {
                        log.error("从微信获取wxMpOauth2AccessToken failed.  openId is null..." + "sessionid:" + session.getId().toString());
                    }

                } else {
                    log.error("fetch access token failed. wxMpOAuth2AccessToken is null... code:" + code + " sessionid:" + session.getId().toString());
                    log.error("fetch access token failed. then enter default session openid fetching ... sessionid:" + session.getId().toString());
                    buildStoredOpenid(model, session, result, response);
                    if (profileId < 0) {
                    	response.sendRedirect("/api/h/1.0/vLoginPage.htm?refurl=" 
                    			+ URLEncoder.encode("userCenter.htm"+(null==request.getQueryString() ? "" : "?"+request.getQueryString()), "utf-8"));
                        return null;
                    }
                    return "userCenter";
                }
                result.setStatus(ResponseStatusEnum.SUCCESS.getCode());
            }
            //不在微信公众环境内/获取不到微信信息
            else {
                if (null != session) {
                    model.addAttribute(BizConstants.OPENID, (String) session.getAttribute(BizConstants.OPENID));
                    model.addAttribute(BizConstants.PROFILE_ID, session.getAttribute(BizConstants.PROFILE_ID));
                    setUmp(model, profileId);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            log.error("catch fetch wechat info failed exception. exp:" + e.getCause() + " ,Message: " + e.getMessage() + "sessionid:" + session.getId().toString());
            buildStoredOpenid(model, session, result, response);
            if (profileId < 0) {
            	response.sendRedirect("/api/h/1.0/vLoginPage.htm?refurl=" 
            			+ URLEncoder.encode("userCenter.htm"+(null==request.getQueryString() ? "" : "?"+request.getQueryString()), "utf-8"));
                return null;
            }
            return "userCenter";
        }

        if (env.isDev() && StringUtils.equals("true", ParameterUtil.getParameter(request, "debug"))) {
            profileId = 277;
            String openid = BizConstants.TEST_OPEN_ID_FOR_LUOMO;
            model.addAttribute("openid", openId);
            session.setAttribute(BizConstants.PROFILE_ID, profileId);
            profileExtService.updateLastLogin(profileId);
            ProfileDO profileDO = userService.fetchProfileById(profileId);
            if(null != profileDO){
            	 session.setAttribute("isAgency", null == profileDO.getIsAgency() ? "0" : profileDO.getIsAgency());
            }
            session.setAttribute(BizConstants.OPENID, openid);
            Cookie cookie = new Cookie(BizConstants.JSESSION_ID, session.getId().toString());
            cookie.setMaxAge(TimeUtils.TIME_1_MONTH_SEC);
            cookie.setPath("/");
            response.addCookie(cookie);
        }
        Profiler.release();
        
        //设置默认站点信息
        userService.setCommunity(model, session, response, profileId);
        setUmp(model, profileId);
        return "userCenter";
    }

    private void setUmp(ModelMap context, long profileId) {
        long point;
        long couponCount;
        point = pointService.findAvailablePointByUserId(profileId);
        couponCount = couponService.findUserCouponCountByUserId(profileId);
        context.put("pointCount", point);
        context.put("couponCount", couponCount);
    }

    private void setHost(ModelMap context) {
        if (env.isProd()) {
            context.put(BizConstants.HOST, BizConstants.H5_SEVEN_NB_HOST);
        } else {
            context.put(BizConstants.HOST, BizConstants.H5_WEILINJIA_HOST);
        }
    }

    private void buildStoredOpenid(ModelMap context, Session session, ResponseResult result, HttpServletResponse response) {
        String openId;
        result.setStatus(ResponseStatusEnum.SUCCESS.getCode());
        context.put("result", result);
        String tmpOpenid = (String) session.getAttribute(BizConstants.OPENID);
        if (StringUtils.isNotBlank(tmpOpenid)) {
            openId = tmpOpenid;
            context.put(BizConstants.OPENID, openId);
            context.put(BizConstants.OPENID, tmpOpenid);
            long profileId = userService.fetchProfileIdByOpenid(openId);
            if (profileId > 0) {
                session.setAttribute(BizConstants.PROFILE_ID, profileId);
                ProfileDO profileDO = userService.fetchProfileById(profileId);
                profileExtService.updateLastLogin(profileId);
                if(null != profileDO){
                	 session.setAttribute("isAgency", null == profileDO.getIsAgency() ? "0" : profileDO.getIsAgency());
                }
                context.put(BizConstants.PROFILE_ID, profileId);
                session.setAttribute(BizConstants.OPENID, openId);
                Cookie cookie = new Cookie(BizConstants.JSESSION_ID, session.getId().toString());
                cookie.setMaxAge(TimeUtils.TIME_1_MONTH_SEC);
                cookie.setPath("/");
                response.addCookie(cookie);
                log.info("pre fetch... user enter set sessionid and openid has profile ... sesionid:" + session.getId() + ",openid:" + openId);
            } else {
                session.setAttribute(BizConstants.OPENID, openId);
                Cookie cookie = new Cookie(BizConstants.JSESSION_ID, session.getId().toString());
                cookie.setMaxAge(TimeUtils.TIME_1_MONTH_SEC);
                cookie.setPath("/");
                response.addCookie(cookie);
                log.info("pre fetch ... user enter set sessionid and openid has profile ... sesionid:" + session.getId() + ",openid:" + openId);
            }
        } else {
            log.error("fetch tmpOpenid is null too ... sessionid:" + session.getId().toString());
        }
    }

}
