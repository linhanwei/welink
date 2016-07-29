package com.welink.buy.utils;

/**
 * Created by saarixx on 15/9/14.
 */
public class Constants {

    public static final String XDJ_JTOKEN = "XDJ_JTOKEN";

    public static final String JTOKEN = "jtoken";

    /**
     * @since 2.0
     */
    public static enum AppointmentServiceCategory {

        FRESH_SEA("生鲜配送", 20000002L),//生鲜配送一级类目

        //运费专用
        PostFeeService("运费类目", 100000L),
        // 家政服务
        HouseholdManagementService("家政服务", 100010L),
        // 维修服务
        MaintenanceService("维修服务", 100011L),
        // 送水服务
        BottledWaterService("送水服务", 100012L),
        //since 2.0
        //全国特产
        NationalSpecialProduct("全国特产", 20100000L),
        //家庭套餐
        FamilyMeal("家庭套餐", 20200000L),
        //生鲜蔬菜
        FreshVegetables("生鲜蔬菜", 20300000L),
        //肉禽蛋品
        PoultryProducts("肉禽蛋品", 20400000L),
        //水产海鲜
        SeaFoods("水产海鲜", 20500000L),
        //粮油米面
        GrainAndOil("粮油米面", 20600000L),

        FirstInstallActive("初装有礼", 20000024L),//20000024
        ;

        private String name;

        private long categoryId;

        AppointmentServiceCategory(String name, long categoryId) {
            this.name = name;
            this.categoryId = categoryId;
        }

        public String getName() {
            return name;
        }

        public long getCategoryId() {
            return categoryId;
        }

        public static boolean contains(final long categoryId) {
            for (AppointmentServiceCategory appointmentServiceCategory : values()) {
                if (appointmentServiceCategory.categoryId == categoryId) {
                    return true;
                }
            }
            return false;
        }
    }

    public static enum TradeFrom {
        APP_IOS((byte) 1, "手机app ios"),
        APP_ANDROID((byte) 2, "手机app android"),
        H5((byte) 3, "手机H5"),
        UNKNOWN((byte) 4, "未区分类型");

        private byte typeId;

        private String name;

        TradeFrom(byte typeId, String name) {
            this.typeId = typeId;
            this.name = name;
        }

        public byte getTypeId() {
            return typeId;
        }
    }


    public static enum TradeType {
        fixed((byte) 1, "一口价"),
        cod((byte) 2, "货到付款"),
        groupon((byte) 3, "万人团"),
        nopaid((byte) 4, "无付款订单"),
        pre_auth_type((byte) 5, "预授权0元购机交易"),
        one_buy_type((byte) 6, "一元购"),
        crowdfund_type((byte) 7, "众筹"),
        lotteryDraw_type((byte) 8, "抽奖"),
        join_agency((byte) 9, "成为代理"),
        scratch_card((byte) 10, "刮刮卡"),
        topic_cut((byte) 11, "满减"),
        dz_type((byte) 12, "定制"),;

        private byte typeId;

        private String name;

        TradeType(byte typeId, String name) {
            this.typeId = typeId;
            this.name = name;
        }

        public byte getTradeTypeId() {
            return typeId;
        }
    }

    public static enum PmfOrderType {

        PRE_PMF(-13003l, "预缴纳物业费");

        private long typeId;

        private String name;

        PmfOrderType(long typeId, String name) {
            this.typeId = typeId;
        }

        public long getPmfOrderTypeId() {
            return typeId;
        }
    }

    /**
     * 交易状态
     */
    public static enum TradeStatus {
        TRADE_NO_CREATE_PAY((byte) 1, "没有创建支付宝交易"),
        WAIT_BUYER_PAY((byte) 2, "等待买家付款"),
        SELLER_CONSIGNED_PART((byte) 3, "卖家部分发货"),
        WAIT_SELLER_SEND_GOODS((byte) 4, "等待卖家发货,即:买家已付款"),
        WAIT_BUYER_CONFIRM_GOODS((byte) 5, "等待买家确认收货,即:卖家已发货"),
        TRADE_BUYER_SIGNED((byte) 6, "已备货"),
        //TRADE_FINISHED((byte) 7, "交易成功"),
        TRADE_FINISHED((byte) 7, "确认收货"),
        TRADE_CLOSED((byte) 8, "付款以后用户退款成功，交易自动关闭"),
        TRADE_CLOSED_BY_TAOBAO((byte) 9, "付款以前，卖家或买家主动关闭交易"),
        PAY_PENDING((byte) 10, "国际信用卡支付付款确认中"),
        WAIT_PRE_AUTH_CONFIRM((byte) 11, "0元购合约中"),
        
        //TRADE_COMMENTED((byte) 12, "已评价"),
        //TRADE_RETURNNING((byte) 13, "退货中"),
        //TRADE_RETURNED((byte) 14, "已退货"),
        TRADE_SUCCESSED((byte) 20, "交易成功"),

