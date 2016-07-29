package com.welink.web.common.action;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.SessionAware;

import java.util.Map;

public abstract class BaseAction extends ActionSupport implements SessionAware {

    /**
     *
     */
    private static final long serialVersionUID = -647335480800451539L;
    /**
     * 基础跳转
     */

    public Map<Object, Object> session;

    public void setSession(Map session) {
        this.session = session;
    }

    public abstract String execute() throws Exception;

}