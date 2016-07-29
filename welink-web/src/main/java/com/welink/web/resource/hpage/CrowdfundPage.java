package com.welink.web.resource.hpage;

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

import com.daniel.weixin.mp.api.WxMpService;
import com.daniel.weixin.mp.bean.result.WxMpOAuth2AccessToken;
import com.daniel.weixin.mp.bean.result.WxMpUser;
import com.google.common.base.Preconditions;
import com.welink.biz.service.ProfileExtService;
import com.welink.biz.service.UserService;
import com.welink.biz.util.StringUtil;
import com.welink.biz.util.TimeUtils;
import com.welink.biz.util.UserUtils;
import com.welink.biz.wx.tenpay.util.ConstantUtil;
import com.welink.buy.utils.PhenixUserHander;
import com.welink.commons.Env;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.CommunityDO;
import com.welink.commons.domain.CommunityDOExample;
import com.welink.commons.domain.ProfileDO;
import com.welink.commons.persistence.CommunityDOMapper;
import com.welink.web.common.constants.ResponseStatusEnum;
import com.welink.web.common.filter.Profiler;
import com.welink.web.common.model.ResponseResult;
import com.welink.web.common.util.ParameterUtil;

/**
 * Created by daniel on 15-1-13.
 */
@Controller
public class CrowdfundPage {

	private static org.slf4j.Logger log = LoggerFactory.getLogger(CrowdfundPage.class);
	
    @Resource
    private Env env;

    @Resource
    private UserService userService;
    
    @Resource
    private WxMpService wxMpService;
    
    @Resource
    private CommunityDOMapper communityDOMapper;

    @Resource
    private ProfileExtService profileExtService;

    @RequestMapping(value = {"/api/h/1.0/crowdfundPage.htm"}, produces = "text/html;charset=utf-8")
    public String crowdfundPage(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
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
        return "crowdfundPage";
    }
    
    /**
     * 
     * crowdfundHomePage:(众筹首页). <br/>
     * TODO(这里描述这个方法适用条件 – 可选).<br/>
     *
     * @author LuoGuangChun
     * @param request
     * @param response
     * @param model
     * @return
     */
    @RequestMapping(value = {"/api/h/1.0/crowdfundHomePage.htm"}, produces = "text/html;charset=utf-8")
    public String crowdfundHomePage(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
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
        return "crowdfundHomePage";
    }
    
    /**
     * 
     * crowdfundListPage:(众筹排行榜和新品上市). <br/>
     *
     * @author LuoGuangChun
     * @param request
     * @param response
     * @param model
     * @return
     */
    @RequestMapping(value = {"/api/h/1.0/crowdfundListPage.htm"}, produces = "text/html;charset=utf-8")
    public String crowdfundListPage(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
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
        return "crowdfundListPage";
    }
    
    /**
     * 
     * mineCrowdfundPage:(我的众筹项目). <br/>
     * TODO(这里描述这个方法适用条件 – 可选).<br/>
     *
     * @author LuoGuangChun
     * @param request
     * @param response
     * @param model
     * @return
     */
    @RequestMapping(value = {"/api/h/1.0/mineCrowdfundPage.htm"}, produces = "text/html;charset=utf-8")
    public String mineCrowdfundPage(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
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
        return "mineCrowdfundPage";
    }
    
    /**
     * 
     * crowdfundInfoPage:(众筹详情页). <br/>
     * TODO(这里描述这个方法适用条件 – 可选).<br/>
     *
     * @author LuoGuangChun
     * @param request
     * @param response
     * @param model
     * @return
     */
    @RequestMapping(value = {"/api/h/1.0/crowdfundInfoPage.htm"}, produces = "text/html;charset=utf-8")
    public String crowdfundInfoPage(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
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
        return "crowdfundInfoPage";
    }
    
    /**
     * 
     * crowdfundItemListPage:(众筹商品详情 我要支持页面). <br/>
     * TODO(这里描述这个方法适用条件 – 可选).<br/>
     *
     * @author LuoGuangChun
     * @param request
     * @param response
     * @param model
     * @return
     * @throws Exception
     * @since JDK 1.6
     */
    @RequestMapping(value = {"/api/h/1.0/crowdfundItemListPage2.htm"}, produces = "text/html;charset=utf-8")
    public String crowdfundItemListPage2(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
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
        return "crowdfundItemListPage";
    }
    
