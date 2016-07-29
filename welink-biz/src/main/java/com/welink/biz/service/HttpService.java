package com.welink.biz.service;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.Response;
import com.welink.commons.commons.BizConstants;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Created by daniel on 14-10-15.
 */

@Service
public class HttpService {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(HttpService.class);

    final AsyncHttpClient asyncHttpClient = new AsyncHttpClient();

    public void sendAsyncHttp(String urlWithParas) {
        try {
            AsyncHttpClientConfig.Builder builder = new AsyncHttpClientConfig.Builder();
            builder.setCompressionEnabled(true).setConnectionTimeoutInMs(8000).setRequestTimeoutInMs(30000)
                    .setIdleConnectionTimeoutInMs(1000 * 60 * 10)
                    .build();

            AsyncHttpClient client = new AsyncHttpClient(builder.build());
            client.prepareGet(urlWithParas).execute(new AsyncCompletionHandler<Response>() {

                @Override
                public Response onCompleted(Response response) throws Exception {
                    String body = response.getResponseBody();
                    System.out.print("=============================");
                    System.out.println(body);

                    System.out.print("=============================");
                    return response;
                }

                @Override
                public void onThrowable(Throwable t) {
                    log.error("exception while send async http get . excption:" + t.getMessage() + ",cause:" + t.getCause());
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("exception.....................");
        }
    }

    public static void main(String[] args) {
        HttpService s = new HttpService();
        String ak = "pWtRwFvKi5EG0Ui6nag4EGdU";

        String q = "超市";
        String location = "39.915,116.404";
        String url = BizConstants.BAIDU_API_URL;
        String radius = "3000";
        String pageSize = "2";
        url = url + "ak=" + BizConstants.BAIDU_API_AK + "&output=json&query=" + q + "&page_size=" + pageSize + "&page_num=0&scope=2&location=" +
                location + "&radius=" + radius;
        s.sendAsyncHttp("http://www.baidu.com");
    }
}
