package com.welink.biz.common.security;

/**
 * Created by daniel on 14-12-20.
 */
public class SSLHttpClientUtil {

//    private static org.slf4j.Logger log = LoggerFactory.getLogger(SSLHttpClientUtil.class);
//
//    public String doPost(String url, Map<String, String> map, String charset) {
//        HttpClient httpClient = null;
//        HttpPost httpPost = null;
//        String result = null;
//        try {
//            httpClient = new SSLClient();
//            httpPost = new HttpPost(url);
//            //设置参数
//            List<NameValuePair> list = new ArrayList<NameValuePair>();
//            Iterator iterator = map.entrySet().iterator();
//            while (iterator.hasNext()) {
//                Entry<String, String> elem = (Entry<String, String>) iterator.next();
//                list.add(new BasicNameValuePair(elem.getKey(), elem.getValue()));
//            }
//            if (list.size() > 0) {
//                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list, charset);
//                httpPost.setEntity(entity);
//            }
//            HttpResponse response = httpClient.execute(httpPost);
//            if (response != null) {
//                HttpEntity resEntity = response.getEntity();
//                if (resEntity != null) {
//                    result = EntityUtils.toString(resEntity, charset);
//                }
//            }
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return result;
//    }
//
//    public String doGet(String url) {
//        org.apache.commons.httpclient.HttpClient client = new org.apache.commons.httpclient.HttpClient();
//        HttpMethod method = new GetMethod(url);
//        method.getParams().setContentCharset("UTF-8");
//        HttpMethodParams param = method.getParams();
//        param.setContentCharset("UTF-8");
//        try {
//            client.executeMethod(method);
//        } catch (IOException e) {
//            log.error("fetch user info from wechat failed. url:" + url + ",exp:" + e.getMessage());
//        }
//        String result = null;
//        try {
//            result = method.getResponseBodyAsString();
//        } catch (IOException e) {
//            log.error("fetch user info from wechat - get response failed. url:" + url + ",exp:" + e.getMessage());
//        }
//        return result;
//    }
//
//    public String doPostJson(String url, String jsonStr, String charset) {
//        HttpClient httpClient = null;
//        HttpPost httpPost = null;
//        String result = null;
//        try {
//            httpClient = new SSLClient();
//            httpPost = new HttpPost(url);
//            StringEntity entity = new StringEntity(jsonStr);
//            //设置参数
//            httpPost.setEntity(entity);
//            HttpResponse response = httpClient.execute(httpPost);
//            if (response != null) {
//                HttpEntity resEntity = response.getEntity();
//                if (resEntity != null) {
//                    result = EntityUtils.toString(resEntity, charset);
//                }
//            }
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return result;
//    }
//
//    /**
//     * as stream
//     * @param url
//     * @return
//     */
//    public InputStream doGetAsStream(String url) {
//        org.apache.commons.httpclient.HttpClient client = new org.apache.commons.httpclient.HttpClient();
//        HttpMethod method = new GetMethod(url);
//        method.getParams().setContentCharset("UTF-8");
//        HttpMethodParams param = method.getParams();
//        param.setContentCharset("UTF-8");
//        try {
//            client.executeMethod(method);
//        } catch (IOException e) {
//            log.error("fetch user info from wechat failed. url:" + url + ",exp:" + e.getMessage());
//        }
//        InputStream result = null;
//        try {
//            result = method.getResponseBodyAsStream();
//        } catch (IOException e) {
//            log.error("fetch user info from wechat - get response failed. url:" + url + ",exp:" + e.getMessage());
//        }
//        return result;
//    }
}
