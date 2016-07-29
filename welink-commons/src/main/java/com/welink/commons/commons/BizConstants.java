package com.welink.commons.commons;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by daniel on 14-10-23.
 */
public class BizConstants {

    public static final String SPLIT = "\u0001";

    public static final String CHECK_NO_PREFIX = "checkNO_";

    public static final String CHECK_SALT_PREFIX = "tokenNO_";

    public static final int CHECK_NO_LEN = 4;

    public static final int ORDER_TYPE_WATER = 1;

    public static final int ORDER_TYPE_REPAIR = 2;

    public static final int ORDER_TYPE_HOMEMAKING = 3;

    public static final String PROFILE_ID = "profileId";

    public static final String SHOP_ID = "shop_id";

    public static final String LBS_LAST_ADDRESS = "lbs_last_address_a";

    public static final String WEIXIN_MP_STATE = "weixin_mp_state";

    public static final String UNION_ID = "unionId";

    public static final String MOBILE = "mobile";

    public static final String RESULT = "result";

    public static final String COMPANY_NAME = "米酷SDP";
    
    public static final String APP_NAME = "米酷SDP";

    public static final String WE_CHAT_USER_ID = "openid";

    public static final int ANNOUCE_STATUS_VALID = 1;//公告有效

    public static final int ANNOUCE_STATUS_INVALID = 0;//公告已无效

    public static final String DEFAULT_ANNOUNCE = "欢迎进入米酷，购COOL，够实惠";

    public static final String USER_AGENT = "welinkagent";

    public static final String P_USER_ID = "userId";

    public static final String ORIGIN_AGENT = "User-Agent";

    public static final String BAIDU_API_URL = "http://api.map.baidu.com/place/v2/search";

    public static final String BAIDU_API_AK = "pWtRwFvKi5EG0Ui6nag4EGdU";

    public static final String ILLIGAL_REQUEST = "failed";

    public static final int MESSAGE_TIME_BEFORE_NOW = -12;

    public static final int REPORT_INTERVAL_TIME_SECOND = 600;

    public static final String LINK_PUSH_MESSAGE = "您的分享有新的回复哦~";

    public static final long WELINK_ID = 999;

    public static final int TRADE_OUT_OF_DATE_MINUTE = 30;

    public static final int TRADE_OUT_OF_DATE_MINUTE_2_HOUR = 120;

    public static final int TRADE_OUT_OF_DATE_MINUTE_24_HOUR = 1440;

    public static final String FETCH_ITEMS = "item-list";

    public static final String ITEM_CART = "item-cart";

    public static final String CART_OP = "cart-op";

    public static final String CONSIGNEE = "consignee";

    public static final String FETCH_ITEM = "item-detail";

    public static final String CONFIRM_ORDER = "confirm-order";

    public static final String LOGIN = "login-action";

    public static final String LOGIN_TYPE = "login_type";

    public static final String TRANSACTION_CHECKER = "transaction-checker";

    public static final String PRE_CLIENT_FLAG = "com.welink.sunflowerseeds.pre";

    public static final String IOS_CLIENT_FLAG = "com.welink.sunflowerseeds";

    public static final String PUB = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCTevDTNKW0N9GO4UhGhFLjCV/9JCWKgvkQSYs1BrR3Ak/z+Hvo3jIx7uZw8hTB4pnungvKju5ix9IGMf0M6J53tpiZ1rGZh6HEPBdsUebuAeKGlgSkf2wqbtrxZ6Git9CybvmBAM34qzFCrajRKWBcAKHq1bHkLQ/GRT1EDemt1wIDAQAB";

    public static final String WX_LANG = "zh_CN";

    public static final int CONSIGNEE_COUNT = 5;

    public static final byte STATUS_VALID = 1;

    public static final byte STATUS_INVALID = 0;

    public static final byte SELF_PICK_SHIPPING_TYPE = 1;//自提类型

