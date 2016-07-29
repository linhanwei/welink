package com.welink.web.resource.hpage;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.daniel.weixin.mp.api.WxMpService;
import com.daniel.weixin.mp.bean.result.WxMpOAuth2AccessToken;
import com.daniel.weixin.mp.bean.result.WxMpUser;
import com.google.common.base.Preconditions;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.service.MikuUserAgencyService;
import com.welink.biz.service.UserService;
import com.welink.biz.util.TimeUtils;
import com.welink.biz.wx.tenpay.util.ConstantUtil;
import com.welink.buy.utils.PhenixUserHander;
import com.welink.commons.Env;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.MikuUserAgencyDOExample;
import com.welink.commons.domain.ProfileDO;
import com.welink.commons.domain.ProfileDOExample;
import com.welink.commons.domain.ProfileTempDO;
import com.welink.commons.domain.ProfileTempDOExample;
import com.welink.commons.persistence.MikuUserAgencyDOMapper;
import com.welink.commons.persistence.ProfileDOMapper;
import com.welink.commons.persistence.ProfileTempDOMapper;
import com.welink.commons.utils.MobileUtils;
import com.welink.web.common.constants.ResponseStatusEnum;
import com.welink.web.common.filter.Profiler;
import com.welink.web.common.util.ParameterUtil;

/**
 * Created by daniel on 15-1-13.
 */
@Controller
public class SpreadPage {
	private static org.slf4j.Logger log = LoggerFactory.getLogger(SpreadPage.class);

    @Resource
    private Env env;
    
    @Resource
    private ProfileTempDOMapper profileTempDOMapper;
    
	@Resource
    private WxMpService wxMpService;
	
	@Resource
    private ProfileDOMapper profileDOMapper;

    @Resource
    private UserService userService;
    
    @Resource
    private MikuUserAgencyDOMapper mikuUserAgencyDOMapper;
    
    @Resource
    private MikuUserAgencyService mikuUserAgencyService;

    /**
     * 
     * marketSpreadPage:(营销推广). <br/>
     * TODO(这里描述这个方法适用条件 – 可选).<br/>
     *
     * @author LuoGuangChun
     * @param request
     * @param response
     * @param model
     * @return
     */
    @RequestMapping(value = {"/api/h/1.0/marketSpreadPage.htm"}, produces = "text/html;charset=utf-8")
    public String marketing(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
        model.addAttribute("ver", new Date().getTime() / (1000 * 60 * 60 * 24));
        String state = "1";
        if (env.isProd()) {
        	state = ConstantUtil.GUOGEGE_WX_CONF_KEY;
            model.addAttribute(BizConstants.HOST, BizConstants.H5_SEVEN_NB_HOST);
        } else {
        	state = ConstantUtil.XIAOWEILINJU_WX_CONF_KEY;
            model.addAttribute(BizConstants.HOST, BizConstants.H5_WEILINJIA_HOST);
        }
        model.addAttribute("appid", ConstantUtil.mcMap.get(state).getAppId());
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        if (null != session) {
            model.addAttribute(BizConstants.OPENID, (String) session.getAttribute(BizConstants.OPENID));
            model.addAttribute(BizConstants.PROFILE_ID, session.getAttribute(BizConstants.PROFILE_ID));
            model.addAttribute(BizConstants.P_USER_ID, PhenixUserHander.encodeUserId((Long) session.getAttribute(BizConstants.PROFILE_ID)));
            //设置默认站点信息
            long profileId = -1l;
            userService.setCommunity(model, session, response, profileId);
        }
        return "marketSpreadPage";
    }
    
