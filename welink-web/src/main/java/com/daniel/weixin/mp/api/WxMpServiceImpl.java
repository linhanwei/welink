package com.daniel.weixin.mp.api;

import com.daniel.weixin.common.model.response.WxMediaUploadResult;
import com.daniel.weixin.common.session.StandardSessionManager;
import com.daniel.weixin.common.session.WxSessionManager;
import com.daniel.weixin.common.util.RandomUtils;
import com.daniel.weixin.common.util.StringUtils;
import com.daniel.weixin.common.util.crypto.SHA1;
import com.daniel.weixin.common.util.fileUtil.FileUtils;
import com.daniel.weixin.common.util.http.MediaDownloadRequestExecutor;
import com.daniel.weixin.common.util.http.MediaUploadRequestExecutor;
import com.daniel.weixin.common.util.http.URIUtil;
import com.daniel.weixin.common.util.json.GsonHelper;
import com.daniel.weixin.mp.bean.WxMpCustomMessage;
import com.daniel.weixin.mp.bean.WxMpMassGroupMessage;
import com.daniel.weixin.mp.bean.WxMpMassNews;
import com.daniel.weixin.mp.bean.WxMpMassOpenIdsMessage;
import com.daniel.weixin.mp.bean.WxMpMassVideo;
import com.daniel.weixin.mp.bean.WxMpSemanticQuery;
import com.daniel.weixin.mp.bean.WxMpTemplateMessage;
import com.daniel.weixin.mp.bean.result.WxMpMassSendResult;
import com.daniel.weixin.mp.bean.result.WxMpMassUploadResult;
import com.daniel.weixin.mp.bean.result.WxMpSemanticQueryResult;
import com.daniel.weixin.mp.bean.result.WxMpUserCumulate;
import com.daniel.weixin.mp.bean.result.WxMpUserList;
import com.daniel.weixin.mp.bean.result.WxMpUserSummary;
import com.daniel.weixin.mp.util.http.QrCodeRequestExecutor;
import com.daniel.weixin.mp.util.json.WxMpGsonBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.UUID;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.LoggerFactory;

public class WxMpServiceImpl implements WxMpService
{
  protected final org.slf4j.Logger log = LoggerFactory.getLogger(WxMpServiceImpl.class);
  
  protected final Object globalAccessTokenRefreshLock = new Object();
  
  protected final Object globalJsapiTicketRefreshLock = new Object();
  
  protected WxMpConfigStorage wxMpConfigStorage;
  
  protected org.apache.http.impl.client.CloseableHttpClient httpClient;
  
  protected HttpHost httpProxy;
  
  private int retrySleepMillis = 1000;
  
  private int maxRetryTimes = 5;
  
  protected WxSessionManager sessionManager = new StandardSessionManager();
  
  public boolean checkSignature(String timestamp, String nonce, String signature) {
    try {
      return SHA1.gen(new String[] { this.wxMpConfigStorage.getToken(), timestamp, nonce }).equals(signature);
    } catch (Exception e) {}
    return false;
  }
  
  public boolean checkSignature(String timestamp, String nonce, String signature, String mpTag)
  {
    try
    {
      return SHA1.gen(new String[] { this.wxMpConfigStorage.getToken(mpTag), timestamp, nonce }).equals(signature);
    } catch (Exception e) {}
    return false;
  }
  
  public String getAccessToken() throws com.daniel.weixin.common.exception.WxErrorException
  {
    return getAccessToken(false);
  }
  
  public String getAccessToken(String mpTag) throws com.daniel.weixin.common.exception.WxErrorException
  {
    return getAccessToken(false, mpTag);
  }
  
  public String getAccessToken(boolean forceRefresh) throws com.daniel.weixin.common.exception.WxErrorException {
    if (forceRefresh) {
      this.wxMpConfigStorage.expireAccessToken();
    }
    if (this.wxMpConfigStorage.isAccessTokenExpired()) {
      synchronized (this.globalAccessTokenRefreshLock) {
        if (this.wxMpConfigStorage.isAccessTokenExpired())
        {
          String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + this.wxMpConfigStorage.getAppId() + "&secret=" + this.wxMpConfigStorage.getSecret();
          
          try
          {
            org.apache.http.client.methods.HttpGet httpGet = new org.apache.http.client.methods.HttpGet(url);
            if (this.httpProxy != null) {
              RequestConfig config = RequestConfig.custom().setProxy(this.httpProxy).build();
              httpGet.setConfig(config);
            }
            org.apache.http.impl.client.CloseableHttpClient httpclient = getHttpclient();
            CloseableHttpResponse response = httpclient.execute(httpGet);
            String resultContent = new BasicResponseHandler().handleResponse(response);
            com.daniel.weixin.common.model.response.error.WxError error = com.daniel.weixin.common.model.response.error.WxError.fromJson(resultContent);
            if (error.getErrorCode() != 0) {
              throw new com.daniel.weixin.common.exception.WxErrorException(error);
            }
            com.daniel.weixin.common.model.response.WxAccessToken accessToken = com.daniel.weixin.common.model.response.WxAccessToken.fromJson(resultContent);
            this.wxMpConfigStorage.updateAccessToken(accessToken.getAccessToken(), accessToken.getExpiresIn());
          } catch (org.apache.http.client.ClientProtocolException e) {
            this.log.error("==========appid:" + this.wxMpConfigStorage.getAppId());
            this.log.error("==========secret:" + this.wxMpConfigStorage.getSecret());
            throw new RuntimeException(e);
          } catch (java.io.IOException e) {
            this.log.error("==========appid:" + this.wxMpConfigStorage.getAppId());
            this.log.error("==========secret:" + this.wxMpConfigStorage.getSecret());
            throw new RuntimeException(e);
          }
        }
      }
    }
    return this.wxMpConfigStorage.getAccessToken();
  }
  
