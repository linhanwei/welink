package com.welink.web.resource.hpage;

import java.util.Date;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.spy.memcached.MemcachedClient;

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
import com.welink.biz.common.constants.TimeConstants;
import com.welink.biz.service.ProfileExtService;
import com.welink.biz.service.UserService;
import com.welink.biz.util.StringUtil;
import com.welink.biz.util.TimeUtils;
import com.welink.biz.wx.tenpay.util.ConstantUtil;
import com.welink.commons.Env;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.ProfileDO;
import com.welink.commons.domain.ProfileWeChatDO;
import com.welink.commons.persistence.ProfileDOMapper;
import com.welink.commons.utils.PhenixUserHander;
import com.welink.web.common.constants.ResponseStatusEnum;
import com.welink.web.common.filter.Profiler;
import com.welink.web.common.model.ResponseResult;
import com.welink.web.common.util.ParameterUtil;

/**
 * Created by daniel on 15-1-4.
 */
@Controller
public class IndexPage {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(IndexPage.class);

    @Resource
    private UserService userService;
    
    @Resource
    private ProfileDOMapper profileDOMapper;

    @Resource
    private WxMpService wxMpService;

    @Resource
    private ProfileExtService profileExtService;
    
    @Resource
    private DetailPage detailPage;
    
    @Resource
	private MemcachedClient memcachedClient; 
    
    
    public void setMemcachedClient(MemcachedClient memcachedClient) {
		this.memcachedClient = memcachedClient;
	}

	@Resource
    private Env env;

