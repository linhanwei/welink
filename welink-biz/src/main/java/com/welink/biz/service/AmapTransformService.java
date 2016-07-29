package com.welink.biz.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.io.CharStreams;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by saarixx on 21/1/15.
 */
@Service
public class AmapTransformService {

    Logger logger = LoggerFactory.getLogger(AmapTransformService.class);

    private CloseableHttpClient httpClient = HttpClients.custom() //
            .setMaxConnTotal(20) //
            .setConnectionManager(new PoolingHttpClientConnectionManager()) //
            .setRetryHandler(new HttpRequestRetryHandler() {
                @Override
                public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
                    if (executionCount >= 3) {
                        // Do not retry if over max retry count
                        return false;
                    }
                    return true;
                }
            }) //
            .setDefaultRequestConfig(RequestConfig.custom()//
                            .setConnectTimeout(10000) //
                            .setConnectionRequestTimeout(30000) //
                            .setSocketTimeout(10000) //
                            .build()
            )
            .build();

    /**
     * gps transform amap point
     *
     * @param point
     * @return
     */
    public Optional<String> pointTransform(String point) throws IOException {
        String query = String.format("http://restapi.amap.com/v3/assistant/coordinate/convert?locations=%s&coordsys=gps&output=json&key=%s", point, "36d9f8e2f79ffb2dd451b6d109ac1d4b");
        HttpGet httpGet = new HttpGet(query);
        CloseableHttpResponse httpResponse = null;
        try {
            httpResponse = httpClient.execute(httpGet);
            String content = CharStreams.toString(new InputStreamReader(httpResponse.getEntity().getContent(), Charsets.UTF_8));

            JSONObject jsonObject = JSON.parseObject(content);

            if (jsonObject.getString("status").equals("1")) {
                return Optional.fromNullable(jsonObject.getString("locations"));
            } else {
                logger.error("the return is invalid [{}], the input parameter is [{}]", jsonObject.toString(), point);
                return Optional.absent();
            }
        } finally {
            try {
                httpResponse.close();
            } catch (Exception ignored) {

            }
        }

    }
}
