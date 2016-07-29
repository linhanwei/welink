package com.welink.web.resource.hpage;

import com.welink.biz.service.UserService;
import com.welink.biz.util.UserUtils;
import com.welink.biz.wx.tenpay.util.ConstantUtil;
import com.welink.buy.utils.PhenixUserHander;
import com.welink.commons.Env;
import com.welink.commons.commons.BizConstants;
import com.welink.web.common.util.ParameterUtil;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Date;

/**
 * 下单确认页面
 * Created by daniel on 15-1-4.
 */
@Controller
public class PlaceOrder {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(PlaceOrder.class);

    @Resource
    private Env env;

    @Resource
    private UserService userService;

    @RequestMapping(value = {"/api/h/1.0/placeOrder.htm"}, produces = "text/html;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
        String redirectUrl = "";
        String oauth = ParameterUtil.getParameter(request, "oauth");
        model.addAttribute("ver", new Date().getTime() / (1000 * 60 * 60 * 24));
        //model.addAttribute(BizConstants.HOST, BizConstants.H5_WEILINJIA_HOST);
        String state = "1";
        /*if (StringUtils.isNotBlank((String) session.getAttribute(BizConstants.WEIXIN_MP_STATE))) {
            state = (String) session.getAttribute(BizConstants.WEIXIN_MP_STATE);
        }*/
        if (env.isProd()) {
        	state = ConstantUtil.GUOGEGE_WX_CONF_KEY;
            model.addAttribute(BizConstants.HOST, BizConstants.H5_SEVEN_NB_HOST);
        } else {
        	state = ConstantUtil.XIAOWEILINJU_WX_CONF_KEY;
            model.addAttribute(BizConstants.HOST, BizConstants.H5_WEILINJIA_HOST);
        }
        model.addAttribute(BizConstants.APPID, ConstantUtil.mcMap.get(state).getAppId());
        
        String pUserId = ParameterUtil.getParameter(request, "pUserId");	//上级用户id
        if(null == pUserId || !StringUtils.isNumeric(pUserId) || StringUtils.isBlank(pUserId)){
        	pUserId = "0";
        }
        model.addAttribute("pUserId", pUserId);	//上级userId

        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        if (null != session) {
            model.addAttribute(BizConstants.SHOP_ID, session.getAttribute(BizConstants.SHOP_ID));
            /*if (UserUtils.redirectLbsShop(session)) {
                response.sendRedirect("/api/h/1.0/indexPage.htm?session=expire?pUserId="+pUserId);
                return null;
            }*/
            model.addAttribute(BizConstants.OPENID, (String) session.getAttribute(BizConstants.OPENID));
            model.addAttribute(BizConstants.PROFILE_ID, session.getAttribute(BizConstants.PROFILE_ID));
            //设置默认站点信息
            long profileId = -1l;
            userService.setCommunity(model, session, response, profileId);
            model.addAttribute(BizConstants.P_USER_ID, PhenixUserHander.encodeUserId((Long) session.getAttribute(BizConstants.PROFILE_ID)));
        }
        
        //获取当前url
        /*String currentUrl = request.getScheme() + "://";
        currentUrl += request.getServerName();//获取到域名
        currentUrl += request.getRequestURI();
        if (request.getQueryString() != null) {
            currentUrl += "?" + request.getQueryString() + "&oauth=true";
        }*/
        String currentUrl = "", queryString="";
        if (request.getQueryString() != null) {
        	queryString += request.getQueryString() + "&oauth=true";
        }
        if (env.isProd()) {
            //redirectUrl = "http://m." + BizConstants.ONLINE_DOMAIN + "/api/h/1.0/vLoginPage.htm?refurl=" + URLEncoder.encode(currentUrl, "utf-8");
        	//currentUrl = "http://" + BizConstants.ONLINE_DOMAIN + "/api/h/1.0/placeOrder.htm?" + "pUserId="+pUserId + "&"+queryString ;
        	currentUrl = "http://" + BizConstants.ONLINE_DOMAIN + "/api/h/1.0/placeOrder.htm?"+queryString ;
        } else {
            //redirectUrl = "http://m.7nb.com.cn/api/h/1.0/vLoginPage.htm?refurl=" + URLEncoder.encode(currentUrl, "utf-8");
        	currentUrl = "http://" + BizConstants.ONLINE_DOMAIN_TEST + "/api/h/1.0/placeOrder.htm?" + queryString;
        }
        if (UserUtils.redirect(session)) {
            model.addAttribute("url", currentUrl);
            if (env.isProd()) {
                //redirectUrl = "http://m." + BizConstants.ONLINE_DOMAIN + "/api/h/1.0/vLoginPage.htm?refurl=" + URLEncoder.encode(currentUrl, "utf-8");
            	redirectUrl = "http://" + BizConstants.ONLINE_DOMAIN + "/api/h/1.0/vLoginPage.htm?refurl=" + URLEncoder.encode(currentUrl, "utf-8") + "&pUserId="+pUserId;
            } else {
                //redirectUrl = "http://m.7nb.com.cn/api/h/1.0/vLoginPage.htm?refurl=" + URLEncoder.encode(currentUrl, "utf-8");
                redirectUrl = "http://" + BizConstants.ONLINE_DOMAIN_TEST + "/api/h/1.0/vLoginPage.htm?refurl=" + URLEncoder.encode(currentUrl, "utf-8") + "&pUserId="+pUserId;
            }
            response.sendRedirect(redirectUrl);
            return null;
        }
        boolean wxEnv = true;
        String userAgent = request.getHeader("User-Agent");
        if (!StringUtils.containsIgnoreCase(userAgent, "micromessenger")) {
            wxEnv = false;
        }
        //是否需要走微信oauth
        if (wxEnv && UserUtils.oauthRedirect(session) && !StringUtils.equals("true", oauth)) {
            String toRedirectUrl = null;
            if (request.getQueryString() != null) {
            	System.out.println("--------------------placeOrder-----------------------------------------------------");
            	System.out.println("---------------getAppId:"+ConstantUtil.mcMap.get(state).getAppId()+"---state:"+state);
                currentUrl = URLEncoder.encode(currentUrl);
                //String wxUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=wxac1318c07e16ee69&redirect_uri=";
                String wxUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="+ConstantUtil.mcMap.get(state).getAppId()+"&redirect_uri=";
                String wxUrlTail = "&response_type=code&scope=snsapi_base&state=" + state + "&connect_redirect=2#wechat_redirect";
                String rUrl = null;
                if (env.isProd()) {
                    //rUrl = "http://m." + BizConstants.ONLINE_DOMAIN + "/api/h/1.0/oauth.htm?redirect=" + URLEncoder.encode(currentUrl, "utf-8");
                	rUrl = "http://" + BizConstants.ONLINE_DOMAIN + "/api/h/1.0/oauth.htm?redirect=" + URLEncoder.encode(currentUrl, "utf-8") + "&pUserId="+pUserId;
                } else {
                    //rUrl = "http://m.7nb.com.cn/api/h/1.0/oauth.htm?redirect=" + URLEncoder.encode(currentUrl, "utf-8");
                	rUrl = "http://" + BizConstants.ONLINE_DOMAIN_TEST + "/api/h/1.0/oauth.htm?redirect=" + URLEncoder.encode(currentUrl, "utf-8") + "&pUserId="+pUserId;
                }
                log.info("========rUrl:" + rUrl);
                rUrl = URLEncoder.encode(rUrl);
                toRedirectUrl = wxUrl + rUrl + wxUrlTail;
                model.addAttribute("url", toRedirectUrl);
                log.info("=====toRedirectUrl:" + toRedirectUrl);
                redirectUrl = toRedirectUrl;
                response.sendRedirect(redirectUrl);
                return null;
            }

        }
        return "placeOrder";
    }
}