    @RequestMapping(value = {"/api/h/1.0/indexPage.htm"}, produces = "text/html;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
        response.setCharacterEncoding("UTF-8");
        log.info("query string : " + request.getQueryString());
        String code = ParameterUtil.getParameter(request, BizConstants.WX_PARAM_CODE);
        String shopRedirect = ParameterUtil.getParameter(request, "session");
        String state = ParameterUtil.getParameter(request, BizConstants.WX_PARAM_STATE);
        StringUtil stringUtil = new StringUtil();
        model.addAttribute("stringUtil", stringUtil); 
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        session.setTimeout(TimeUtils.TIME_1_MONTH_MILi);
        boolean updateProfile = false;
        if (StringUtils.isNotBlank(state)) { 
            session.setAttribute(BizConstants.WEIXIN_MP_STATE, state);
        }
        model.addAttribute("subscribeWx", 0);	//是否订阅微信（0=未订阅；1=已订阅；2=已微信注册）
        long profileId = -1l;
        if (null != session.getAttribute(BizConstants.PROFILE_ID)) {
            profileId = (long) session.getAttribute(BizConstants.PROFILE_ID);
            model.addAttribute(BizConstants.PROFILE_ID, session.getAttribute(BizConstants.PROFILE_ID));
            model.addAttribute(BizConstants.SHOP_ID, session.getAttribute(BizConstants.SHOP_ID));
        }
        if (profileId > 0) {
        	model.addAttribute(BizConstants.PROFILE_ID, (long) session.getAttribute(BizConstants.PROFILE_ID));
        	try {
        		detailPage.setUserAgency(model, session, request);	//设置代理关系
	        	Integer subscribeWx = 0; 
	        	Object subscribeWxMemcached = memcachedClient.get(BizConstants.SUBSCRIBE_WX+profileId);
	        	if(null != subscribeWxMemcached 
	        			&& StringUtils.isNotBlank(String.valueOf(subscribeWxMemcached))
	        			&& StringUtils.isNumeric(String.valueOf(subscribeWxMemcached))){
	        		subscribeWx = (Integer)subscribeWxMemcached;
	        	}
	        	
	        	if(null == subscribeWx || (null != subscribeWx && subscribeWx.equals(0))){
	        		ProfileWeChatDO profileWeChatDO = userService.getProfileWeChatByProfileId(profileId);
	        		if(null != profileWeChatDO){
	        			model.addAttribute("subscribeWx", 2);	//是否订阅微信（0=未订阅；1=已订阅；2=已微信注册）
	        			memcachedClient.set(BizConstants.SUBSCRIBE_WX+profileId, 10*TimeConstants.REDIS_EXPIRE_DAY_1_2, 2);
	        			String lang = "zh_CN"; //语言
	        			
	        				WxMpUser user = wxMpService.userInfo(profileWeChatDO.getOpenid(), lang);
	        				if(null != user){
	        					if(user.getSubscribe()){
	        						model.addAttribute("subscribe", 1);		//已订阅
	        						memcachedClient.set(BizConstants.SUBSCRIBE_WX+profileId, 10*TimeConstants.REDIS_EXPIRE_DAY_1_2, 1);
	        					}else{
	        						model.addAttribute("subscribe", 0);		//未订阅
	        					}
	        				}
	        			
	        		}else{
	        			model.addAttribute("subscribeWx", 0);	//是否订阅微信（0=未订阅；1=已订阅；2=已微信注册）
	        		}
	        	}
        	} catch (Exception e) {
			}
        }
        ResponseResult result = new ResponseResult();
        model.addAttribute("ver", new Date().getTime() / (1000 * 60 * 60 * 24));
        //设置host
        setHost(model);
        if (null != session.getAttribute(BizConstants.PROFILE_ID)) {
            model.addAttribute(BizConstants.PROFILE_ID, session.getAttribute(BizConstants.PROFILE_ID));
            model.addAttribute(BizConstants.SHOP_ID, session.getAttribute(BizConstants.SHOP_ID));
        }
        String userAgent = ((HttpServletRequest) request).getHeader("user-agent").toLowerCase();
        //openId 检测并进行缓存处理
        String openId = null;
        Profiler.enter(" fetch wechat user info.  ");
        log.info("user enter fetch wechat page ... sesionid:" + session.getId());
        try {
            if (userAgent.indexOf("micromessenger") > 0 && StringUtils.isNotBlank(code)) {// 是微信浏览器
                log.info("user enter fetch wechat page has code ... sesionid:" + session.getId()+"------AppId--"+ConstantUtil.mcMap.get(state).getAppId());
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
                                    log.error("insert wechat user info failed. openId:" + wxMpOAuth2AccessToken.getOpenId() + "sessionid:" + session.getId() + ",openid:" + openId);
                                } else {
                                	if (profileId > 0) {
                                		long wxProfileId = userService.checkWxMpBinded(profileId);
                                        //未绑定--->绑定
                                        if (wxProfileId < 0 && null != user.getOpenId()) {
                                            userService.bindWxByProfileIdAndOpenId(profileId, user.getOpenId());
                                        }
                                	}
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
                            	long wxProfileId = userService.checkWxMpBinded(profileId);
                                //未绑定--->绑定
                                if (wxProfileId < 0 && null != user && null != user.getOpenId()) {
                                    userService.bindWxByProfileIdAndOpenId(profileId, user.getOpenId());
                                }
                            	
                                //profileExtService.updateLastLogin(profileId);
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
                        if (profileId < 0 && null != unionId) {
                            profileId = userService.checkWxMpBinded(unionId);
                        }
                        if (profileId > 0) {
                            session.setAttribute(BizConstants.PROFILE_ID, profileId);
                            ProfileDO profileDO = userService.fetchProfileById(profileId);
                            profileExtService.updateLastLogin(profileId);
                            if(null != profileDO){
                            	 session.setAttribute("isAgency", null == profileDO.getIsAgency() ? "0" : profileDO.getIsAgency());
                            	 if(null != user && (null == profileDO.getProfilePic() || "".equals(profileDO.getProfilePic()))){
                            		 profileDO.setProfilePic(user.getHeadImgUrl());
                            		 profileDOMapper.updateByPrimaryKeySelective(profileDO);
                            	 }
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
                        response.sendRedirect("/api/h/1.0/indexPage.htm?session=expire");
                        return null;
                    }*/
                    return "indexPage";
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
            /*if (userAgent.indexOf("micromessenger") > 0
            		&& StringUtils.isNotBlank(state)) {// 是微信浏览器
            	//获取当前url
                String currentUrl = request.getScheme() + "://";
                currentUrl += request.getServerName();//获取到域名
                currentUrl += request.getRequestURI();
                if (request.getQueryString() != null) {
                    currentUrl += "?" + request.getQueryString() + "&oauth=true";
                }
            	String detailPageUrlEUrl = URLEncoder.encode(currentUrl, "UTF-8");
            	String url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="+ConstantUtil.GUOGEGE_H5_APP_ID
            			+	"&redirect_uri=" + detailPageUrlEUrl
            			+ "&response_type=code&scope=snsapi_base&state=1#wechat_redirect";
            	response.sendRedirect(url);
            	return null;
            }*/
            
            model.addAttribute("result", result);
            buildStoredOpenid(session, response, model);
            /*if (!StringUtils.equals("expire", shopRedirect) && UserUtils.redirectLbsShop(session)) {
                response.sendRedirect("/api/h/1.0/indexPage.htm?session=expire");
                return null;
            }*/
            return "indexPage";
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
            response.sendRedirect("/api/h/1.0/indexPage.htm?session=expire");
            return null;
        }*/
        return "indexPage";
    }

    private void setHost(ModelMap model) {
        if (env.isProd()) {
            model.addAttribute(BizConstants.HOST, BizConstants.H5_SEVEN_NB_HOST);
        } else {
            model.addAttribute(BizConstants.HOST, BizConstants.H5_WEILINJIA_HOST);
        }
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
