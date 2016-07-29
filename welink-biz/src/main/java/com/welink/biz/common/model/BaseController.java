package com.welink.biz.common.model;

import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by daniel on 15-5-5.
 */
public class BaseController {

    @ExceptionHandler
    public String exp(HttpServletRequest request, Exception ex) {

        request.setAttribute("ex", ex);

        // 根据不同错误转向不同页面
//        if(ex instanceof BusinessException) {
//            return "error-business";
//        }else if(ex instanceof ParameterException) {
//            return "error-parameter";
//        } else {
//            return "error";
//        }
        return "error";
    }
}
