package com.welink.web.resource;

import com.alibaba.fastjson.JSON;
import com.daniel.weixin.common.exception.WxErrorException;
import com.welink.biz.common.model.JsApiTicketJson;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.common.security.SSLClient;
import com.welink.biz.util.TimeUtils;
import com.welink.biz.wx.tenpay.RequestHandler;
import com.welink.biz.wx.tenpay.util.ConstantUtil;
import com.welink.biz.wx.tenpay.util.Sha1Util;
import com.welink.biz.wx.tenpay.util.WXUtil;
import com.welink.commons.Env;
import com.welink.commons.commons.BizConstants;
import com.welink.web.common.util.ParameterUtil;
import net.spy.memcached.MemcachedClient;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.util.*;

/**
 * Created by daniel on 15-1-13.
 */
@RestController
public class HJsSdkParams {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(HJsSdkParams.class);

    @Resource
    private MemcachedClient memcachedClient;

    @Resource
    private Env env;

    private static final String JS_API_TICKET = "jsApiTicket_tickets";

    @RequestMapping(value = {"/api/h/1.0/hjsApiParams.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        WelinkVO welinkVO = new WelinkVO();
        RequestHandler prepayReqHandler = new RequestHandler();//获取prepayid的请求类
        //获取token值
        String appId = ConstantUtil.GUOGEGE_H5_APP_ID;
        String appSecret = ConstantUtil.GUOGEGE_APP_SECRET;
        String state = (String) session.getAttribute(BizConstants.WEIXIN_MP_STATE);
        log.info("=======================fetch state===========state:" + state + ",sessionId:" + session.getId().toString());
        if (env.isProd()) {
        	state = ConstantUtil.GUOGEGE_WX_CONF_KEY;
        } else {
        	state = ConstantUtil.XIAOWEILINJU_WX_CONF_KEY;
        }
        if (StringUtils.isNotBlank(state) && !StringUtils.equalsIgnoreCase("null", state)) {
            appId = ConstantUtil.mcMap.get(state).getAppId();
            appSecret = ConstantUtil.mcMap.get(state).getAppSecret();
        	/*appId = ConstantUtil.GUOGEGE_H5_APP_ID;
            appSecret = ConstantUtil.GUOGEGE_APP_SECRET;*/
        } else {
            appId = ConstantUtil.GUOGEGE_H5_APP_ID;
            appSecret = ConstantUtil.GUOGEGE_APP_SECRET;
        }
        String token = "";
        if (StringUtils.isNotBlank((String) memcachedClient.get(BizConstants.JSAPI_TICKETS_TOKEN + state))) {
            token = (String) memcachedClient.get(BizConstants.JSAPI_TICKETS_TOKEN + state);
        } else {
            token = prepayReqHandler.GetToken(appId, appSecret);
            if (StringUtils.isNotBlank(token)) {
                memcachedClient.set(BizConstants.JSAPI_TICKETS_TOKEN + state, TimeUtils.JS_API_TICKET_TIMEOUT, token);
            }
        }

        String jsApiTicket = getJsApiTicket(token, state);
        String noncestr = WXUtil.getNonceStr();
        String timestamp = WXUtil.getTimeStamp();
        
        String url = URLDecoder.decode(ParameterUtil.getParameter(request, "url"));
        TreeMap<String, String> parameters = new TreeMap<String, String>();
        if (StringUtils.isNotBlank(jsApiTicket)) {
            parameters.put("noncestr", noncestr);
            parameters.put("jsapi_ticket", jsApiTicket);
            parameters.put("timestamp", timestamp);
            parameters.put("url", url);
        }
        String sign = createSHA1Sign(parameters);
        welinkVO.setStatus(1);
        Map resultMap = new HashMap();
        resultMap.put("timestamp", Long.valueOf(timestamp));
        resultMap.put("nonceStr", noncestr);
        resultMap.put("signature", sign);
        resultMap.put("appId", appId);
        welinkVO.setResult(resultMap);
        return JSON.toJSONString(welinkVO);
    }

    //获取jsapi_ticket:先从memcachedClient取，若没有则发送请求获取后，放到memcachedClient中
    public String getJsApiTicket(String accessToken, String state) throws WxErrorException {
        if (memcachedClient.get(JS_API_TICKET + state) != null) {
            return String.valueOf(memcachedClient.get(JS_API_TICKET + state));
        }
        String url = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?";
        url += "access_token=" + accessToken;
        url += "&type=jsapi";
        String responseText = SSLClient.doGet(url);
        JsApiTicketJson jsApiTicketJson = JsApiTicketJson.fromJson(responseText);
        memcachedClient.set(JS_API_TICKET + state, TimeUtils.JS_API_TICKET_TIMEOUT, jsApiTicketJson.getTicket());
        return jsApiTicketJson.getTicket();
    }

    /**
     * 创建签名SHA1
     *
     * @return
     * @throws Exception
     */
    private String createSHA1Sign(Map<String, String> parameters) {
        StringBuffer sb = new StringBuffer();
        Set es = parameters.entrySet();
        Iterator it = es.iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String k = (String) entry.getKey();
            String v = (String) entry.getValue();
            sb.append(k + "=" + v + "&");
        }
        String params = sb.substring(0, sb.lastIndexOf("&"));
        System.out.println("HJsSdkParams.params......"+params);
        return Sha1Util.getSha1(params);
    }
}
