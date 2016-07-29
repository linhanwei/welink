package com.welink.biz.util;

/**
 * Created by daniel on 15-1-29.
 */
public enum OpenSearchType {

    RECOMMEND_DESC(1, "-rank", "推荐排序，或者人工排序，按照加权降序排列"),

    RECOMMEND_ASC(2, "+rank", "推荐排序，或者人工排序，按照加权升序排列"),

    PRICE_DESC(3, "-price", "最高的价格排在最前"),

    PRICE_ASC(4, "+price", "最高的价格排在最后"),

    SOLD_DESC(5, "-sold_count", "最高的销量排在最前"),

    SOLD_ASC(6, "+sold_count", "最高的销量排在最后"),

    DATA_CREATED_DESC(7, "-date_created", "最新的商品排在最前"),

    DATA_CREATED_ASC(8, "+date_created", "最新的商品排在最后"),
    
    WEIGHT_DESC(9, "-weight", "权重最高的商品排在最后"),
    
    WEIGHT_ASC(10, "+weight", "权重最高的商品排在最前"),;

    private int type;

    private String formulaName;

    private String description;

    public int getType() {
        return type;
    }

    public String getFormulaName() {
        return formulaName;
    }

    public String getDescription() {
        return description;
    }

    OpenSearchType(int type, String formulaName, String description) {
        this.type = type;
        this.formulaName = formulaName;
        this.description = description;

    }

    public static OpenSearchType findByType(int type) {
        for (OpenSearchType openSearchOrder : values()) {
            if (Integer.compare(openSearchOrder.getType(), type) == 0) {
                return openSearchOrder;
            }
        }
        throw new IllegalStateException("it can not happen the type [" + type + "] is not in the enums... ");
    }

}
