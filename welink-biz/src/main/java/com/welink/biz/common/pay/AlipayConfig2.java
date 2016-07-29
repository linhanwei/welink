package com.welink.biz.common.pay;

import org.apache.commons.lang.StringUtils;

import java.security.interfaces.RSAPrivateKey;

/**
 * Created by daniel on 14-10-20.
 */
/* *
 *类名：AlipayConfig
 *功能：基础配置类
 *详细：设置帐户有关信息及返回路径
 *版本：3.3
 *日期：2012-08-10
 *说明：
 *以下代码只是为了方便商户测试而提供的样例代码，商户可以根据自己网站的需要，按照技术文档编写,并非一定要使用该代码。
 *该代码仅供学习和研究支付宝接口使用，只是提供一个参考。

 *提示：如何获取安全校验码和合作身份者ID
 *1.用您的签约支付宝账号登录支付宝网站(www.alipay.com)
 *2.点击“商家服务”(https://b.alipay.com/order/myOrder.htm)
 *3.点击“查询合作者身份(PID)”、“查询安全校验码(Key)”

 *安全校验码查看时，输入支付密码后，页面呈灰色的现象，怎么办？
 *解决方法：
 *1、检查浏览器配置，不让浏览器做弹框屏蔽设置
 *2、更换浏览器或电脑，重新登录查询。
 */

public class AlipayConfig2 {

    public static final String WELINK_ONLINE_DOMAIN = "unesmall.com";

    public static boolean partner_key_loaded = false;

    //↓↓↓↓↓↓↓↓↓↓请在这里配置您的基本信息↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
    // 合作身份者ID，以2088开头由16位纯数字组成的字符串
    public static String partner = "2088611242254399";// "2088611242254399"; //"2088101568353491";//TODO:测试账号PID   online  2088611242254399

    public static String test_partner = "2088101568353491";
    // 支付宝的公钥，无需修改该值
    public static String ali_public_key = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCnxj/9qwVfgoUh/y2W89L6BkRAFljhNhgPdyPuBV64bfQNN1PjbCzkIM6qRdKBoLPXmKKMiFYnkd6rAoprih3/PrQEB/VsW8OoM8fxn67UDYuyBTqA23MML9q1+ilIZwBC2AQ2UBVOrFXfFl75p6/B5KsiNG9zpgmLCUYuLkxpLQIDAQAB";

    public static String test_007_ali_public_key = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC65t90Pk3Z+nNaWLTCvaReVF7W/4e3IuaI9OeAYZH6RG2GgcwJN6kYr+j4VD4VD5u5E3yU3W2rAoBLst/nDvI/edjw+u/SieGYXpqLIa9L7bIi5sB2QPikJ8Q1+7swNCXjCsIwji2/4/cvDjYAj+IpsXGPw83HOVYonoJSRl5P6QIDAQAB";

    public static RSAPrivateKey PARTNER_PRIVATE_KEY = null;

    public static String h5_ali_public_key = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDCLQ/n91Nz2DducFUzVCHkAa/7OBqKfrvy54ww FJg5ET2Eom78ppHflrXT44CVdH9W39dnzCnsup/jTv9WHewAHPfubLRPwARR057mW9cjwgCx4HHM 5pyjg12frtZ7YStbegkQRhX8BAf8GtOLhJA3fUHzFNyW95By9l0DT/cqfQIDAQAB";

    public static String ALIPAY_APP_ID = "2015020500030457";

    // 商户的私钥
    public static String private_key = "ICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAM0Fb0z35B7tyWljD4Ue2sMa/xE91BjZO930RBrfuqksdk/iRWgwsACzECr2WdukdrFKXS0qnPm8ctqWg2ZCHFFbkQWm81K8DvF5DY6D/+pnhv3myut3jsW+6Lr6bLpcbuclZIzxNIJiKJKZXlhQhbvAeozlzEEZd9v/PChoookpAgMBAAECgYEAtgzHYh8Uj2oyWMQ/ucNGGc1VBKhUN6ReBp6qAvr7MtFqVVDktLnW7ygRiTSrYd/ODWutBTg7n354tiTElP7LSNK1I8LaO4RiM1V7IlXbm4HojnwzJ6C9tIJbqz/36eNM5aKOd4SZ9369OwK05EGT6XfgFgPPvRD1di7ts/TnU0kCQQD45mXQqirDxKwuCWKlWlZVfjpU9AkQliUmV7ahacNaeP6nEv2ozUoUS8zRXwgtPpj/Y0eLYwphNAv86CCQbkwrAkEA0t6cUVz8TUpVYKGdflnogizerqnq8hlyoBROCKukz1dg/JJbmQXir6jWkYLA1xOHq4mReawDqTe8hzvU8c0R+wJAQXS94+FMe812BmlbcubN+4/FoV/IHn+N54Z7tflNcbaOKbv5z3GlgK7qf0lBqjxjGg6u2v5B1YxNdsZJAhKvnwJBAJhHF/2Nt8mc94Db1R8skEKJYpohrL/+bo95eky6nz74K9rZWnpsDKKru5/DG711phDTPJHwDYjC7kt+gq3HWMcCQE9b3rU44bnTrVLF79kz36AnxHHI9QebDa7w3EK9BYf4EOBSdSj28GlNtLTzuzMnujRHHe9vKZfBHq75pxVRLZI=";//TODO:测试账号MD5 KEY

