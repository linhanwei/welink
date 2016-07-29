package com.welink.biz.common.security;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.*;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.nio.charset.CodingErrorAction;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;


/**
 * Created by daniel on 14-12-20.
 */
public class SSLClient {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(SSLClient.class);

    private static PoolingHttpClientConnectionManager connManager = null;

    private static final int REQUEST_TIMEOUT = 30 * 1000; // 设置请求超时10秒钟
    private static final int TIMEOUT = 60 * 1000; // 连接超时时间
    private static final int SO_TIMEOUT = 60 * 1000; // 数据传输超时
    private static final String CHARSET = "UTF-8";

    public static CloseableHttpClient getHttpClient() throws Exception {
        try {
            SSLContextBuilder sslContextbuilder = new SSLContextBuilder();
            sslContextbuilder.useTLS();
            SSLContext sslContext = sslContextbuilder.loadTrustMaterial(null, new TrustStrategy() {

                // 信任所有
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }

            }).build();

            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.INSTANCE)
                    .register("https", new SSLConnectionSocketFactory(sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)).build();

            // Create ConnectionManager

            connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
            // Create socket configuration
            SocketConfig socketConfig = SocketConfig.custom().setTcpNoDelay(true).build();
            connManager.setDefaultSocketConfig(socketConfig);

            // Create message constraints
            MessageConstraints messageConstraints = MessageConstraints.custom().setMaxHeaderCount(200).setMaxLineLength(2000).build();

            // Create connection configuration
            ConnectionConfig connectionConfig = ConnectionConfig.custom()
                    .setMalformedInputAction(CodingErrorAction.IGNORE)
                    .setUnmappableInputAction(CodingErrorAction.IGNORE)
                    .setCharset(Consts.UTF_8)
                    .setMessageConstraints(messageConstraints).build();

            connManager.setDefaultConnectionConfig(connectionConfig);
            connManager.setMaxTotal(200);
            connManager.setDefaultMaxPerRoute(20);

            // Create httpClient
            return HttpClients.custom().disableRedirectHandling().setConnectionManager(connManager).build();
        } catch (KeyManagementException e) {
            log.error("KeyManagementException", e);
        } catch (NoSuchAlgorithmException e) {
            log.error("NoSuchAlgorithmException", e);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 指定参数名GET方式请求数据
     *
     * @param url
     * @param paramsMap QueryString
     * @return
     */
    public static String doGet(String url, Map<String, String> paramsMap) {
        return doGet(invokeUrl(url, paramsMap));
    }

    /**
     * GET方式请求数据
     *
     * @param url
     */
    public static String doGet(String url) {
        HttpGet httpGet = new HttpGet(url);
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(SO_TIMEOUT)
                .setConnectTimeout(TIMEOUT)
                .setConnectionRequestTimeout(REQUEST_TIMEOUT).build();
        httpGet.setConfig(requestConfig);

        long responseLength = 0; // 响应长度
        String responseContent = null; // 响应内容
        String strRep = null;
        try {
            // 执行get请求
            HttpResponse httpResponse = getHttpClient().execute(httpGet);

            // 获取响应消息实体
            HttpEntity entity = httpResponse.getEntity();
            if (entity != null) {
                responseLength = entity.getContentLength();
                responseContent = EntityUtils.toString(entity, CHARSET);//不能重复调用此方法，IO流已关闭。

                System.err.println("内容编码: " + entity.getContentEncoding());
                System.err.println("请求地址: " + httpGet.getURI());
                System.err.println("响应状态: " + httpResponse.getStatusLine());
                System.err.println("响应长度: " + responseLength);
                System.err.println("响应内容: \r\n" + responseContent);

                // 获取HTTP响应的状态码
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                if (statusCode == HttpStatus.SC_OK) {
                    strRep = responseContent; // EntityUtils.toString(httpResponse.getEntity());
                }

                // Consume response content
                EntityUtils.consume(entity);
                // Do not need the rest
                httpGet.abort();
            }
        } catch (ClientProtocolException e) {
            log.error("ClientProtocolException", e);
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            log.error("UnsupportedEncodingException", e);
            e.printStackTrace();
        } catch (ConnectTimeoutException e) {
            log.error("ConnectTimeoutException", e);
            e.printStackTrace();
        } catch (SocketTimeoutException e) {
            log.error("SocketTimeoutException", e);
            e.printStackTrace();
        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            httpGet.releaseConnection();
        }

        return strRep;
    }

    /**
     * GET方式传参
     *
     * @param url
     * @param paramsMap
     * @return
     */
    public static String invokeUrl(String url, Map<String, String> paramsMap) {
        StringBuilder sb = new StringBuilder();
        sb.append(url);
        int i = 0;
        if (paramsMap != null && paramsMap.size() > 0) {
            for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
                if (i == 0 && !url.contains("?")) {
                    sb.append("?");
                } else {
                    sb.append("&");
                }
                sb.append(entry.getKey());
                sb.append("=");
                String value = entry.getValue();
                try {
                    sb.append(URLEncoder.encode(value, CHARSET));
                } catch (UnsupportedEncodingException e) {
                    log.warn("encode http get params error, value is " + value, e);
                    try {
                        sb.append(URLEncoder.encode(value, null));
                    } catch (UnsupportedEncodingException e1) {
                        e1.printStackTrace();
                    }
                }

                i++;
            }
        }

        return sb.toString();
    }

    public static void main(String[] args) {
        String s = doGet("http://www.baidu.com");
        System.out.println("===============");
        System.out.println(s);
    }
}