    /**
     * 
     * posterSpreadPage:(海报推广). <br/>
     * TODO(这里描述这个方法适用条件 – 可选).<br/>
     *
     * @author LuoGuangChun
     * @param request
     * @param response
     * @param model
     * @return
     */
    @RequestMapping(value = {"/api/h/1.0/posterSpreadPage.htm"}, produces = "text/html;charset=utf-8")
    public String posterSpreadPage(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
        model.addAttribute("ver", new Date().getTime() / (1000 * 60 * 60 * 24));
        if (env.isProd()) {
            model.addAttribute(BizConstants.HOST, BizConstants.H5_SEVEN_NB_HOST);
        } else {
            model.addAttribute(BizConstants.HOST, BizConstants.H5_WEILINJIA_HOST);
        }
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        if (null != session) {
            model.addAttribute(BizConstants.OPENID, (String) session.getAttribute(BizConstants.OPENID));
            model.addAttribute(BizConstants.PROFILE_ID, session.getAttribute(BizConstants.PROFILE_ID));
            model.addAttribute(BizConstants.P_USER_ID, PhenixUserHander.encodeUserId((Long) session.getAttribute(BizConstants.PROFILE_ID)));
            //设置默认站点信息
            long profileId = -1l;
            userService.setCommunity(model, session, response, profileId);
        }
        return "posterSpreadPage";
    }
    
    
    @RequestMapping(value = {"/api/h/1.0/inviteSpreadMangerPage2.htm"}, produces = "text/html;charset=utf-8")
    public String inviteSpreadMangerPage2(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        session.setTimeout(TimeUtils.TIME_1_MONTH_MILi);
        
        model.addAttribute("ver", new Date().getTime() / (1000 * 60 * 60 * 24));
        if (env.isProd()) {
            model.addAttribute(BizConstants.HOST, BizConstants.H5_SEVEN_NB_HOST);
        } else {
            model.addAttribute(BizConstants.HOST, BizConstants.H5_WEILINJIA_HOST);
        }
        if (null != session) {
            model.addAttribute(BizConstants.OPENID, (String) session.getAttribute(BizConstants.OPENID));
            model.addAttribute(BizConstants.PROFILE_ID, session.getAttribute(BizConstants.PROFILE_ID));
            model.addAttribute(BizConstants.P_USER_ID, PhenixUserHander.encodeUserId((Long) session.getAttribute(BizConstants.PROFILE_ID)));
            //设置默认站点信息
            long profileId = -1l;
            userService.setCommunity(model, session, response, profileId);
        }
        return "inviteSpreadMangerPage";
    }
    
