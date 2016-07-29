package com.welink.web.common.filter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.MessageFormat;

/**
 * 记录servlet处理时间的filter。
 * <p>
 * <code>web.xml</code>配置文件格式如下：
 * <p/>
 * <pre>
 * &lt;![CDATA[
 *  &lt;filter&gt;
 *  &lt;filter-name&gt;timer&lt;/filter-name&gt;
 *  &lt;filter-class&gt;com.alibaba.webx.filter.timer.TimerFilter&lt;/filter-class&gt;
 *  &lt;init-param&gt;
 *  &lt;param-name&gt;threshold&lt;/param-name&gt;
 *  &lt;param-value&gt;30000&lt;/param-value&gt;
 *  &lt;/init-param&gt;
 *  &lt;/filter&gt;
 *  ]]&gt;
 * </pre>
 * <p/>
 * </p>
 * <p>
 * 其中<code>threshold</code>参数表明超时阈值，如果servlet处理的总时间超过该值，则filter会以warning的方式记录该次操作。
 * </p>
 *
 * @author Michael Zhou
 * @version $Id: TimerFilter.java 1042 2004-06-04 11:00:44Z baobao $
 */
public class TimerFilter extends AbstractFilter {
    private int threshold;

    /**
     * 初始化filter, 设置监视参数.
     *
     * @throws ServletException 初始化失败
     */
    public void init() throws ServletException {
        String thresholdString = findInitParameter("threshold", "30000");

        if (thresholdString != null) {
            try {
                threshold = Integer.parseInt(thresholdString);
            } catch (NumberFormatException e) {
                threshold = 0;
            }

            if (threshold <= 0) {
                throw new ServletException(MessageFormat.format("Invalid init parameter for filter: threshold = {0}",
                        new Object[]{thresholdString}));
            }
        }

        log.info("Timer filter started with threshold {}ms", new Integer(threshold));
    }

    /**
     * 执行filter.
     *
     * @param request  HTTP请求
     * @param response HTTP响应
     * @param chain    filter链
     * @throws IOException      处理filter链时发生输入输出错误
     * @throws ServletException 处理filter链时发生的一般错误
     */
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // 开始处理request, 并计时.
        String requestString = dumpRequest(request);

        if (log.isInfoEnabled()) {
            log.info("Started processing request: " + requestString);
        }

        Profiler.start("process HTTP request");

        Throwable failed = null;

        try {
            chain.doFilter(request, response);
        } catch (Throwable e) {
            log.error("time filter error:" + e.getMessage(), e);
            failed = e;
        } finally {
            Profiler.release();

            long duration = Profiler.getDuration();

            if (failed != null) {
                if (log.isErrorEnabled()) {
                    log.error("Response of {} failed in {}ms: {}\n{}\n", requestString, duration, failed.getLocalizedMessage(), getDetail());
                }
            } else if (duration > threshold) {
                if (log.isWarnEnabled()) {
                    log.warn("Response of {} returned in {}ms\n{}\n", requestString, duration, getDetail());
                }
            } else {
                if (log.isInfoEnabled()) {
                    log.info("Response of {} returned in {}ms\n{}\n",
                            requestString, duration, getDetail());
                }
            }

            Profiler.reset();
        }

        if (failed != null) {
            if (failed instanceof Error) {
                throw (Error) failed;
            } else if (failed instanceof RuntimeException) {
                throw (RuntimeException) failed;
            } else if (failed instanceof IOException) {
                throw (IOException) failed;
            } else if (failed instanceof ServletException) {
                throw (ServletException) failed;
            }
        }
    }

    private String getDetail() {
        return Profiler.dump("Detail: ", "        ");
    }
}
