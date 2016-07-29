package com.welink.biz.common.MSG;

import com.alibaba.fastjson.JSON;
import com.welink.biz.common.cache.CheckNOGenerator;
import com.welink.biz.common.constants.TimeConstants;
import com.welink.biz.common.model.SmsResponseTpl;
import com.welink.commons.commons.BizConstants;
import net.spy.memcached.MemcachedClient;
import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * Created by daniel on 14-9-12.
 */
@Service
public class SMSUtils {

    public static final String APIKEY = "aa0791acb6e280103d83f283e7eef954";

    public static final String URL_WITH_TEMPLATE = "http://yunpian.com/v1/sms/tpl_send.json";

    public static final String URL = "http://yunpian.com/v1/sms/send.json";

    private static org.slf4j.Logger log = LoggerFactory.getLogger(SMSUtils.class);

    private static final long CHECK_NO_TPL = 1;

    private static final long REGISTER_CHECK_NO_TPL = 5;//注册用模板

    private static final long SMS_TPL = 2;

    public static void main(String[] args) throws IOException {
        //18258180028
        String mobile = "15622395287";
        
        String msg = "优理氏test";
        boolean sendResult = false;

        String tpl_value = "#code#=" + "123456" + "&#company#=" + "米酷SDP" + "&#app#=" + "米酷哈哈";
        //String tpl_value = msg + "&#company#=" + BizConstants.COMPANY_NAME;
        //String sr = HttpRequest.sendPost(URL_WITH_TEMPLATE, "apikey=" + APIKEY + "&mobile=" + mobile + "&tpl_id=" + REGISTER_CHECK_NO_TPL + "&tpl_value=" + tpl_value);
        String sr = JavaSmsApi.tplSendSms(APIKEY, REGISTER_CHECK_NO_TPL, tpl_value, mobile);
        
        System.out.println(sr);
        SmsResponseTpl responseTpl = JSON.parseObject(sr, SmsResponseTpl.class);
        if (responseTpl.getCode() != 0) {//失败
            sendResult = false;
            log.warn("短信息发送失败. mobile:" + mobile);
        } else {
            sendResult = true;
        }

    }


    @Resource
    private MemcachedClient memcachedClient;

    public static boolean sendSms(String msg, String mobile) {
        boolean sendResult = false;

        String tpl_value = "#msg#=" + msg + "&#company#=" + BizConstants.COMPANY_NAME;
        String sr = HttpRequest.sendPost(URL_WITH_TEMPLATE, "apikey=" + APIKEY + "&mobile=" + mobile + "&tpl_id=" + REGISTER_CHECK_NO_TPL + "&tpl_value=" + tpl_value);
        SmsResponseTpl responseTpl = JSON.parseObject(sr, SmsResponseTpl.class);
        if (responseTpl.getCode() != 0) {//失败
            sendResult = false;
            log.warn("短信息发送失败. mobile:" + mobile);
        } else {
            sendResult = true;
        }
        return sendResult;
    }

    public static boolean sendSmsForAlipayBack(String msg, String mobile) {
        boolean sendResult = false;

        String tpl_value = "#code#=" + msg + "&#company#=" + BizConstants.COMPANY_NAME;
        String sr = HttpRequest.sendPost(URL_WITH_TEMPLATE, "apikey=" + APIKEY + "&mobile=" + "18605816526" + "&tpl_id=" + "7" + "&tpl_value=" + tpl_value);
        SmsResponseTpl responseTpl = JSON.parseObject(sr, SmsResponseTpl.class);
        if (responseTpl.getCode() != 0) {//失败
            sendResult = false;
            log.warn("短信息发送失败. mobile:" + mobile);
        } else {
            sendResult = true;
        }
        return sendResult;
    }


    /**
     * 此方法专为验证码下发服务，下发后进行缓存，请确保传入手机号码为1个
     *
     * @param mobile
     * @param
     * @return
     */
    public boolean sendCheckCodeAndCacheCode(String mobile) {
        boolean sendResult = false;
        //验证码是否已经失效
        String cachedCode = (String) memcachedClient.get(BizConstants.CHECK_NO_PREFIX + mobile);//jedis.get(BizConstants.CHECK_NO_PREFIX + mobile);
        String checkCode = CheckNOGenerator.getFixLenthString(BizConstants.CHECK_NO_LEN);
        if (StringUtils.isNotBlank(cachedCode) && cachedCode.length() == BizConstants.CHECK_NO_LEN) {
            checkCode = cachedCode;
        }
        //发送验证码
        String tpl_value = "#code#=" + checkCode + "&#company#=" + BizConstants.COMPANY_NAME + "&#app#=" + BizConstants.APP_NAME;
        //String sr = HttpRequest.sendPost(URL_WITH_TEMPLATE, "apikey="+APIKEY+"&mobile="+mobile+"&tpl_id="+CHECK_NO_TPL+"&tpl_value="+tpl_value);
        String sr = "";
        try {
            sr = JavaSmsApi.tplSendSms(APIKEY, REGISTER_CHECK_NO_TPL, tpl_value, mobile);
        } catch (IOException e) {
            log.error("send check code failed. exp:" + e.getMessage());
        }
        SmsResponseTpl responseTpl = JSON.parseObject(sr, SmsResponseTpl.class);
        if (responseTpl.getCode() != 0) {//失败
            sendResult = false;
            log.warn("短信息发送失败. mobile:" + mobile + ",reason:" + responseTpl.getMsg());
        } else {
            sendResult = true;
            //cache the code
            memcachedClient.set(BizConstants.CHECK_NO_PREFIX + mobile, TimeConstants.REDIS_EXPIRE_SECONDS_10, checkCode);
            log.info("send check code success. mobile:" + mobile);
        }
        return sendResult;
    }
    
