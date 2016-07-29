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

public class AlipayConfig {

    //public static final String WELINK_ONLINE_DOMAIN = "unesmall.com";
	//public static final String WELINK_ONLINE_DOMAIN = "wechat.unesmall.com";
	public static final String WELINK_ONLINE_DOMAIN = "miku.unesmall.com";
	//public static final String WELINK_ONLINE_DOMAIN = "lgc110.6655.la:10609/welink";
	
	public static final String WELINK_ONLINE_DOMAIN_TEST = "test.unesmall.com/";

    public static boolean partner_key_loaded = false;

    //↓↓↓↓↓↓↓↓↓↓请在这里配置您的基本信息↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
    // 合作身份者ID，以2088开头由16位纯数字组成的字符串
    public static String partner = "2088101295767275";// "2088611242254399"; //"2088101568353491";//TODO:测试账号PID   online  2088611242254399
    public static String test_partner = "2088101295767275";
    
    public static String alipayAccount = "service@unescn.com";
    
    public static String alipayAccount_test = "service@unescn.com";
    
    // 支付宝的公钥，无需修改该值
    //public static String ali_public_key = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDJOMA5oZzd6lklWCYBRKDmrJwgtUeVpvfbHrJUNTii7nQ8fUSknuMsQ8H3wnRvjm6ARh0pm980i8VdvkUu6s4tTv3llyVQbkYOR4GH+urLLGEUx0r815tmP0+oMMCOhaSjEe96fq0hqXcDnnrC0JhfTvFv6l3oCSY2r7sOG9E61wIDAQAB";
    public static String ali_public_key = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCnxj/9qwVfgoUh/y2W89L6BkRAFljhNhgPdyPuBV64bfQNN1PjbCzkIM6qRdKBoLPXmKKMiFYnkd6rAoprih3/PrQEB/VsW8OoM8fxn67UDYuyBTqA23MML9q1+ilIZwBC2AQ2UBVOrFXfFl75p6/B5KsiNG9zpgmLCUYuLkxpLQIDAQAB";
    //public static String ali_public_key = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDDI6d306Q8fIfCOaTXyiUeJHkrIvYISRcc73s3vF1ZT7XN8RNPwJxo8pWaJMmvyTn9N4HQ632qJBVHf8sxHi/fEsraprwCtzvzQETrNRwVxLO5jVmRGi60j8Ue1efIlzPXV9je9mkjzOmdssymZkh2QhUrCmZYI/FCEa3/cNMW0QIDAQAB";
    
    public static String test_007_ali_public_key = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDDI6d306Q8fIfCOaTXyiUeJHkrIvYISRcc73s3vF1ZT7XN8RNPwJxo8pWaJMmvyTn9N4HQ632qJBVHf8sxHi/fEsraprwCtzvzQETrNRwVxLO5jVmRGi60j8Ue1efIlzPXV9je9mkjzOmdssymZkh2QhUrCmZYI/FCEa3/cNMW0QIDAQAB";

    public static RSAPrivateKey PARTNER_PRIVATE_KEY = null;

    public static String h5_ali_public_key = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDDI6d306Q8fIfCOaTXyiUeJHkrIvYISRcc73s3vF1ZT7XN8RNPwJxo8pWaJMmvyTn9N4HQ632qJBVHf8sxHi/fEsraprwCtzvzQETrNRwVxLO5jVmRGi60j8Ue1efIlzPXV9je9mkjzOmdssymZkh2QhUrCmZYI/FCEa3/cNMW0QIDAQAB";

    public static String ALIPAY_APP_ID = "2015092100309094";

