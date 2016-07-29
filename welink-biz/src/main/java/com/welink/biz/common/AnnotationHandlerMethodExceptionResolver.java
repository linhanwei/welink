package com.welink.biz.common;

import com.alibaba.fastjson.JSON;
import com.brsanthu.googleanalytics.ExceptionHit;
import com.brsanthu.googleanalytics.GoogleAnalytics;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.WelinkVO;
import org.apache.shiro.util.StringUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by daniel on 15-5-5.
 */
public class AnnotationHandlerMethodExceptionResolver extends ExceptionHandlerExceptionResolver {

    private String defaultErrorView;

    public String getDefaultErrorView() {
        return defaultErrorView;
    }

    public void setDefaultErrorView(String defaultErrorView) {
        this.defaultErrorView = defaultErrorView;
    }

    protected ModelAndView doResolveHandlerMethodException(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod, Exception exception) {

        if (handlerMethod == null) {
            return null;
        }

        Method method = handlerMethod.getMethod();
        Object obj = handlerMethod.getBean();

        if (method == null) {
            return null;
        }

        ModelAndView returnValue = super.doResolveHandlerMethodException(request, response, handlerMethod, exception);
        RestController restController = AnnotationUtils.findAnnotation(obj.getClass(), RestController.class);
        if (restController != null) {
            try {
                ResponseStatus responseStatusAnn = AnnotationUtils.findAnnotation(method, ResponseStatus.class);
                if (responseStatusAnn != null) {
                    HttpStatus responseStatus = responseStatusAnn.value();
                    String reason = responseStatusAnn.reason();
                    if (!StringUtils.hasText(reason)) {
                        response.setStatus(responseStatus.value());
                    } else {
                        try {
                            response.sendError(responseStatus.value(), reason);
                        } catch (IOException e) {
                        }
                    }
                }

                if (null == returnValue) {
                    logger.error("error occur exception:" + exception.getMessage(), exception);
                    WelinkVO welinkVO = new WelinkVO();
                    welinkVO.setStatus(0);
                    welinkVO.setCode(BizErrorEnum.SYSTEM_BUSY.getCode());
                    welinkVO.setMsg(BizErrorEnum.SYSTEM_BUSY.getMsg());
                    response.setContentType("application/json;charset=utf-8");
                    response.getWriter().write(JSON.toJSONString(welinkVO));
                    return null;
                }
                return handleResponseBody(returnValue, request, response);
            } catch (Exception e) {
                return null;
            }
        }

        if (returnValue.getViewName() == null) {
            returnValue.setViewName(defaultErrorView);
        }

        return returnValue;
    }


    @SuppressWarnings({"unchecked", "rawtypes"})
    private ModelAndView handleResponseBody(ModelAndView returnValue, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map value = returnValue.getModelMap();
        HttpInputMessage inputMessage = new ServletServerHttpRequest(request);
        List<MediaType> acceptedMediaTypes = inputMessage.getHeaders().getAccept();
        if (acceptedMediaTypes.isEmpty()) {
            acceptedMediaTypes = Collections.singletonList(MediaType.ALL);
        }
        MediaType.sortByQualityValue(acceptedMediaTypes);
        HttpOutputMessage outputMessage = new ServletServerHttpResponse(response);
        Class<?> returnValueType = value.getClass();
        List<HttpMessageConverter<?>> messageConverters = super.getMessageConverters();
        if (messageConverters != null) {
            for (MediaType acceptedMediaType : acceptedMediaTypes) {
                for (HttpMessageConverter messageConverter : messageConverters) {
                    if (messageConverter.canWrite(returnValueType, acceptedMediaType)) {
                        messageConverter.write(value, acceptedMediaType, outputMessage);
                        return new ModelAndView();
                    }
                }
            }
        }
        if (logger.isWarnEnabled()) {
            logger.warn("Could not find HttpMessageConverter that supports return type [" + returnValueType + "] and " + acceptedMediaTypes);
        }
        return null;
    }

    /**
     * ga server side
     *
     * @param e
     */
    public void gaRecord(Exception e) {
        GoogleAnalytics ga = new GoogleAnalytics("UA-12345678-1");
        ga.postAsync(new ExceptionHit(e.getClass().getName(), false));
    }

    public static void main(String[] args) {
        Exception e = new IOException();
        System.out.println(e.getCause());
    }
}