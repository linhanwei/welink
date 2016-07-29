package com.welink.biz.common.constants;

/**
 * Created by daniel on 14-9-19.
 */
public enum BizErrorEnum {

    SESSION_TIME_OUT("登录会话失效", (byte) 1),

    CHECK_NO_ERROR("验证码错误", (byte) 2),

    PASSWORD_ERROR("密码错误", (byte) 3),

    PARAMS_ERROR("参数错误", (byte) 4),

    SEND_CHECKNO_FAILED("验证码已发送，请等待接收...", (byte) 5),

    SYSTEM_BUSY("系统繁忙，请稍后再试", (byte) 6),

    REGISTED_YET("您已经注册过，请登录后进行操作", (byte) 7),

    NO_AUTHORITY("对不起，您没权限操作该用户", (byte) 8),

    UPDATE_PROFILE_FAILED("抱歉信息更新失败，请稍后再试", (byte) 9),

    HAS_TENANT_YET("您已经拥有本房子", (byte) 10),

    NO_SUCH_MEMBER("您尚未注册未米酷用户，请先注册", (byte) 11),

    FAVOUR_YET("您已经喜欢过~", (byte) 12),

    TRADE_NOT_FOUND("啊哦~找不到该笔订单，谢谢", (byte) 13),

    SERVICE_TRADE_NOT_FOUND("您的服务订单已经关闭，谢谢", (byte) 14),

    PRICE_ZERO("您的服务订单已经关闭，谢谢", (byte) 15),

    NO_COURIER_YE("您的服务订单尚未安排，不能付款", (byte) 16),

    TRADE_CLOSED_YTE("您的服务订单已关闭，不能付款", (byte) 17),

    COMMUNITY_ALIPAY_NOT_CONFIGURATED("啊哦~ 暂时不能收款", (byte) 18),

    CAN_NOT_FOUND_ITEM("找不到对应商品，无法付款", (byte) 19),

    CAN_NOT_FOUND_SHOP("找不到对应店", (byte) 20),

    CAN_NOT_FOUND_BUILDING_LAST_PMF_PAY_DATE("房屋尚无业主入住，无法缴纳物业费", (byte) 21),

    PERMISSION_DENIED("游客用户无法进行此项操作", (byte) 22),

    NO_SERVICE("您的小区尚未开通此项服务", (byte) 23),

    NO_ITEMS_IN_CATEGORY("啊哦~商品已经被抢光了~", (byte) 24),

    NO_ITEMS_IN_RECOMMEND("啊哦~推荐商品商品已经被抢光了~", (byte) 25),

    CAN_NOT_FIND_TRADE("啊哦~找不到订单，请稍后再试~", (byte) 26),

    ADD_EVAL_TO_ITEM_FAILED("啊哦~评价失败了，请稍后再试~", (byte) 27),

    NO_CONSIGNEE_ADDRESS_ERROR("啊哦~请先设置收货地址哦亲~", (byte) 28),

    SAVE_TRADE_LOGISTICS_ERROR("啊哦~订单收货地址出错了~", (byte) 29),

    FIRST_INSTALL_BUY_YET("啊哦~您已经领取过礼品哦亲~", (byte) 30),

    GROUPON_ID_ERROR("啊哦~找不到团购信息哦~", (byte) 31),

    NO_MORE_ITEMS("啊哦~没有更多商品了哦~", (byte) 32),

    CREATE_TRADE_FAILED("啊哦~创建订单失败，请稍后再试~", (byte) 33),

    CAN_NOT_FIND_USER_IN_CACHE("啊哦~用户不在缓存中~", (byte) 34),

    ALIPAY_AUTH_FAILED("啊哦~支付宝授权失败了，请稍后再试~", (byte) 35),

    NO_ITEM_IN_CART("啊哦~购物车中没有商品哦~", (byte) 36),

    SYNCHRON_USER_INFO_FAILED("啊哦~同步用户信息失败~", (byte) 37),

    SYNCHRON_USER_INFO_FETCH_ACCESSTOKEN_FAILED("啊哦~同步用户信息失败~", (byte) 38),

    SYNCHRON_USER_INFO_NO_LOGIN_FAILED("啊哦~同步用户信息失败~", (byte) 39),

    SYNCHRON_USER_INFO_FETCH_USER_INFO_FAILED("啊哦~同步用户信息失败~", (byte) 40),

    CAN_NOT_FIND_OPEN_ID("啊哦~同步用户信息失败~", (byte) 41),

    ADD_COOP_USER_INFO_FAILED("啊哦~第三方登陆授权失败~", (byte) 42),

    ADD_COOP_USER_RELATIONSHIP_FAILED("啊哦~第三方登陆授权失败~", (byte) 43),

