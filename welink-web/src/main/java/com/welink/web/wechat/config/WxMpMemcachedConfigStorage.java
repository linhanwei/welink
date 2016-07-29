package com.welink.web.wechat.config;

import com.daniel.weixin.common.model.response.WxAccessToken;
import com.daniel.weixin.common.util.StringUtils;
import com.daniel.weixin.mp.api.WxMpConfigStorage;
import com.google.common.base.Preconditions;
import com.welink.biz.wx.tenpay.util.ConstantUtil;
import com.welink.commons.Env;

import net.spy.memcached.MemcachedClient;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.HashMap;

/**
 * Created by saarixx on 25/12/14.
 */
@Service("wxMpConfigStorage")
public class WxMpMemcachedConfigStorage implements WxMpConfigStorage, InitializingBean {

    private static String DEFAULT_WECHAT_ACCESSTOKEN_PREFIX = "$WECHAT_ACCESSTOKEN_$";

    private static String DEFAULT_JSAPI_TICKET_PREFIX = "$JSAPI_TICKET_$";

    @Resource
    private MemcachedClient memcachedClient;

    @Resource
    private Env env;

    private HashMap<String, MpConfigInfo> mpConfigInfoHashMap = new HashMap<>();

    // 以下信息直接写在代码里面就好了
    protected String appId;
    protected String secret;
    protected String token;
    protected String aesKey;

    //
    protected volatile long expiresTime;
    protected volatile long jsapiTicketExpiresTime;

    protected String oauth2redirectUri;
    // 如果没有proxy就可以不写，默认不用写
    protected String http_proxy_host;
    protected int http_proxy_port;
    protected String http_proxy_username;
    protected String http_proxy_password;

    @Override
    public void updateAccessToken(WxAccessToken wxAccessToken) {
        Preconditions.checkNotNull(wxAccessToken);
        updateAccessToken(wxAccessToken.getAccessToken(), wxAccessToken.getExpiresIn());
    }

    public String fetchAppIdByToUserName(String toUserName) {
        for (String k : this.getMpConfigInfoHashMap().keySet()) {
            if (org.apache.commons.lang.StringUtils.isNotBlank(toUserName) &&
                    org.apache.commons.lang.StringUtils.equals(toUserName, this.getMpConfigInfoHashMap().get(k).getToUserName())) {
                return this.getMpConfigInfoHashMap().get(k).getAppId();
            }
        }
        return ConstantUtil.GUOGEGE_H5_APP_ID;//默认米酷的appid
    }

    @Override
    public void updateAccessToken(WxAccessToken wxAccessToken, String mpTag) {
        Preconditions.checkNotNull(wxAccessToken);
        updateAccessToken(wxAccessToken.getAccessToken(), wxAccessToken.getExpiresIn(), mpTag);
    }

    @Override
    public synchronized void updateAccessToken(String accessToken, int expiresInSeconds) {
        StringUtils.isNotBlank(accessToken);
        Preconditions.checkArgument(expiresInSeconds > 0);
        memcachedClient.set(makeKey(DEFAULT_WECHAT_ACCESSTOKEN_PREFIX, this.appId), expiresInSeconds - 60, accessToken);
        this.expiresTime = System.currentTimeMillis() + (expiresInSeconds - 200) * 1000L;
    }

    @Override
    public void updateAccessToken(String accessToken, int expiresIn, String mpTag) {
        StringUtils.isNotBlank(accessToken);
        Preconditions.checkArgument(expiresIn > 0);
        memcachedClient.set(makeKey(DEFAULT_WECHAT_ACCESSTOKEN_PREFIX, this.getMpConfigInfoHashMap().get(mpTag).getAppId()), expiresIn - 60, accessToken);
        this.getMpConfigInfoHashMap().get(mpTag).setExpiresTime(System.currentTimeMillis() + (expiresIn - 200) * 1000L);
    }

    @Override
    public String getJsapiTicket() {
        String jsapiTicket = (String) memcachedClient.get(makeKey(DEFAULT_JSAPI_TICKET_PREFIX, this.appId));
        return jsapiTicket;
    }

