package com.welink.biz.common.MSG;

import com.alibaba.fastjson.JSON;
import com.welink.biz.common.model.PushDO;
import com.welink.biz.common.model.PushNoAlert;
import com.welink.biz.common.model.PushParam;
import com.welink.buy.utils.PhenixUserHander;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Created by daniel on 14-9-28.
 */
public class PushAPI {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(PushAPI.class);

    private static String AVOS_PUSH_URL = "https://cn.avoscloud.com/1.1/push";

    private static String ENCODING = "UTF-8";

    private static final String AVOS_APP_ID = "X-AVOSCloud-Application-Id";

    private static final String AVOS_APP_ID_VALUE = "spqye2ccqzkaslzt56697mkvkoav4ejw2000v5o502zoo4qc";

    private static final String AVOS_APP_KEY = "X-AVOSCloud-Application-Key";

    private static final String AVOS_APP_KEY_VALUE = "oml89mgl88n9z7kdj9efdn9uf8buvykj2te1vxq19sct5ooh";


    /**
     * 推送邻里消息
     *
     * @param action
     * @param userId
     * @param content
     * @param bizType
     * @param linkMsgCnt
     * @return
     */
    public static String pushLinkMsg(String action, String userId, String content, long bizType, long linkMsgCnt) {
        DefaultHttpClient httpclient = new DefaultHttpClient();

        // 目标地址
        HttpPost httppost = new HttpPost(AVOS_PUSH_URL);// new HttpPost(AVOS_PUSH_URL);

        httppost.addHeader(AVOS_APP_ID, AVOS_APP_ID_VALUE);
        httppost.addHeader(AVOS_APP_KEY, AVOS_APP_KEY_VALUE);
        String body = null;

        // 构造最简单的字符串数据
        StringEntity reqEntity = null;

        try {
            PushParam pushParam = new PushParam();
            pushParam.setT(bizType);
            pushParam.setC(linkMsgCnt);

            PushDO pushDO = new PushDO();
            pushDO.setAlert(content);
            pushDO.setAction(action);
            pushDO.setP(pushParam);
            String s = JSON.toJSONString(pushDO);

            String data = null;//"{\"where\": {\"objectId\": \"5427a9dae4b00f18e8702106\"},\"data\": {\"alert\": \"Hello From AVOS Cloud.\"}}";
            data = "{\"where\": {\"userId\":\"" + userId + "\"},\"data\": " + s + "}";
            reqEntity = new StringEntity(data, "utf-8");
            // 设置类型
            reqEntity.setContentType("application/json");
            // 设置请求的数据
            httppost.setEntity(reqEntity);
            // 执行
            HttpResponse httpresponse = httpclient.execute(httppost);
            int status = httpresponse.getStatusLine().getStatusCode();
            HttpEntity entity = httpresponse.getEntity();
            parseErrorViaStatus(status);
            body = EntityUtils.toString(entity);
        } catch (UnsupportedEncodingException e) {
            log.error("push msg failed. exp:" + e.getMessage());
        } catch (ClientProtocolException e) {
            log.error("push msg failed. exp:" + e.getMessage());
        } catch (IOException e) {
            log.error("push msg failed. exp:" + e.getMessage());
        }
        return body;
    }


    /**
     * 推送消息给某个用户
     *
     * @param action
     * @param content
     * @param url
     * @param uId
     * @return
     */
    public static String pushMsg(String action, String content, String url, String uId, String title, long bizType) {

        DefaultHttpClient httpclient = new DefaultHttpClient();

        // 目标地址
        HttpPost httppost = new HttpPost(AVOS_PUSH_URL);// new HttpPost(AVOS_PUSH_URL);

        httppost.addHeader(AVOS_APP_ID, AVOS_APP_ID_VALUE);
        httppost.addHeader(AVOS_APP_KEY, AVOS_APP_KEY_VALUE);
        String body = null;

        // 构造最简单的字符串数据
        StringEntity reqEntity = null;

        try {
            PushParam pushParam = new PushParam();
            pushParam.setUrl(url);
            pushParam.setT(bizType);

            PushDO pushDO = new PushDO();
            pushDO.setAlert(content);
            pushDO.setAction(action);
            pushDO.setP(pushParam);
            pushDO.setTitle(title);
            String s = JSON.toJSONString(pushDO);

            String data = null;//"{\"where\": {\"objectId\": \"5427a9dae4b00f18e8702106\"},\"data\": {\"alert\": \"Hello From AVOS Cloud.\"}}";
            data = "{\"where\": {\"userId\":\"" + uId + "\"},\"data\": " + s + "}";
            reqEntity = new StringEntity(data, "utf-8");
            // 设置类型
            reqEntity.setContentType("application/json");
            // 设置请求的数据
            httppost.setEntity(reqEntity);
            // 执行
            HttpResponse httpresponse = httpclient.execute(httppost);
            int status = httpresponse.getStatusLine().getStatusCode();
            HttpEntity entity = httpresponse.getEntity();
            parseErrorViaStatus(status);
            body = EntityUtils.toString(entity);
        } catch (UnsupportedEncodingException e) {
            log.error("push msg failed. exp:" + e.getMessage());
        } catch (ClientProtocolException e) {
            log.error("push msg failed. exp:" + e.getMessage());
        } catch (IOException e) {
            log.error("push msg failed. exp:" + e.getMessage());
        }
        return body;
    }