    /**
     * 
     * crowdfundItemListPage:(众筹商品详情 我要支持页面). <br/>
     * TODO(这里描述这个方法适用条件 – 可选).<br/>
     *
     * @author LuoGuangChun
     * @param request
     * @param response
     * @param model
     * @return
     * @throws Exception
     * @since JDK 1.6
     */
    @RequestMapping(value = {"/api/h/1.0/crowdfundItemListPage.htm"}, produces = "text/html;charset=utf-8")
    public String crowdfundItemListPage(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
        model.addAttribute("ver", new Date().getTime() / (1000 * 60 * 60 * 24));
        model.addAttribute("ver", new Date().getTime() / (1000 * 60 * 60 * 24));
        if (env.isProd()) {
            model.addAttribute(BizConstants.HOST, BizConstants.H5_SEVEN_NB_HOST);
        } else {
            model.addAttribute(BizConstants.HOST, BizConstants.H5_WEILINJIA_HOST);
        }
        response.setCharacterEncoding("UTF-8");
        log.info("query string : " + request.getQueryString());
        String crowdfundId = ParameterUtil.getParameter(request, "crowdfundId");
        String code = ParameterUtil.getParameter(request, BizConstants.WX_PARAM_CODE);
        String shopRedirect = ParameterUtil.getParameter(request, "session");
        String state = ParameterUtil.getParameter(request, BizConstants.WX_PARAM_STATE);
        String ua = ((HttpServletRequest) request).getHeader("user-agent").toLowerCase();
        StringUtil stringUtil = new StringUtil();
        model.addAttribute("stringUtil", stringUtil);
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        session.setTimeout(TimeUtils.TIME_1_MONTH_MILi);
        boolean updateProfile = false;
        if (StringUtils.isNotBlank(state)) {
            session.setAttribute(BizConstants.WEIXIN_MP_STATE, state);
        }
        long profileId = -1l;
        int crowdItemsPageStatus = -1;	//判断用户是否已使用微信访问商品详情页
        if (null != session.getAttribute(BizConstants.PROFILE_ID)) {
            profileId = (long) session.getAttribute(BizConstants.PROFILE_ID);
            model.addAttribute(BizConstants.PROFILE_ID, session.getAttribute(BizConstants.PROFILE_ID));
            model.addAttribute(BizConstants.SHOP_ID, session.getAttribute(BizConstants.SHOP_ID));
        }
        if(null != session){
        	//设置默认站点信息
            userService.setCommunity(model, session, response, profileId);
        }
        String pUserId = ParameterUtil.getParameter(request, "pUserId");	//上级用户id
        if(null == pUserId || !StringUtils.isNumeric(pUserId) || StringUtils.isBlank(pUserId)){
        	pUserId = "0";
        }
        model.addAttribute("pUserId", pUserId);	//上级userId
        
        if (null != session.getAttribute("crowdItemsPageStatus")) {
        	crowdItemsPageStatus = (int)session.getAttribute("crowdItemsPageStatus");
        }
        if(crowdItemsPageStatus > 0 || profileId > -1L){	//如果已微信注册
        	return "crowdfundItemListPage";
        }
        String state2 = "1";
        String domain = BizConstants.ONLINE_DOMAIN;
        if (env.isProd()) {
        		state2 = ConstantUtil.GUOGEGE_WX_CONF_KEY;
        		domain = BizConstants.ONLINE_DOMAIN;
        } else {
        		state2 = ConstantUtil.XIAOWEILINJU_WX_CONF_KEY;
        		domain = BizConstants.ONLINE_DOMAIN_TEST;
        }
        /*if (StringUtils.isNotBlank((String) session.getAttribute(BizConstants.WEIXIN_MP_STATE))) {
        		state = (String) session.getAttribute(BizConstants.WEIXIN_MP_STATE);
        }*/
        if (crowdItemsPageStatus < 0 && ua.indexOf("micromessenger") > 0 
        		&& StringUtils.isBlank(state) && StringUtils.isBlank(code)) {// 是微信浏览器
        	//String detailPageUrl = "http://"+BizConstants.ONLINE_DOMAIN+"/api/h/1.0/detailPage.htm?"+request.getQueryString();
        	//String detailPageUrl = "http://"+domain+"/api/h/1.0/crowdfundItemListPage.htm?crowdfundId="+crowdfundId + "&pUserId="+pUserId;
        	String detailPageUrl = "http://"+domain+"/api/h/1.0/crowdfundItemListPage.htm"+(null==request.getQueryString() ? "":"?"+request.getQueryString());
        	String detailPageUrlEUrl = URLEncoder.encode(detailPageUrl, "UTF-8");
        	String url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="+ConstantUtil.mcMap.get(state2).getAppId()
        			+	"&redirect_uri=" + detailPageUrlEUrl
        			//+ "&redirect_uri=http%3A%2F%2Fmiku.unesmall.com%2Fapi%2Fh%2F1.0%2FindexPage.htm"
        			+ "&response_type=code&scope=snsapi_base&state="+state2+"#wechat_redirect";
        	response.sendRedirect(url);
        	return null;
        }
        
        session.setAttribute("crowdItemsPageStatus", 1);	//判断用户是否已使用微信访问商品详情页
        if(UserUtils.redirectLbsShop(session)){
        	CommunityDOExample cExample = new CommunityDOExample();
            cExample.createCriteria().andStatusEqualTo((byte) 1);
            List<CommunityDO> communityDOs = communityDOMapper.selectByExample(cExample);
            if (null != communityDOs && communityDOs.size() > 0) {
            	CommunityDO communityDO = communityDOs.get(0);
            	session.setAttribute(BizConstants.SHOP_ID, communityDO.getId());
            	model.addAttribute(BizConstants.SHOP_ID, session.getAttribute(BizConstants.SHOP_ID));
            }
        }
        if (null != session) {
            model.addAttribute(BizConstants.OPENID, (String) session.getAttribute(BizConstants.OPENID));
        }
        
        ResponseResult result = new ResponseResult();
        model.addAttribute("ver", new Date().getTime() / (1000 * 60 * 60 * 24));
        //设置host
        if (null != session && null != session.getAttribute(BizConstants.PROFILE_ID)) {
            model.addAttribute(BizConstants.PROFILE_ID, session.getAttribute(BizConstants.PROFILE_ID));
            model.addAttribute(BizConstants.SHOP_ID, session.getAttribute(BizConstants.SHOP_ID));
        }
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
                    log.info("user enter fetch wechat page  wxMpOAuth access token not null ... sesionid:" + session.getId());
                    WxMpUser user = null;
                    openId = wxMpOAuth2AccessToken.getOpenId();
                    Preconditions.checkArgument(StringUtils.isNotBlank(openId), "只能在微信环境下使用，无法获得openId");
                    if (StringUtils.isNotEmpty(openId)) {
                        if (profileId > 0) {
                            profileExtService.updateOpenId(openId, profileId);
                        }
                        model.addAttribute("openid", openId);
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
                            //获取微信用户信息并存储
                            log.info("user enter fetch wechat page not store wecaht info profile  ... sesionid:" + session.getId() + ",openid" + openId);
                            Profiler.enter(" fetch wechat user info  wechat user....  ");
                            Profiler.release();
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
                        } else {//存储过wechat_profile 关联信息
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
                                model.addAttribute(BizConstants.SHOP_ID, session.getAttribute(BizConstants.SHOP_ID));
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
                            ProfileDO profileDO = userService.fetchProfileById(profileId);
                            profileExtService.updateLastLogin(profileId);
                            if(null != profileDO){
                            	 session.setAttribute("isAgency", null == profileDO.getIsAgency() ? "0" : profileDO.getIsAgency());
                            }
                            model.addAttribute(BizConstants.PROFILE_ID, profileId);
                            model.addAttribute(BizConstants.SHOP_ID, session.getAttribute(BizConstants.SHOP_ID));
                            model.addAttribute(BizConstants.P_USER_ID, PhenixUserHander.encodeUserId((Long) session.getAttribute(BizConstants.PROFILE_ID)));
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
                    buildStoredOpenid(session, response, model);
                    /*if (!StringUtils.equals("expire", shopRedirect) && UserUtils.redirectLbsShop(session)) {
                    	response.sendRedirect("/api/h/1.0/crowdfundItemListPage.htm?crowdfundId="+crowdfundId);
                        return null;
                    }*/
                    return "crowdfundItemListPage";
                }
                result.setStatus(ResponseStatusEnum.SUCCESS.getCode());
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
            model.addAttribute("result", result);
            buildStoredOpenid(session, response, model);
            /*if (!StringUtils.equals("expire", shopRedirect) && UserUtils.redirectLbsShop(session)) {
                response.sendRedirect("/api/h/1.0/crowdfundItemListPage.htm?crowdfundId="+crowdfundId);
                return null;
            }*/
            return "crowdfundItemListPage";
        }
        if (null != session.getAttribute(BizConstants.PROFILE_ID)) {
            model.addAttribute(BizConstants.PROFILE_ID, session.getAttribute(BizConstants.PROFILE_ID));
            model.addAttribute(BizConstants.P_USER_ID, PhenixUserHander.encodeUserId((Long) session.getAttribute(BizConstants.PROFILE_ID)));
        }
        //设置默认站点信息
        userService.setCommunity(model, session, response, profileId);
        if (env.isDev() && StringUtils.equals("true", ParameterUtil.getParameter(request, "debug"))) {
            profileId = 277;
            String openid = BizConstants.TEST_OPEN_ID_FOR_LUOMO;
            model.addAttribute("openid", openid);
            model.addAttribute(BizConstants.SHOP_ID, session.getAttribute(BizConstants.SHOP_ID));
            session.setAttribute(BizConstants.PROFILE_ID, profileId);
            ProfileDO profileDO = userService.fetchProfileById(profileId);
            profileExtService.updateLastLogin(profileId);
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
        /*if (!StringUtils.equals("expire", shopRedirect) && UserUtils.redirectLbsShop(session)) {
            response.sendRedirect("/api/h/1.0/crowdfundItemListPage.htm?crowdfundId="+crowdfundId);
            return null;
        }*/
        return "crowdfundItemListPage";
    }
    