    public static final long SHIPPING_SELF_PICK_CONSING_ID = -1l;//自提consigee_id

    public static final Object USER_CITY = "杭州";

    public static final String WX_PARAM_CODE = "code";

    public static final String WX_PARAM_STATE = "state";

    public static final String TEST_OPEN_ID_FOR_LUOMO = "oAr4Aty9lxdhScOeM8ly8-Lp36g0";

    //public static final String H5_SEVEN_NB_HOST = "mp.unesmall.com";
    /*public static final String H5_SEVEN_NB_HOST = "h.unesmall.com";*/
    public static final String H5_SEVEN_NB_HOST = "h.unesmall.com";

    public static final String H5_WEILINJIA_HOST = "187.unesmall.com";
    //public static final String H5_WEILINJIA_HOST = "mp.unesmall.com";

    public static final String HOST = "host";

    public static final String JSESSION_ID = "JSESSIONID";

    public static final String JSAPI_TICKETS_TOKEN = "jsapi_ticks_token";

    public static final String H5_TOKEN = "h5_token";

    public static final String APP_TOKEN = "app_token";

    public static final String APP_PRE_TOKEN = "app_pre_token";

    public static final String APPOINTMENT_DELIVERY_TIME = "adTime";

    public static final int SHOW_CASE_ITEM_SIZE = 10;

    public static final Long ACTIVE_ITEM_ID = 2266L;

    public static final int NEW_GIFT_FLAG = 1;

    public static final String POST_FEE_MSG = "满10元免费陪送";

    //public static final String ONLINE_DOMAIN = "unesmall.com";
    //public static final String ONLINE_DOMAIN = "120.24.102.187:8082";
    //public static final String ONLINE_DOMAIN = "wechat.unesmall.com";
    public static final String ONLINE_DOMAIN = "miku.unesmall.com";
    
    public static final String ONLINE_DOMAIN_TEST = "test.unesmall.com";

    public static final long APPOINT_DELIVERY_REGION = 150 * 60 * 1000L;//配送间隔2.5小时

    public static final int THRESHOLD = 300;
    
    public static final String ALIYUN_ACCESSKEY_ID_DEV = "GDheOkyZuLg7VALU";
    
    public static final String ALIYUN_ACCESSKEY_SECRET_DEV = "7sQA8nMHZkB3CNgspOWnpzrl5B7tx0"; 

    public static final String ALIYUN_ACCESSKEY_ID_PRO = "aHg4n4dESlLleVJz";
    
    public static final String ALIYUN_ACCESSKEY_SECRET_PRO = "L2iNoTs1UyRStJgUtkTtqmHUS22Thd";
    
    public static final Long CAN_JOIN_AGENCY_FEE =  9900L;
    
    public static final String MAX_VISIT_COUNT =  "max_visit_count";	//最大访问人数（memcache key 值）
    
    public static final String MAX_VISIT_COUNT_RESET =  "max_visit_count_reset";	//默认最大访问人数（memcache key 值）
    
    public static final String MAX_VISIT_TIMES = "max_visit_times";		//最大访问人数时间（memcache key 值）
    
    public static final String MAX_VISIT_END_TIME =  "max_visit_end_time";	//重新设置最大访问人数的间隔结束时间（memcache key 值）
    
    public static final String SUBSCRIBE_WX =  "subscribe_wx";	//是否订阅微信（memcache key 值）
    
    public static final String EMCHAT_EXPERT_PRE = "expert_";		//环信专家用户名前缀
    
    //public static final String EMCHAT_CUSTOMER_PRE = "customer_";	//环信客户用户名前缀
    public static final String EMCHAT_CUSTOMER_PRE = "miku_";	//环信客户用户名前缀
    
    public static final String EMCHAT_PW = "miku2015";				//环信用户密码
    
    //public static final String EMCHAT_EXPERT_PW = "mikuExpert2015";				//环信专家用户密码
    
