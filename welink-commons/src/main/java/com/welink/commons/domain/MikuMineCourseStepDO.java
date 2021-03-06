package com.welink.commons.domain;

import java.util.Date;

public class MikuMineCourseStepDO {
    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column miku_mine_course_step.id
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    private Long id;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column miku_mine_course_step.course_id
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    private Long courseId;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column miku_mine_course_step.step_name
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    private String stepName;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column miku_mine_course_step.step_short_name
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    private String stepShortName;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column miku_mine_course_step.step_order
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    private Integer stepOrder;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column miku_mine_course_step.step_type
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    private Byte stepType;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column miku_mine_course_step.step_interval
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    private Integer stepInterval;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column miku_mine_course_step.step_use_standard_frequency
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    private Byte stepUseStandardFrequency;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column miku_mine_course_step.step_use_standard_period
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    private Byte stepUseStandardPeriod;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column miku_mine_course_step.step_use_standard_condition
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    private Byte stepUseStandardCondition;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column miku_mine_course_step.use_time
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    private Byte useTime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column miku_mine_course_step.prod_id
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    private Long prodId;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column miku_mine_course_step.prod_use_remind
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    private String prodUseRemind;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column miku_mine_course_step.video_name
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    private String videoName;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column miku_mine_course_step.video_short_name
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    private String videoShortName;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column miku_mine_course_step.video_url
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    private String videoUrl;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column miku_mine_course_step.video_time
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    private Integer videoTime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column miku_mine_course_step.video_use_remind
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    private String videoUseRemind;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column miku_mine_course_step.version
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    private Long version;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column miku_mine_course_step.date_created
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    private Date dateCreated;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column miku_mine_course_step.last_updated
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    private Date lastUpdated;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column miku_mine_course_step.id
     *
     * @return the value of miku_mine_course_step.id
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    public Long getId() {
        return id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column miku_mine_course_step.id
     *
     * @param id the value for miku_mine_course_step.id
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column miku_mine_course_step.course_id
     *
     * @return the value of miku_mine_course_step.course_id
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    public Long getCourseId() {
        return courseId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column miku_mine_course_step.course_id
     *
     * @param courseId the value for miku_mine_course_step.course_id
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column miku_mine_course_step.step_name
     *
     * @return the value of miku_mine_course_step.step_name
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    public String getStepName() {
        return stepName;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column miku_mine_course_step.step_name
     *
     * @param stepName the value for miku_mine_course_step.step_name
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    public void setStepName(String stepName) {
        this.stepName = stepName == null ? null : stepName.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column miku_mine_course_step.step_short_name
     *
     * @return the value of miku_mine_course_step.step_short_name
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    public String getStepShortName() {
        return stepShortName;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column miku_mine_course_step.step_short_name
     *
     * @param stepShortName the value for miku_mine_course_step.step_short_name
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    public void setStepShortName(String stepShortName) {
        this.stepShortName = stepShortName == null ? null : stepShortName.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column miku_mine_course_step.step_order
     *
     * @return the value of miku_mine_course_step.step_order
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    public Integer getStepOrder() {
        return stepOrder;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column miku_mine_course_step.step_order
     *
     * @param stepOrder the value for miku_mine_course_step.step_order
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    public void setStepOrder(Integer stepOrder) {
        this.stepOrder = stepOrder;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column miku_mine_course_step.step_type
     *
     * @return the value of miku_mine_course_step.step_type
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    public Byte getStepType() {
        return stepType;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column miku_mine_course_step.step_type
     *
     * @param stepType the value for miku_mine_course_step.step_type
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    public void setStepType(Byte stepType) {
        this.stepType = stepType;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column miku_mine_course_step.step_interval
     *
     * @return the value of miku_mine_course_step.step_interval
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    public Integer getStepInterval() {
        return stepInterval;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column miku_mine_course_step.step_interval
     *
     * @param stepInterval the value for miku_mine_course_step.step_interval
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    public void setStepInterval(Integer stepInterval) {
        this.stepInterval = stepInterval;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column miku_mine_course_step.step_use_standard_frequency
     *
     * @return the value of miku_mine_course_step.step_use_standard_frequency
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    public Byte getStepUseStandardFrequency() {
        return stepUseStandardFrequency;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column miku_mine_course_step.step_use_standard_frequency
     *
     * @param stepUseStandardFrequency the value for miku_mine_course_step.step_use_standard_frequency
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    public void setStepUseStandardFrequency(Byte stepUseStandardFrequency) {
        this.stepUseStandardFrequency = stepUseStandardFrequency;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column miku_mine_course_step.step_use_standard_period
     *
     * @return the value of miku_mine_course_step.step_use_standard_period
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    public Byte getStepUseStandardPeriod() {
        return stepUseStandardPeriod;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column miku_mine_course_step.step_use_standard_period
     *
     * @param stepUseStandardPeriod the value for miku_mine_course_step.step_use_standard_period
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    public void setStepUseStandardPeriod(Byte stepUseStandardPeriod) {
        this.stepUseStandardPeriod = stepUseStandardPeriod;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column miku_mine_course_step.step_use_standard_condition
     *
     * @return the value of miku_mine_course_step.step_use_standard_condition
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    public Byte getStepUseStandardCondition() {
        return stepUseStandardCondition;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column miku_mine_course_step.step_use_standard_condition
     *
     * @param stepUseStandardCondition the value for miku_mine_course_step.step_use_standard_condition
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    public void setStepUseStandardCondition(Byte stepUseStandardCondition) {
        this.stepUseStandardCondition = stepUseStandardCondition;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column miku_mine_course_step.use_time
     *
     * @return the value of miku_mine_course_step.use_time
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    public Byte getUseTime() {
        return useTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column miku_mine_course_step.use_time
     *
     * @param useTime the value for miku_mine_course_step.use_time
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    public void setUseTime(Byte useTime) {
        this.useTime = useTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column miku_mine_course_step.prod_id
     *
     * @return the value of miku_mine_course_step.prod_id
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    public Long getProdId() {
        return prodId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column miku_mine_course_step.prod_id
     *
     * @param prodId the value for miku_mine_course_step.prod_id
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    public void setProdId(Long prodId) {
        this.prodId = prodId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column miku_mine_course_step.prod_use_remind
     *
     * @return the value of miku_mine_course_step.prod_use_remind
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    public String getProdUseRemind() {
        return prodUseRemind;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column miku_mine_course_step.prod_use_remind
     *
     * @param prodUseRemind the value for miku_mine_course_step.prod_use_remind
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    public void setProdUseRemind(String prodUseRemind) {
        this.prodUseRemind = prodUseRemind == null ? null : prodUseRemind.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column miku_mine_course_step.video_name
     *
     * @return the value of miku_mine_course_step.video_name
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    public String getVideoName() {
        return videoName;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column miku_mine_course_step.video_name
     *
     * @param videoName the value for miku_mine_course_step.video_name
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    public void setVideoName(String videoName) {
        this.videoName = videoName == null ? null : videoName.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column miku_mine_course_step.video_short_name
     *
     * @return the value of miku_mine_course_step.video_short_name
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    public String getVideoShortName() {
        return videoShortName;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column miku_mine_course_step.video_short_name
     *
     * @param videoShortName the value for miku_mine_course_step.video_short_name
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    public void setVideoShortName(String videoShortName) {
        this.videoShortName = videoShortName == null ? null : videoShortName.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column miku_mine_course_step.video_url
     *
     * @return the value of miku_mine_course_step.video_url
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    public String getVideoUrl() {
        return videoUrl;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column miku_mine_course_step.video_url
     *
     * @param videoUrl the value for miku_mine_course_step.video_url
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl == null ? null : videoUrl.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column miku_mine_course_step.video_time
     *
     * @return the value of miku_mine_course_step.video_time
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    public Integer getVideoTime() {
        return videoTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column miku_mine_course_step.video_time
     *
     * @param videoTime the value for miku_mine_course_step.video_time
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    public void setVideoTime(Integer videoTime) {
        this.videoTime = videoTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column miku_mine_course_step.video_use_remind
     *
     * @return the value of miku_mine_course_step.video_use_remind
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    public String getVideoUseRemind() {
        return videoUseRemind;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column miku_mine_course_step.video_use_remind
     *
     * @param videoUseRemind the value for miku_mine_course_step.video_use_remind
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    public void setVideoUseRemind(String videoUseRemind) {
        this.videoUseRemind = videoUseRemind == null ? null : videoUseRemind.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column miku_mine_course_step.version
     *
     * @return the value of miku_mine_course_step.version
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    public Long getVersion() {
        return version;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column miku_mine_course_step.version
     *
     * @param version the value for miku_mine_course_step.version
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    public void setVersion(Long version) {
        this.version = version;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column miku_mine_course_step.date_created
     *
     * @return the value of miku_mine_course_step.date_created
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    public Date getDateCreated() {
        return dateCreated;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column miku_mine_course_step.date_created
     *
     * @param dateCreated the value for miku_mine_course_step.date_created
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column miku_mine_course_step.last_updated
     *
     * @return the value of miku_mine_course_step.last_updated
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    public Date getLastUpdated() {
        return lastUpdated;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column miku_mine_course_step.last_updated
     *
     * @param lastUpdated the value for miku_mine_course_step.last_updated
     *
     * @mbggenerated Sat May 21 16:59:55 CST 2016
     */
    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}