    @Override
    public String getJsapiTicket(String mpTag) {
        String jsapiTicket = (String) memcachedClient.get(makeKey(DEFAULT_JSAPI_TICKET_PREFIX, this.getMpConfigInfoHashMap().get(mpTag).getAppId()));
        return jsapiTicket;
    }

    @Override
    public boolean isJsapiTicketExpired() {
        return System.currentTimeMillis() > this.jsapiTicketExpiresTime;
    }

    @Override
    public boolean isJsapiTicketExpired(String mpTag) {
        return System.currentTimeMillis() > this.getMpConfigInfoHashMap().get(mpTag).getJsapiTicketExpiresTime();
    }

    @Override
    public synchronized void expireJsapiTicket() {
        memcachedClient.delete(makeKey(DEFAULT_JSAPI_TICKET_PREFIX, this.appId));
        this.jsapiTicketExpiresTime = 0L;
    }

    @Override
    public void expireJsapiTicket(String mpTag) {
        memcachedClient.delete(makeKey(DEFAULT_JSAPI_TICKET_PREFIX, this.getMpConfigInfoHashMap().get(mpTag).getAppId()));
        this.getMpConfigInfoHashMap().get(mpTag).setJsapiTicketExpiresTime(0L);
    }

    @Override
    public synchronized void updateJsapiTicket(String jsapiTicket, int expiresInSeconds) {
        // 预留200秒的时间
        memcachedClient.set(makeKey(DEFAULT_JSAPI_TICKET_PREFIX, this.appId), expiresInSeconds - 60, jsapiTicket);
        this.jsapiTicketExpiresTime = System.currentTimeMillis() + (expiresInSeconds - 200) * 1000L;
    }

    @Override
    public void updateJsapiTicket(String jsapiTicket, int expiresInSeconds, String mpTag) {
        // 预留200秒的时间
        memcachedClient.set(makeKey(DEFAULT_JSAPI_TICKET_PREFIX, this.getMpConfigInfoHashMap().get(mpTag).getAppId()), expiresInSeconds - 60, jsapiTicket);
        this.getMpConfigInfoHashMap().get(mpTag).setJsapiTicketExpiresTime(System.currentTimeMillis() + (expiresInSeconds - 200) * 1000L);
    }

    @Override
    public String getAccessToken() {
        String accessToken = (String) memcachedClient.get(makeKey(DEFAULT_WECHAT_ACCESSTOKEN_PREFIX, this.appId));
        return accessToken;
    }

    @Override
    public String getAccessToken(String mpTag) {
        String accessToken = (String) memcachedClient.get(makeKey(DEFAULT_WECHAT_ACCESSTOKEN_PREFIX, this.getMpConfigInfoHashMap().get(mpTag).getAppId()));
        return accessToken;
    }

    @Override
    public boolean isAccessTokenExpired() {
        return System.currentTimeMillis() > this.expiresTime;
    }

    @Override
    public boolean isAccessTokenExpired(String mpTag) {
        return System.currentTimeMillis() > this.getMpConfigInfoHashMap().get(mpTag).getExpiresTime();
    }

    @Override
    public void expireAccessToken() {
        memcachedClient.delete(makeKey(DEFAULT_WECHAT_ACCESSTOKEN_PREFIX, this.appId));
        this.expiresTime = 0L;
    }

    @Override
    public void expireAccessToken(String mpTag) {
        memcachedClient.delete(makeKey(DEFAULT_WECHAT_ACCESSTOKEN_PREFIX, this.getMpConfigInfoHashMap().get(mpTag).getAppId()));
        this.getMpConfigInfoHashMap().get(mpTag).setExpiresTime(0);
    }

    @Override
    public String getAppId() {
        return this.appId;
    }

    @Override
    public String getAppId(String mpTag) {
        if (null != this.getMpConfigInfoHashMap() && null != this.getMpConfigInfoHashMap().get(mpTag) &&
                null != this.getMpConfigInfoHashMap().get(mpTag).getAppId()) {
            return this.getMpConfigInfoHashMap().get(mpTag).getAppId();
        }
        return ConstantUtil.GUOGEGE_H5_APP_ID;
    }

    @Override
    public void setAppId(String mpTag, boolean multi) {
        this.appId = this.getMpConfigInfoHashMap().get(mpTag).getAppId();
    }


    @Override
    public String getSecret() {
        return this.secret;
    }

