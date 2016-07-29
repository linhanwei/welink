package com.welink.biz.wx.tenpay.util;

import com.welink.commons.commons.WxPayModel;

import java.util.HashMap;

public class ConstantUtil {

    /**
     * 商家可以考虑读取配置文件
     */

    public static String PREPAYURL = "https://api.mch.weixin.qq.com/pay/unifiedorder";//获取预支付id的接口url

    //public static final String WELINK_ONLINE_DOMAIN = "unesmall.com";
    //public static final String WELINK_ONLINE_DOMAIN = "120.24.102.187:8082";
    //public static final String WELINK_ONLINE_DOMAIN = "wechat.unesmall.com";
    public static final String WELINK_ONLINE_DOMAIN = "miku.unesmall.com";
    
    public static final String WELINK_ONLINE_DOMAIN_TEST = "test.unesmall.com";

    public static final String GUOGEGE_WX_CONF_KEY = "1";

    public static final String XIAOWEILINJU_WX_CONF_KEY = "2";

    public static final String XIANGDANGJIA_WX_CONF_KEY = "3";

    public static final String GUOGEGE_TEST_WX_CONF_KEY = "101";

    //-------------公众号 丽元堂start---------------------------
    /*public static final String GUOGEGE_AES_KEY = "HNXNh9eizCdfJFFbxChOI0e8aihXLWA2JhvBHUjHCeK";
    public static final String GUOGEGE_TOKEN = "wxfx2015dd";*/
    //-------------公众号 丽元堂end---------------------------
    
    //-------------公众号 米酷start---------------------------
    //public static final String GUOGEGE_AES_KEY = "32Nc2ZuUOm6okNMdW20oSdqFFzxS2qynQiuY9cXkmT";
    public static final String GUOGEGE_AES_KEY = "332Nc2ZuUOm6okNMdW20oSdqFFzxS2qynQiuY9cXkmT";
    public static final String GUOGEGE_TOKEN = "wxfx2015dd";
    //-------------公众号 米酷end---------------------------

    //public static final String GUOGEGE_NOTIFY_URL = "http://m." + WELINK_ONLINE_DOMAIN + "/api/h/1.0/hWeiXinCallBack.htm";
    public static final String GUOGEGE_NOTIFY_URL = "http://" + WELINK_ONLINE_DOMAIN + "/api/h/1.0/hWeiXinCallBack.htm";

    public static HashMap<String, WxPayModel> mcMap = new HashMap<>();

    static {
        //米酷
        WxPayModel gggWxPayModel = new WxPayModel();
        /*gggWxPayModel.setAppId("wxccc8e5b9786aa8ee");//wxccc8e5b9786aa8ee
        gggWxPayModel.setAppKey("741cfd9021b2d0e7f4c883027513f153");
        gggWxPayModel.setAppSecret("d0088fb1eaa778f534e114d50bd545eb");
        gggWxPayModel.setMch_id("1242526802");//商戶號已修改1245066302
        gggWxPayModel.setNotify_url("http://" + WELINK_ONLINE_DOMAIN_TEST + "/api/h/1.0/hWeiXinCallBack.htm");*/
        
        //-------------公众号 丽元堂start---------------------------
        gggWxPayModel.setAppId("wx82d4b04a531ac1a3");//wxccc8e5b9786aa8ee
        gggWxPayModel.setAppKey("741cfd9021b2d0e7f4c883027513f153");
        gggWxPayModel.setAppSecret("d0088fb1eaa778f534e114d50bd545eb");
        gggWxPayModel.setMch_id("1242526802");//商戶號已修改1245066302
        gggWxPayModel.setNotify_url("http://" + WELINK_ONLINE_DOMAIN_TEST + "/api/h/1.0/hWeiXinCallBack.htm");
        mcMap.put(XIAOWEILINJU_WX_CONF_KEY, gggWxPayModel);	//测试
        //-------------公众号 丽元堂end---------------------------
        
        //-------------公众号 米酷SDP start---------------------------
        WxPayModel gggWxPayModel2 = new WxPayModel();
        gggWxPayModel2.setAppId("wxec8055b78fdb49d4");//wxccc8e5b9786aa8ee
        gggWxPayModel2.setAppKey("741cfd9021b2d0e7f4c883027513f153");
        gggWxPayModel2.setAppSecret("545921479fb8b3149de3f89681998961");
        gggWxPayModel2.setMch_id("1323192201");//商戶號已修改1323192201
        gggWxPayModel2.setNotify_url("http://" + WELINK_ONLINE_DOMAIN + "/api/h/1.0/hWeiXinCallBack.htm");
        //-------------公众号 米酷SDP end---------------------------
        mcMap.put(GUOGEGE_WX_CONF_KEY, gggWxPayModel2);	//测试 
       
        //-------------公众号 米酷mine start---------------------------
        /*WxPayModel gggWxPayModel2 = new WxPayModel();
        gggWxPayModel2.setAppId("wx21647f957347c195");//wxccc8e5b9786aa8ee
        gggWxPayModel2.setAppKey("741cfd9021b2d0e7f4c883027513f153");
        gggWxPayModel2.setAppSecret("58814e064126d4622307d9fc17049166");
        gggWxPayModel2.setMch_id("1235700302");//商戶號已修改1245066302
        gggWxPayModel2.setNotify_url("http://" + WELINK_ONLINE_DOMAIN + "/api/h/1.0/hWeiXinCallBack.htm");
        //-------------公众号 米酷mine end---------------------------
        mcMap.put(GUOGEGE_WX_CONF_KEY, gggWxPayModel2);	//生产 */  
     

    }

