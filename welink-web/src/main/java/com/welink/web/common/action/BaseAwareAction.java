package com.welink.web.common.action;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.interceptor.ServletRequestAware;

import java.io.IOException;

/**
 * Created by daniel on 14-10-14.
 */
public abstract class BaseAwareAction extends BaseAction implements ServletRequestAware {

    HttpServletRequest request;

    @Override
    public void setServletRequest(HttpServletRequest request) {
        this.request = request;
    }

    public HttpServletRequest getSevletRequest() {
        return request;
    }

    @Override
    public String execute() throws IOException {
        return null;
    }
}