    /**
     * 
     * crowdfundPaymentPage:(众筹商品详情 支付页面). <br/>
     * TODO(这里描述这个方法适用条件 – 可选).<br/>
     *
     * @author LuoGuangChun
     * @param request
     * @param response
     * @param model
     * @return
     * @throws Exception
     * @since JDK 1.6
     */
    @RequestMapping(value = {"/api/h/1.0/crowdfundPaymentPage.htm"}, produces = "text/html;charset=utf-8")
    public String crowdfundPaymentPage(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
    	String redirectUrl = "";
        String oauth = ParameterUtil.getParameter(request, "oauth");
        model.addAttribute("ver", new Date().getTime() / (1000 * 60 * 60 * 24));
        if (env.isProd()) {
            model.addAttribute(BizConstants.HOST, BizConstants.H5_SEVEN_NB_HOST);
        } else {
            model.addAttribute(BizConstants.HOST, BizConstants.H5_WEILINJIA_HOST);
        }
        model.addAttribute(BizConstants.APPID, ConstantUtil.GUOGEGE_H5_APP_ID);
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
        
        String pUserId = ParameterUtil.getParameter(request, "pUserId");	//上级用户id
        if(null == pUserId || !StringUtils.isNumeric(pUserId) || StringUtils.isBlank(pUserId)){
        	pUserId = "0";
        }
        model.addAttribute("pUserId", pUserId);	//上级userId
        
        //获取当前url
        String currentUrl = request.getScheme() + "://";
        currentUrl += request.getServerName();//获取到域名
        currentUrl += request.getRequestURI();
        if (request.getQueryString() != null) {
            currentUrl += "?" + request.getQueryString() + "&oauth=true";
        }
        if (UserUtils.redirect(session)) {
            model.addAttribute("url", currentUrl);
            log.info("=====toRedirectUrl:" + currentUrl);
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
            String state = "1";
            if (env.isProd()) {
                state = ConstantUtil.GUOGEGE_WX_CONF_KEY;
            } else {
                //state = ConstantUtil.XIAOWEILINJU_WX_CONF_KEY;
            	state = ConstantUtil.GUOGEGE_WX_CONF_KEY;
            }
            /*if (StringUtils.isNotBlank((String) session.getAttribute(BizConstants.WEIXIN_MP_STATE))) {
                state = (String) session.getAttribute(BizConstants.WEIXIN_MP_STATE);
            }*/
            String toRedirectUrl = null;
            if (request.getQueryString() != null) {
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
        
        return "crowdfundPaymentPage";
    }
    
    private void buildStoredOpenid(Session session, HttpServletResponse response, ModelMap model) {
        String openId;
        String tmpOpenid = (String) session.getAttribute(BizConstants.OPENID);
        if (StringUtils.isNotBlank(tmpOpenid)) {
            model.addAttribute(BizConstants.OPENID, tmpOpenid);
            openId = tmpOpenid;
            long profileId = userService.fetchProfileIdByOpenid(openId);
            if (profileId > 0) {
                profileExtService.updateLastLogin(profileId);
                session.setAttribute(BizConstants.PROFILE_ID, profileId);
                ProfileDO profileDO = userService.fetchProfileById(profileId);
                profileExtService.updateLastLogin(profileId);
                if(null != profileDO){
                	 session.setAttribute("isAgency", null == profileDO.getIsAgency() ? "0" : profileDO.getIsAgency());
                }
                model.addAttribute(BizConstants.PROFILE_ID, profileId);
                session.setAttribute(BizConstants.OPENID, openId);
                model.addAttribute(BizConstants.SHOP_ID, session.getAttribute(BizConstants.SHOP_ID));
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
