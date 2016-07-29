/**
 * Project Name:welink-web
 * File Name:ResponseLoggingFilter.java
 * Package Name:com.welink.web.common.filter
 * Date:2015年9月30日上午11:51:59
 * Copyright (c) 2015, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.web.common.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.LoggerFactory;


/**
 * ClassName:ResponseLoggingFilter <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2015年9月30日 上午11:51:59 <br/>
 * @author   LuoGuangChun
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
public class ResponseLoggingFilter implements Filter {

	private static org.slf4j.Logger log = LoggerFactory.getLogger(ResponseLoggingFilter.class);
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		/*final CopyPrintWriter writer = new CopyPrintWriter(response.getWriter());
	    chain.doFilter(request, new HttpServletResponseWrapper((HttpServletResponse) response) {
	        @Override public PrintWriter getWriter() {
	            return writer;
	        }
	    });
	    String str = writer.getCopy();
	    String ip = getIpAddress((HttpServletRequest)request);
	    StringBuffer url = ((HttpServletRequest)request).getRequestURL();
	    log.info("请求ip："+ip+"; >>>"+"请求Url："+url+"; >>>返回值："+writer.getCopy());*/
		
		/*HttpServletRequest req = (HttpServletRequest)request;
		HttpServletResponse res = (HttpServletResponse)response;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    final PrintStream ps = new PrintStream(baos);

	    chain.doFilter(req,new HttpServletResponseWrapper(res) {
	         @Override
	         public ServletOutputStream getOutputStream() throws IOException {
	            return new DelegatingServletOutputStream(new TeeOutputStream(super.getOutputStream(), ps)
	            );
	         }
	         @Override
	         public  PrintWriter getWriter() throws IOException {
	            return new PrintWriter(new DelegatingServletOutputStream (new TeeOutputStream(super.getOutputStream(), ps))
	            );
	         }
	      });
	    String str = baos.toString();
	    log.info("请求ip："+getIpAddress(req)+"; >>>"+"请求Url："+req.getRequestURL()+"; >>>返回值："+baos.toString());*/
	}

	@Override
	public void destroy() {
		
		
	}
	
	//获取真实ip
    public String getIpAddress(HttpServletRequest request) { 
        String ip = request.getHeader("x-forwarded-for"); 
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
          ip = request.getHeader("Proxy-Client-IP"); 
        } 
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
          ip = request.getHeader("WL-Proxy-Client-IP"); 
        } 
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
          ip = request.getHeader("HTTP_CLIENT_IP"); 
        } 
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
          ip = request.getHeader("HTTP_X_FORWARDED_FOR"); 
        } 
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
          ip = request.getRemoteAddr(); 
        } 
        return ip; 
      }


}

