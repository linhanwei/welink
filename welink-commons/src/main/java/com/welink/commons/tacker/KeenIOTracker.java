package com.welink.commons.tacker;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;
import com.welink.commons.Env;
import com.welink.commons.utils.NoNullFieldStringStyle;
import io.keen.client.java.JavaKeenClientBuilder;
import io.keen.client.java.KeenClient;
import io.keen.client.java.KeenLogging;
import io.keen.client.java.KeenProject;
import io.keen.client.java.http.HttpHandler;
import io.keen.client.java.http.Request;
import io.keen.client.java.http.Response;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestFactory;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.DefaultHttpRequestFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by saarixx on 6/1/15.
 */
@Service
public class KeenIOTracker {

    static Logger logger = LoggerFactory.getLogger(KeenIOTracker.class);

    private KeenClient keenClient;

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
                            .setProxy(new HttpHost("106.185.37.153", 21768)) //
                            .setConnectTimeout(10000) //
                            .setConnectionRequestTimeout(30000) //
                            .setSocketTimeout(10000) //
                            .build()
            )
            .build();


    @Resource
    private Env env;

    public void track(String mobile, String category, Object object, Map<String, Object> features) {

        checkNotNull(object);
        checkArgument(StringUtils.isNotBlank(mobile));
        checkArgument(StringUtils.isNotBlank(category));


        Map<String, Object> eventMap = transform(mobile, object);

        checkNotNull(eventMap, "error parse object to map -> %s", ToStringBuilder.reflectionToString(object, new NoNullFieldStringStyle()));

        if (features != null) {
            eventMap.put("features", features);
        }

        if (!env.isProd()) {
            category = "sandbox";
        }

        keenClient.addEvent(category, eventMap);
    }

    @PostConstruct
    public void init() {
        keenClient = new JavaKeenClientBuilder() //
                .withJsonHandler(new FastJsonHandler()) //
                .withHttpHandler(new HttpHandler() {
                    @Override
                    public Response execute(Request request) throws IOException {

                        HttpRequest httpRequest = transform(request);
                        HttpHost httpHost = toHttpHost(request);

                        CloseableHttpResponse httpResponse = httpClient.execute(httpHost, httpRequest);

                        try {
                            String content = CharStreams.toString(new InputStreamReader(httpResponse.getEntity().getContent(), Charsets.UTF_8));
                            int statusCode = httpResponse.getStatusLine().getStatusCode();

                            logger.info("keen io status --> {} : {}", statusCode, content);

                            return new Response(statusCode, content);
                        } finally {
                            httpResponse.close();
                        }
                    }
                })
                .build();

        keenClient.setDebugMode(true);
        KeenLogging.enableLogging();
        KeenProject project = new KeenProject("549d2c9246f9a71b4e266b6e", "fc8fe594ff9b45c0e109634be0f3eb128d7f055ecbd9d0178cef0fa5e369f982955fd5ab37b569f644299e273ec99235f4b6da726f8fbf0217b4de491b98a44c8cb78eac572ec4fce24f89f8f02b1787d488af7e5742c0094ca082bf3b8a419b232a036149ad045933a4641b2ec8e9ea", "acf39e4593f46967bb81d13e7159b314b14f5a21b5bb3ee567fb1af57486fd1722b24490c358b7e3f32327e8f075b0727979d190cb4bb554d4e9f80071c566de437f5be33f4fea65f33984ac49613be943465e79a2d405c7ec9aebb14345f76b1b95f305d9066194e67e66063c64ff69");
        keenClient.setDefaultProject(project);


    }

    public HttpHost toHttpHost(Request request) {
        try {
            return URIUtils.extractHost(request.url.toURI());
        } catch (Exception e) {
            Throwables.propagate(e);
        }

        throw new IllegalStateException("error toHttpHost status ...");
    }

    public HttpRequest transform(Request request) {
        HttpRequestFactory httpRequestFactory = new DefaultHttpRequestFactory();
        try {
            HttpRequest httpRequest = httpRequestFactory.newHttpRequest(request.method, String.valueOf(request.url.toURI()));

            httpRequest.addHeader("Accept", "application/json");
            httpRequest.addHeader("Authorization", request.authorization);

            if (request.body != null) {
                httpRequest.addHeader("Content-Type", "application/json");
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                request.body.writeTo(outputStream);
                InputStreamEntity entity = new InputStreamEntity(new ByteArrayInputStream(outputStream.toByteArray()));
                if (httpRequest instanceof HttpEntityEnclosingRequest) {
                    ((HttpEntityEnclosingRequest) httpRequest).setEntity(entity);
                }
            }

            return httpRequest;

        } catch (Exception e) {
            Throwables.propagate(e);
        }

        throw new IllegalStateException("error transform status ...");
    }

    Map<String, Object> transform(String mobile, Object object) {
        Map<String, Object> map = Maps.newHashMap();
        map.put(FastJsonHandler.DEFAULT_MAP_KEY_MOBILE, mobile);
        map.put(FastJsonHandler.DEFAULT_MAP_KEY_OBJECT, object);
        return map;
    }
}
