package com.welink.promotion.reactive;

/**
 * Created by saarixx on 5/3/15.
 */
public class PromotionResult {

    /**
     * 这次的互动是否得到了优惠
     */
    private Boolean reward;

    /**
     * 拒绝的错误码
     */
    private int code;

    /**
     * 返回拒绝原因
     */
    private String message;

    /**
     * 优惠id，积分还是优惠券，可以通过type来区分
     */
    private Long promotionId;

    /**
     * 交互id
     */
    private Long actionId;

    /**
     * 互动类型
     */
    private Integer type;

    /**
     * 互动来源
     */
    private Integer from;

    public Boolean getReward() {
        return reward;
    }

    public void setReward(Boolean reward) {
        this.reward = reward;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getPromotionId() {
        return promotionId;
    }

    public void setPromotionId(Long promotionId) {
        this.promotionId = promotionId;
    }

    public Long getActionId() {
        return actionId;
    }

    public void setActionId(Long actionId) {
        this.actionId = actionId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getFrom() {
        return from;
    }

    public void setFrom(Integer from) {
        this.from = from;
    }
}