    public static String pushMsg2CommunityMembers(String action, String content, String url, String communityId, String title, long bizType, long tradeId) {

        DefaultHttpClient httpclient = new DefaultHttpClient();

        // 目标地址
        HttpPost httppost = new HttpPost(AVOS_PUSH_URL);// new HttpPost(AVOS_PUSH_URL);

        httppost.addHeader(AVOS_APP_ID, AVOS_APP_ID_VALUE);
        httppost.addHeader(AVOS_APP_KEY, AVOS_APP_KEY_VALUE);
        String body = null;

        // 构造最简单的字符串数据
        StringEntity reqEntity = null;

        try {
            PushParam pushParam = new PushParam();
            pushParam.setUrl(url);
            pushParam.setT(bizType);

            PushDO pushDO = new PushDO();
            pushDO.setAlert(content);
            pushDO.setAction(action);
            pushDO.setP(pushParam);
            pushDO.setTitle(title);
            String s = JSON.toJSONString(pushDO);

            String data = null;//"{\"where\": {\"objectId\": \"5427a9dae4b00f18e8702106\"},\"data\": {\"alert\": \"Hello From AVOS Cloud.\"}}";
            data = "{\"where\": {\"communityId\":\"" + communityId + "\"},\"data\": " + s + "}";
            reqEntity = new StringEntity(data, "utf-8");
            // 设置类型
            reqEntity.setContentType("application/json");
            // 设置请求的数据
            httppost.setEntity(reqEntity);
            // 执行
            HttpResponse httpresponse = httpclient.execute(httppost);
            int status = httpresponse.getStatusLine().getStatusCode();
            HttpEntity entity = httpresponse.getEntity();
            parseErrorViaStatus(status);
            body = EntityUtils.toString(entity);
        } catch (UnsupportedEncodingException e) {
            log.error("push msg failed. exp:" + e.getMessage());
        } catch (ClientProtocolException e) {
            log.error("push msg failed. exp:" + e.getMessage());
        } catch (IOException e) {
            log.error("push msg failed. exp:" + e.getMessage());
        }
        return body;
    }

    public static String pushMsg2Channel(String action, String content, String url, String communityId, String title, long bizType) {

        DefaultHttpClient httpclient = new DefaultHttpClient();

        // 目标地址
        HttpPost httppost = new HttpPost(AVOS_PUSH_URL);// new HttpPost(AVOS_PUSH_URL);

        httppost.addHeader(AVOS_APP_ID, AVOS_APP_ID_VALUE);
        httppost.addHeader(AVOS_APP_KEY, AVOS_APP_KEY_VALUE);
        String body = null;

        // 构造最简单的字符串数据
        StringEntity reqEntity = null;

        try {
            PushParam pushParam = new PushParam();
            pushParam.setUrl(url);
            pushParam.setT(bizType);

            PushDO pushDO = new PushDO();
            pushDO.setAlert(content);
            pushDO.setAction(action);
            pushDO.setP(pushParam);
            pushDO.setTitle(title);
            String s = JSON.toJSONString(pushDO);

            String data = null;
            data = "{\"channels\": [\"" + communityId + "\"],\"data\": " + s + "}";
            reqEntity = new StringEntity(data, "utf-8");
            // 设置类型
            reqEntity.setContentType("application/json");
            // 设置请求的数据
            httppost.setEntity(reqEntity);
            // 执行
            HttpResponse httpresponse = httpclient.execute(httppost);
            int status = httpresponse.getStatusLine().getStatusCode();
            HttpEntity entity = httpresponse.getEntity();
            parseErrorViaStatus(status);
            body = EntityUtils.toString(entity);
        } catch (UnsupportedEncodingException e) {
            log.error("push msg failed. exp:" + e.getMessage());
        } catch (ClientProtocolException e) {
            log.error("push msg failed. exp:" + e.getMessage());
        } catch (IOException e) {
            log.error("push msg failed. exp:" + e.getMessage());
        }
        return body;
    }