    //-------------公众号 丽元堂start---------------------------
    /*public static String GUOGEGE_H5_APP_ID = "wx82d4b04a531ac1a3";//公众号appId
    public static String GUOGEGE_APP_KEY = "741cfd9021b2d0e7f4c883027513f153";//公众号32位Api密钥
    public static String GUOGEGE_MCH_ID = "1242526802";//公众号财付通商户号
    public static String GUOGEGE_APP_SECRET = "d0088fb1eaa778f534e114d50bd545eb";//公众号secret*/ 
    //-------------公众号 丽元堂end---------------------------
    
    //-------------公众号 米酷mine start---------------------------
    /*public static String GUOGEGE_H5_APP_ID = "wx21647f957347c195";//公众号appId
    public static String GUOGEGE_APP_KEY = "741cfd9021b2d0e7f4c883027513f153";//公众号32位Api密钥
    public static String GUOGEGE_MCH_ID = "1235700302";//公众号财付通商户号
    public static String GUOGEGE_APP_SECRET = "58814e064126d4622307d9fc17049166";//公众号secret 
    //-------------公众号 米酷mine end--------------------------- */
   
    
    //-------------公众号 米酷SDP start---------------------------
    public static String GUOGEGE_H5_APP_ID = "wxec8055b78fdb49d4";//公众号appId
    public static String GUOGEGE_APP_KEY = "741cfd9021b2d0e7f4c883027513f153";//公众号32位Api密钥
    public static String GUOGEGE_MCH_ID = "1323192201";//公众号财付通商户号
    public static String GUOGEGE_APP_SECRET = "545921479fb8b3149de3f89681998961";//公众号secret 
    //-------------公众号 米酷SDP end---------------------------    

    //APP微信支付参数（米酷）
    public static String APP_APP_ID = "wxd23a16ff5cb412b9";//开放平台appId
    public static String APP_APP_SECRET = "1b59d79ad5ca9450ad3b504ffc23b466";//开放平台appSecret
    public static String PARTNER_ID = "1270566201";//开放平台财付通商户号
    public static String PARTNER_KEY = "d22898c75a950fe927a5e1facb90e032";//开放平台财付通partner_key
    public static String TOKENURL = "https://api.weixin.qq.com/cgi-bin/token";//开放平台获取Token的URL
    public static String APPPREPAYURL = "https://api.weixin.qq.com/pay/genprepay";//开放平台获取Token的URL
    public static String APPKEY = "d22898c75a950fe927a5e1facb90e032";//开放平台PaySignKey TODO：

  //APP微信支付参数（米酷SDP）service@mikumine.com
    /*public static String APP_APP_ID = "wxb71e86e6f6fba049";//开放平台appId
    public static String APP_APP_SECRET = "d6073875881be2c73b47e8ce36a8d4b7";//开放平台appSecret
    public static String PARTNER_ID = "1341193001";//开放平台财付通商户号
    public static String PARTNER_KEY = "d22898c75a950fe927a5e1facb90e032";//开放平台财付通partner_key
    public static String TOKENURL = "https://api.weixin.qq.com/cgi-bin/token";//开放平台获取Token的URL
    public static String APPPREPAYURL = "https://api.weixin.qq.com/pay/genprepay";//开放平台获取Token的URL
    public static String APPKEY = "d22898c75a950fe927a5e1facb90e032";//开放平台PaySignKey TODO：*/
    
    
}
