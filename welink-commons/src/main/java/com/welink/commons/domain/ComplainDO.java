package com.welink.commons.domain;

import java.util.Date;

public class ComplainDO {
    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column complain.id
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    private Long id;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column complain.version
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    private Long version;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column complain.building_id
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    private Long buildingId;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column complain.community_id
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    private Long communityId;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column complain.content
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    private String content;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column complain.date_created
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    private Date dateCreated;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column complain.last_updated
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    private Date lastUpdated;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column complain.pic_urls
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    private String picUrls;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column complain.profile_id
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    private Long profileId;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column complain.status
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    private Byte status;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column complain.title
     *
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    private String title;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column complain.id
     *
     * @return the value of complain.id
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    public Long getId() {
        return id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column complain.id
     *
     * @param id the value for complain.id
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column complain.version
     *
     * @return the value of complain.version
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    public Long getVersion() {
        return version;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column complain.version
     *
     * @param version the value for complain.version
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    public void setVersion(Long version) {
        this.version = version;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column complain.building_id
     *
     * @return the value of complain.building_id
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    public Long getBuildingId() {
        return buildingId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column complain.building_id
     *
     * @param buildingId the value for complain.building_id
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    public void setBuildingId(Long buildingId) {
        this.buildingId = buildingId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column complain.community_id
     *
     * @return the value of complain.community_id
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    public Long getCommunityId() {
        return communityId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column complain.community_id
     *
     * @param communityId the value for complain.community_id
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    public void setCommunityId(Long communityId) {
        this.communityId = communityId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column complain.content
     *
     * @return the value of complain.content
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    public String getContent() {
        return content;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column complain.content
     *
     * @param content the value for complain.content
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    public void setContent(String content) {
        this.content = content == null ? null : content.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column complain.date_created
     *
     * @return the value of complain.date_created
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    public Date getDateCreated() {
        return dateCreated;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column complain.date_created
     *
     * @param dateCreated the value for complain.date_created
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column complain.last_updated
     *
     * @return the value of complain.last_updated
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    public Date getLastUpdated() {
        return lastUpdated;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column complain.last_updated
     *
     * @param lastUpdated the value for complain.last_updated
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column complain.pic_urls
     *
     * @return the value of complain.pic_urls
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    public String getPicUrls() {
        return picUrls;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column complain.pic_urls
     *
     * @param picUrls the value for complain.pic_urls
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    public void setPicUrls(String picUrls) {
        this.picUrls = picUrls == null ? null : picUrls.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column complain.profile_id
     *
     * @return the value of complain.profile_id
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    public Long getProfileId() {
        return profileId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column complain.profile_id
     *
     * @param profileId the value for complain.profile_id
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    public void setProfileId(Long profileId) {
        this.profileId = profileId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column complain.status
     *
     * @return the value of complain.status
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    public Byte getStatus() {
        return status;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column complain.status
     *
     * @param status the value for complain.status
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    public void setStatus(Byte status) {
        this.status = status;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column complain.title
     *
     * @return the value of complain.title
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    public String getTitle() {
        return title;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column complain.title
     *
     * @param title the value for complain.title
     * @mbggenerated Thu Nov 27 18:10:21 CST 2014
     */
    public void setTitle(String title) {
        this.title = title == null ? null : title.trim();
    }
}