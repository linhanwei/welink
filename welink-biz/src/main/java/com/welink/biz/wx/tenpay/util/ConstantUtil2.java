package com.welink.biz.wx.tenpay.util;

import com.welink.commons.commons.WxPayModel;

import java.util.HashMap;

public class ConstantUtil2 {

    /**
     * 商家可以考虑读取配置文件
     */

    public static String PREPAYURL = "https://api.mch.weixin.qq.com/pay/unifiedorder";//获取预支付id的接口url

    public static final String WELINK_ONLINE_DOMAIN = "unesmall.com";

    public static final String GUOGEGE_WX_CONF_KEY = "1";

    public static final String XIAOWEILINJU_WX_CONF_KEY = "2";

    public static final String XIANGDANGJIA_WX_CONF_KEY = "3";

    public static final String GUOGEGE_TEST_WX_CONF_KEY = "101";

    public static final String GUOGEGE_AES_KEY = "HNXNh9eizCdfJFFbxChOI0e8aihXLWA2JhvBHUjHCeK";

    public static final String GUOGEGE_TOKEN = "wxfx2015dd";

    public static final String GUOGEGE_NOTIFY_URL = "http://m." + WELINK_ONLINE_DOMAIN + "/api/h/1.0/hWeiXinCallBack.htm";

    public static HashMap<String, WxPayModel> mcMap = new HashMap<>();

    static {
        //米酷
        WxPayModel gggWxPayModel = new WxPayModel();
        gggWxPayModel.setAppId("wxccc8e5b9786aa8ee");//wxccc8e5b9786aa8ee
        gggWxPayModel.setAppKey("741cfd9021b2d0e7f4c883027513f153");
        gggWxPayModel.setAppSecret("72830cb66db45f1aa0120850553f47ec");
        gggWxPayModel.setMch_id("1245066302");//商戶號已修改1245066302
        gggWxPayModel.setNotify_url("http://m." + WELINK_ONLINE_DOMAIN + "/api/h/1.0/hWeiXinCallBack.htm");
        mcMap.put(GUOGEGE_WX_CONF_KEY, gggWxPayModel);

    }

    public static String GUOGEGE_H5_APP_ID = "wx82d4b04a531ac1a3";//公众号appId
    public static String GUOGEGE_APP_KEY = "68f9e01f6152417192dd425457053dea";//公众号32位Api密钥
    public static String GUOGEGE_MCH_ID = "1242526802";//公众号财付通商户号
    public static String GUOGEGE_APP_SECRET = "d0088fb1eaa778f534e114d50bd545eb";//公众号secret

    //APP微信支付参数（米酷）
    public static String APP_APP_ID = "wx14f53a7c1a0ad934";//开放平台appId
    public static String APP_APP_SECRET = "af0d9d57d7cfb04526f345daf744dbef";//开放平台appSecret
    public static String PARTNER_ID = "1229980301";//开放平台财付通商户号
    public static String PARTNER_KEY = "e8cc55da5a5f3f84e56f4328fc7b406e";//开放平台财付通partner_key
    public static String TOKENURL = "https://api.weixin.qq.com/cgi-bin/token";//开放平台获取Token的URL
    public static String APPPREPAYURL = "https://api.weixin.qq.com/pay/genprepay";//开放平台获取Token的URL
    public static String APPKEY = "741cfd9021b2d0e7f4c883027513f153";//开放平台PaySignKey TODO：

}