    /**
     * 根据status解析错误原因
     *
     * @param status
     */
    private static void parseErrorViaStatus(int status) {
        if (200 == status) {
            log.info("push task send success.");
        } else {
            switch (status) {
                case 1:
                    log.error("Internal server error. No information available");
                    break;
                case 100:
                    log.error("The connection to the AVOS servers failed.");
                    break;
                case 101:
                    log.error("Object doesn't exist, or has an incorrect password");
                    break;
                case 103:
                    log.error("Missing or invalid classname. Classnames are case-sensitive. They must start with a letter, and a-zA-Z0-9_ are the only valid characters.");
                    break;
                case 104:
                    log.error("Missing object id.");
                    break;
                case 105:
                    log.error("Invalid key name. Keys are case-sensitive. They must start with a letter, and a-zA-Z0-9_ are the only valid characters.");
                    break;
                case 106:
                    log.error("Malformed pointer. Pointers must be arrays of a classname and an object id.");
                    break;
                case 107:
                    log.error("Malformed json object. A json dictionary is expected.");
                    break;
                case 108:
                    log.error("Tried to access a feature only available internally.");
                    break;
                case 111:
                    log.error("Field set to incorrect type.");
                    break;
                case 112:
                    log.error("Invalid channel name. A channel name is either an empty string (the broadcast channel) or contains only a-zA-Z0-9_ characters and starts with a letter.");
                    break;
                case 114:
                    log.error("Invalid device token.");
                    break;
                case 116:
                    log.error("The object is too large.");
                    break;
                case 119:
                    log.error("That operation isn't allowed for clients.");
                    break;
                case 120:
                    log.error("The results were not found in the cache.");
                    break;
                case 121:
                    log.error("Keys in NSDictionary values may not include '#39; or'.'.");
                    break;
                case 122:
                    log.error("Invalid file name. A file name contains only a-zA-Z0-9_. characters and is between 1 and 36 characters.");
                    break;
                case 123:
                    log.error("Invalid ACL. An ACL with an invalid format was saved. This should not happen if you use AVACL.");
                    break;
                case 124:
                    log.error("The request timed out on the server. Typically this indicates the request is too expensive.");
                    break;
                case 125:
                    log.error("The email address was invalid.");
                    break;
                case 126:
                    log.error("Invalid user id.");
                    break;
                case 127:
                    log.error("The mobile phone number was invalid.");
                    break;
                case 137:
                    log.error("A unique field was given a value that is already taken.");
                    break;
                case 139:
                    log.error("Role's name is invalid.");
                    break;
                case 140:
                    log.error("Exceeded an application quota. Upgrade to resolve.");
                    break;
                case 141:
                    log.error("Cloud Code script had an error.");
                    break;
                case 142:
                    log.error("Cloud Code validation failed.");
                    break;
                case 145:
                    log.error("Payment is disabled on this device");
                    break;
                case 150:
                    log.error("Fail to convert data to image.");
                    break;
                case 201:
                    log.error("Password is missing or empty");
                    break;
                case 202:
                    log.error("Username has already been taken");
                    break;
                case 203:
                    log.error("Email has already been taken");
                    break;
                case 204:
                    log.error("The email is missing, and must be specified");
                    break;
                case 205:
                    log.error("A user with the specified email was not found");
                    break;
                case 206:
                    log.error("The user cannot be altered by a client without the session.");
                    break;
                case 207:
                    log.error("Users can only be created through sign up");
                    break;
                case 208:
                    log.error("An existing account already linked to another user.");
                    break;
                case 210:
                    log.error("The username and password mismatch.");
                    break;
                case 211:
                    log.error("Cloud not find user");
                    break;
                case 212:
                    log.error("The mobile phone number is missing, and must be specified");
                    break;
                case 213:
                    log.error("An user with the specified mobile phone number was not found");
                    break;
                case 214:
                    log.error("Mobile phone number has already been taken");
                    break;
                case 215:
                    log.error("Mobile phone number isn't verified.");
                    break;
                case 250:
                    log.error("Linked id missing from request");
                    break;
                case 251:
                    log.error("Invalid linked session  OR Invalid Weibo session");
                    break;
                case 300:
                    log.error("CQL syntax error.");
                    break;
                case 401:
                    log.error("Unauthorized.");
                    break;
                case 403:
                    log.error("Forbidden to xxx by class permissions");
                    break;
                case 503:
                    log.error("Rate limit exceeded.");
                    break;
            }

        }
    }

