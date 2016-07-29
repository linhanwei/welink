package com.welink.web.resource.hpage;

import com.daniel.weixin.mp.api.WxMpService;
import com.daniel.weixin.mp.bean.result.WxMpOAuth2AccessToken;
import com.google.common.base.Preconditions;
import com.welink.biz.util.TimeUtils;
import com.welink.biz.wx.tenpay.util.ConstantUtil;
import com.welink.commons.Env;
import com.welink.commons.commons.BizConstants;
import com.welink.web.common.constants.ResponseStatusEnum;
import com.welink.web.common.filter.Profiler;
import com.welink.web.common.model.ResponseResult;
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

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Date;

/**
 * Created by daniel on 15-3-18.
 */
@Controller
public class Oauth {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(Oauth.class);

    @Resource
    private WxMpService wxMpService;

    @Resource
    private Env env;

    @RequestMapping(value = {"/api/h/1.0/oauth.htm"}, produces = "text/html;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
        String redirectUrl = "";
        String toRedirect = ParameterUtil.getParameter(request, "redirect");
        log.info("oauth query string : " + request.getQueryString());
        String code = ParameterUtil.getParameter(request, BizConstants.WX_PARAM_CODE);
        String state = ParameterUtil.getParameter(request, BizConstants.WX_PARAM_STATE);
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        session.setTimeout(TimeUtils.TIME_1_MONTH_MILi);
        if (env.isProd()) {
            state = ConstantUtil.GUOGEGE_WX_CONF_KEY;
        } else {
            //state = ConstantUtil.XIAOWEILINJU_WX_CONF_KEY;
        	state = ConstantUtil.XIAOWEILINJU_WX_CONF_KEY;
        }
        if (StringUtils.isNotBlank(state)) {
            session.setAttribute(BizConstants.WEIXIN_MP_STATE, state);
        }
        if (null != session.getAttribute(BizConstants.PROFILE_ID)) {
            model.addAttribute(BizConstants.PROFILE_ID, session.getAttribute(BizConstants.PROFILE_ID));
        }
        ResponseResult result = new ResponseResult();
        model.addAttribute("ver", new Date().getTime() / (1000 * 60 * 60 * 24));
        //设置host
        setHost(model);
        //openId 检测并进行缓存处理
        String openId = null;
        Profiler.enter(" fetch wechat user info.  ");
        log.info("user enter fetch wechat page ... sesionid:" + session.getId());
        try {
            if (StringUtils.isNotBlank(code)) {
                log.info("user enter fetch wechat page has code ... sesionid:" + session.getId());
                Profiler.enter(" fetch wechat user info  wechat oauth....  ");
                WxMpOAuth2AccessToken wxMpOAuth2AccessToken = wxMpService.oauth2getAccessToken(code, state);
                Profiler.release();
                if (null != wxMpOAuth2AccessToken) {
                    openId = wxMpOAuth2AccessToken.getOpenId();
                    log.info("user enter Oauth page  wxMpOAuth  ...openid:" + openId + " sesionid:" + session.getId());
                    Preconditions.checkArgument(StringUtils.isNotBlank(openId), "只能在微信环境下使用，无法获得openId");
                    if (StringUtils.isNotEmpty(openId)) {
                        model.addAttribute("openid", openId);
                        session.setAttribute(BizConstants.OPENID, openId);
                        Cookie cookie = new Cookie(BizConstants.JSESSION_ID, session.getId().toString());
                        cookie.setMaxAge(TimeUtils.TIME_1_MONTH_SEC);
                        cookie.setPath("/");
                        response.addCookie(cookie);

                        String toRedirectUrl = toRedirect;
                        model.addAttribute("url", toRedirectUrl);
                        redirectUrl = toRedirectUrl;
                        if (true) {
                        	response.sendRedirect(URLDecoder.decode(toRedirect, "utf-8"));
                            return null;
                        }
                    }
                }
                result.setStatus(ResponseStatusEnum.SUCCESS.getCode());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            log.error("catch fetch wechat info failed exception. exp:" + e.getCause() + " ,Message: " + e.getMessage() + "sessionid:" + session.getId().toString());
            model.addAttribute("result", result);

        }
        /*if (env.isProd() && StringUtils.equals("true", ParameterUtil.getParameter(request, "debug"))) {
            long profileId = 277;
            String openid = BizConstants.TEST_OPEN_ID_FOR_LUOMO;
            model.addAttribute("openid", openid);
            session.setAttribute(BizConstants.PROFILE_ID, profileId);
            session.setAttribute(BizConstants.OPENID, openid);
            Cookie cookie = new Cookie(BizConstants.JSESSION_ID, session.getId().toString());
            cookie.setMaxAge(TimeUtils.TIME_1_MONTH_SEC);
            cookie.setPath("/");
            response.addCookie(cookie);
        }*/
        Profiler.release();
        response.sendRedirect(URLDecoder.decode(toRedirect, "utf-8"));
        return null;
    }

    private void setHost(ModelMap model) {
        if (env.isProd()) {
            model.addAttribute(BizConstants.HOST, BizConstants.H5_SEVEN_NB_HOST);
        } else {
            model.addAttribute(BizConstants.HOST, BizConstants.H5_WEILINJIA_HOST);
        }
    }
}
