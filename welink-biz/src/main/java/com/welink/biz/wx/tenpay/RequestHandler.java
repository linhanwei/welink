package com.welink.biz.wx.tenpay;

import com.alibaba.fastjson.JSONException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.welink.biz.wx.tenpay.client.TenpayHttpClient;
import com.welink.biz.wx.tenpay.util.ConstantUtil;
import com.welink.biz.wx.tenpay.util.PayCommonUtil;
import com.welink.biz.wx.tenpay.util.XMLUtil;
import org.jdom.JDOMException;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;


public class RequestHandler {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(RequestHandler.class);

    // 提交预支付
    public String sendPrepay(SortedMap<Object, Object> parameters, String requestUrl) throws JSONException {
        String prepayId = "";
        String params = PayCommonUtil.getRequestXml(parameters);
        TenpayHttpClient httpClient = new TenpayHttpClient();
        httpClient.setReqContent(requestUrl);
        String resContent = "";
        if (httpClient.callHttpPost(requestUrl, params)) {
            resContent = httpClient.getResContent();
            try {
                Map<String, String> map = XMLUtil.doXMLParse(resContent);
                if ("SUCCESS".equals(map.get("return_code")) && "SUCCESS".equals(map.get("result_code"))) {
                    prepayId = map.get("prepay_id");
                } else {
                    log.error("gen prepayId failed,response=" + resContent + " ,params=" + params);
                }
            } catch (JDOMException e) {
                log.error("JDOMException parse xml from string to map failed " + e.getMessage());
            } catch (IOException e) {
                log.error("IOException parse xml from string to map failed " + e.getMessage());
            }
        }
        return prepayId;
    }

    //APP微信支付时获取Token
    public String GetToken(String appId, String appSecret) {
        String token = "";
        String requestUrl = ConstantUtil.TOKENURL + "?grant_type=client_credential&appid="
                + appId + "&secret=" + appSecret;
        TenpayHttpClient httpClient = new TenpayHttpClient();
        httpClient.setReqContent(requestUrl);
        if (httpClient.call()) {
            String res = httpClient.getResContent();
            Gson gson = new Gson();
            TreeMap map = gson.fromJson(res, TreeMap.class);
            // 在有效期内直接返回access_token
            if (map.containsKey("access_token")) {
                token = map.get("access_token").toString();
            } else {
                log.error("没有获取到token,response=" + res);
            }
        }


        return token;
    }


    // 特殊字符处理
    public String UrlEncode(String src) throws UnsupportedEncodingException {
        return URLEncoder.encode(src, "UTF-8").replace("+", "%20");
    }

    // 获取package带参数的签名包
    public String genPackage(SortedMap<Object, Object> packageParams, String key)
            throws UnsupportedEncodingException {
        String sign = PayCommonUtil.createSign("UTF-8", packageParams, key);

        StringBuffer sb = new StringBuffer();
        Set es = packageParams.entrySet();
        Iterator it = es.iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String k = (String) entry.getKey();
            String v = (String) entry.getValue();
            sb.append(k + "=" + UrlEncode(v) + "&");
        }

        // 去掉最后一个&
        String packageValue = sb.append("sign=" + sign).toString();
        return packageValue;
    }

    // 提交预支付
    public String appSendPrepay(SortedMap packageParams, String requestUrl, String token) {
        String prepayid = "";
        // 转换成json
        Gson gson = new Gson();
        /* String postData =gson.toJson(packageParams); */
        String postData = "{";
        Set es = packageParams.entrySet();
        Iterator it = es.iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String k = (String) entry.getKey();
            String v = (String) entry.getValue();
            if (k != "appkey") {
                if (postData.length() > 1)
                    postData += ",";
                postData += "\"" + k + "\":\"" + v + "\"";
            }
        }
        postData += "}";
        // 设置链接参数
        String url = requestUrl + "?access_token=" + token;
        TenpayHttpClient httpClient = new TenpayHttpClient();
        httpClient.setReqContent(requestUrl);
        //httpClient.setReqContent(url);
        String resContent = "";
        if (httpClient.callHttpPost(requestUrl, postData)) {
            resContent = httpClient.getResContent();
            Map<String, String> map = gson.fromJson(resContent,
                    new TypeToken<Map<String, String>>() {
                    }.getType());
            if ("0".equals(map.get("errcode"))) {
                prepayid = map.get("prepayid");
            } else {
                System.out.println("get token err ,info =" + map.get("errmsg"));
            }
            // 设置debug info
            System.out.println("res json=" + resContent);
        }
        return prepayid;
    }
}
