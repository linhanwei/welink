package com.welink.biz.common.MSG;

/**
 * Created by daniel on 14-9-12.
 */

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

import java.io.IOException;

/**
 * 短信http接口的java代码调用示例
 *
 * @author jacky
 * @since 2013-12-1
 */
public class JavaSmsApi {

    //public static final String APIKEY = "84ce5d237a29ae98b1629c0b177ab95d";
	public static final String APIKEY = "aa0791acb6e280103d83f283e7eef954";

    public static final String URL_WITH_TEMPLATE = "http://yunpian.com/v1/sms/tpl_send.json";

    public static final String URL = "http://yunpian.com/v1/sms/send.json";
    /**
     * 服务http地址
     */
    private static String BASE_URI = "http://yunpian.com";
    /**
     * 服务版本号
     */
    private static String VERSION = "v1";
    /**
     * 编码格式
     */
    private static String ENCODING = "UTF-8";
    /**
     * 查账户信息的http地址
     */
    private static String URI_GET_USER_INFO = BASE_URI + "/" + VERSION + "/user/get.json";
    /**
     * 通用发送接口的http地址
     */
    private static String URI_SEND_SMS = BASE_URI + "/" + VERSION + "/sms/send.json";
    /**
     * 模板发送接口的http地址
     */
    private static String URI_TPL_SEND_SMS = BASE_URI + "/" + VERSION + "/sms/tpl_send.json";

    /**
     * 获取状态报告地址
     * String url = "http://yunpian.com/v1/sms/pull_status.json";
     */
    private static String URI_FETCH_SMS_STATUS = BASE_URI + "/" + VERSION + "/sms/pull_status.json";

    /**
     * 取账户信息
     *
     * @return json格式字符串
     * @throws IOException
     */
    public static String getUserInfo(String apikey) throws IOException {
        HttpClient client = new HttpClient();
        HttpMethod method = new GetMethod(URI_GET_USER_INFO + "?apikey=" + apikey);
        HttpMethodParams param = method.getParams();
        param.setContentCharset(ENCODING);
        client.executeMethod(method);
        return method.getResponseBodyAsString();
    }

    /**
     * 发短信
     *
     * @param apikey apikey
     * @param text   　短信内容
     * @param mobile 　接受的手机号
     * @return json格式字符串
     * @throws IOException
     */
    public static String sendSms(String apikey, String text, String mobile) throws IOException {
        HttpClient client = new HttpClient();
        NameValuePair[] nameValuePairs = new NameValuePair[3];
        nameValuePairs[0] = new NameValuePair("apikey", apikey);
        nameValuePairs[1] = new NameValuePair("text", text);
        nameValuePairs[2] = new NameValuePair("mobile", mobile);
        PostMethod method = new PostMethod(URI_SEND_SMS);
        method.setRequestBody(nameValuePairs);
        HttpMethodParams param = method.getParams();
        param.setContentCharset(ENCODING);
        client.executeMethod(method);
        return method.getResponseBodyAsString();
    }

    /**
     * 通过模板发送短信
     *
     * @param apikey    apikey
     * @param tpl_id    　模板id
     * @param tpl_value 　模板变量值
     * @param mobile    　接受的手机号
     * @return json格式字符串
     * @throws IOException
     */
    public static String tplSendSms(String apikey, long tpl_id, String tpl_value, String mobile) throws IOException {
        HttpClient client = new HttpClient();
        NameValuePair[] nameValuePairs = new NameValuePair[4];
        nameValuePairs[0] = new NameValuePair("apikey", apikey);
        nameValuePairs[1] = new NameValuePair("tpl_id", String.valueOf(tpl_id));
        nameValuePairs[2] = new NameValuePair("tpl_value", tpl_value);
        nameValuePairs[3] = new NameValuePair("mobile", mobile);
        PostMethod method = new PostMethod(URI_TPL_SEND_SMS);
        method.setRequestBody(nameValuePairs);
        HttpMethodParams param = method.getParams();
        param.setContentCharset(ENCODING);
        client.executeMethod(method);
        return method.getResponseBodyAsString();
    }

    public static String fetchSmsStatus(String apikey, String pageSize) throws IOException {
        HttpClient client = new HttpClient();
        NameValuePair[] nameValuePairs = new NameValuePair[4];
        nameValuePairs[0] = new NameValuePair("apikey", apikey);
        nameValuePairs[1] = new NameValuePair("page_size", pageSize);
        PostMethod method = new PostMethod(URI_FETCH_SMS_STATUS);
        method.setRequestBody(nameValuePairs);
        HttpMethodParams param = method.getParams();
        param.setContentCharset(ENCODING);
        client.executeMethod(method);
        return method.getResponseBodyAsString();
    }

    public static void main(String[] args) throws IOException {
        //修改为您的apikey
        String apikey = APIKEY;
        //修改为您要发送的手机号
        String mobile = "15622395287";///*,18506835790,15381142360*/";

        /**************** 查账户信息调用示例 *****************/
        //System.out.println(JavaSmsApi.getUserInfo(apikey));

        /**************** 使用通用接口发短信 *****************/
        //设置您要发送的内容
        //String text = "您的验证码是1234【云片网】";
        //发短信调用示例
        //System.out.println(JavaSmsApi.sendSms(apikey, text, mobile));

        /**************** 使用模板接口发短信 *****************/
        //设置模板ID，如使用1号模板:您的验证码是#code#【#company#】
        long tpl_id = 1;
        //设置对应的模板变量值
        //String tpl_value = "#code#=952700&#company#=微邻科技";
        String tpl_value = "#code#=1234&#company#=云片网";
        //模板发送的调用示例
        System.out.println(JavaSmsApi.tplSendSms(apikey, tpl_id, tpl_value, mobile));


        //获取状态报告
        System.out.println("==============获取状态报告=================");
        //System.out.println(JavaSmsApi.fetchSmsStatus(apikey, "20"));

    }
}