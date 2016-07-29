package com.welink.web.common.logging;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import com.welink.commons.tacker.EventTracker;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.LoggerFactory;

/**
 * Created by daniel on 14-9-22.
 */
public class CustomExceptionInterceptor extends AbstractInterceptor {

    private static final long serialVersionUID = -8970508093762607641L;

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(CustomExceptionInterceptor.class);

    @Override
    public String intercept(ActionInvocation invocation) {
        try {
            return invocation.invoke();
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);

            try {
                // 获得 调用位置
                String[] stackFrames = ExceptionUtils.getRootCauseStackTrace(e);
                StringBuilder stringBuilder = new StringBuilder();
                int count = 0;
                for (String string : stackFrames) {
                    if (string.contains("com.welink")) {
                        if (count > 0) {
                            stringBuilder.append(" + ");
                        }
                        stringBuilder.append(StringUtils.removeStart(string, "\tat "));
                        count++;
                    }

                    // 单条日志小于512K
                    if (count == 10) {
                        break;
                    }
                }
                EventTracker.track("uncaught-exception", "exception", e.getMessage(), stringBuilder.toString(), 1L);
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
            return "error";
        }
    }
}
