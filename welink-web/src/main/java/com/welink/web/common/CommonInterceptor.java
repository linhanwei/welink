package com.welink.web.common;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.slf4j.LoggerFactory;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.welink.biz.common.constants.BizErrorEnum;
import com.welink.biz.common.model.WelinkVO;
import com.welink.biz.util.TokenUtil;
import com.welink.buy.utils.Constants;
import com.welink.buy.utils.PhenixUserHander;
import com.welink.commons.Env;
import com.welink.commons.commons.BizConstants;
import com.welink.web.common.util.CookieCheckUtils;

/**
 * Created by daniel on 15-4-24.
 */
public class CommonInterceptor implements HandlerInterceptor {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(CommonInterceptor.class);

    private static final String tm = "sarah";//漂亮
    
    @Resource
    private Env env;

    private static final List<String> pageAction = new ArrayList<>();

    static {
        pageAction.add("list");
        pageAction.add("hOrder");
        pageAction.add("getUserInfo");
        pageAction.add("hjsApiParams");
        pageAction.add("hAlipayCallBack");
        pageAction.add("searchList");
        pageAction.add("hBack");
        pageAction.add("hActive");
        pageAction.add("hSpringActive");
        pageAction.add("hwxOrder");
        pageAction.add("checkEmployee");
        pageAction.add("changeTradeStatus");
        pageAction.add("searchTrades");
        pageAction.add("halfActiveSnapshot");
        pageAction.add("appWxOrder");
        pageAction.add("checkTradeId");
        pageAction.add("relatedItems");
        pageAction.add("homeBanner");
        pageAction.add("christ");
        pageAction.add("checkActiveOrder");
        pageAction.add("genQr");
        pageAction.add("synCheckMobile");
        pageAction.add("addLog");
        pageAction.add("weChatNotification");
        pageAction.add("bindMobile");
        pageAction.add("cartPage");
        pageAction.add("communityPage");
        pageAction.add("hpay");
        pageAction.add("detailPage");
        pageAction.add("editAddr");
        pageAction.add("helpPage");
        pageAction.add("indexPage");
        pageAction.add("listPage");
        pageAction.add("orderDetail");
        pageAction.add("orderList");
        pageAction.add("placeOrder");
        pageAction.add("userCenter");
        pageAction.add("item");
        pageAction.add("address");
        pageAction.add("delUser");
        pageAction.add("fetchItems");
        pageAction.add("soldout");
        pageAction.add("addCart");
        pageAction.add("fetchCates");
        pageAction.add("hWeiXinCallBack");
        pageAction.add("oCartItems");
        pageAction.add("weiXinCallBack");
        pageAction.add("newerGift");
        pageAction.add("myPoint");
        pageAction.add("myCoupon");
        pageAction.add("vLoginPage");
        pageAction.add("oauth");
        pageAction.add("oauthFrame");
        pageAction.add("alipayDrawboard");
        pageAction.add("alipayGuide");
        pageAction.add("clearSession");
        pageAction.add("helloVelocity");
        pageAction.add("hello");
        pageAction.add("greeting");
        pageAction.add("good");
        pageAction.add("homeBanner");
        pageAction.add("preLoad");
        pageAction.add("error");
    }

    public CommonInterceptor() {
//        log.info("=======");
    }

    private String mappingURL;//利用正则映射到需要拦截的路径

    public void setMappingURL(String mappingURL) {
        this.mappingURL = mappingURL;
    }