    public static String pushMsgDeviceToken(String action, String content, String url, String deviceToken, String title, long bizType) {

        DefaultHttpClient httpclient = new DefaultHttpClient();

        // 目标地址
        HttpPost httppost = new HttpPost(AVOS_PUSH_URL);// new HttpPost(AVOS_PUSH_URL);

        httppost.addHeader(AVOS_APP_ID, AVOS_APP_ID_VALUE);
        httppost.addHeader(AVOS_APP_KEY, AVOS_APP_KEY_VALUE);
        String body = null;

        // 构造最简单的字符串数据
        StringEntity reqEntity = null;

        try {
            PushParam pushParam = new PushParam();
            pushParam.setUrl(url);
            pushParam.setT(bizType);

            PushDO pushDO = new PushDO();
            pushDO.setAlert(content);
            pushDO.setAction(action);
            pushDO.setP(pushParam);
            pushDO.setTitle(title);
            String s = JSON.toJSONString(pushDO);

            String data = null;//"{\"where\": {\"objectId\": \"5427a9dae4b00f18e8702106\"},\"data\": {\"alert\": \"Hello From AVOS Cloud.\"}}";
            data = "{\"where\": {\"deviceToken\":\"" + deviceToken + "\"},\"data\": " + s + "}";
            reqEntity = new StringEntity(data, "utf-8");
            // 设置类型
            reqEntity.setContentType("application/json");
            // 设置请求的数据
            httppost.setEntity(reqEntity);
            // 执行
            HttpResponse httpresponse = httpclient.execute(httppost);
            int status = httpresponse.getStatusLine().getStatusCode();
            HttpEntity entity = httpresponse.getEntity();
            parseErrorViaStatus(status);
            body = EntityUtils.toString(entity);
        } catch (UnsupportedEncodingException e) {
            log.error("push msg failed. exp:" + e.getMessage());
        } catch (ClientProtocolException e) {
            log.error("push msg failed. exp:" + e.getMessage());
        } catch (IOException e) {
            log.error("push msg failed. exp:" + e.getMessage());
        }
        return body;
    }

    public static String pushMsgWithoutAlert(String action, String uId) {

        DefaultHttpClient httpclient = new DefaultHttpClient();

        // 目标地址
        HttpPost httppost = new HttpPost(AVOS_PUSH_URL);// new HttpPost(AVOS_PUSH_URL);

        httppost.addHeader(AVOS_APP_ID, AVOS_APP_ID_VALUE);
        httppost.addHeader(AVOS_APP_KEY, AVOS_APP_KEY_VALUE);
        String body = null;

        // 构造最简单的字符串数据
        StringEntity reqEntity = null;

        try {
            PushNoAlert pushNoAlert = new PushNoAlert();
            pushNoAlert.setAction(action);
            String s = JSON.toJSONString(pushNoAlert);

            String data = null;//"{\"where\": {\"objectId\": \"5427a9dae4b00f18e8702106\"},\"data\": {\"alert\": \"Hello From AVOS Cloud.\"}}";
            data = "{\"where\": {\"userId\":\"" + uId + "\"},\"data\": " + s + "}";
            reqEntity = new StringEntity(data, "utf-8");
            // 设置类型
            reqEntity.setContentType("application/json");
            // 设置请求的数据
            httppost.setEntity(reqEntity);
            // 执行
            HttpResponse httpresponse = httpclient.execute(httppost);
            int status = httpresponse.getStatusLine().getStatusCode();
            HttpEntity entity = httpresponse.getEntity();
            parseErrorViaStatus(status);
            body = EntityUtils.toString(entity);
        } catch (UnsupportedEncodingException e) {
            log.error("push msg failed. exp:" + e.getMessage());
        } catch (ClientProtocolException e) {
            log.error("push msg failed. exp:" + e.getMessage());
        } catch (IOException e) {
            log.error("push msg failed. exp:" + e.getMessage());
        }
        return body;
    }

    public static void main(String[] args) {
        String uid = PhenixUserHander.encodeUserId(207l);//userid  chaochao 15  xiaofei 14
        pushMsg("1", "AVOS！你好", "", uid, "11111", 64);
        pushMsgWithoutAlert("1", uid);
        //             String action, String content, String url, String deviceToken, String title,long bizType
        //pushMsgDeviceToken("1","devicetoken push",null,"ca0a3021e6b175e4844b42d6dcd26647b3a73b71b9b234bfaeea1199ac21e842","hal",64l);
    }
}