    public static final boolean QUERY_BROKERAGEFEE = true;				//是否查询佣金(true=查询佣金;false=不查询佣金)
    
    public static final String HAS_AGENCY_RELATION = "hasAgencyRelation";	//是否有代理关系session

    public static List<Long> AllBizTypes = new ArrayList<Long>();

    static {
        AllBizTypes.add(lpow(2, 0));
        AllBizTypes.add(lpow(2, 1));
    }

    public static String OPENID = "openid";

    public static String APPID = "appid";

    public static String EMPLOYEE_MOBILE = "employee_mobile";

    public static String H = "h";

    public static String ALIPAY_ID = "alipayId";

    public static String REDIRECT_LBS_SHOP = "lbsShop";

    public static String ITEM_OUT_OF_NUM = "100";

    public static String ITEM_NOT_ON_SAIL = "101";

    public static enum UserNotifyTagEnum {

        ANNOUNCEMENT(lpow(2, 0), "建议反馈"),

        ORDER(lpow(2, 1), "我的订单"),;

        private long tagId;

        private String name;

        UserNotifyTagEnum(long tagId, String name) {
            this.tagId = tagId;
            this.name = name;
        }

        public long getUserNotifyTagId() {
            return tagId;
        }

        public static String getUserNotifyTagName(long tagId) {
            for (UserNotifyTagEnum c : UserNotifyTagEnum.values()) {
                if (Long.compare(c.getUserNotifyTagId(), tagId) == 0) {
                    return c.getName();
                }
            }
            return "";
        }