    @Override
    public void setSecret(String mpTag, boolean multi) {
        this.secret = this.getMpConfigInfoHashMap().get(mpTag).getSecret();
    }

    @Override
    public String getSecret(String mpTag) {
        if (null != this.getMpConfigInfoHashMap() && null != this.getMpConfigInfoHashMap().get(mpTag) &&
                null != this.getMpConfigInfoHashMap().get(mpTag).getSecret()) {
            return this.getMpConfigInfoHashMap().get(mpTag).getSecret();
        } else {
            return ConstantUtil.GUOGEGE_APP_SECRET;//默认米酷的secret
        }
    }

    @Override
    public String getToken() {
        return this.token;
    }

    @Override
    public void setToken(String mpTag, boolean multi) {
        this.token = this.getMpConfigInfoHashMap().get(mpTag).getToken();
    }

    @Override
    public String getToken(String mpTag) {
        if (null != this.getMpConfigInfoHashMap() && null != this.getMpConfigInfoHashMap().get(mpTag) &&
                null != this.getMpConfigInfoHashMap().get(mpTag).getToken()) {
            return this.getMpConfigInfoHashMap().get(mpTag).getToken();
        } else {
            return ConstantUtil.GUOGEGE_TOKEN;//默认米酷的token
        }
    }

    @Override
    public String getAesKey() {
        return this.aesKey;
    }

    @Override
    public void setAesKey(String mpTag, boolean multi) {
        this.aesKey = this.getMpConfigInfoHashMap().get(mpTag).getAesKey();
    }


    @Override
    public String getAesKey(String mpTag) {
        if (null != this.getMpConfigInfoHashMap() && null != this.getMpConfigInfoHashMap().get(mpTag) &&
                null != this.getMpConfigInfoHashMap().get(mpTag).getAesKey()) {
            return this.getMpConfigInfoHashMap().get(mpTag).getAesKey();
        } else {
            return ConstantUtil.GUOGEGE_AES_KEY;//默认米酷的key
        }
    }

    @Override
    public long getExpiresTime() {
        return this.expiresTime;
    }

    @Override
    public void setExpiresTime(String mpTag, boolean multi) {
        this.expiresTime = this.getMpConfigInfoHashMap().get(mpTag).getExpiresTime();
    }

    @Override
    public long getExpiresTime(String mpTag) {
        return this.getMpConfigInfoHashMap().get(mpTag).getExpiresTime();
    }

    @Override
    public String getOauth2redirectUri() {
        return this.oauth2redirectUri;
    }

    @Override
    public String getOauth2redirectUri(String mpTag) {
        return this.getMpConfigInfoHashMap().get(mpTag).getOauth2redirectUri();
    }

    @Override
    public String getHttp_proxy_host() {
        return this.http_proxy_host;
    }

    @Override
    public String getHttp_proxy_host(String mpTag) {
        return this.getMpConfigInfoHashMap().get(mpTag).getHttp_proxy_host();
    }

    @Override
    public int getHttp_proxy_port() {
        return this.http_proxy_port;
    }

    @Override
    public int getHttp_proxy_port(String mpTag) {
        return this.getMpConfigInfoHashMap().get(mpTag).getHttp_proxy_port();
    }

    @Override
    public String getHttp_proxy_username() {
        return this.http_proxy_username;
    }

    @Override
    public String getHttp_proxy_username(String mpTag) {
        return this.getMpConfigInfoHashMap().get(mpTag).getHttp_proxy_username();
    }

    @Override
    public String getHttp_proxy_password() {
        return this.http_proxy_password;
    }

    @Override
    public String getHttp_proxy_password(String mpTag) {
        return this.getMpConfigInfoHashMap().get(mpTag).getHttp_proxy_password();
    }

