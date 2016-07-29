package com.welink.web.resource.hpage;

import com.daniel.weixin.mp.api.WxMpService;
import com.daniel.weixin.mp.bean.result.WxMpOAuth2AccessToken;
import com.daniel.weixin.mp.bean.result.WxMpUser;
import com.welink.biz.service.UserService;
import com.welink.biz.util.TimeUtils;
import com.welink.biz.util.UserUtils;
import com.welink.biz.wx.tenpay.util.ConstantUtil;
import com.welink.commons.Env;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.ProfileDO;
import com.welink.web.common.filter.Profiler;
import com.welink.web.common.util.ParameterUtil;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Date;

/**
 * Created by daniel on 15-4-14.
 */
@Controller
public class Christ {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(Christ.class);

    @Resource
    private UserService userService;

    @Resource
    private WxMpService wxMpService;

    @Resource
    private Env env;

    @RequestMapping(value = {"/api/h/1.0/christ.htm"}, produces = "text/html;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
        String code = ParameterUtil.getParameter(request, BizConstants.WX_PARAM_CODE);
        String state = ParameterUtil.getParameter(request, BizConstants.WX_PARAM_STATE);
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        session.setTimeout(TimeUtils.TIME_1_MONTH_MILi);
        System.out.println("Christ..............................."+state);
        if (StringUtils.isNotBlank(state)) {
            session.setAttribute(BizConstants.WEIXIN_MP_STATE, state);
        }

        long profileId = -1l;
        if (null != session.getAttribute(BizConstants.PROFILE_ID)) {
            profileId = (long) session.getAttribute(BizConstants.PROFILE_ID);
            model.addAttribute(BizConstants.PROFILE_ID, session.getAttribute(BizConstants.PROFILE_ID));
            model.addAttribute(BizConstants.SHOP_ID, session.getAttribute(BizConstants.SHOP_ID));
        }
        model.addAttribute("ver", new Date().getTime() / (1000 * 60 * 60 * 24));
        model.addAttribute("time", new Date().getTime());
        //设置host参数
        setHost(model);
        //openid 检测并进行缓存处理
        String openid = null;
        if (null != session.getAttribute(BizConstants.OPENID)) {
            openid = (String) session.getAttribute(BizConstants.OPENID);
            //根据openid获取profile并进行缓存处理
            if (StringUtils.isNotBlank(openid)) {
                try {
                    profileId = userService.fetchProfileIdByOpenid(openid);
                } catch (Exception e) {
                    log.error("cant find profileId by openid.openid:" + openid);
                }
                if (profileId > 0) {
                    session.setAttribute(BizConstants.PROFILE_ID, profileId);
                    ProfileDO profileDO = userService.fetchProfileById(profileId);
                    if(null != profileDO){
                    	 session.setAttribute("isAgency", null == profileDO.getIsAgency() ? "0" : profileDO.getIsAgency());
                    }
                    model.addAttribute(BizConstants.SHOP_ID, session.getAttribute(BizConstants.SHOP_ID));
                }
            }
        }
        Profiler.enter(" fetch wechat user info.  ");
        try {
            if (StringUtils.isNotBlank(code)) {
                Profiler.enter(" fetch wechat user info  wechat oauth....  ");
                WxMpOAuth2AccessToken wxMpOAuth2AccessToken = wxMpService.oauth2getAccessToken(code);
                Profiler.release();
                if (null != wxMpOAuth2AccessToken) {
                    WxMpUser user = null;
                    openid = wxMpOAuth2AccessToken.getOpenId();
                    if (StringUtils.isNotEmpty(openid)) {
                        model.addAttribute(BizConstants.OPENID, openid);
                        String unionId = null;
                        user = wxMpService.userInfo(wxMpOAuth2AccessToken.getOpenId(), BizConstants.WX_LANG, state);
                        if (null != user) {
                            unionId = user.getUnionId();
                        }
                        //update微信存储信息
                        userService.updateProfileWechatInfo(user);
                        boolean hasFetchedWxInfo = userService.isOauthed(wxMpOAuth2AccessToken.getOpenId());
                        if (!hasFetchedWxInfo) {//未存储过微信用户信息profile wechat
                            if (null != user) {
                                if (!userService.addWechatInfoByToken(user, wxMpOAuth2AccessToken)) {
                                    log.error("insert wechat user info failed. openId:" + wxMpOAuth2AccessToken.getOpenId());
                                } else {
                                    session.setAttribute(BizConstants.OPENID, wxMpOAuth2AccessToken.getOpenId());
                                    Cookie cookie = new Cookie(BizConstants.JSESSION_ID, session.getId().toString());
                                    cookie.setMaxAge(TimeUtils.TIME_1_MONTH_SEC);
                                    cookie.setPath("/");
                                    response.addCookie(cookie);
                                }
                            } else {
                                log.error("fetch wechat user info failed. maybe user refresh home page..." + ",sessionId:" + session.getId());
                            }
                        } else {//存储过profile wechat 用户微信信息
                            if (profileId < 0) {
                                profileId = userService.fetchProfileIdByOpenid(openid);
                            }
                            if (profileId > 0) {
                                session.setAttribute(BizConstants.PROFILE_ID, profileId);
                                ProfileDO profileDO = userService.fetchProfileById(profileId);
                                if(null != profileDO){
                                	 session.setAttribute("isAgency", null == profileDO.getIsAgency() ? "0" : profileDO.getIsAgency());
                                }
                                model.addAttribute(BizConstants.SHOP_ID, session.getAttribute(BizConstants.SHOP_ID));
                                model.addAttribute(BizConstants.PROFILE_ID, profileId);
                                session.setAttribute(BizConstants.OPENID, wxMpOAuth2AccessToken.getOpenId());
                                Cookie cookie = new Cookie(BizConstants.JSESSION_ID, session.getId().toString());
                                cookie.setMaxAge(TimeUtils.TIME_1_MONTH_SEC);
                                cookie.setPath("/");
                                response.addCookie(cookie);
                            }
                        }
                        if (profileId < 0) {
                            profileId = userService.checkWxMpBinded(unionId);
                        }
                        if (profileId > 0) {
                            session.setAttribute(BizConstants.PROFILE_ID, profileId);
                            ProfileDO profileDO = userService.fetchProfileById(profileId);
                            if(null != profileDO){
                            	 session.setAttribute("isAgency", null == profileDO.getIsAgency() ? "0" : profileDO.getIsAgency());
                            }
                            model.addAttribute(BizConstants.SHOP_ID, session.getAttribute(BizConstants.SHOP_ID));
                            model.addAttribute(BizConstants.PROFILE_ID, profileId);
                            session.setAttribute(BizConstants.OPENID, wxMpOAuth2AccessToken.getOpenId());
                            Cookie cookie = new Cookie(BizConstants.JSESSION_ID, session.getId().toString());
                            cookie.setMaxAge(TimeUtils.TIME_1_MONTH_SEC);
                            cookie.setPath("/");
                            response.addCookie(cookie);
                        }
                    } else {
                        log.error("从微信获取wxMpOauth2AccessToken failed.  openId is null..." + "sessionid:" + session.getId().toString());
                    }
                } else {
                    if (null != session.getAttribute(BizConstants.PROFILE_ID)) {
                        model.addAttribute(BizConstants.PROFILE_ID, session.getAttribute(BizConstants.PROFILE_ID));
                    }
                }
            }
            //不在微信公众环境/获取不到微信用户信息
            else {
                if (null != session) {
                    model.addAttribute(BizConstants.OPENID, (String) session.getAttribute(BizConstants.OPENID));
                    model.addAttribute(BizConstants.PROFILE_ID, session.getAttribute(BizConstants.PROFILE_ID));
                    model.addAttribute(BizConstants.SHOP_ID, session.getAttribute(BizConstants.SHOP_ID));
                }
            }
        } catch (Exception e) {
            log.error("fetch wechat info failed. exp:" + e.getCause() + ",sessionId:" + session.getId());
        }
        if (null != session.getAttribute(BizConstants.PROFILE_ID)) {
            model.addAttribute(BizConstants.PROFILE_ID, session.getAttribute(BizConstants.PROFILE_ID));
            model.addAttribute(BizConstants.SHOP_ID, session.getAttribute(BizConstants.SHOP_ID));
        }
        if (StringUtils.equals("true", ParameterUtil.getParameter(request, "debug"))) {
            profileId = 275;
            openid = "oF6JLs3AfrY8Q4Ok7WbU15-9FuTk";
            session.setAttribute(BizConstants.PROFILE_ID, profileId);
            ProfileDO profileDO = userService.fetchProfileById(profileId);
            if(null != profileDO){
            	 session.setAttribute("isAgency", null == profileDO.getIsAgency() ? "0" : profileDO.getIsAgency());
            }
            session.setAttribute(BizConstants.OPENID, openid);
            model.addAttribute(BizConstants.SHOP_ID, session.getAttribute(BizConstants.SHOP_ID));
            Cookie cookie = new Cookie(BizConstants.JSESSION_ID, session.getId().toString());
            cookie.setMaxAge(TimeUtils.TIME_1_MONTH_SEC);
            cookie.setPath("/");
            response.addCookie(cookie);
        }
        //设置默认站点信息
        userService.setCommunity(model, session, response, profileId);
        Profiler.release();
        /*if (UserUtils.redirectLbsShop(session)) {
            response.sendRedirect("/api/h/1.0/indexPage.htm?session=expire");
            return null;
        }*/
        return "christ";
    }

    private void setHost(ModelMap model) {
        model.addAttribute(BizConstants.HOST, BizConstants.H5_WEILINJIA_HOST);
        model.addAttribute(BizConstants.APPID, ConstantUtil.GUOGEGE_H5_APP_ID);
    }
}