        public static UserNotifyTagEnum getUserNotifyTag(long tagId) {
            for (UserNotifyTagEnum c : UserNotifyTagEnum.values()) {
                if (Long.compare(c.getUserNotifyTagId(), tagId) == 0) {
                    return c;
                }
            }
            return null;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    /**
     * AVOS 推送消息类型枚举 客户端根据不同action进行不同的解析动作
     */
    public static enum PushActionEnum {

        ALERT((byte) 1, "简单的弹出消息 不跳转"),

        NO_ALERT((byte) 2, "仅提醒不弹出"),

        REDIRECT((byte) 3, "弹框(如果app打开中)并带跳转的");

        private byte action;

        private String name;

        PushActionEnum(byte action, String name) {
            this.action = action;
            this.name = name;
        }

        public byte getAction() {
            return action;
        }

        public static String getPushActionEnumName(byte action) {
            for (PushActionEnum c : PushActionEnum.values()) {
                if (Byte.compare(c.getAction(), action) == 0) {
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

    public static enum BannerActionEnum {

        TO_ITEM(311, "转至detail"),

        TO_LIST(312, "转至list"),

        TO_POINT(314, "转至积分"),

        TO_COUPON(315, "优惠券"),;

        private int action;

        private String name;

        BannerActionEnum(int action, String name) {
            this.action = action;
            this.name = name;
        }

        public int getAction() {
            return action;
        }

        public static String getBannerActionEnumName(byte action) {
            for (BannerActionEnum c : BannerActionEnum.values()) {
                if (Integer.compare(c.getAction(), action) == 0) {
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

    public static enum MessageBizTypeEnum {

        TO_ITEM(0, "转至商品detail"),

        TO_TRADE(1, "转至trade"),

        TO_HOME(2, "转至首页"),

        TO_LIST(3, "转至list"),

        TO_URL(4, "转至url")
        ;

        private long bizType;

        private String name;

        MessageBizTypeEnum(int bizType, String name) {
            this.bizType = bizType;
            this.name = name;
        }

        public long getBizType() {
            return bizType;
        }

        public static String getMessageBizTypeName(long bizType) {
            for (MessageBizTypeEnum c : MessageBizTypeEnum.values()) {
                if (Long.compare(c.getBizType(), bizType) == 0) {
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

    public static enum MessageTypeEnum {

        NON_PAY(0, "订单未支付通知"),

        TRADE_STATUS_CHANGE(1, "订单状态更新通知");

        private long bizType;

        private String name;

        MessageTypeEnum(int bizType, String name) {
            this.bizType = bizType;
            this.name = name;
        }

        public long getBizType() {
            return bizType;
        }

        public static String getMessageTypeEnumName(long bizType) {
            for (MessageTypeEnum c : MessageTypeEnum.values()) {
                if (Long.compare(c.getBizType(), bizType) == 0) {
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

    public static enum PushRedirectEnum {

        TRADE_DETAIL((byte) 1, "到订单"),

        EVAL((byte) 2, "到评价"),;

        private byte action;

        private String name;

        PushRedirectEnum(byte action, String name) {
            this.action = action;
            this.name = name;
        }

        public byte getAction() {
            return action;
        }

        public static String getPushRedirectEnumName(byte action) {
            for (PushRedirectEnum c : PushRedirectEnum.values()) {
                if (Byte.compare(c.getAction(), action) == 0) {
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

    public static enum ShopTypeEnum {

        SUB_SHOP((byte) 1, "站点店铺"),

        SOURCE_SHOP((byte) 2, "总资源店铺"),;

        private byte type;

        private String name;

        ShopTypeEnum(byte type, String name) {
            this.type = type;
            this.name = name;
        }

        public byte getAction() {
            return type;
        }

        public static String getShopTypeEnumName(byte type) {
            for (ShopTypeEnum c : ShopTypeEnum.values()) {
                if (Byte.compare(c.getAction(), type) == 0) {
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

    public static enum ShowCaseEnum {

        SHOW((byte) 1, "展示特推"),

        NON_SHOW((byte) 0, "隐藏特推"),;

        private byte type;

        private String name;

        ShowCaseEnum(byte type, String name) {
            this.type = type;
            this.name = name;
        }

        public byte getType() {
            return type;
        }

        public static String getShowCaseEnumName(byte action) {
            for (PushRedirectEnum c : PushRedirectEnum.values()) {
                if (Byte.compare(c.getAction(), action) == 0) {
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

    public static enum ItemStockTypeEnum {

        PAY((byte) 1, "付款减库存"),

        ORDER((byte) 2, "拍下减库存"),;

        private byte type;

        private String name;

        ItemStockTypeEnum(byte type, String name) {
            this.type = type;
            this.name = name;
        }

        public byte getType() {
            return type;
        }

        public static String getItemStockTypeEnumName(byte action) {
            for (ItemStockTypeEnum c : ItemStockTypeEnum.values()) {
                if (Byte.compare(c.getType(), action) == 0) {
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

    /**
     * 标签类型
     */
    public static enum TagTypeEnum {

        ITEM_EXHIBITION(1000, "商品标-展示"),

        ITEM_LOGIC(1001, "商品标-逻辑处理"),

        CATEGORY(2000, "类目标"),

        SHOP(3000, "店铺标"),

        USER(4000, "用户标"),;

        private int action;

        private String name;

        TagTypeEnum(int action, String name) {
            this.action = action;
            this.name = name;
        }

        public int getAction() {
            return action;
        }

        public static String getTagTypeEnumName(byte action) {
            for (TagTypeEnum c : TagTypeEnum.values()) {
                if (Integer.compare(c.getAction(), action) == 0) {
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

    public static enum SearchTagEnum {

        LIMIT_BUY((long) Math.pow(2, 0), "限购购买数量"),

        HALF_PRICE((long) Math.pow(2, 1), "特价"),

        SHOW_CASE((long) Math.pow(2, 2), "橱窗推荐"),

        NON_POINT((long) Math.pow(2, 3), "不支持积分"),

        PIERRE_ITEM((long) Math.pow(2, 4), "臻品"),

        SEASON_ITEM((long) Math.pow(2, 5), "时令商品"),

        NON_COUPON((long) Math.pow(2, 6), "不支持优惠券"),

        NON_POST_FEE((long) Math.pow(2, 7), "不支持运费"),

        ORDER_ONLY((long) Math.pow(2, 8), "不支持加入购物车"),

        NON_DELIVERY((long) Math.pow(2, 9), "不支持配送"),
        
        PANIC_BUYING((long) Math.pow(2, 11), "抢购标"),;

        private long tag;

        private String name;

        SearchTagEnum(long tag, String name) {
            this.tag = tag;
            this.name = name;
        }

        public long getTag() {
            return tag;
        }

        public static String getSearchTagEnumName(byte action) {
            for (SearchTagEnum c : SearchTagEnum.values()) {
                if (Long.compare(c.getTag(), action) == 0) {
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


    public static enum LoginEnum {

        APP((byte) 1, "独立app登录"),

        SERVICE((byte) 2, "微信服务号"),

        WX_LOGIN((byte) 3, "微信联合登录"),

        ALIPAY_LOGIN((byte) 4, "支付宝联合登录"),
        
        QQ_LOGIN((byte) 6, "QQ互联登录"),

        H5_MOBILE_CODE((byte) 5, "H5手机验证码Q"),;

        private byte type;

        private String name;

        LoginEnum(byte type, String name) {
            this.type = type;
            this.name = name;
        }

        public byte getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static enum ConsigneeTypeEnum {

        SEARTCH((byte) 1, "通过搜索获得的地址"),;

        private byte type;

        private String name;

        ConsigneeTypeEnum(byte type, String name) {
            this.type = type;
            this.name = name;
        }

        public byte getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }


    public static enum UserNotifyMsgStatus {

        NON_READ(1, "标记待读 即未读状态"),

        DELETE(0, "标记删除"),

        READED(2, "标记已读"),

        INVALID(3, "标记无效");

        private int status;

        private String name;

        UserNotifyMsgStatus(int status, String name) {
            this.status = status;
            this.name = name;
        }

        public int getStatus() {
            return status;
        }

        public static String getUserNotifyStatusName(int status) {
            for (UserNotifyMsgStatus c : UserNotifyMsgStatus.values()) {
                if (Integer.compare(c.getStatus(), status) == 0) {
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

    /**
     * 便民热线状态
     */
    public static enum HotLineStatus {

        NON_READ((byte) 1, "有效"),

        DELETE((byte) 0, "已删除/无效");


        private byte status;

        private String name;

        HotLineStatus(byte status, String name) {
            this.status = status;
            this.name = name;
        }

        public byte getStatus() {
            return status;
        }

        public static String getHotLineStatusName(byte status) {
            for (HotLineStatus c : HotLineStatus.values()) {
                if (Byte.compare(c.getStatus(), status) == 0) {
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

    /**
     * 商品上下架状态
     */
    public static enum ItemApproveStatus {

        ON_SALE((byte) 1, "在售"),

        OFF_SALE((byte) 0, "已删除/无效");

        private byte status;

        private String name;

        ItemApproveStatus(byte status, String name) {
            this.status = status;
            this.name = name;
        }

        public byte getStatus() {
            return status;
        }

        public static String getItemApproveStatusName(byte status) {
            for (ItemApproveStatus c : ItemApproveStatus.values()) {
                if (Byte.compare(c.getStatus(), status) == 0) {
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

    /**
     * 投诉建议状态
     */
    public static enum ComplainStatus {

        NEW_CREATE((byte) 1, "已提交"),

        DELETE((byte) 0, "已删除/无效"),

        ACCEPT((byte) 2, "已接收"),

        DONE((byte) 3, "已完成"),;


        private byte status;

        private String name;

        ComplainStatus(byte status, String name) {
            this.status = status;
            this.name = name;

        }

        public byte getStatus() {
            return status;
        }

        public static String getComplainStatusName(byte status) {
            for (ComplainStatus c : ComplainStatus.values()) {
                if (Byte.compare(c.getStatus(), status) == 0) {
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

    /**
     * 推送消息的类型
     */
    public static enum NotifyActionType {

        alert((byte) 1, "弹窗"),

        open_url((byte) 2, "跳转到指定url"),

        open_page((byte) 3, "打开指定界面"),;

        private byte type;

        private String name;

        NotifyActionType(byte type, String name) {
            this.type = type;
            this.name = name;
        }

        public byte getType() {
            return type;
        }

        public static String getNotifyActionTypeName(byte type) {
            for (NotifyActionType c : NotifyActionType.values()) {
                if (Byte.compare(c.getType(), type) == 0) {
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


    public static enum OrderEvalType {

        BUYER_EVAL((byte) 1, "买家评价"),

        SELLER_REPLY((byte) 2, "卖家回复"),

        SYSTEM_EVAL((byte) 3, "系统默认评价"),;

        private byte type;

        private String name;

        OrderEvalType(byte type, String name) {
            this.type = type;
            this.name = name;

        }

        public byte getType() {
            return type;
        }

        public static String getOrderEvalTypeName(byte type) {
            for (OrderEvalType c : OrderEvalType.values()) {
                if (Byte.compare(c.getType(), type) == 0) {
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

    public static enum LinkReplyType {

        FAVOUR((byte) 1, "赞"),

        REPLY((byte) 2, "回复"),;

        private byte type;

        private String name;

        LinkReplyType(byte type, String name) {
            this.type = type;
            this.name = name;

        }

        public byte getType() {
            return type;
        }

        public static String getLinkReplyTypeName(byte type) {
            for (ComplainReplyType c : ComplainReplyType.values()) {
                if (Byte.compare(c.getType(), type) == 0) {
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

    public static enum OrderEvalAnonymousType {

        ANONYMOUS((byte) 1, "匿名"),

        NON_ANONYMOUS((byte) 2, "非匿名"),;

        private byte type;

        private String name;

        OrderEvalAnonymousType(byte type, String name) {
            this.type = type;
            this.name = name;

        }

        public byte getType() {
            return type;
        }

        public static String getOrderEvalAnonymousName(byte type) {
            for (OrderEvalAnonymousType c : OrderEvalAnonymousType.values()) {
                if (Byte.compare(c.getType(), type) == 0) {
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

    public static enum EvalRoleType {

        BUYER((byte) 1, "买家"),

        SELLER((byte) 2, "卖家"),

        SYSTEM((byte) 3, "系统"),;

        private byte type;

        private String name;

        EvalRoleType(byte type, String name) {
            this.type = type;
            this.name = name;
        }

        public byte getType() {
            return type;
        }

        public static String getEvalRoleTypeName(byte type) {
            for (EvalRoleType c : EvalRoleType.values()) {
                if (Byte.compare(c.getType(), type) == 0) {
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


    /**
     * 半价活动商品状态枚举
     */
    public static enum HalfItemStatus {

        DONT_ANNOUNCE(0, "未开始预告"),

        ANNOUNCING(7, "开始展示"),

        ACTIVING_IN_STOCK(15, "活动开始中-有库存"),

        ACTIVING_NO_STOCK(12, "活动开始中-有库存"),

        ACTIVE_ENDS(1, "活动结束"),;

        private int status;

        private String name;

        HalfItemStatus(int status, String name) {
            this.status = status;
            this.name = name;
        }

        public int getStatus() {
            return status;
        }

        public static String getHalfItemStatus(int status) {
            for (HalfItemStatus c : HalfItemStatus.values()) {
                if (Integer.compare(c.getStatus(), status) == 0) {
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

    /**
     * 周边热线类型  --  待补全
     */
    public static enum NumberType {

        MARKET((byte) 1, "超市"),

        FOOD((byte) 2, "美食"),

        ENTERTAINMENT((byte) 3, "娱乐"),

        HOMEMAKING((byte) 4, "家政"),

        BANK((byte) 5, "银行"),

        DRUGSTORE((byte) 6, "药店"),

        PET((byte) 7, "宠物"),

        CHILD((byte) 8, "儿童"),;

        private byte type;

        private String name;

        NumberType(byte type, String name) {
            this.type = type;
            this.name = name;

        }

        public byte getType() {
            return type;
        }

        public static String getNumberTypeName(byte type) {
            for (NumberType c : NumberType.values()) {
                if (Byte.compare(c.getType(), type) == 0) {
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

    /**
     * 快递代收状态
     */
    public static enum ComplainReplyType {

        COMPLAINER((byte) 1, "投诉人"),

        SERVICER((byte) 2, "物业服务人"),;

        private byte type;

        private String name;

        ComplainReplyType(byte type, String name) {
            this.type = type;
            this.name = name;

        }

        public byte getType() {
            return type;
        }

        public static String getComplainReplyTypeName(byte type) {
            for (ComplainReplyType c : ComplainReplyType.values()) {
                if (Byte.compare(c.getType(), type) == 0) {
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

    /**
     * 快递代收状态
     */
    public static enum ExpressStatus {

        NON_READ((byte) 1, "未读"),

        DELETE((byte) 0, "已删除/无效"),

        PICKED_UP((byte) 2, "已领取");


        private byte status;

        private String name;

        ExpressStatus(byte status, String name) {
            this.status = status;
            this.name = name;

        }

        public byte getStatus() {
            return status;
        }

        public static String getHotLineStatusName(byte status) {
            for (ExpressStatus c : ExpressStatus.values()) {
                if (Byte.compare(c.getStatus(), status) == 0) {
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

    public static enum UserMsgBizStatus {

        OUT_DATE(0, "过期"),

        VALID(1, "有效"),

        INVALID(2, "失效"),

        PRE_DATE(3, "未到展示期");

        private int status;

        private String name;

        UserMsgBizStatus(int status, String name) {
            this.status = status;
            this.name = name;
        }

        public int getStatus() {
            return status;
        }

        public static String getUserMsgBizStatusName(int status) {
            for (UserNotifyMsgStatus c : UserNotifyMsgStatus.values()) {
                if (Integer.compare(c.getStatus(), status) == 0) {
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
    
    public static enum AgencyLevel {

        aaa(1, "一度"),
        bbb(2, "二度"),
        ccc(3, "三度"),
        ddd(4, "四度"),
        eee(5, "五度"),
        fff(6, "六度"),
        ggg(7, "七度"),
        hhh(8, "八度"),
        
        CEO1(12, "联合创始人"),
        CEO2(11, "股东"),
        CEO3(10, "CEO"),
        CEO4(9, "首席推广经理"),
        
        CEO1Gift(13, "联合创始人"),
        CEO2Gift(14, "股东"),
        CEO3Gift(15, "CEO"),
        CEO4Gift(16, "首席推广经理"),
        CEO5Gift(17, "推广经理"),;

        private AgencyLevel(Integer id, String name) {
			this.id = id;
			this.name = name;
		}

		private Integer id;

        private String name;

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

    }
    
    public static enum MikuAgencyLevel {
        CEO1(5L, "联合创始人"),
        CEO2(4L, "股东"),
        CEO3(3L, "CEO"),
        CEO4(2L, "首席推广经理"),
        CEO5(1L, "推广经理"),
        USER(-1L, "普通用户"),;

        private MikuAgencyLevel(Long id, String name) {
			this.id = id;
			this.name = name;
		}

		private Long id;

        private String name;

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

    }

    public static long lpow(long a, long b) {
        long tmp = a;
        if (Long.compare(b, 0) == 0) {
            return 1;
        }
        for (int i = 0; i < b - 1; i++) {
            a = a * tmp;
        }
        return a;
    }


    public static void main(String[] args) {
        int a = 13;
        int b = 2;
        int c = a & b;
        System.out.println(c);
    }


    public static final long WELINK_RATE_ID = -326l;
}