        PAY_SUCCESS_AFTER_COMBINE_TRADE((byte) 23, "交易合并以后支付成功标识"),

        ORIGINAL((byte) 99, "物业费原始态"),;

        private byte id;

        private String name;

        TradeStatus(byte id, String name) {
            this.id = id;
            this.name = name;
        }

        public byte getTradeStatusId() {
            return id;
        }
    }

    public static enum CodStatus {
        NEW_CREATED((byte) 1, "提交订单"),
        ACCEPTED_BY_COMPANY((byte) 2, "接单成功"),// --- 4
        REJECTED_BY_COMPANY((byte) 3, "接单失败"),
        RECIEVE_TIMEOUT((byte) 4, "接单超时"),
        TAKEN_IN_SUCCESS((byte) 5, "揽收成功"), // ---5
        TAKEN_IN_FAILED((byte) 6, "揽收失败"),
        TAKEN_TIMEOUT((byte) 7, "揽收超时"),
        SIGN_IN((byte) 8, " 签收成功 "),       // ---7
        REJECTED_BY_OTHER_SIDE((byte) 9, "签收失败"),
        WAITING_TO_BE_SENT((byte) 10, "订单等待发送给物流公司"),
        CANCELED((byte) 11, "用户取消物流订单");

        private byte id;

        private String name;

        CodStatus(byte id, String name) {
            this.id = id;
            this.name = name;
        }

        public byte getCodStatusId() {
            return id;
        }
    }

    public static enum SellerType {

        COMMUNITY_SELLER((byte) 1, "总店隐藏店铺"),

        SHOP_SUPPORT((byte) 2, "站点商品");

        private byte id;

        private String name;

        SellerType(byte id, String name) {
            this.id = id;
            this.name = name;
        }

        public byte getSellerTypeId() {
            return id;
        }

        public static SellerType getSellerTypeId(long id) {
            for (SellerType c : SellerType.values()) {
                if (Long.compare(c.getSellerTypeId(), id) == 0) {
                    return c;
                }
            }
            return null;
        }
    }

    public static enum RateType {

        RATED((byte) 1, "已评价"),

        NONE_RATE((byte) 0, "未评价"),;


        private byte id;

        private String name;

        RateType(byte id, String name) {
            this.id = id;
            this.name = name;
        }

        public byte getRateTypeId() {
            return id;
        }

        public static RateType getRateTypeById(long id) {
            for (RateType c : RateType.values()) {
                if (Long.compare(c.getRateTypeId(), id) == 0) {
                    return c;
                }
            }
            return null;
        }
    }

    public static enum PayType {

        ON_LINE((byte) 1, "线上支付"),

        OFF_LINE((byte) 2, "线下支付/货到付款"),

        ONLINE_ALIPAY((byte) 3, "线上支付宝支付"),

        ONLINE_WXPAY((byte) 4, "线上微信支付"),

        ONLINE_ZERO_PAY((byte) 5, "线上0元支付"),

        OFFLINE_CASH((byte) 6, "线下现金支付"),

        OFFLINE_ALIPAY((byte) 7, "线下支付宝支付"),

        OFFLINE_WXPAY((byte) 8, "线下微信支付"),;

        private byte id;

        private String name;

        PayType(byte id, String name) {
            this.id = id;
            this.name = name;
        }

        public byte getPayTypeId() {
            return id;
        }

        public static PayType getPayTypeById(long id) {
            for (PayType c : PayType.values()) {
                if (Long.compare(c.getPayTypeId(), id) == 0) {
                    return c;
                }
            }
            return null;
        }
    }

    public static enum ApproveStatus {
        ON_SALE((byte) 1, "出售中"),
        IN_STOCK((byte) 0, "库中");

        private byte id;

        private String name;

        ApproveStatus(byte id, String name) {
            this.id = id;
            this.name = name;
        }

        public byte getApproveStatusId() {
            return id;
        }

    }

    public static enum PmfBillStatus {//只用到0，1  1有效，0 无效

        VALID(1, "有效"),

        INVALID(0, "无效/已缴纳");

        private int status;

        private String name;