    public SmsResponseTpl sendCheckCodeAndCacheCodeSmsResponseTpl(String mobile) {
        boolean sendResult = false;
        //验证码是否已经失效
        String cachedCode = (String) memcachedClient.get(BizConstants.CHECK_NO_PREFIX + mobile);//jedis.get(BizConstants.CHECK_NO_PREFIX + mobile);
        String checkCode = CheckNOGenerator.getFixLenthString(BizConstants.CHECK_NO_LEN);
        if (StringUtils.isNotBlank(cachedCode) && cachedCode.length() == BizConstants.CHECK_NO_LEN) {
            checkCode = cachedCode;
        }
        //发送验证码
        String tpl_value = "#code#=" + checkCode + "&#company#=" + BizConstants.COMPANY_NAME + "&#app#=" + BizConstants.APP_NAME;
        //String sr = HttpRequest.sendPost(URL_WITH_TEMPLATE, "apikey="+APIKEY+"&mobile="+mobile+"&tpl_id="+CHECK_NO_TPL+"&tpl_value="+tpl_value);
        String sr = "";
        try {
            sr = JavaSmsApi.tplSendSms(APIKEY, REGISTER_CHECK_NO_TPL, tpl_value, mobile);
        } catch (IOException e) {
            log.error("send check code failed. exp:" + e.getMessage());
        }
        SmsResponseTpl responseTpl = JSON.parseObject(sr, SmsResponseTpl.class);
        if (responseTpl.getCode() != 0) {//失败
            sendResult = false;
            log.warn("短信息发送失败. mobile:" + mobile + ",reason:" + responseTpl.getMsg());
        } else {
            sendResult = true;
            //cache the code
            memcachedClient.set(BizConstants.CHECK_NO_PREFIX + mobile, TimeConstants.REDIS_EXPIRE_SECONDS_10, checkCode);
            log.info("send check code success. mobile:" + mobile);
        }
        return responseTpl;
    }
    
    
    public SmsResponseTpl sendCheckCodeAndCacheCodeSmsResponseTplTest(String mobile, String checkCode) {
        boolean sendResult = false;
        //验证码是否已经失效
        String cachedCode = (String) memcachedClient.get(BizConstants.CHECK_NO_PREFIX + mobile);//jedis.get(BizConstants.CHECK_NO_PREFIX + mobile);
        //String checkCode = CheckNOGenerator.getFixLenthString(BizConstants.CHECK_NO_LEN);
        if (StringUtils.isNotBlank(cachedCode) && cachedCode.length() == BizConstants.CHECK_NO_LEN) {
            checkCode = cachedCode;
        }
        //发送验证码
        String tpl_value = "#code#=" + checkCode + "&#company#=" + BizConstants.COMPANY_NAME + "&#app#=" + BizConstants.APP_NAME;
        //String sr = HttpRequest.sendPost(URL_WITH_TEMPLATE, "apikey="+APIKEY+"&mobile="+mobile+"&tpl_id="+CHECK_NO_TPL+"&tpl_value="+tpl_value);
        String sr = "";
        try {
            sr = JavaSmsApi.tplSendSms(APIKEY, REGISTER_CHECK_NO_TPL, tpl_value, mobile);
        } catch (IOException e) {
            log.error("send check code failed. exp:" + e.getMessage());
        }
        SmsResponseTpl responseTpl = JSON.parseObject(sr, SmsResponseTpl.class);
        if (responseTpl.getCode() != 0) {//失败
            sendResult = false;
            log.warn("短信息发送失败. mobile:" + mobile + ",reason:" + responseTpl.getMsg());
        } else {
            sendResult = true;
            //cache the code
            //memcachedClient.set(BizConstants.CHECK_NO_PREFIX + mobile, TimeConstants.REDIS_EXPIRE_SECONDS_10, checkCode);
            log.info("send check code success. mobile:" + mobile);
        }
        return responseTpl;
    }
    
    /*
    {
        "code": 0,
        "msg": "OK",
        "result": {
            "count": 1,   //成功发送的短信个数
            "fee": 1,     //扣费条数，70个字一条，超出70个字时按每67字一条计
            "sid": 1097   //短信id
        }
    }
    * */


}