    public static String makeKey(String prefix, String appId) {
        return prefix + appId;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    	String state = ConstantUtil.GUOGEGE_WX_CONF_KEY;
        if (env.isProd()) {
        	state = ConstantUtil.GUOGEGE_WX_CONF_KEY;
        }else{
        	state = ConstantUtil.XIAOWEILINJU_WX_CONF_KEY;
        }
        appId = ConstantUtil.mcMap.get(state).getAppId();
        secret = ConstantUtil.mcMap.get(state).getAppSecret();
        token = ConstantUtil.GUOGEGE_TOKEN;
        aesKey = ConstantUtil.GUOGEGE_AES_KEY;
        MpConfigInfo mpConfigInfoGuogege = new MpConfigInfo();
        mpConfigInfoGuogege.setAppId(appId);
        mpConfigInfoGuogege.setSecret(secret);
        mpConfigInfoGuogege.setToken(token);
        mpConfigInfoGuogege.setAesKey(aesKey);
        // 1 --> guogege
        //this.getMpConfigInfoHashMap().put(ConstantUtil.GUOGEGE_WX_CONF_KEY, mpConfigInfoGuogege);
        this.getMpConfigInfoHashMap().put(state, mpConfigInfoGuogege);
        //米酷
        /*appId = ConstantUtil.GUOGEGE_H5_APP_ID;
        secret = ConstantUtil.GUOGEGE_APP_SECRET;
        token = ConstantUtil.GUOGEGE_TOKEN;
        aesKey = ConstantUtil.GUOGEGE_AES_KEY;
        MpConfigInfo mpConfigInfoGuogege = new MpConfigInfo();
        mpConfigInfoGuogege.setAppId(appId);
        mpConfigInfoGuogege.setSecret(secret);
        mpConfigInfoGuogege.setToken(token);
        mpConfigInfoGuogege.setAesKey(aesKey);
        // 1 --> guogege
        this.getMpConfigInfoHashMap().put(ConstantUtil.GUOGEGE_WX_CONF_KEY, mpConfigInfoGuogege);*/
    }

    /**
     * Mp 配置信息
     */
    private class MpConfigInfo {
        private String appId;
        private String secret;
        private String token;
        private String aesKey;
        private String toUserName;
        protected volatile long expiresTime;
        protected volatile long jsapiTicketExpiresTime;

        protected String oauth2redirectUri;
        // 如果没有proxy就可以不写，默认不用写
        protected String http_proxy_host;
        protected int http_proxy_port;
        protected String http_proxy_username;
        protected String http_proxy_password;

        public String getToUserName() {
            return toUserName;
        }

        public void setToUserName(String toUserName) {
            this.toUserName = toUserName;
        }

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getAesKey() {
            return aesKey;
        }

        public void setAesKey(String aesKey) {
            this.aesKey = aesKey;
        }

        public long getExpiresTime() {
            return expiresTime;
        }

        public void setExpiresTime(long expiresTime) {
            this.expiresTime = expiresTime;
        }

        public long getJsapiTicketExpiresTime() {
            return jsapiTicketExpiresTime;
        }

        public void setJsapiTicketExpiresTime(long jsapiTicketExpiresTime) {
            this.jsapiTicketExpiresTime = jsapiTicketExpiresTime;
        }

        public String getOauth2redirectUri() {
            return oauth2redirectUri;
        }

        public void setOauth2redirectUri(String oauth2redirectUri) {
            this.oauth2redirectUri = oauth2redirectUri;
        }

        public String getHttp_proxy_host() {
            return http_proxy_host;
        }

        public void setHttp_proxy_host(String http_proxy_host) {
            this.http_proxy_host = http_proxy_host;
        }

        public int getHttp_proxy_port() {
            return http_proxy_port;
        }

        public void setHttp_proxy_port(int http_proxy_port) {
            this.http_proxy_port = http_proxy_port;
        }

        public String getHttp_proxy_username() {
            return http_proxy_username;
        }

        public void setHttp_proxy_username(String http_proxy_username) {
            this.http_proxy_username = http_proxy_username;
        }

        public String getHttp_proxy_password() {
            return http_proxy_password;
        }

        public void setHttp_proxy_password(String http_proxy_password) {
            this.http_proxy_password = http_proxy_password;
        }
    }

    public HashMap<String, MpConfigInfo> getMpConfigInfoHashMap() {
        return mpConfigInfoHashMap;
    }

    public void setMpConfigInfoHashMap(HashMap<String, MpConfigInfo> mpConfigInfoHashMap) {
        this.mpConfigInfoHashMap = mpConfigInfoHashMap;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setAesKey(String aesKey) {
        this.aesKey = aesKey;
    }
}