    //↑↑↑↑↑↑↑↑↑↑请在这里配置您的基本信息↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑

    public static final String Notify_URL = "http://m." + WELINK_ONLINE_DOMAIN + "/api/m/1.0/alipayCallBack.htm";

    public static final String APP_DAILY_Notify_URL = "http://m.7nb.com.cn/api/m/1.0/alipayCallBack.htm";

    public static final String DAILY_Notify_URL = "http://m.7nb.com.cn/api/h/1.0/listPage.htm";

    public static final String ONLINE_Notify_URL = "http://m." + WELINK_ONLINE_DOMAIN + "/api/h/1.0/listPage.htm";

    public static final String DEV_H5_NOTIFY_URL = "http://m.7nb.com.cn/api/h/1.0/hAlipayCallBack.htm";

    public static final String ONLINE_H5_NOTIFY_URL = "http://m." + WELINK_ONLINE_DOMAIN + "/api/h/1.0/hAlipayCallBack.htm";

    public static final String ONLINE_H5_GUIDE_URL = "http://m." + WELINK_ONLINE_DOMAIN + "/api/h/1.0/alipayGuide.htm";

    public static final String DAILY_H5_GUIDE_URL = "http://m.7nb.com.cn/api/h/1.0/alipayGuide.htm";

    // 调试用，创建TXT日志文件夹路径
    public static String log_path = "/home/admin/logs/welinkserver/";//TODO:

    // 字符编码格式 目前支持 gbk 或 utf-8
    public static String input_charset = "utf-8";

    public static String V = "2.0";

    public static String FORMAT = "xml";

    public static String OUT_OF_DATE_TIME = "2d";

    public static String ALIPAY_PAY_INTERFACE = "mobile.securitypay.pay";

    public static String ALIPAY_AUTH_INTERFACE = "alipay.wap.trade.create.direct";

    public static String ALIPAY_AUTH_EXECUTE = "alipay.wap.auth.authAndExecute";// "alipay.wap.auth.authandexecute";

    public static String ALIPAY_WAP_PAY_INTERFACE = "create_direct_pay_by_user";

    public static String ALIPAY_SELLER_ACCOUNT = "iwelink@163.com";

    public static String TEST_ALIPAY_SELLER_ACCOUNT = "alipay-test07@alipay.com";

    public static String ALIPAY_OUT_TIME = "36000";

    public static String ALIPAY_OUT_TIME_2_HOUR = "120";//分钟

    public static String ALIPAY_OUT_TIME_24_HOUR = "1440";//分钟

    public static String ALIPAY_AUTH_URL = "http://wappaygw.alipay.com/service/rest.htm?";

    public static final String ALIPAY_GATEWAY_NEW = "https://mapi.alipay.com/gateway.do?";

    // 签名方式 不需修改
    public static String sign_type = "RSA";

    public static String wap_sign_type = "0001";

    public static String key = "";//没有用到MD5方式

    public static RSAPrivateKey getPARTNER_PRIVATE_KEY() {
        return PARTNER_PRIVATE_KEY;
    }

    public static void setPARTNER_PRIVATE_KEY(RSAPrivateKey PARTNER_PRIVATE_KEY) {
        AlipayConfig2.PARTNER_PRIVATE_KEY = PARTNER_PRIVATE_KEY;
    }

    public boolean isPartner_key_loaded() {
        return partner_key_loaded;
    }

    public void setPartner_key_loaded(boolean partner_key_loaded) {
        this.partner_key_loaded = partner_key_loaded;
    }

    public static enum AlipayErrorCode {

        SYSTEM_EXCEPTION("0000", "system exception"),

        PARAM_ERROR("0001", "common params illegal"),

        SIGN_ILLEGAL("0002", "sign illegal"),

        SERVICE_NOT_EXIST("0003", "service not exist"),

        REQUEST_DATA_ILLEGAL("0004", "req_data illegal"),

        PARTNER_ILLEGAL("0005", "partner illegal"),

        SIGNE_METHOD_ERROR("0006", "sec_id not exist"),

        BIZ_PARA_NOT_SETED("0007", "biz params illegal"),

        BIZ_PARA_TOO_LONG("0008", "biz params too long"),

        SELLER_ACCOUNT_NOT_EXIST("0009", "seller_account_not_ match"),;

        private String code;

        private String name;

        AlipayErrorCode(String code, String name) {
            this.code = code;
            this.name = name;

        }

        public String getCode() {
            return code;
        }

        public static String getAlipayErrorCode(String code) {
            for (AlipayErrorCode c : AlipayErrorCode.values()) {
                if (StringUtils.equals(c.getCode(), code)) {
                    return c.getName();
                }
            }
            return "";
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

}