package com.welink.commons.domain;

import java.util.Date;

public class Snapshot {
    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column snapshot.id
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    private Long id;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column snapshot.version
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    private Long version;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column snapshot.date_created
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    private Date dateCreated;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column snapshot.detail
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    private String detail;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column snapshot.detail_path
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    private String detailPath;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column snapshot.last_updated
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    private Date lastUpdated;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column snapshot.order_id
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    private Long orderId;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column snapshot.trade_id
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    private Long tradeId;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column snapshot.id
     *
     * @return the value of snapshot.id
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    public Long getId() {
        return id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column snapshot.id
     *
     * @param id the value for snapshot.id
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column snapshot.version
     *
     * @return the value of snapshot.version
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    public Long getVersion() {
        return version;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column snapshot.version
     *
     * @param version the value for snapshot.version
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    public void setVersion(Long version) {
        this.version = version;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column snapshot.date_created
     *
     * @return the value of snapshot.date_created
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    public Date getDateCreated() {
        return dateCreated;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column snapshot.date_created
     *
     * @param dateCreated the value for snapshot.date_created
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column snapshot.detail
     *
     * @return the value of snapshot.detail
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    public String getDetail() {
        return detail;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column snapshot.detail
     *
     * @param detail the value for snapshot.detail
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    public void setDetail(String detail) {
        this.detail = detail == null ? null : detail.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column snapshot.detail_path
     *
     * @return the value of snapshot.detail_path
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    public String getDetailPath() {
        return detailPath;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column snapshot.detail_path
     *
     * @param detailPath the value for snapshot.detail_path
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    public void setDetailPath(String detailPath) {
        this.detailPath = detailPath == null ? null : detailPath.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column snapshot.last_updated
     *
     * @return the value of snapshot.last_updated
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    public Date getLastUpdated() {
        return lastUpdated;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column snapshot.last_updated
     *
     * @param lastUpdated the value for snapshot.last_updated
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column snapshot.order_id
     *
     * @return the value of snapshot.order_id
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    public Long getOrderId() {
        return orderId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column snapshot.order_id
     *
     * @param orderId the value for snapshot.order_id
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column snapshot.trade_id
     *
     * @return the value of snapshot.trade_id
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    public Long getTradeId() {
        return tradeId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column snapshot.trade_id
     *
     * @param tradeId the value for snapshot.trade_id
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    public void setTradeId(Long tradeId) {
        this.tradeId = tradeId;
    }
}