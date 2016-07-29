package com.welink.web.resource.hpage;

import com.welink.biz.common.pay.AlipayConfig;
import com.welink.web.common.util.ParameterUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;

/**
 * Created by daniel on 15-4-6.
 */
@Controller
public class AlipayDrawboard {

    public String redirectUrl;

    @RequestMapping(value = {"/api/h/1.0/alipayDrawboard.htm"}, produces = "text/html;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
        String fm = ParameterUtil.getParameter(request, "fm");
        String userAgent = request.getHeader("User-Agent");
        //Mozilla/5.0 (iPhone; CPU iPhone OS 7_0 like Mac OS X; en-us) AppleWebKit/537.51.1 (KHTML, like Gecko) Version/7.0 Mobile/11A465 Safari/9537.53/micromessenger
        if (StringUtils.containsIgnoreCase(userAgent, "micromessenger")) {
            model.addAttribute("fm", "<img src=\"http://welinklife.b0.upaiyun.com/1/LTE=/SVRFTS1QVUJMSVNI/MA==/20150406/vNTT-0-1428313240953.jpg\"/>");
        } else {
            fm = URLDecoder.decode(fm, "utf-8");
            fm = AlipayConfig.ALIPAY_AUTH_URL + fm;
            fm = "<script>location.href=\"" + fm + "\";</script>页面正在跳转";
            model.addAttribute("fm", fm);
        }
        return "alipayDrawboard";
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }
}