  public String getAccessToken(boolean forceRefresh, String mpTag) throws com.daniel.weixin.common.exception.WxErrorException
  {
    if (forceRefresh) {
      this.wxMpConfigStorage.expireAccessToken(mpTag);
    }
    if (this.wxMpConfigStorage.isAccessTokenExpired(mpTag)) {
      synchronized (this.globalAccessTokenRefreshLock) {
        if (this.wxMpConfigStorage.isAccessTokenExpired(mpTag)) {
          String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + this.wxMpConfigStorage.getAppId(mpTag) + "&secret=" + this.wxMpConfigStorage.getSecret(mpTag);
          
          try
          {
            org.apache.http.client.methods.HttpGet httpGet = new org.apache.http.client.methods.HttpGet(url);
            if (this.httpProxy != null) {
              RequestConfig config = RequestConfig.custom().setProxy(this.httpProxy).build();
              httpGet.setConfig(config);
            }
            org.apache.http.impl.client.CloseableHttpClient httpclient = getHttpclient();
            CloseableHttpResponse response = httpclient.execute(httpGet);
            String resultContent = new BasicResponseHandler().handleResponse(response);
            com.daniel.weixin.common.model.response.error.WxError error = com.daniel.weixin.common.model.response.error.WxError.fromJson(resultContent);
            if (error.getErrorCode() != 0) {
              throw new com.daniel.weixin.common.exception.WxErrorException(error);
            }
            com.daniel.weixin.common.model.response.WxAccessToken accessToken = com.daniel.weixin.common.model.response.WxAccessToken.fromJson(resultContent);
            this.wxMpConfigStorage.updateAccessToken(accessToken.getAccessToken(), accessToken.getExpiresIn(), mpTag);
          } catch (org.apache.http.client.ClientProtocolException e) {
            throw new RuntimeException(e);
          } catch (java.io.IOException e) {
            throw new RuntimeException(e);
          }
        }
      }
    }
    return this.wxMpConfigStorage.getAccessToken(mpTag);
  }
  
  public String getJsapiTicket() throws com.daniel.weixin.common.exception.WxErrorException {
    return getJsapiTicket(false);
  }
  
  public String getJsapiTicket(String mpTag) throws com.daniel.weixin.common.exception.WxErrorException
  {
    return getJsapiTicket(false, mpTag);
  }
  
  public String getJsapiTicket(boolean forceRefresh) throws com.daniel.weixin.common.exception.WxErrorException {
    if (forceRefresh) {
      this.wxMpConfigStorage.expireJsapiTicket();
    }
    if (this.wxMpConfigStorage.isJsapiTicketExpired()) {
      synchronized (this.globalJsapiTicketRefreshLock) {
        if (this.wxMpConfigStorage.isJsapiTicketExpired()) {
          String url = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?type=jsapi";
          String responseContent = (String)execute(new com.daniel.weixin.common.util.http.SimpleGetRequestExecutor(), url, null);
          com.google.gson.JsonElement tmpJsonElement = com.google.gson.internal.Streams.parse(new com.google.gson.stream.JsonReader(new java.io.StringReader(responseContent)));
          com.google.gson.JsonObject tmpJsonObject = tmpJsonElement.getAsJsonObject();
          String jsapiTicket = tmpJsonObject.get("ticket").getAsString();
          int expiresInSeconds = tmpJsonObject.get("expires_in").getAsInt();
          this.wxMpConfigStorage.updateJsapiTicket(jsapiTicket, expiresInSeconds);
        }
      }
    }
    return this.wxMpConfigStorage.getJsapiTicket();
  }
  
  public String getJsapiTicket(boolean forceRefresh, String mpTag) throws com.daniel.weixin.common.exception.WxErrorException
  {
    if (forceRefresh) {
      this.wxMpConfigStorage.expireJsapiTicket(mpTag);
    }
    if (this.wxMpConfigStorage.isJsapiTicketExpired(mpTag)) {
      synchronized (this.globalJsapiTicketRefreshLock) {
        if (this.wxMpConfigStorage.isJsapiTicketExpired(mpTag)) {
          String url = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?type=jsapi";
          
          String responseContent = null;
          if (StringUtils.isBlank(mpTag)) {
            responseContent = (String)execute(new com.daniel.weixin.common.util.http.SimpleGetRequestExecutor(), url, null, mpTag);
          } else {
            responseContent = (String)execute(new com.daniel.weixin.common.util.http.SimpleGetRequestExecutor(), url, null);
          }
          com.google.gson.JsonElement tmpJsonElement = com.google.gson.internal.Streams.parse(new com.google.gson.stream.JsonReader(new java.io.StringReader(responseContent)));
          com.google.gson.JsonObject tmpJsonObject = tmpJsonElement.getAsJsonObject();
          String jsapiTicket = tmpJsonObject.get("ticket").getAsString();
          int expiresInSeconds = tmpJsonObject.get("expires_in").getAsInt();
          this.wxMpConfigStorage.updateJsapiTicket(jsapiTicket, expiresInSeconds, mpTag);
        }
      }
    }
    return this.wxMpConfigStorage.getJsapiTicket(mpTag);
  }
  
