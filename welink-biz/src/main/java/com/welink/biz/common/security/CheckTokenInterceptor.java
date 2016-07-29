package com.welink.biz.common.security;

import com.alibaba.fastjson.JSON;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.Interceptor;
import com.welink.biz.common.model.WelinkAgent;
import com.welink.biz.util.TokenUtil;
import com.welink.buy.utils.Constants;
import com.welink.commons.commons.BizConstants;
import net.spy.memcached.MemcachedClient;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Created by daniel on 14-10-21.
 */
public class CheckTokenInterceptor implements Interceptor {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(CheckTokenInterceptor.class);

    private static final long serialVersionUID = 1L;

    private static final String tm = "sarah";//漂亮

    private static final List<String> pageAction = new ArrayList<>();

    @Resource
    private MemcachedClient memcachedClient;

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
    }

    @Override
    public void destroy() {

    }

    @Override
    public void init() {

    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    @Override
    public String intercept(ActionInvocation invocation) throws Exception {

        HttpServletRequest request = (HttpServletRequest) invocation.getInvocationContext().get(ServletActionContext.HTTP_REQUEST);
        ActionContext ac = invocation.getInvocationContext();
        String namespace = invocation.getProxy().getNamespace();
        namespace = StringUtils.substring(namespace, 5, 6);
        String actionName = ac.getName();
        boolean isH5 = false;
        if (StringUtils.equals(BizConstants.H, namespace)) {
            isH5 = true;
            if (pageAction.contains(actionName)) {

            } else {
                String token = CookieCheckUtils.getCookie(request, Constants.XDJ_JTOKEN);
                String jtoken = request.getParameter(Constants.JTOKEN);
                if (!(StringUtils.isNotBlank(jtoken) && StringUtils.equals(token, jtoken))) {
                    return "error";
                }
            }
        }
        //session -- version -- os
        //traceLog(request, actionName);
        Map values = request.getParameterMap();
        String clientCode = "";
        if (pageAction.contains(actionName) || isH5 || StringUtils.equals(actionName, "alipayCallBack") || StringUtils.equals(actionName, "preLoad") || StringUtils.equals(actionName, "sarah") || StringUtils.equals(actionName, "transChecker")
                || StringUtils.equals(actionName, "hOrder")) {
            return invocation.invoke();
        } else {
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
            if (StringUtils.isBlank(clientCode)) {
                return "error";
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
                System.out.println(key + ":" + paraMap.get(key));
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
                return "faied";
            }

        }
        String result = invocation.invoke();
        return result;

    }

    private void traceLog(HttpServletRequest request, String actionName) {
        String userAgent = request.getHeader(BizConstants.USER_AGENT);
        WelinkAgent welinkAgent = new WelinkAgent();
        try {
            if (StringUtils.isNotBlank(userAgent)) {
                welinkAgent = JSON.parseObject(userAgent, WelinkAgent.class);
            }
            if (null != welinkAgent) {
                log.info("action:" + actionName + ",os:" + welinkAgent.getMode() + ",version:" + welinkAgent.getVersion() + ",sysVersion:" + welinkAgent.getSystemVersion());
            }
        } catch (Exception e) {
            log.error("record client  failed... actionName:" + actionName);
        }
    }

    public static void main(String[] args) {
        Map<String, String> map = new TreeMap<String, String>(
                new Comparator<String>() {
                    public int compare(String obj1, String obj2) {
                        // 降序排序
                        return obj1.compareTo(obj2);
                    }
                }
        );

        map.put("b", "ccccc");
        map.put("d", "aaaaa");
        map.put("c", "bbbbb");
        map.put("a", "ddddd");
        map.put("2", "2");
        map.put("12", "12");

        Set<String> keySet = map.keySet();
        Iterator<String> iter = keySet.iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            System.out.println(key + ":" + map.get(key));
        }
    }
}
