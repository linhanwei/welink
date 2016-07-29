package com.welink.web.resource;

import com.alibaba.fastjson.JSON;
import com.welink.biz.common.pay.AlipayConfig;
import com.welink.biz.common.pay.AlipayNotify;
import com.welink.commons.domain.AlipayBackDO;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by daniel on 15-4-9.
 */
@RestController
public class TraceAlipayCallBack {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(TraceAlipayCallBack.class);

    private static final String WAIT_BUYER_PAY = "WAIT_BUYER_PAY";

    private static final String TRADE_SUCCESS = "TRADE_SUCCESS";

    private static final String TRADE_FINISH = "TRADE_FINISHED";


    @RequestMapping(value = {"/h/1.0/hAlipayCallBack.json"}, produces = "application/json;charset=utf-8")
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.info("enter h1 alipaycallback................");
        //获取支付宝POST过来反馈信息
        Map<String, String> params = new HashMap<String, String>();
        Map requestParams = request.getParameterMap();
        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
            //valueStr = new String(valueStr.getBytes("ISO-8859-1"), "gbk");
            params.put(name, valueStr);
        }
        //RSA签名解密
        if (AlipayConfig.wap_sign_type.equals("0001")) {
            params = AlipayNotify.decrypt(params);
        }
        //获取参数
        //XML解析notify_data数据
        Document doc_notify_data = DocumentHelper.parseText(params.get("notify_data"));
        String sign = params.get("sign");
        //商户订单号
        String out_trade_no = doc_notify_data.selectSingleNode("//notify/out_trade_no").getText();
        //支付宝交易号
        String trade_no = doc_notify_data.selectSingleNode("//notify/trade_no").getText();
        //交易状态
        String trade_status = doc_notify_data.selectSingleNode("//notify/trade_status").getText();
        String payment_type = doc_notify_data.selectSingleNode("//notify/payment_type").getText();
        String subject = doc_notify_data.selectSingleNode("//notify/subject").getText();
        String buyer_email = doc_notify_data.selectSingleNode("//notify/buyer_email").getText();
        String gmt_create = doc_notify_data.selectSingleNode("//notify/gmt_create").getText();
        String notify_type = doc_notify_data.selectSingleNode("//notify/notify_type").getText();
        String quantity = doc_notify_data.selectSingleNode("//notify/quantity").getText();
        String notify_time = doc_notify_data.selectSingleNode("//notify/notify_time").getText();
        String seller_id = doc_notify_data.selectSingleNode("//notify/seller_id").getText();
        String is_total_fee_adjust = doc_notify_data.selectSingleNode("//notify/is_total_fee_adjust").getText();
        String total_fee = doc_notify_data.selectSingleNode("//notify/total_fee").getText();
        String gmt_payment = doc_notify_data.selectSingleNode("//notify/gmt_payment").getText();
        String seller_email = doc_notify_data.selectSingleNode("//notify/seller_email").getText();
        String price = doc_notify_data.selectSingleNode("//notify/price").getText();
        String notify_id = doc_notify_data.selectSingleNode("//notify/notify_id").getText();
        String use_coupon = doc_notify_data.selectSingleNode("//notify/use_coupon").getText();
        String buyer_id = doc_notify_data.selectSingleNode("//notify/buyer_id").getText();
        String sign_type = AlipayConfig.wap_sign_type;
        final AlipayBackDO callBackDO = new AlipayBackDO();
        log.info("out trade no :" + trade_no + ", tradestatus:" + trade_status + ",total fee:" + payment_type + ",buyerId:" + buyer_id
                + ",gmt_payment:" + gmt_payment + ",gmt_created:" + gmt_create + ",buyerEmail:" + buyer_email + ",tradestatus:" + trade_status + ",out_trade_no:" + out_trade_no);
        return JSON.toJSONString("success");
    }
}