  public com.daniel.weixin.common.model.response.WxJsapiSignature createJsapiSignature(String url) throws com.daniel.weixin.common.exception.WxErrorException {
    long timestamp = System.currentTimeMillis() / 1000L;
    String noncestr = RandomUtils.getRandomStr();
    String jsapiTicket = getJsapiTicket(false);
    try {
      String signature = SHA1.genWithAmple(new String[] { "jsapi_ticket=" + jsapiTicket, "noncestr=" + noncestr, "timestamp=" + timestamp, "url=" + url });
      
      com.daniel.weixin.common.model.response.WxJsapiSignature jsapiSignature = new com.daniel.weixin.common.model.response.WxJsapiSignature();
      jsapiSignature.setTimestamp(timestamp);
      jsapiSignature.setNoncestr(noncestr);
      jsapiSignature.setUrl(url);
      jsapiSignature.setSignature(signature);
      return jsapiSignature;
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }
  
  public void customMessageSend(WxMpCustomMessage message) throws com.daniel.weixin.common.exception.WxErrorException {
    String url = "https://api.weixin.qq.com/cgi-bin/message/custom/send";
    execute(new com.daniel.weixin.common.util.http.SimplePostRequestExecutor(), url, message.toJson());
  }
  
  public void menuCreate(com.daniel.weixin.common.model.response.WxMenu menu) throws com.daniel.weixin.common.exception.WxErrorException {
    String url = "https://api.weixin.qq.com/cgi-bin/menu/create";
    execute(new com.daniel.weixin.common.util.http.SimplePostRequestExecutor(), url, menu.toJson());
  }
  
  public void menuDelete() throws com.daniel.weixin.common.exception.WxErrorException {
    String url = "https://api.weixin.qq.com/cgi-bin/menu/delete";
    execute(new com.daniel.weixin.common.util.http.SimpleGetRequestExecutor(), url, null);
  }
  
  public com.daniel.weixin.common.model.response.WxMenu menuGet() throws com.daniel.weixin.common.exception.WxErrorException {
    String url = "https://api.weixin.qq.com/cgi-bin/menu/get";
    try {
      String resultContent = (String)execute(new com.daniel.weixin.common.util.http.SimpleGetRequestExecutor(), url, null);
      return com.daniel.weixin.common.model.response.WxMenu.fromJson(resultContent);
    }
    catch (com.daniel.weixin.common.exception.WxErrorException e) {
      if (e.getError().getErrorCode() == 46003) {
        return null;
      }
      throw e;
    }
  }
  
  public WxMediaUploadResult mediaUpload(String mediaType, String fileType, InputStream inputStream) throws com.daniel.weixin.common.exception.WxErrorException, java.io.IOException {
    return mediaUpload(mediaType, FileUtils.createTmpFile(inputStream, UUID.randomUUID().toString(), fileType));
  }
  
  public WxMediaUploadResult mediaUpload(String mediaType, java.io.File file) throws com.daniel.weixin.common.exception.WxErrorException {
    String url = "http://file.api.weixin.qq.com/cgi-bin/media/upload?type=" + mediaType;
    return (WxMediaUploadResult)execute(new MediaUploadRequestExecutor(), url, file);
  }
  
  public java.io.File mediaDownload(String media_id) throws com.daniel.weixin.common.exception.WxErrorException {
    String url = "http://file.api.weixin.qq.com/cgi-bin/media/get";
    return (java.io.File)execute(new MediaDownloadRequestExecutor(), url, "media_id=" + media_id);
  }
  
  public WxMpMassUploadResult massNewsUpload(WxMpMassNews news) throws com.daniel.weixin.common.exception.WxErrorException {
    String url = "https://api.weixin.qq.com/cgi-bin/media/uploadnews";
    String responseContent = (String)execute(new com.daniel.weixin.common.util.http.SimplePostRequestExecutor(), url, news.toJson());
    return WxMpMassUploadResult.fromJson(responseContent);
  }
  
  public WxMpMassUploadResult massVideoUpload(WxMpMassVideo video) throws com.daniel.weixin.common.exception.WxErrorException {
    String url = "http://file.api.weixin.qq.com/cgi-bin/media/uploadvideo";
    String responseContent = (String)execute(new com.daniel.weixin.common.util.http.SimplePostRequestExecutor(), url, video.toJson());
    return WxMpMassUploadResult.fromJson(responseContent);
  }
  
  public WxMpMassSendResult massGroupMessageSend(WxMpMassGroupMessage message) throws com.daniel.weixin.common.exception.WxErrorException {
    String url = "https://api.weixin.qq.com/cgi-bin/message/mass/sendall";
    String responseContent = (String)execute(new com.daniel.weixin.common.util.http.SimplePostRequestExecutor(), url, message.toJson());
    return WxMpMassSendResult.fromJson(responseContent);
  }
  
  public WxMpMassSendResult massOpenIdsMessageSend(WxMpMassOpenIdsMessage message) throws com.daniel.weixin.common.exception.WxErrorException {
    String url = "https://api.weixin.qq.com/cgi-bin/message/mass/send";
    String responseContent = (String)execute(new com.daniel.weixin.common.util.http.SimplePostRequestExecutor(), url, message.toJson());
    return WxMpMassSendResult.fromJson(responseContent);
  }
  
  public com.daniel.weixin.mp.bean.WxMpGroup groupCreate(String name) throws com.daniel.weixin.common.exception.WxErrorException {
    String url = "https://api.weixin.qq.com/cgi-bin/groups/create";
    com.google.gson.JsonObject json = new com.google.gson.JsonObject();
    com.google.gson.JsonObject groupJson = new com.google.gson.JsonObject();
    json.add("group", groupJson);
    groupJson.addProperty("name", name);
    
    String responseContent = (String)execute(new com.daniel.weixin.common.util.http.SimplePostRequestExecutor(), url, json.toString());
    
    return com.daniel.weixin.mp.bean.WxMpGroup.fromJson(responseContent);
  }
  
  public java.util.List<com.daniel.weixin.mp.bean.WxMpGroup> groupGet() throws com.daniel.weixin.common.exception.WxErrorException {
    String url = "https://api.weixin.qq.com/cgi-bin/groups/get";
    String responseContent = (String)execute(new com.daniel.weixin.common.util.http.SimpleGetRequestExecutor(), url, null);
    
    com.google.gson.JsonElement tmpJsonElement = com.google.gson.internal.Streams.parse(new com.google.gson.stream.JsonReader(new java.io.StringReader(responseContent)));
    return (java.util.List)WxMpGsonBuilder.INSTANCE.create().fromJson(tmpJsonElement.getAsJsonObject().get("groups"), new TypeToken() {}.getType());
  }
  
  public long userGetGroup(String openid) throws com.daniel.weixin.common.exception.WxErrorException
  {
    String url = "https://api.weixin.qq.com/cgi-bin/groups/getid";
    com.google.gson.JsonObject o = new com.google.gson.JsonObject();
    o.addProperty("openid", openid);
    String responseContent = (String)execute(new com.daniel.weixin.common.util.http.SimplePostRequestExecutor(), url, o.toString());
    com.google.gson.JsonElement tmpJsonElement = com.google.gson.internal.Streams.parse(new com.google.gson.stream.JsonReader(new java.io.StringReader(responseContent)));
    return GsonHelper.getAsLong(tmpJsonElement.getAsJsonObject().get("groupid")).longValue();
  }
  
  public void groupUpdate(com.daniel.weixin.mp.bean.WxMpGroup group) throws com.daniel.weixin.common.exception.WxErrorException {
    String url = "https://api.weixin.qq.com/cgi-bin/groups/update";
    execute(new com.daniel.weixin.common.util.http.SimplePostRequestExecutor(), url, group.toJson());
  }
  
  public void userUpdateGroup(String openid, long to_groupid) throws com.daniel.weixin.common.exception.WxErrorException {
    String url = "https://api.weixin.qq.com/cgi-bin/groups/members/update";
    com.google.gson.JsonObject json = new com.google.gson.JsonObject();
    json.addProperty("openid", openid);
    json.addProperty("to_groupid", Long.valueOf(to_groupid));
    execute(new com.daniel.weixin.common.util.http.SimplePostRequestExecutor(), url, json.toString());
  }
  
  public void userUpdateRemark(String openid, String remark) throws com.daniel.weixin.common.exception.WxErrorException {
    String url = "https://api.weixin.qq.com/cgi-bin/user/info/updateremark";
    com.google.gson.JsonObject json = new com.google.gson.JsonObject();
    json.addProperty("openid", openid);
    json.addProperty("remark", remark);
    execute(new com.daniel.weixin.common.util.http.SimplePostRequestExecutor(), url, json.toString());
  }
  
  public com.daniel.weixin.mp.bean.result.WxMpUser userInfo(String openid, String lang) throws com.daniel.weixin.common.exception.WxErrorException {
    String url = "https://api.weixin.qq.com/cgi-bin/user/info";
    lang = lang == null ? "zh_CN" : lang;
    String responseContent = (String)execute(new com.daniel.weixin.common.util.http.SimpleGetRequestExecutor(), url, "openid=" + openid + "&lang=" + lang);
    return com.daniel.weixin.mp.bean.result.WxMpUser.fromJson(responseContent);
  }
  
  public com.daniel.weixin.mp.bean.result.WxMpUser userInfo(String openid, String lang, String mpTag) throws com.daniel.weixin.common.exception.WxErrorException
  {
    String url = "https://api.weixin.qq.com/cgi-bin/user/info";
    lang = lang == null ? "zh_CN" : lang;
    String responseContent = (String)execute(new com.daniel.weixin.common.util.http.SimpleGetRequestExecutor(), url, "openid=" + openid + "&lang=" + lang, mpTag);
    return com.daniel.weixin.mp.bean.result.WxMpUser.fromJson(responseContent);
  }
  
  public WxMpUserList userList(String next_openid) throws com.daniel.weixin.common.exception.WxErrorException {
    String url = "https://api.weixin.qq.com/cgi-bin/user/get";
    String responseContent = (String)execute(new com.daniel.weixin.common.util.http.SimpleGetRequestExecutor(), url, "next_openid=" + next_openid);
    return WxMpUserList.fromJson(responseContent);
  }
  
  public com.daniel.weixin.mp.bean.result.WxMpQrCodeTicket qrCodeCreateTmpTicket(int scene_id, Integer expire_seconds) throws com.daniel.weixin.common.exception.WxErrorException {
    String url = "https://api.weixin.qq.com/cgi-bin/qrcode/create";
    com.google.gson.JsonObject json = new com.google.gson.JsonObject();
    json.addProperty("action_name", "QR_SCENE");
    if (expire_seconds != null) {
      json.addProperty("expire_seconds", expire_seconds);
    }
    com.google.gson.JsonObject actionInfo = new com.google.gson.JsonObject();
    com.google.gson.JsonObject scene = new com.google.gson.JsonObject();
    scene.addProperty("scene_id", Integer.valueOf(scene_id));
    actionInfo.add("scene", scene);
    json.add("action_info", actionInfo);
    String responseContent = (String)execute(new com.daniel.weixin.common.util.http.SimplePostRequestExecutor(), url, json.toString());
    return com.daniel.weixin.mp.bean.result.WxMpQrCodeTicket.fromJson(responseContent);
  }
  
  public com.daniel.weixin.mp.bean.result.WxMpQrCodeTicket qrCodeCreateLastTicket(int scene_id) throws com.daniel.weixin.common.exception.WxErrorException {
    String url = "https://api.weixin.qq.com/cgi-bin/qrcode/create";
    com.google.gson.JsonObject json = new com.google.gson.JsonObject();
    json.addProperty("action_name", "QR_LIMIT_SCENE");
    com.google.gson.JsonObject actionInfo = new com.google.gson.JsonObject();
    com.google.gson.JsonObject scene = new com.google.gson.JsonObject();
    scene.addProperty("scene_id", Integer.valueOf(scene_id));
    actionInfo.add("scene", scene);
    json.add("action_info", actionInfo);
    String responseContent = (String)execute(new com.daniel.weixin.common.util.http.SimplePostRequestExecutor(), url, json.toString());
    return com.daniel.weixin.mp.bean.result.WxMpQrCodeTicket.fromJson(responseContent);
  }
  
  public java.io.File qrCodePicture(com.daniel.weixin.mp.bean.result.WxMpQrCodeTicket ticket) throws com.daniel.weixin.common.exception.WxErrorException {
    String url = "https://mp.weixin.qq.com/cgi-bin/showqrcode";
    return (java.io.File)execute(new QrCodeRequestExecutor(), url, ticket);
  }
  
  public String shortUrl(String long_url) throws com.daniel.weixin.common.exception.WxErrorException {
    String url = "https://api.weixin.qq.com/cgi-bin/shorturl";
    com.google.gson.JsonObject o = new com.google.gson.JsonObject();
    o.addProperty("action", "long2short");
    o.addProperty("long_url", long_url);
    String responseContent = (String)execute(new com.daniel.weixin.common.util.http.SimplePostRequestExecutor(), url, o.toString());
    com.google.gson.JsonElement tmpJsonElement = com.google.gson.internal.Streams.parse(new com.google.gson.stream.JsonReader(new java.io.StringReader(responseContent)));
    return tmpJsonElement.getAsJsonObject().get("short_url").getAsString();
  }
  
  public String templateSend(WxMpTemplateMessage templateMessage) throws com.daniel.weixin.common.exception.WxErrorException {
    String url = "https://api.weixin.qq.com/cgi-bin/message/template/send";
    String responseContent = (String)execute(new com.daniel.weixin.common.util.http.SimplePostRequestExecutor(), url, templateMessage.toJson());
    com.google.gson.JsonElement tmpJsonElement = com.google.gson.internal.Streams.parse(new com.google.gson.stream.JsonReader(new java.io.StringReader(responseContent)));
    return tmpJsonElement.getAsJsonObject().get("msgid").getAsString();
  }
  
  public WxMpSemanticQueryResult semanticQuery(WxMpSemanticQuery semanticQuery) throws com.daniel.weixin.common.exception.WxErrorException {
    String url = "https://api.weixin.qq.com/semantic/semproxy/search";
    String responseContent = (String)execute(new com.daniel.weixin.common.util.http.SimplePostRequestExecutor(), url, semanticQuery.toJson());
    return WxMpSemanticQueryResult.fromJson(responseContent);
  }
  
  public String oauth2buildAuthorizationUrl(String scope, String state)
  {
    String url = "https://open.weixin.qq.com/connect/oauth2/authorize?";
    url = url + "appid=" + this.wxMpConfigStorage.getAppId();
    url = url + "&redirect_uri=" + URIUtil.encodeURIComponent(this.wxMpConfigStorage.getOauth2redirectUri());
    url = url + "&response_type=code";
    url = url + "&scope=" + scope;
    if (state != null) {
      url = url + "&state=" + state;
    }
    url = url + "#wechat_redirect";
    return url;
  }
  
  public com.daniel.weixin.mp.bean.result.WxMpOAuth2AccessToken oauth2getAccessToken(String code) throws com.daniel.weixin.common.exception.WxErrorException
  {
    String url = "https://api.weixin.qq.com/sns/oauth2/access_token?";
    url = url + "appid=" + this.wxMpConfigStorage.getAppId();
    url = url + "&secret=" + this.wxMpConfigStorage.getSecret();
    url = url + "&code=" + code;
    url = url + "&grant_type=authorization_code";
    try
    {
      com.daniel.weixin.common.util.http.RequestExecutor<String, String> executor = new com.daniel.weixin.common.util.http.SimpleGetRequestExecutor();
      String responseText = (String)executor.execute(getHttpclient(), this.httpProxy, url, null);
      return com.daniel.weixin.mp.bean.result.WxMpOAuth2AccessToken.fromJson(responseText);
    } catch (org.apache.http.client.ClientProtocolException e) {
      throw new RuntimeException(e);
    } catch (java.io.IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  public com.daniel.weixin.mp.bean.result.WxMpOAuth2AccessToken oauth2getAccessToken(String code, String mpTag) throws com.daniel.weixin.common.exception.WxErrorException
  {
    String url = "https://api.weixin.qq.com/sns/oauth2/access_token?";
    url = url + "appid=" + this.wxMpConfigStorage.getAppId(mpTag);
    url = url + "&secret=" + this.wxMpConfigStorage.getSecret(mpTag);
    url = url + "&code=" + code;
    url = url + "&grant_type=authorization_code";
    System.out.println("-------------------oauth2getAccessToken-------------------------------------------------------------");
    System.out.println("....AppId."+this.wxMpConfigStorage.getAppId(mpTag)+"....Secret."+this.wxMpConfigStorage.getSecret(mpTag));
    try
    {
      com.daniel.weixin.common.util.http.RequestExecutor<String, String> executor = new com.daniel.weixin.common.util.http.SimpleGetRequestExecutor();
      String responseText = (String)executor.execute(getHttpclient(), this.httpProxy, url, null, mpTag);
      return com.daniel.weixin.mp.bean.result.WxMpOAuth2AccessToken.fromJson(responseText);
    } catch (org.apache.http.client.ClientProtocolException e) {
      throw new RuntimeException(e);
    } catch (java.io.IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  public com.daniel.weixin.mp.bean.result.WxMpOAuth2AccessToken oauth2refreshAccessToken(String refreshToken) throws com.daniel.weixin.common.exception.WxErrorException
  {
    String url = "https://api.weixin.qq.com/sns/oauth2/refresh_token?";
    url = url + "appid=" + this.wxMpConfigStorage.getAppId();
    url = url + "&grant_type=refresh_token";
    url = url + "&refresh_token=" + refreshToken;
    try
    {
      com.daniel.weixin.common.util.http.RequestExecutor<String, String> executor = new com.daniel.weixin.common.util.http.SimpleGetRequestExecutor();
      String responseText = (String)executor.execute(getHttpclient(), this.httpProxy, url, null);
      return com.daniel.weixin.mp.bean.result.WxMpOAuth2AccessToken.fromJson(responseText);
    } catch (org.apache.http.client.ClientProtocolException e) {
      throw new RuntimeException(e);
    } catch (java.io.IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  public com.daniel.weixin.mp.bean.result.WxMpOAuth2AccessToken oauth2refreshAccessToken(String refreshToken, String mpTag) throws com.daniel.weixin.common.exception.WxErrorException
  {
    String url = "https://api.weixin.qq.com/sns/oauth2/refresh_token?";
    url = url + "appid=" + this.wxMpConfigStorage.getAppId(mpTag);
    url = url + "&grant_type=refresh_token";
    url = url + "&refresh_token=" + refreshToken;
    try
    {
      com.daniel.weixin.common.util.http.RequestExecutor<String, String> executor = new com.daniel.weixin.common.util.http.SimpleGetRequestExecutor();
      String responseText = (String)executor.execute(getHttpclient(), this.httpProxy, url, null, mpTag);
      return com.daniel.weixin.mp.bean.result.WxMpOAuth2AccessToken.fromJson(responseText);
    } catch (org.apache.http.client.ClientProtocolException e) {
      throw new RuntimeException(e);
    } catch (java.io.IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  public com.daniel.weixin.mp.bean.result.WxMpUser oauth2getUserInfo(com.daniel.weixin.mp.bean.result.WxMpOAuth2AccessToken oAuth2AccessToken, String lang) throws com.daniel.weixin.common.exception.WxErrorException
  {
    String url = "https://api.weixin.qq.com/sns/userinfo?";
    url = url + "access_token=" + oAuth2AccessToken.getAccessToken();
    url = url + "&openid=" + oAuth2AccessToken.getOpenId();
    if (lang == null) {
      url = url + "&lang=zh_CN";
    } else {
      url = url + "&lang=" + lang;
    }
    try
    {
      com.daniel.weixin.common.util.http.RequestExecutor<String, String> executor = new com.daniel.weixin.common.util.http.SimpleGetRequestExecutor();
      String responseText = (String)executor.execute(getHttpclient(), this.httpProxy, url, null);
      return com.daniel.weixin.mp.bean.result.WxMpUser.fromJson(responseText);
    } catch (org.apache.http.client.ClientProtocolException e) {
      throw new RuntimeException(e);
    } catch (java.io.IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  public com.daniel.weixin.mp.bean.result.WxMpUser oauth2getUserInfo(com.daniel.weixin.mp.bean.result.WxMpOAuth2AccessToken oAuth2AccessToken, String lang, String mpTag) throws com.daniel.weixin.common.exception.WxErrorException
  {
    String url = "https://api.weixin.qq.com/sns/userinfo?";
    url = url + "access_token=" + oAuth2AccessToken.getAccessToken();
    url = url + "&openid=" + oAuth2AccessToken.getOpenId();
    if (lang == null) {
      url = url + "&lang=zh_CN";
    } else {
      url = url + "&lang=" + lang;
    }
    try
    {
      com.daniel.weixin.common.util.http.RequestExecutor<String, String> executor = new com.daniel.weixin.common.util.http.SimpleGetRequestExecutor();
      String responseText = (String)executor.execute(getHttpclient(), this.httpProxy, url, null, mpTag);
      return com.daniel.weixin.mp.bean.result.WxMpUser.fromJson(responseText);
    } catch (org.apache.http.client.ClientProtocolException e) {
      throw new RuntimeException(e);
    } catch (java.io.IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  public boolean oauth2validateAccessToken(com.daniel.weixin.mp.bean.result.WxMpOAuth2AccessToken oAuth2AccessToken)
  {
    String url = "https://api.weixin.qq.com/sns/auth?";
    url = url + "access_token=" + oAuth2AccessToken.getAccessToken();
    url = url + "&openid=" + oAuth2AccessToken.getOpenId();
    try
    {
      com.daniel.weixin.common.util.http.RequestExecutor<String, String> executor = new com.daniel.weixin.common.util.http.SimpleGetRequestExecutor();
      executor.execute(getHttpclient(), this.httpProxy, url, null);
    } catch (org.apache.http.client.ClientProtocolException e) {
      throw new RuntimeException(e);
    } catch (java.io.IOException e) {
      throw new RuntimeException(e);
    } catch (com.daniel.weixin.common.exception.WxErrorException e) {
      return false;
    }
    return true;
  }
  
  public boolean oauth2validateAccessToken(com.daniel.weixin.mp.bean.result.WxMpOAuth2AccessToken oAuth2AccessToken, String mpTag)
  {
    String url = "https://api.weixin.qq.com/sns/auth?";
    url = url + "access_token=" + oAuth2AccessToken.getAccessToken();
    url = url + "&openid=" + oAuth2AccessToken.getOpenId();
    try
    {
      com.daniel.weixin.common.util.http.RequestExecutor<String, String> executor = new com.daniel.weixin.common.util.http.SimpleGetRequestExecutor();
      executor.execute(getHttpclient(), this.httpProxy, url, null, mpTag);
    } catch (org.apache.http.client.ClientProtocolException e) {
      throw new RuntimeException(e);
    } catch (java.io.IOException e) {
      throw new RuntimeException(e);
    } catch (com.daniel.weixin.common.exception.WxErrorException e) {
      return false;
    }
    return true;
  }
  
  public String[] getCallbackIP() throws com.daniel.weixin.common.exception.WxErrorException
  {
    String url = "https://api.weixin.qq.com/cgi-bin/getcallbackip";
    String responseContent = get(url, null);
    com.google.gson.JsonElement tmpJsonElement = com.google.gson.internal.Streams.parse(new com.google.gson.stream.JsonReader(new java.io.StringReader(responseContent)));
    JsonArray ipList = tmpJsonElement.getAsJsonObject().get("ip_list").getAsJsonArray();
    String[] ipArray = new String[ipList.size()];
    for (int i = 0; i < ipList.size(); i++) {
      ipArray[i] = ipList.get(i).getAsString();
    }
    return ipArray;
  }
  
  public java.util.List<WxMpUserSummary> getUserSummary(java.util.Date beginDate, java.util.Date endDate)
    throws com.daniel.weixin.common.exception.WxErrorException
  {
    String url = "https://api.weixin.qq.com/datacube/getusersummary";
    com.google.gson.JsonObject param = new com.google.gson.JsonObject();
    param.addProperty("begin_date", SIMPLE_DATE_FORMAT.format(beginDate));
    param.addProperty("end_date", SIMPLE_DATE_FORMAT.format(endDate));
    String responseContent = post(url, param.toString());
    com.google.gson.JsonElement tmpJsonElement = com.google.gson.internal.Streams.parse(new com.google.gson.stream.JsonReader(new java.io.StringReader(responseContent)));
    return (java.util.List)WxMpGsonBuilder.INSTANCE.create().fromJson(tmpJsonElement.getAsJsonObject().get("list"), new TypeToken() {}.getType());
  }
  
  public java.util.List<WxMpUserCumulate> getUserCumulate(java.util.Date beginDate, java.util.Date endDate)
    throws com.daniel.weixin.common.exception.WxErrorException
  {
    String url = "https://api.weixin.qq.com/datacube/getusercumulate";
    com.google.gson.JsonObject param = new com.google.gson.JsonObject();
    param.addProperty("begin_date", SIMPLE_DATE_FORMAT.format(beginDate));
    param.addProperty("end_date", SIMPLE_DATE_FORMAT.format(endDate));
    String responseContent = post(url, param.toString());
    com.google.gson.JsonElement tmpJsonElement = com.google.gson.internal.Streams.parse(new com.google.gson.stream.JsonReader(new java.io.StringReader(responseContent)));
    return (java.util.List)WxMpGsonBuilder.INSTANCE.create().fromJson(tmpJsonElement.getAsJsonObject().get("list"), new TypeToken() {}.getType());
  }
  
  public String get(String url, String queryParam) throws com.daniel.weixin.common.exception.WxErrorException
  {
    return (String)execute(new com.daniel.weixin.common.util.http.SimpleGetRequestExecutor(), url, queryParam);
  }
  
  public String post(String url, String postData) throws com.daniel.weixin.common.exception.WxErrorException {
    return (String)execute(new com.daniel.weixin.common.util.http.SimplePostRequestExecutor(), url, postData);
  }
  
  public <T, E> T execute(com.daniel.weixin.common.util.http.RequestExecutor<T, E> executor, String uri, E data) throws com.daniel.weixin.common.exception.WxErrorException
  {
    int retryTimes = 0;
    do {
      try {
        return executeInternal(executor, uri, data);
      } catch (com.daniel.weixin.common.exception.WxErrorException e) {
        com.daniel.weixin.common.model.response.error.WxError error = e.getError();
        
        if (error.getErrorCode() == -1) {
          int sleepMillis = this.retrySleepMillis * (1 << retryTimes);
          try {
            this.log.debug("微信系统繁忙，{}ms 后重试(第{}次)", Integer.valueOf(sleepMillis), Integer.valueOf(retryTimes + 1));
            Thread.sleep(sleepMillis);
          } catch (InterruptedException e1) {
            throw new RuntimeException(e1);
          }
        } else {
          throw e;
        }
      }
      retryTimes++; } while (retryTimes < this.maxRetryTimes);
    
    throw new RuntimeException("微信服务端异常，超出重试次数");
  }
  
  public <T, E> T execute(com.daniel.weixin.common.util.http.RequestExecutor<T, E> executor, String uri, E data, String mpTag)
    throws com.daniel.weixin.common.exception.WxErrorException
  {
    int retryTimes = 0;
    do {
      try {
        return executeInternal(executor, uri, data, mpTag);
      } catch (com.daniel.weixin.common.exception.WxErrorException e) {
        com.daniel.weixin.common.model.response.error.WxError error = e.getError();
        
        if (error.getErrorCode() == -1) {
          int sleepMillis = this.retrySleepMillis * (1 << retryTimes);
          try {
            this.log.debug("微信系统繁忙，{}ms 后重试(第{}次)", Integer.valueOf(sleepMillis), Integer.valueOf(retryTimes + 1));
            Thread.sleep(sleepMillis);
          } catch (InterruptedException e1) {
            throw new RuntimeException(e1);
          }
        } else {
          throw e;
        }
      }
      retryTimes++; } while (retryTimes < this.maxRetryTimes);
    
    throw new RuntimeException("微信服务端异常，超出重试次数");
  }
  
  protected <T, E> T executeInternal(com.daniel.weixin.common.util.http.RequestExecutor<T, E> executor, String uri, E data, String mpTag) throws com.daniel.weixin.common.exception.WxErrorException {
    String accessToken = getAccessToken(false, mpTag);
    
    String uriWithAccessToken = uri;
    uriWithAccessToken = uriWithAccessToken + (uri.indexOf('?') == -1 ? "?access_token=" + accessToken : new StringBuilder().append("&access_token=").append(accessToken).toString());
    try
    {
      return executor.execute(getHttpclient(), this.httpProxy, uriWithAccessToken, data, mpTag);
    } catch (com.daniel.weixin.common.exception.WxErrorException e) {
      com.daniel.weixin.common.model.response.error.WxError error = e.getError();
      
      if ((error.getErrorCode() == 42001) || (error.getErrorCode() == 40001))
      {
        if (StringUtils.isNotBlank(mpTag)) {
          this.wxMpConfigStorage.expireAccessToken(mpTag);
        } else {
          this.wxMpConfigStorage.expireAccessToken();
        }
        return execute(executor, uri, data, mpTag);
      }
      if (error.getErrorCode() != 0) {
        throw new com.daniel.weixin.common.exception.WxErrorException(error);
      }
      return null;
    } catch (org.apache.http.client.ClientProtocolException e) {
      throw new RuntimeException(e);
    } catch (java.io.IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  protected <T, E> T executeInternal(com.daniel.weixin.common.util.http.RequestExecutor<T, E> executor, String uri, E data) throws com.daniel.weixin.common.exception.WxErrorException {
    String accessToken = getAccessToken(false);
    
    String uriWithAccessToken = uri;
    uriWithAccessToken = uriWithAccessToken + (uri.indexOf('?') == -1 ? "?access_token=" + accessToken : new StringBuilder().append("&access_token=").append(accessToken).toString());
    try
    {
      return executor.execute(getHttpclient(), this.httpProxy, uriWithAccessToken, data);
    } catch (com.daniel.weixin.common.exception.WxErrorException e) {
      com.daniel.weixin.common.model.response.error.WxError error = e.getError();
      
      if ((error.getErrorCode() == 42001) || (error.getErrorCode() == 40001))
      {
        this.wxMpConfigStorage.expireAccessToken();
        return execute(executor, uri, data);
      }
      if (error.getErrorCode() != 0) {
        throw new com.daniel.weixin.common.exception.WxErrorException(error);
      }
      return null;
    } catch (org.apache.http.client.ClientProtocolException e) {
      throw new RuntimeException(e);
    } catch (java.io.IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  protected org.apache.http.impl.client.CloseableHttpClient getHttpclient() {
    return this.httpClient;
  }
  
  public void setWxMpConfigStorage(WxMpConfigStorage wxConfigProvider) {
    this.wxMpConfigStorage = wxConfigProvider;
    
    String http_proxy_host = this.wxMpConfigStorage.getHttp_proxy_host();
    int http_proxy_port = this.wxMpConfigStorage.getHttp_proxy_port();
    String http_proxy_username = this.wxMpConfigStorage.getHttp_proxy_username();
    String http_proxy_password = this.wxMpConfigStorage.getHttp_proxy_password();
    
    if (StringUtils.isNotBlank(http_proxy_host))
    {
      if (StringUtils.isNotBlank(http_proxy_username))
      {
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(http_proxy_host, http_proxy_port), new UsernamePasswordCredentials(http_proxy_username, http_proxy_password));
        
        this.httpClient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
      }
      else
      {
        this.httpClient = HttpClients.createDefault();
      }
      this.httpProxy = new HttpHost(http_proxy_host, http_proxy_port);
    } else {
      this.httpClient = HttpClients.createDefault();
    }
  }
  
  public void setRetrySleepMillis(int retrySleepMillis)
  {
    this.retrySleepMillis = retrySleepMillis;
  }
  
  public void setMaxRetryTimes(int maxRetryTimes)
  {
    this.maxRetryTimes = maxRetryTimes;
  }
}