    /**
     * 在业务处理器处理请求之前被调用
     * 如果返回false
     * 从当前的拦截器往回执行所有拦截器的afterCompletion(),再退出拦截器链
     * <p/>
     * 如果返回true
     * 执行下一个拦截器,直到所有的拦截器都执行完毕
     * 再执行被拦截的Controller
     * 然后进入拦截器链,
     * 从最后一个拦截器往回执行所有的postHandle()
     * 接着再从最后一个拦截器往回执行所有的afterCompletion()
     */
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) throws Exception {
        request.getAuthType();
        String servletPath = request.getServletPath();
        String namespace = null;
        if (StringUtils.contains(servletPath, "/api/h/")) {
            namespace = BizConstants.H;
        }
        String actionName = StringUtils.substringAfterLast(servletPath, "/");
        actionName = StringUtils.substringBefore(actionName, ".");
        
        Map values = request.getParameterMap();
        String paramStr = "";
        for (Object s : values.keySet()) {
            String v = ((String[]) values.get(s))[0];
            paramStr += s +": "+ v + "; ";
        }
        log.info("请求ip："+getIpAddress(request)+"; >>>"+"请求Url："+request.getRequestURL()+">>>参数：{"+paramStr+"}");		//记录访问链接和访问参数
        //System.out.println("请求ip："+getIpAddress(request)+"; >>>"+"请求Url："+request.getRequestURL()+">>>参数：{"+paramStr+"}");
        if (env.isDev()) {
        }
        if(1==1){
    		return true;
    	}
        boolean isH5 = false;
        if (StringUtils.equals(BizConstants.H, namespace)) {
            isH5 = true;
            request.setAttribute("isH5",true);
            if (pageAction.contains(actionName)) {

            } else {
                String token = CookieCheckUtils.getCookie(request, Constants.XDJ_JTOKEN);
                String jtoken = request.getParameter(Constants.JTOKEN);
                if (!(StringUtils.isNotBlank(jtoken) && StringUtils.equals(token, jtoken))) {
                    WelinkVO welinkVO = new WelinkVO();
                    welinkVO.setCode(BizErrorEnum.APP_SECRET_ERROR.getCode());
                    welinkVO.setMsg(BizErrorEnum.APP_SECRET_ERROR.getMsg());
                    welinkVO.setStatus(0);
                    response.setContentType("application/json;charset=utf-8");
                    response.getWriter().write(JSON.toJSONString(welinkVO));
                    return false;
                }
            }
        }

        String clientCode = "";
        if (pageAction.contains(actionName) || isH5 || StringUtils.equals(actionName, "alipayCallBack") || StringUtils.equals(actionName, "preLoad") || StringUtils.equals(actionName, "sarah") || StringUtils.equals(actionName, "transChecker")
                || StringUtils.equals(actionName, "hOrder")) {
            return true;
        } else {
        	if(1==1){
        		return true;
        	}
            //排序参数
            Map<String, String> paraMap = new TreeMap<String, String>(
                    new Comparator<String>() {
                        public int compare(String obj1, String obj2) {
                            // 降序排序
                            return obj1.compareTo(obj2);
                        }
                    }
            );

            for (Object s : values.keySet()) {
                String v = ((String[]) values.get(s))[0];
                paraMap.put((String) s, v);
                if (StringUtils.equals(tm, (String) s)) {
                    clientCode = ((String[]) values.get(s))[0];
                }
            }
            //app校验失败
            if (StringUtils.isBlank(clientCode)) {
                WelinkVO welinkVO = new WelinkVO();
                welinkVO.setCode(BizErrorEnum.APP_SECRET_ERROR.getCode());
                welinkVO.setMsg(BizErrorEnum.APP_SECRET_ERROR.getMsg());
                welinkVO.setStatus(0);
                response.setContentType("application/json;charset=utf-8");
                response.getWriter().write(JSON.toJSONString(welinkVO));
                return false;
            }
            Set<String> keySet = paraMap.keySet();
            Iterator<String> iter = keySet.iterator();
            String valuesStr = "";
            while (iter.hasNext()) {
                String key = iter.next();
                if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(paraMap.get(key))) {
                    if (!StringUtils.equals(tm, key)) {
                        valuesStr += paraMap.get(key);
                    }
                }
            }
            //233adfad
            boolean valid = false;
            Date nDate = new Date();
            long nDatel = nDate.getTime() / 60000;
            long start = nDatel - 5;
            for (; start < nDatel + 5; start++) {
                long code = TokenUtil.fetchCode(valuesStr, start);
                if (StringUtils.equals(clientCode, String.valueOf(code))) {
                    valid = true;
                    break;
                }
            }
            if (!valid) {
                WelinkVO welinkVO = new WelinkVO();
                welinkVO.setCode(BizErrorEnum.APP_SECRET_ERROR.getCode());
                welinkVO.setMsg(BizErrorEnum.APP_SECRET_ERROR.getMsg());
                welinkVO.setStatus(0);
                response.setContentType("application/json;charset=utf-8");
                response.getWriter().write(JSON.toJSONString(welinkVO));
                return false;
            }

        }
        return true;
    }

    //在业务处理器处理请求执行完成后,生成视图之前执行的动作
    @Override
    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {
        // TODO Auto-generated method stub
        //log.info("==============执行顺序: 2、postHandle================");
    	if(null != modelAndView){
    		ModelMap model = new ModelMap();
    		org.apache.shiro.subject.Subject currentUser = SecurityUtils.getSubject();
    		Session session = currentUser.getSession();
    		if (null != session) {
    			model.addAttribute(BizConstants.OPENID, (String) session.getAttribute(BizConstants.OPENID));
    			model.addAttribute(BizConstants.PROFILE_ID, session.getAttribute(BizConstants.PROFILE_ID));
    			model.addAttribute(BizConstants.P_USER_ID, PhenixUserHander.encodeUserId((Long) session.getAttribute(BizConstants.PROFILE_ID)));
    			//设置默认站点信息
    			long profileId = -1l;
    			//userService.setCommunity(model, session, response, profileId);
    		}
    		
    		String ua = request.getHeader("user-agent");
    		if (ua.indexOf("micromessenger") > 0) {// 是微信浏览器
    			//validation = true;
    			model.addAttribute("isWx", "1");
    		}else{
    			model.addAttribute("isWx", "0");
    		}
    		modelAndView.addAllObjects(model);
    	}
    }

    /**
     * 在DispatcherServlet完全处理完请求后被调用
     * <p/>
     * 当有拦截器抛出异常时,会从当前拦截器往回执行所有的拦截器的afterCompletion()
     */
    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        // TODO Auto-generated method stub
        //log.info("==============执行顺序: 3、afterCompletion================");
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
