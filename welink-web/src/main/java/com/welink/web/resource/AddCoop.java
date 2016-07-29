package com.welink.web.resource;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipaySystemOauthTokenRequest;
import com.alipay.api.request.AlipayUserUserinfoShareRequest;
import com.alipay.api.response.AlipaySystemOauthTokenResponse;
import com.alipay.api.response.AlipayUserUserinfoShareResponse;
import com.opensymphony.xwork2.ActionContext;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.AlipayInfoModel;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.common.model.WxInfoModel;
import com.welink.biz.common.pay.AlipayConfig;
import com.welink.biz.common.pay.RSA;
import com.welink.biz.service.UserService;
import com.welink.biz.util.TimeUtils;
import com.welink.buy.utils.PhenixUserHander;
import com.welink.commons.commons.BizConstants;
import com.welink.commons.domain.ProfileCoopDO;
import com.welink.commons.domain.ProfileDO;
import com.welink.web.common.constants.ResponseStatusEnum;
import com.welink.web.common.model.ResponseResult;
import com.welink.web.common.util.ParameterUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by daniel on 15-1-8.
 */
@RestController
public class AddCoop {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(CoopLogin.class);

    @Resource
    private UserService userService;

    @RequestMapping(value = {"/api/m/1.0/addCoop.json", "/api/h/1.0/addCoop.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String loginType = ParameterUtil.getParameter(request, "ltype");
        String uInfo = ParameterUtil.getParameter(request, "uInfo");
        String authCode = ParameterUtil.getParameter(request, "oCode");
        ActionContext context = ActionContext.getContext();
        ResponseResult result = new ResponseResult();
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        WelinkVO welinkVO = new WelinkVO();
        session.setAttribute(BizConstants.LOGIN_TYPE, loginType);
        //微信登陆
//        if (StringUtils.equalsIgnoreCase(loginType, String.valueOf(BizConstants.LoginEnum.WX_LOGIN.getType()))) {
        if (StringUtils.equalsIgnoreCase(loginType, String.valueOf(BizConstants.LoginEnum.WX_LOGIN.getType())) || StringUtils.equalsIgnoreCase(loginType, String.valueOf(BizConstants.LoginEnum.QQ_LOGIN.getType()))) {
            WxInfoModel wxInfoModel = JSON.parseObject(uInfo, WxInfoModel.class);
            if (null == wxInfoModel || (null != wxInfoModel && StringUtils.isBlank(wxInfoModel.getUnionid()))) {
                log.error("login coop with wx .  wxInfo is null... uInfo:" + uInfo + ",sessionId:" + session.getId());
                welinkVO.setStatus(0);
                welinkVO.setCode(BizErrorEnum.WEIXIN_AUTH_FAILED.getCode());
                welinkVO.setMsg(BizErrorEnum.WEIXIN_AUTH_FAILED.getMsg());
                return JSON.toJSONString(welinkVO);
            }
            //判断是否已经存储过
            ProfileCoopDO profileCoopDO = userService.fetchProfileCoopByUnionId(loginType, wxInfoModel.getUnionid());
            if (null == profileCoopDO) {
                boolean addCoopInfo = userService.addCoopInfo(loginType, wxInfoModel, session);
                Cookie cookie = new Cookie(BizConstants.JSESSION_ID, session.getId().toString());
                cookie.setMaxAge(TimeUtils.TIME_1_MONTH_SEC);
                cookie.setPath("/");
                response.addCookie(cookie);
                if (!addCoopInfo) {
                    result.setStatus(ResponseStatusEnum.FAILED.getCode());
                    result.setErrorCode(BizErrorEnum.ADD_COOP_USER_INFO_FAILED.getCode());
                    result.setMsg(BizErrorEnum.ADD_COOP_USER_INFO_FAILED.getMsg());
                    context.put("result", result);
                    welinkVO.setStatus(0);
                    welinkVO.setCode(BizErrorEnum.ADD_COOP_USER_INFO_FAILED.getCode());
                    welinkVO.setMsg(BizErrorEnum.ADD_COOP_USER_INFO_FAILED.getMsg());
                    return JSON.toJSONString(welinkVO);
                }
                welinkVO.setStatus(1);
                Map resultMap = new HashMap();
                resultMap.put("bind", false);
                resultMap.put("openid", wxInfoModel.getOpenid());
                resultMap.put("unionId", wxInfoModel.getUnionid());
                resultMap.put("profilePic", wxInfoModel.getHeadimgurl());
                welinkVO.setResult(resultMap);
                return JSON.toJSONString(welinkVO);
            } else {
                //已经存储过看是否绑定过
                long profileId = userService.hasBindUser(loginType, profileCoopDO.getId(), session);
                if (profileId > 0) {
                    welinkVO.setStatus(1);
                    Map resultMap = new HashMap();
                    resultMap.put("bind", true);
                    Cookie cookie = new Cookie(BizConstants.JSESSION_ID, session.getId().toString());
                    cookie.setMaxAge(TimeUtils.TIME_1_MONTH_SEC);
                    cookie.setPath("/");
                    response.addCookie(cookie);
                    resultMap.put("uid", PhenixUserHander.encodeUserId(profileId));
                    resultMap.put("openid", wxInfoModel.getOpenid());
                    resultMap.put("unionId", wxInfoModel.getUnionid());
                    ProfileDO profileDO = userService.fetchProfileById(profileId);
                    resultMap.put("profilePic", wxInfoModel.getHeadimgurl());
                    if(null != profileDO){
                    	resultMap.put(BizConstants.MOBILE, profileDO.getMobile());
                    }
                    welinkVO.setResult(resultMap);
                    return JSON.toJSONString(welinkVO);
                } else {
                    welinkVO.setStatus(1);
                    Map resultMap = new HashMap();
                    resultMap.put("bind", false);
                    welinkVO.setResult(resultMap);
                    return JSON.toJSONString(welinkVO);
                }
            }
        }
        //支付宝登陆
        else {
            //支付宝网关地址
            String serverUrl = "https://openapi.alipay.com/gateway.do"; //应用 ID
            String appId = AlipayConfig.ALIPAY_APP_ID;
            //返回结果格式:xml、json;
            String format = "json";
            String privateKey = RSA.fetchPrivateKeyStr();
            //字符集格式
            String charset = "utf-8";
            //客户端返回的 auth_code
            //请求对象
            AlipaySystemOauthTokenRequest req = new AlipaySystemOauthTokenRequest();
            req.setCode(authCode);
            //GrantType 传固定值 authorization_code
            req.setGrantType("authorization_code");
            AlipayClient client = new DefaultAlipayClient(serverUrl, appId, privateKey, format, charset);
            //返回结果对象
            AlipaySystemOauthTokenResponse res = null;
            try {
                res = client.execute(req);
            } catch (Exception e) {
                log.error("alipay client excute failed.  e:" + e.getMessage() + ",cause:" + e.getCause() + ",sessionId:" + session.getId());
                log.error(e.getMessage() + ",sessionId:" + session.getId(), e);
            }
            String accessToken = null;
            if (null != res) {
                accessToken = res.getAccessToken();
            }
            //********************************************************************
            //   获取用户信息
            //********************************************************************
            if (StringUtils.isNotBlank(accessToken)) {
                String prodCode = "WAP_FAST_LOGIN";
                AlipayUserUserinfoShareRequest infoReq = new AlipayUserUserinfoShareRequest();
                infoReq.setProdCode(prodCode);
                client = new DefaultAlipayClient(serverUrl, appId, privateKey, format, charset); //返回结果对象
                AlipayUserUserinfoShareResponse infoRes = client.execute(infoReq, accessToken);
                if (null != infoRes) {
                    ProfileCoopDO profileCoopDO = checkAlipayUser(loginType, infoRes.getUserId());
                    if (null == profileCoopDO) {
                        AlipayInfoModel alipayInfoModel = new AlipayInfoModel();
                        alipayInfoModel.setUserId(infoRes.getUserId());
                        boolean addCoopInfo = userService.addCoopInfo(loginType, alipayInfoModel, session);
                        Cookie cookie = new Cookie(BizConstants.JSESSION_ID, session.getId().toString());
                        cookie.setMaxAge(TimeUtils.TIME_1_MONTH_SEC);
                        cookie.setPath("/");
                        response.addCookie(cookie);
                        if (!addCoopInfo) {
                            result.setStatus(ResponseStatusEnum.FAILED.getCode());
                            result.setErrorCode(BizErrorEnum.ADD_COOP_USER_INFO_FAILED.getCode());
                            result.setMsg(BizErrorEnum.ADD_COOP_USER_INFO_FAILED.getMsg());
                            context.put("result", result);
                            welinkVO.setStatus(0);
                            welinkVO.setCode(BizErrorEnum.ADD_COOP_USER_INFO_FAILED.getCode());
                            welinkVO.setMsg(BizErrorEnum.ADD_COOP_USER_INFO_FAILED.getMsg());
                            return JSON.toJSONString(welinkVO);
                        }
                        Map resultMap = new HashMap();
                        resultMap.put("bind", false);
                        resultMap.put(BizConstants.ALIPAY_ID, infoRes.getUserId());
                        welinkVO.setStatus((byte) 1);
                        welinkVO.setResult(resultMap);
                        return JSON.toJSONString(welinkVO);
                    } else {
                        //已经存储过看是否绑定过
                        long profileId = userService.hasBindUser(loginType, profileCoopDO.getId(), session);
                        if (profileId > 0) {
                            welinkVO.setStatus(1);
                            Map resultMap = new HashMap();
                            resultMap.put("bind", true);
                            Cookie cookie = new Cookie(BizConstants.JSESSION_ID, session.getId().toString());
                            cookie.setMaxAge(TimeUtils.TIME_1_MONTH_SEC);
                            cookie.setPath("/");
                            response.addCookie(cookie);
                            resultMap.put("uid", PhenixUserHander.encodeUserId(profileId));
                            ProfileDO profileDO = userService.fetchProfileById(profileId);
                            resultMap.put("mobile", profileDO.getMobile());
                            resultMap.put("lemonName", profileDO.getLemonName());
                            resultMap.put("profilePic", profileDO.getProfilePic());
                            resultMap.put("isAgency", null == profileDO.getIsAgency() ? "0" : profileDO.getIsAgency());
                            resultMap.put(BizConstants.ALIPAY_ID, infoRes.getUserId());
                            welinkVO.setResult(resultMap);
                            return JSON.toJSONString(welinkVO);
                        } else {
                            welinkVO.setStatus(1);
                            Map resultMap = new HashMap();
                            resultMap.put("bind", false);
                            resultMap.put(BizConstants.ALIPAY_ID, infoRes.getUserId());
                            welinkVO.setResult(resultMap);
                            return JSON.toJSONString(welinkVO);
                        }
                    }
                } else {
                    log.error("从支付宝获取AlipayUserUserinfoShareResponse为空" + ",sessionId:" + session.getId());
                    welinkVO.setStatus(0);
                    welinkVO.setCode(BizErrorEnum.ALIPAY_AUTH_FAILED.getCode());
                    welinkVO.setMsg(BizErrorEnum.ALIPAY_AUTH_FAILED.getMsg());
                    return JSON.toJSONString(welinkVO);
                }
            } else {
                log.error("accessToken获取失败... sessionId:" + session.getId());
                welinkVO.setStatus(0);
                welinkVO.setCode(BizErrorEnum.ALIPAY_AUTH_FAILED.getCode());
                welinkVO.setMsg(BizErrorEnum.ALIPAY_AUTH_FAILED.getMsg());
                return JSON.toJSONString(welinkVO);
            }
        }
    }

    /**
     * 判断用户是否已经存储过
     *
     * @param userId
     * @return
     */
    private ProfileCoopDO checkAlipayUser(String loginType, String userId) {
        ProfileCoopDO profileCoopDO = userService.fetchProfileCoopByAlipayUserId(loginType, userId);
        if (null != profileCoopDO) {
            return profileCoopDO;
        }
        return null;
    }
}