    UPDATE_CONSIGNEE_ADDRESS_FAILED("啊哦~更新收货地址失败~", (byte) 44),

    ADD_CONSIGNEE_ADDRESS_FAILED("啊哦~更新收货地址失败~", (byte) 45),

    ADD_CONSIGNEE_ADDRESS_FAILED_COUNT_FULL("啊哦~每个人只能设置5个收货地址~", (byte) 46),

    LBS_TRANSFORM_FAILED("啊哦~坐标转换失败~", (byte) 47),

    FETCH_TRADE_YET("啊哦~该订单已经签收过了~", (byte) 48),

    CONFIRM_SIGN_NOT_SAME_PERSON("啊哦~签收人不是确认收货人，不能签收~", (byte) 49),

    NOT_CONFIRM_YET("啊哦~还未确认收货，不能签收哦~", (byte) 50),

    NOT_SYSTEM_USERS("啊哦~该手机号码还未注册~", (byte) 51),

    NOT_EMPLOYEE("啊哦~您不是我们的员工哦~", (byte) 52),

    ADD_CART_FAILED_ITEM_COUNT("啊哦~您买的商品库存不足了哦~", (byte) 53),

    WEIXIN_AUTH_FAILED("啊哦~微信授权失败了，请稍后再试~", (byte) 54),

    WEIXIN_PREPAY_FAILED("啊哦~获取微信prepayId失败，请稍后再试~", (byte) 55),

    ONE_TIME_TRADE_MULTI("啊哦~限购商品，您已经下单成功，请到未付款订单进行支持，请稍后再试~", (byte) 56),

    FETCH_OPEN_ID_OAUTH_FAILED("啊哦~暂时获取不到必要的支付信息，请稍后再试~", (byte) 57),

    COD_NOT_SUPPORT_PROMOTION("啊哦~货到付款不支持使用积分或优惠券~", (byte) 58),

    CAN_NOT_COMMUNITY("啊哦~找不到您需要的站点，请选择其他站点~", (byte) 59),

    SHOULD_CHANGE_COMMUNITY("啊哦~需要切换站点吗~", (byte) 60),

    NO_SHOP_IN_SESSION("啊哦~需要您的定位信息哦~", (byte) 61),

    ITEM_LIMIT_BUY_COUNTS("啊哦~您购买的商品超出限购数量了~", (byte) 62),

    COMMUNITY_NOT_MATCHING("啊哦~请选择您所在站点范围内的收货地址~", (byte) 63),

    COMMUNITY_SHOP_NOT_MATCHING("啊哦~找不到对应店铺~", (byte) 64),

    APP_SECRET_ERROR("啊哦~访问出错了~", (byte) 65),

    TRADE_PAYED_ALREADY("啊哦~该笔订单已无法进行付款操作~", (byte) 66),

    SELF_PICK_NOT_SUPPORTED("啊哦~我们提供送货上门服务，不用自提哦~", (byte) 67),

    NO_EQUAL_COMMUNITY("啊哦~您选择的收货地址不在当前店铺配送范围内哦~", (byte) 68),
    
    /*-------------------------------------------------------------------------------------------------*/
    REQ_PAY_OUT("啊哦~无可提现金额~", (byte) 69),
    
    NO_DIRECT_ALLY("啊哦~您没有直接盟友~", (byte) 70),
    
    NO_INDIRECT_ALLY("啊哦~您没有间接盟友~", (byte) 71),
    
    NO_AGENCY("啊哦~您不是代理~", (byte) 72),
    
    PASSWORD_NEQ("啊哦~密码不相等~", (byte) 73),
    
    IS_MOBILE("请输入正确的电话号码~", (byte) 74),
    
    SENSITIVE_WORD("啊哦~请文明评论~评论不能包含", (byte) 75),
    
    ID_CARD_NOT_FOUND("啊哦~身份证号不存在", (byte) 76),
    
    ID_CARD_NOT_SAME("啊哦~姓名和身份证号不一致", (byte) 77),
    
    REQ_PAY_MIN("啊哦~可提现金额必须大于一元~", (byte) 69),
    ;

    // 成员变量
    private String msg;

    private byte code;

    // 构造方法
    private BizErrorEnum(String msg, byte code) {
        this.msg = msg;
        this.code = code;
    }

    // 普通方法
    public static String getMsg(int code) {
        for (BizErrorEnum c : BizErrorEnum.values()) {
            if (Integer.compare(c.getCode(), code) == 0) {
                return c.msg;
            }
        }
        return "";
    }

    // get set 方法
    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public byte getCode() {
        return code;
    }

    public void setCode(byte code) {
        this.code = code;
    }
}