    // 商户的私钥   PKCS8格式的私钥
    public static String private_key = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAMka1ievYjMuuvhb"+
									    "xRJG23O66oODaC2B8oXPv1C4144AdgZKVDNFW82HMyfdk1C6THF0aDR6SEha8Xl8"+
									    "K72dFj5vGOHrcqxF6jfqY21npAv9v8WdTWqR+LYhUMvhIzI/zq460PUgXuhf+4gY"+
									    "TcS5w/x7vkLRCADePIq/ibsc6jkBAgMBAAECgYEAk+FoGUaer4RteLQC9q2kMjYA"+
									    "vTSkJtKnxD5AMNu7pnAwWe/OeuVF3JzOwUHuUi6sh8FR30UMYih96RemEoixEWpZ"+
									    "lgO1LcvnIh9jAVSfTpgcacgeY71HV6wVoYYWDUXlXFBBF2E71ZUBMCa2NnFuSvPI"+
									    "iLFYZ2NVYqgla2ZeRQECQQDn11IMr0UXpko7HO/lMJ6pSgsSPIci+iix+BP8h26z"+
									    "OS6MmmW5Npk6xxhlRfm4aRdnRkHas230p0H8XUalHlyLAkEA3g+Vfn3JFlskYsEM"+
									    "AL9k4Ezr/DvVAflXcipo3zawXLf+i7g4L/rNL5+AJWXPjeiu19mYSG2ZvAs5IwDV"+
									    "o0L2IwJBAKleJsW3gS0ewVnf8O0pEK+xtNbUuxB1WidxiQBR3DA4FOaIoMmmtlg3"+
									    "PH43mY/7zioXLGLd8/Tn/4+igMp9nLMCQGgslWxO5Dw5q7ssZ04ee9uCGp8tDoOr"+
									    "jqt/W82DoX8NzjuLw8g2d3Xk9MAWoCUpyIR4jylDSYDfHwHdzVuW+VMCQGhB/b+7"+
									    "zukvthddVvkNT5ZIuzRFXQlByeoyWhrwj6+MsGku7l43phmMagN1EgnNtz895gAs"+
									    "9BwngO9xkbRiuNU=";
    //↑↑↑↑↑↑↑↑↑↑请在这里配置您的基本信息↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑

    public static final String Notify_URL = "http://" + WELINK_ONLINE_DOMAIN + "/api/m/1.0/alipayCallBack.htm";	//app生产

    //public static final String APP_DAILY_Notify_URL = "http://m.7nb.com.cn/api/m/1.0/alipayCallBack.htm";
    public static final String APP_DAILY_Notify_URL = "http://" + WELINK_ONLINE_DOMAIN_TEST + "/api/m/1.0/alipayCallBack.htm";	//app开发测试

    //public static final String DAILY_Notify_URL = "http://m.7nb.com.cn/api/h/1.0/listPage.htm";
    public static final String DAILY_Notify_URL = "http://" + WELINK_ONLINE_DOMAIN + "/api/h/1.0/listPage.htm";

    public static final String ONLINE_Notify_URL = "http://" + WELINK_ONLINE_DOMAIN + "/api/h/1.0/listPage.htm";

    //public static final String DEV_H5_NOTIFY_URL = "http://m.7nb.com.cn/api/h/1.0/hAlipayCallBack.htm";
    public static final String DEV_H5_NOTIFY_URL = "http://" + WELINK_ONLINE_DOMAIN + "/api/h/1.0/hAlipayCallBack.htm";

    public static final String ONLINE_H5_NOTIFY_URL = "http://" + WELINK_ONLINE_DOMAIN + "/api/h/1.0/hAlipayCallBack.htm";

    public static final String ONLINE_H5_GUIDE_URL = "http://" + WELINK_ONLINE_DOMAIN + "/api/h/1.0/alipayGuide.htm";

    //public static final String DAILY_H5_GUIDE_URL = "http://m.7nb.com.cn/api/h/1.0/alipayGuide.htm";
    public static final String DAILY_H5_GUIDE_URL = "http://" + WELINK_ONLINE_DOMAIN + "/api/h/1.0/alipayGuide.htm";

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

    //public static String ALIPAY_SELLER_ACCOUNT = "iwelink@163.com";
    public static String ALIPAY_SELLER_ACCOUNT = "service@unescn.com";

    //public static String TEST_ALIPAY_SELLER_ACCOUNT = "alipay-test07@alipay.com";
    public static String TEST_ALIPAY_SELLER_ACCOUNT = "service@unescn.com";

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
        AlipayConfig.PARTNER_PRIVATE_KEY = PARTNER_PRIVATE_KEY;
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