        PmfBillStatus(int status, String name) {
            this.status = status;
            this.name = name;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(byte status) {
            this.status = status;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static enum GrouponItemStatus {

        VALID((byte) 1, "出售中"),

        INVALID((byte) 0, "无效/过期"),

        NOT_START((byte) 2, "未开始"),;

        private byte status;

        private String name;

        GrouponItemStatus(byte status, String name) {
            this.status = status;
            this.name = name;
        }

        public byte getStatus() {
            return status;
        }

        public void setStatus(byte status) {
            this.status = status;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static enum BuyErrorStatus {
        NO_BUILDING(1, "查找房屋信息(含物业费信息)失败"),
        PARA_ERROR(2, "参数错误"),
        PARA_ERROR_NO_BILL(3, "参数错误-没有必缴和预缴账单"),;

        private int status;

        private String msg;

        BuyErrorStatus(int status, String msg) {
            this.status = status;
            this.msg = msg;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(byte status) {
            this.status = status;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }
    }
    
    
    /*---------------------------------------------------------------------*/
    /**
     * 退款状态
     */
    public static enum ReturnGoodsStatus {
    	//状态(-1:删除;0:正常单;1:申请退货;2=退货中;3=退货完成;4=退货异常)
    	//状态(-1=删除;0=正常单;1=申请退货;2=申请退货审核;3=退货中;4=退货确认收货;5=退货完成;6=退货异常)
        DEL((byte) -1, "删除"),
        NORMAL((byte) 0, "正常单"),
        REQ_RETURN((byte) 1, "申请退货"),
        REQ_EXAMINE((byte) 2, "申请退货审核"),
        RETURNNING((byte) 3, "退货中"),
        CONFIRM_RECEIPT((byte) 4, "退货确认收货"),
        FINISHED((byte) 5, "退货完成"),
        RETURN_ERROR((byte) 6, "退货异常"),;

        private byte id;

        private String name;

        ReturnGoodsStatus(byte id, String name) {
            this.id = id;
            this.name = name;
        }

        public byte getStatusId() {
            return id;
        }
    }
    
    /**
     * 提现状态
     */
    public static enum GetPayStatus {
    	//提现状态(-1=未提现/删除；0=提现中/待审核；1=已提现/已审核;2=提现异常)
        NOGETPAY((byte) -1, "未提现/删除"),
        GETPAYING((byte) 0, "提现中/待审核"),
        GETPAYED((byte) 1, "已提现/已审核"),
        GETPAYABNORMAL((byte) 2, "提现异常"),;

        private byte id;

        private String name;

        GetPayStatus(byte id, String name) {
            this.id = id;
            this.name = name;
        }

        public byte getStatusId() {
            return id;
        }
    }
    
    /**
     * 交易退货状态
     */
    public static enum TradeReturnStatus {
    	//交易退货状态(0=正常单；1=退货中;2=已退货)
    	NORMAL((byte) 0, "正常单"),
    	RETURNNING((byte) 1, "退货中"),
    	RETURNED((byte) 2, "已退货"),;

        private byte id;

        private String name;

        TradeReturnStatus(byte id, String name) {
            this.id = id;
            this.name = name;
        }

        public byte getStatusId() {
            return id;
        }
    }
    
    /**
     * 众筹状态
     */
    public static enum CrowdfundStatus {
    	//众筹状态(-1=无效;0=正常;1=成功;2=失败)
    	DEL((byte) -1, "无效"),
    	NORMAL((byte) 0, "正常单"),
    	SUCCESS((byte) 1, "成功"),
    	FAILD((byte) 2, "失败"),;

        private byte id;

        private String name;

        CrowdfundStatus(byte id, String name) {
            this.id = id;
            this.name = name;
        }

        public byte getStatusId() {
            return id;
        }
    }
    
    /**
     * 众筹退款状态
     */
    public static enum CrowdfundRefundStatus {
    	//众筹退款状态(0=正常;1=退款中;2=已退款)
    	//众筹退款状态(0=众筹中；1=退款中;2=已退款;3=众筹成功)
    	NORMAL((byte) 0, "众筹中"),
    	REFUNDING((byte) 1, "退款中"),
    	REFUNDED((byte) 2, "已退款"),
    	SUCCESS((byte) 3, "众筹成功"),;

        private byte id;

        private String name;

        CrowdfundRefundStatus(byte id, String name) {
            this.id = id;
            this.name = name;
        }

        public byte getStatusId() {
            return id;
        }
    }
    
    /**
     * 刮刮卡状态
     */
    public static enum ScratchCardStatus {
    	//刮刮卡状态(0=已取消;1=未下单;2=未付款;3=已付款)
    	CANCEL((byte) 0, "已取消"),
    	NO_ORDER((byte) 1, "未下单"),
    	NO_PAY((byte) 2, "未付款"),
    	PAYED((byte) 3, "已付款"),;

        private byte id;

        private String name;

        ScratchCardStatus(byte id, String name) {
            this.id = id;
            this.name = name;
        }

        public byte getStatusId() {
            return id;
        }
    }
    
    //是否收运费的临界值
    public static final long POST_FEE_STEP = 9900;
    //运费5元
    //public static final long POST_FEE = 800;
    public static final long POST_FEE = 1000;
    //用户每天可摇的优惠券次数
    public static final int TOTAL_SHAKE_COUPON_COUNT = 3;
    //新用户发放500积分
    public static final long ROOKIE_POINT = 500;
    //给邀请用户发放500积分
    public static final long INVITE_POINT = 500;
    //每天用户可使用的最大积分数
    public static final long MAX_USE_POINT = 2000000;

    //IOS版本需要升级临界值
    public static final long IOS_VERSION = 111;

    //IOSPre版本需要升级临界值
    public static final long IOS_PRE_VERSION = 112;

    //Android版本需要升级临界值
    public static final long ANDROID_VERSION = 0;

}