    /**
     * 
     * inviteSpreadMangerPage:(邀请成为推广经理). <br/>
     * TODO(这里描述这个方法适用条件 – 可选).<br/>
     *
     * @author LuoGuangChun
     * @param request
     * @param response
     * @param model
     * @return
     */
    @RequestMapping(value = {"/api/h/1.0/inviteSpreadMangerPage.htm"}, produces = "text/html;charset=utf-8")
    public String inviteSpreadMangerPage(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
    	String code = ParameterUtil.getParameter(request, BizConstants.WX_PARAM_CODE);
        String shopRedirect = ParameterUtil.getParameter(request, "session");
        String state = ParameterUtil.getParameter(request, BizConstants.WX_PARAM_STATE);
        String ua = ((HttpServletRequest) request).getHeader("user-agent").toLowerCase();
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        session.setTimeout(TimeUtils.TIME_1_MONTH_MILi);
        if (StringUtils.isNotBlank(state)) {
            session.setAttribute(BizConstants.WEIXIN_MP_STATE, state);
        }
        
        model.addAttribute("ver", new Date().getTime() / (1000 * 60 * 60 * 24));
        if (env.isProd()) {
        	state = ConstantUtil.GUOGEGE_WX_CONF_KEY;
            model.addAttribute(BizConstants.HOST, BizConstants.H5_SEVEN_NB_HOST);
        } else {
        	state = ConstantUtil.XIAOWEILINJU_WX_CONF_KEY;
            model.addAttribute(BizConstants.HOST, BizConstants.H5_WEILINJIA_HOST);
        }
        model.addAttribute("appid", ConstantUtil.mcMap.get(state).getAppId());
        String pUserId = ParameterUtil.getParameter(request, "pUserId");	//上级用户id
        if(null == pUserId || !StringUtils.isNumeric(pUserId) || StringUtils.isBlank(pUserId)){
        	pUserId = "0";
        }
        model.addAttribute("pUserId", pUserId);	//上级userId
        Long pUserIdLg = Long.valueOf(pUserId);	//上级userId
        
        /*if ( pUserIdLg > 0 && null != session && null == session.getAttribute(BizConstants.PROFILE_ID)) {
        	//跳转到登陆页面
        	String currentUrl = "", queryString="";
			String redirectUrl = "";
			boolean wxEnv = true;
			String userAgent = request.getHeader("User-Agent");
			if (!StringUtils.containsIgnoreCase(userAgent, "micromessenger")) {
				wxEnv = false;
			}
			String wxUrl = "";
			String wxUrlTail = "";
            String rUrl = null;
			if (request.getQueryString() != null) {
	        	queryString += request.getQueryString();
	        }
	        if (env.isProd()) {
	        	currentUrl = "http://" + BizConstants.ONLINE_DOMAIN + "/api/h/1.0/inviteSpreadMangerPage.htm?"+queryString ;
	        } else {
	        	currentUrl = "http://" + BizConstants.ONLINE_DOMAIN_TEST + "/api/h/1.0/inviteSpreadMangerPage.htm?" + queryString;
	        }
	        if(wxEnv){
				wxUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="+ConstantUtil.mcMap.get(state).getAppId()+"&redirect_uri=";
    			wxUrlTail = "&response_type=code&scope=snsapi_base&state=" + state + "&connect_redirect=2#wechat_redirect";
    			rUrl = URLEncoder.encode(currentUrl);
    			redirectUrl = wxUrl + rUrl + wxUrlTail;
			}else{
				redirectUrl = rUrl = URLEncoder.encode(currentUrl);
			}
			
            if (env.isProd()) {
            	redirectUrl = "http://" + BizConstants.ONLINE_DOMAIN + "/api/h/1.0/vLoginPage.htm?" + "pUserId="+pUserId + " &refurl=" + URLEncoder.encode(redirectUrl, "utf-8");
            } else {
            	redirectUrl = "http://" + BizConstants.ONLINE_DOMAIN_TEST + "/api/h/1.0/vLoginPage.htm?" + "pUserId="+pUserId + " &refurl=" + URLEncoder.encode(redirectUrl, "utf-8");
            }
            response.sendRedirect(redirectUrl);
            return null;
        }*/
        
        boolean wxEnv = true;	//true=微信浏览器；false=不是微信浏览器
        boolean isLogin = true;	 //true=已登录；false=未登录
        if(null != session && null == session.getAttribute(BizConstants.PROFILE_ID)){
        	isLogin = false;
        }else if(null == session){
        	isLogin = false;
        }
		String userAgent = request.getHeader("User-Agent").toLowerCase();
		if (!StringUtils.containsIgnoreCase(userAgent, "micromessenger")) {
			wxEnv = false;
		}
        if (!wxEnv && pUserIdLg > 0 && !isLogin) {
        	///若不是微信浏览器和未登录有上级,跳转到登陆页面
			response.sendRedirect("/api/h/1.0/vLoginPage.htm?refurl=" + 
    				URLEncoder.encode("inviteSpreadMangerPage.htm"+(null==request.getQueryString() ? "" : "?"+request.getQueryString()), "utf-8"));
			return null;
        }
        
        /*model.addAttribute("agencyRelation", 0);	//代理关系(0=正常；1=已有代理关系；2=成功建立代理关系；3建立代理关系失败)
        int detailPageStatus = -1;	//判断用户是否已使用微信访问商品详情页
        if (null != session.getAttribute(BizConstants.PROFILE_ID) && pUserIdLg > 0) {
            Long profileId = (long) session.getAttribute(BizConstants.PROFILE_ID);
            //是否有代理关系session(0=无代理关系;1=有代理关系)
            Integer hasAgencyRelationValue = (Integer) session.getAttribute(BizConstants.HAS_AGENCY_RELATION);
            if(null == hasAgencyRelationValue || hasAgencyRelationValue != 1){
            	hasAgencyRelationValue = 0;
            }
            //String hasAgencyRelation = (String) session.getAttribute(BizConstants.HAS_AGENCY_RELATION);	
            if(profileId > 0 && hasAgencyRelationValue.equals(0)
            		&& pUserIdLg > 0){
            	MikuUserAgencyDOExample mikuUserAgencyDOExample = new MikuUserAgencyDOExample();
        		mikuUserAgencyDOExample.createCriteria().andUserIdEqualTo(profileId);
        		int userAgencyCount = mikuUserAgencyDOMapper.countByExample(mikuUserAgencyDOExample);
        		if(userAgencyCount < 1){	//如果未建立代理关系
        			int updateMikuUserAgency = mikuUserAgencyService.updateMikuUserAgency(pUserIdLg, profileId, null);
        			if(updateMikuUserAgency > 0){
        				model.addAttribute("agencyRelation", 2);	//代理关系(0=正常；1=已有代理关系；2=成功建立代理关系；3建立代理关系失败)
        			}else{
        				model.addAttribute("agencyRelation", 3);	//代理关系(0=正常；1=已有代理关系；2=成功建立代理关系；3建立代理关系失败)
        			}
        		}else{
        			model.addAttribute("agencyRelation", 1);	//代理关系(0=正常；1=已有代理关系；2=成功建立代理关系；3建立代理关系失败)
        			session.setAttribute(BizConstants.HAS_AGENCY_RELATION, 1);
        		}
            }
        }*/
        
        if (ua.indexOf("micromessenger") > 0 
        		&& StringUtils.isBlank(state) && StringUtils.isBlank(code)) {// 是微信浏览器
        	String toRedirectUrl = "", queryString="";
            if (request.getQueryString() != null) {
            	queryString += "?" + request.getQueryString() + "&oauth=true";
            }else{
            	queryString += "?pUserId=" + pUserId;
            }
        	String wxUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="+ConstantUtil.mcMap.get(state).getAppId()+"&redirect_uri=";
            String wxUrlTail = "&response_type=code&scope=snsapi_base&state=" + state + "&connect_redirect=2#wechat_redirect";
            String rUrl = null;
            if (env.isProd()) {
            	rUrl = "http://" + BizConstants.ONLINE_DOMAIN + "/api/h/1.0/inviteSpreadMangerPage.htm" + queryString;
            } else {
            	rUrl = "http://" + BizConstants.ONLINE_DOMAIN_TEST + "/api/h/1.0/inviteSpreadMangerPage.htm" + queryString;
            }
            rUrl = URLEncoder.encode(rUrl);
            toRedirectUrl = wxUrl + rUrl + wxUrlTail;
            response.sendRedirect(toRedirectUrl);
            return null;
        }
        
        if (null != session) {
            model.addAttribute(BizConstants.OPENID, (String) session.getAttribute(BizConstants.OPENID));
            model.addAttribute(BizConstants.PROFILE_ID, session.getAttribute(BizConstants.PROFILE_ID));
            model.addAttribute(BizConstants.P_USER_ID, PhenixUserHander.encodeUserId((Long) session.getAttribute(BizConstants.PROFILE_ID)));
            //设置默认站点信息
            long profileId = -1l;
            userService.setCommunity(model, session, response, profileId);
        }
        //String userAgent = ((HttpServletRequest) request).getHeader("user-agent").toLowerCase();
        //openId 检测并进行缓存处理
        String openId = null;
        Profiler.enter(" fetch wechat user info.  ");
        log.info("user enter fetch wechat page ... sesionid:" + session.getId());
        try {
            //if (userAgent.indexOf("micromessenger") > 0 && StringUtils.isNotBlank(code)) {// 是微信浏览器
        	if (StringUtils.isNotBlank(code)) {// 是微信浏览器
                log.info("user enter fetch wechat page has code ... sesionid:" + session.getId());
                Profiler.enter(" fetch wechat user info  wechat oauth....  ");
                WxMpOAuth2AccessToken wxMpOAuth2AccessToken = wxMpService.oauth2getAccessToken(code, state);
                Profiler.release();
                if (null != wxMpOAuth2AccessToken) {
                    log.info("user enter fetch wechat page  wxMpOAuth access token not null ... sesionid:" + session.getId());
                    WxMpUser user = null;
                    openId = wxMpOAuth2AccessToken.getOpenId();
                    Preconditions.checkArgument(StringUtils.isNotBlank(openId), "只能在微信环境下使用，无法获得openId");
                    if (StringUtils.isNotEmpty(openId)) {
                        model.addAttribute("openid", openId);
                        log.info("user enter fetch wechat page open id is not null  ... sesionid:" + session.getId() + ",openid:" + openId);
                        user = wxMpService.userInfo(openId, BizConstants.WX_LANG, state);
                        String unionId = null;
                        if (null != user) {
                            unionId = user.getUnionId();
                            if(user.getSubscribe()){
                         		model.addAttribute("subscribeWx", 1);	//是否订阅微信（0=未订阅；1=已订阅；2=已微信注册）
                         	}else{
                         		model.addAttribute("subscribeWx", 0);	//是否订阅微信（0=未订阅；1=已订阅；2=已微信注册）
                         	}
                        }
                        //update微信存储信息
                        userService.updateProfileWechatInfo(user);
                        //有存储过微信信息且已经获取过union_id
                        boolean hasFetchedWxInfo = userService.isOauthed(openId);
                        if (!hasFetchedWxInfo) {//未存储过微信用户信息profile wechat
                            //获取微信用户信息并存储
                            log.info("user enter fetch wechat page not store wecaht info profile  ... sesionid:" + session.getId() + ",openid" + openId);
                            Profiler.enter(" fetch wechat user info  wechat user....  ");
                            Profiler.release();
                            if (null != user) {
                                if (!userService.addWechatInfoByToken(user, wxMpOAuth2AccessToken)) {
                                	session.setAttribute(BizConstants.OPENID, openId);
                                    Cookie cookie = new Cookie(BizConstants.JSESSION_ID, session.getId().toString());
                                    cookie.setMaxAge(TimeUtils.TIME_1_MONTH_SEC);
                                    cookie.setPath("/");
                                    response.addCookie(cookie);
                                    log.error("insert wechat user info failed. openId:" + wxMpOAuth2AccessToken.getOpenId() + "sessionid:" + session.getId() + ",openid:" + openId);
                                } else {
                                    session.setAttribute(BizConstants.OPENID, openId);
                                    Cookie cookie = new Cookie(BizConstants.JSESSION_ID, session.getId().toString());
                                    cookie.setMaxAge(TimeUtils.TIME_1_MONTH_SEC);
                                    cookie.setPath("/");
                                    response.addCookie(cookie);
                                    log.info("user enter fetch wechat page ... sesionid:" + session.getId() + ",openid:" + openId);
                                    if (pUserIdLg > 0 && !isLogin) {
                                    	//若不是微信浏览器和未登录有上级,跳转到登陆页面
                                    	response.sendRedirect("/api/h/1.0/vLoginPage.htm?refurl=" + 
                                    			URLEncoder.encode("inviteSpreadMangerPage.htm"+(null==request.getQueryString() ? "" : "?"+request.getQueryString()), "utf-8"));
                                    	return null;
                                    }
                                }
                            } else {
                                log.error("fetch wechat user info failed. maybe user refresh home page..." + "sessionid:" + session.getId().toString() + ",openid:" + openId);
                            }
                        }
                    } else {
                        log.error("从微信获取wxMpOauth2AccessToken failed.  openId is null..." + "sessionid:" + session.getId().toString());
                    }

                } else {
                    if (null != session.getAttribute(BizConstants.PROFILE_ID)) {
                        model.addAttribute(BizConstants.PROFILE_ID, session.getAttribute(BizConstants.PROFILE_ID));
                        model.addAttribute(BizConstants.P_USER_ID, PhenixUserHander.encodeUserId((Long) session.getAttribute(BizConstants.PROFILE_ID)));
                        model.addAttribute(BizConstants.SHOP_ID, session.getAttribute(BizConstants.SHOP_ID));
                    }
                    Cookie cookie = new Cookie(BizConstants.JSESSION_ID, session.getId().toString());
                    cookie.setMaxAge(TimeUtils.TIME_1_MONTH_SEC);
                    cookie.setPath("/");
                    response.addCookie(cookie);
                    /*if (!StringUtils.equals("expire", shopRedirect) && UserUtils.redirectLbsShop(session)) {
                        response.sendRedirect("/api/h/1.0/indexPage.htm?session=expire");
                        return null;
                    }*/
                    if (pUserIdLg > 0 && !isLogin) {
                    	//若不是微信浏览器和未登录有上级,跳转到登陆页面
                    	response.sendRedirect("/api/h/1.0/vLoginPage.htm?refurl=" + 
                    			URLEncoder.encode("inviteSpreadMangerPage.htm"+(null==request.getQueryString() ? "" : "?"+request.getQueryString()), "utf-8"));
                    	return null;
                    }
                    setUserAgency(model, session, request);	//设置代理关系
                    return "inviteSpreadMangerPage";
                }
            }
            //不在微信公众号环境内/获取不到微信信息
            else {
                if (null != session) {
                    model.addAttribute(BizConstants.OPENID, (String) session.getAttribute(BizConstants.OPENID));
                    model.addAttribute(BizConstants.PROFILE_ID, session.getAttribute(BizConstants.PROFILE_ID));
                    model.addAttribute(BizConstants.SHOP_ID, session.getAttribute(BizConstants.SHOP_ID));
                    model.addAttribute(BizConstants.P_USER_ID, PhenixUserHander.encodeUserId((Long) session.getAttribute(BizConstants.PROFILE_ID)));
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            log.error("catch fetch wechat info failed exception. exp:" + e.getCause() + " ,Message: " + e.getMessage() + "sessionid:" + session.getId().toString());
            if (pUserIdLg > 0 && !isLogin) {
            	//若不是微信浏览器和未登录有上级,跳转到登陆页面
            	response.sendRedirect("/api/h/1.0/vLoginPage.htm?refurl=" + 
            			URLEncoder.encode("inviteSpreadMangerPage.htm"+(null==request.getQueryString() ? "" : "?"+request.getQueryString()), "utf-8"));
            	return null;
            }
            setUserAgency(model, session, request);	//设置代理关系
            return "inviteSpreadMangerPage";
        }
        Cookie cookie = new Cookie(BizConstants.JSESSION_ID, session.getId().toString());
        cookie.setMaxAge(TimeUtils.TIME_1_MONTH_SEC);
        cookie.setPath("/");
        response.addCookie(cookie);
        if (pUserIdLg > 0 && !isLogin) {
        	//若不是微信浏览器和未登录有上级,跳转到登陆页面
        	response.sendRedirect("/api/h/1.0/vLoginPage.htm?refurl=" + 
        			URLEncoder.encode("inviteSpreadMangerPage.htm"+(null==request.getQueryString() ? "" : "?"+request.getQueryString()), "utf-8"));
        	return null;
        }
        setUserAgency(model, session, request);	//设置代理关系
        return "inviteSpreadMangerPage";
    }
    
    /**
     * 
     * receiveInviteSpreadMangerPage:(接收邀请推广管理). <br/>
     * TODO(这里描述这个方法适用条件 – 可选).<br/>
     *
     * @author LuoGuangChun
     * @param request
     * @param response
     * @param model
     * @param mobile
     * @param parentId
     * @return
     */
    @RequestMapping(value = {"/api/h/1.0/receiveInviteSpreadMangerPage.htm"}, produces = "text/html;charset=utf-8")
    public String receiveInviteSpreadMangerPage(HttpServletRequest request, HttpServletResponse response, ModelMap model,
    		@RequestParam String mobile, @RequestParam(value="parentId", required = false, defaultValue="0") Long parentId) throws Exception {
    	response.setCharacterEncoding("UTF-8");
    	Long invitId = parentId;
    	
    	if(!MobileUtils.isMobile(mobile)){
    		model.addAttribute("status", 0);	//异常
        	model.addAttribute("msg", BizErrorEnum.IS_MOBILE.getMsg());
        	return "receiveInviteSpreadMangerPage";
    	}
    	
        model.addAttribute("ver", new Date().getTime() / (1000 * 60 * 60 * 24));
        if (env.isProd()) {
            model.addAttribute(BizConstants.HOST, BizConstants.H5_SEVEN_NB_HOST);
        } else {
            model.addAttribute(BizConstants.HOST, BizConstants.H5_WEILINJIA_HOST);
        }
        
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        if (null != session) {
            model.addAttribute(BizConstants.OPENID, (String) session.getAttribute(BizConstants.OPENID));
            model.addAttribute(BizConstants.PROFILE_ID, session.getAttribute(BizConstants.PROFILE_ID));
            model.addAttribute(BizConstants.P_USER_ID, PhenixUserHander.encodeUserId((Long) session.getAttribute(BizConstants.PROFILE_ID)));
            //设置默认站点信息
            long profileId = -1l;
            userService.setCommunity(model, session, response, profileId);
        }
        
        model.addAttribute("mobile", mobile);
        
        ProfileDO parentProfileDO = profileDOMapper.selectByPrimaryKey(parentId);
        if(null == parentProfileDO){
        	parentId = 0L;
        	model.addAttribute("parentIsAgency", "0");
        }else if(null != parentProfileDO && !parentProfileDO.getIsAgency().equals((byte)1)){
        	parentId = 0L;
        }
        /*if(null != parentProfileDO && !parentProfileDO.getIsAgency().equals(1)){
        	model.put("status", 5);		//邀请人不是代理
        }*/
        
        //根据电话查出profile信息
        ProfileDOExample profileDOExample = new ProfileDOExample();
        profileDOExample.createCriteria().andMobileEqualTo(mobile).andStatusEqualTo((byte) 1);
        List<ProfileDO> thisNOprofiles = profileDOMapper.selectByExample(profileDOExample);
        if (null != thisNOprofiles && thisNOprofiles.size() > 0) {
        	model.addAttribute("status", 1);		//已注册
        }else{	//未注册
        	ProfileTempDOExample profileTempDOExample = new ProfileTempDOExample();
        	profileTempDOExample.createCriteria().andMobileEqualTo(mobile);
        	List<ProfileTempDO> profileTempDOList = profileTempDOMapper.selectByExample(profileTempDOExample);
        	if(!profileTempDOList.isEmpty()){
        		model.put("status", 1);		//已注册
        		return "receiveInviteSpreadMangerPage";
        	}
        	
        	try {
        		ProfileTempDO profileTempDO = new ProfileTempDO();
        		profileTempDO.setMobile(mobile);
        		profileTempDO.setPid(parentId);
        		profileTempDO.setInviteId(invitId);;
        		profileTempDO.setStatus((byte)1);
        		profileTempDO.setDateCreated(new Date());
        		
        		if(profileTempDOMapper.insertSelective(profileTempDO) != 1){
        			model.addAttribute("status", 0);	//异常
                	model.addAttribute("msg", BizErrorEnum.SYSTEM_BUSY.getMsg());
                	return "receiveInviteSpreadMangerPage";
        		}
                
                String ua = ((HttpServletRequest) request).getHeader("user-agent").toLowerCase();
    	        if (ua.indexOf("micromessenger") > 0) {// 是微信浏览器
    	        	
    	        	 String code = ParameterUtil.getParameter(request, BizConstants.WX_PARAM_CODE);
    	             String state = ParameterUtil.getParameter(request, BizConstants.WX_PARAM_STATE);
    	             String openId = null;
    	             if (StringUtils.isNotBlank(code)) {
    	             	WxMpOAuth2AccessToken wxMpOAuth2AccessToken = wxMpService.oauth2getAccessToken(code, state);
    	             	if (null != wxMpOAuth2AccessToken) {
    	             		WxMpUser user = null;
    	             		openId = wxMpOAuth2AccessToken.getOpenId();
    	             		Preconditions.checkArgument(StringUtils.isNotBlank(openId), "只能在微信环境下使用，无法获得openId");
    	             		if (StringUtils.isNotEmpty(openId)) {
    	             			//model.addAttribute("openid2", openId);
    	                         log.info("user enter fetch wechat page open id is not null  :"+ "openid:" + openId);
    	                         user = wxMpService.userInfo(openId, BizConstants.WX_LANG, state);
    	                         if (null != user) {
    	                        	 model.addAttribute("nickName", user.getNickname());
    	                        	 //model.addAttribute("status", 1);
    	                        	 if(user.getSubscribe()){
    	                         		model.addAttribute("status", 2);	//已订阅
    	                         	}else{
    	                         		model.addAttribute("status", 3);	//未订阅
    	                         	}
    	                         }else{
    	                        	 model.addAttribute("status", 0);
    	                         	model.addAttribute("msg", "阿哦，没有此微信用户");
    	                         }
    	             		}else{
    	            			model.addAttribute("status", 0);
    	            			model.addAttribute("msg", "阿哦，只能在微信环境下使用");
    	            		}
    	             	}
    	             }
    	        }else{
    	        	model.addAttribute("status", 4);		//不是微信领取
    	        }
                
            } catch (Exception e) {
            	model.addAttribute("status", 0);	//异常
            	model.addAttribute("msg", BizErrorEnum.SYSTEM_BUSY.getMsg());
            }
        	
        }
        
        return "receiveInviteSpreadMangerPage";
    }
    
    /**
     * 设置代理关系
     * @param model
     * @param session
     * @param request
     */
    public void setUserAgency(ModelMap model, Session session, HttpServletRequest request){
    	Long profileId = -1L;
    	String pUserId = null;
		try {
			pUserId = ParameterUtil.getParameter(request, "pUserId");
		} catch (UnsupportedEncodingException e) {
		}	//上级用户id
        if(null == pUserId || !StringUtils.isNumeric(pUserId) || StringUtils.isBlank(pUserId)){
        	pUserId = "0";
        }
        model.addAttribute("pUserId", pUserId);	//上级userId
        Long pUserIdLg = Long.valueOf(pUserId);	//上级userId
    	model.addAttribute("pUserId", pUserId);	//上级userId
        model.addAttribute("agencyRelation", 0);	//代理关系(0=正常；1=已有代理关系；2=成功建立代理关系；3建立代理关系失败)
        if (null != session && null != session.getAttribute(BizConstants.PROFILE_ID) && pUserIdLg > 0) {
            profileId = (long) session.getAttribute(BizConstants.PROFILE_ID);
            //是否有代理关系session(0=无代理关系;1=有代理关系)
            Integer hasAgencyRelationValue = (Integer) session.getAttribute(BizConstants.HAS_AGENCY_RELATION);
            if(null == hasAgencyRelationValue || hasAgencyRelationValue != 1){
            	hasAgencyRelationValue = 0;
            }
            //String hasAgencyRelation = (String) session.getAttribute(BizConstants.HAS_AGENCY_RELATION);	
            if(profileId > 0 && hasAgencyRelationValue.equals(0)
            		&& pUserIdLg > 0){
            	MikuUserAgencyDOExample mikuUserAgencyDOExample = new MikuUserAgencyDOExample();
        		mikuUserAgencyDOExample.createCriteria().andUserIdEqualTo(profileId);
        		int userAgencyCount = mikuUserAgencyDOMapper.countByExample(mikuUserAgencyDOExample);
        		if(userAgencyCount < 1){	//如果未建立代理关系
        			int updateMikuUserAgency = mikuUserAgencyService.updateMikuUserAgency(pUserIdLg, profileId, null);
        			if(updateMikuUserAgency > 0){
        				model.addAttribute("agencyRelation", 2);	//代理关系(0=正常；1=已有代理关系；2=成功建立代理关系；3建立代理关系失败)
        			}else{
        				model.addAttribute("agencyRelation", 3);	//代理关系(0=正常；1=已有代理关系；2=成功建立代理关系；3建立代理关系失败)
        			}
        		}else{
        			model.addAttribute("agencyRelation", 1);	//代理关系(0=正常；1=已有代理关系；2=成功建立代理关系；3建立代理关系失败)
        			session.setAttribute(BizConstants.HAS_AGENCY_RELATION, 1);
        		}
            }
        }
    }
    
}
