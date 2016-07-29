package com.welink.commons.vo;

import java.util.Date;
import java.util.List;

import com.welink.commons.domain.MikuMineCourseDO;
import com.welink.commons.domain.MikuMineScProductDO;

public class MikuMineScBoxVO {
    private Long id;
    private Long userId;
    private Long itemId;
    private String boxName;
    private Long price;
    private Byte payStatus;
    private Long detectReportId;
    private String expertDbIds;
    private String scProductIds;
    private Long courseId;
    private Long version;
    private Date dateCreated;
    private Date lastUpdated;
    
    private Long tradeId;
    private Byte tradeStatus;
    
    private Long serviceId;
	private String csadName;
	private String csadPicUrl;
	
	private String userName;
	private String userMobile;
	private String userPicUrl;
	
	MikuMineCourseDO mineCourse;			//课程实体
	List<MikuMineScProductDO> productList;	//定制商品列表
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public Long getItemId() {
		return itemId;
	}
	public void setItemId(Long itemId) {
		this.itemId = itemId;
	}
	public String getBoxName() {
		return boxName;
	}
	public void setBoxName(String boxName) {
		this.boxName = boxName;
	}
	public Long getPrice() {
		return price;
	}
	public void setPrice(Long price) {
		this.price = price;
	}
	public Byte getPayStatus() {
		return payStatus;
	}
	public void setPayStatus(Byte payStatus) {
		this.payStatus = payStatus;
	}
	public Long getDetectReportId() {
		return detectReportId;
	}
	public void setDetectReportId(Long detectReportId) {
		this.detectReportId = detectReportId;
	}
	public String getExpertDbIds() {
		return expertDbIds;
	}
	public void setExpertDbIds(String expertDbIds) {
		this.expertDbIds = expertDbIds;
	}
	public String getScProductIds() {
		return scProductIds;
	}
	public void setScProductIds(String scProductIds) {
		this.scProductIds = scProductIds;
	}
	public Long getCourseId() {
		return courseId;
	}
	public void setCourseId(Long courseId) {
		this.courseId = courseId;
	}
	public Long getVersion() {
		return version;
	}
	public void setVersion(Long version) {
		this.version = version;
	}
	public Date getDateCreated() {
		return dateCreated;
	}
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	public Date getLastUpdated() {
		return lastUpdated;
	}
	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}
	public Long getTradeId() {
		return tradeId;
	}
	public void setTradeId(Long tradeId) {
		this.tradeId = tradeId;
	}
	public Byte getTradeStatus() {
		return tradeStatus;
	}
	public void setTradeStatus(Byte tradeStatus) {
		this.tradeStatus = tradeStatus;
	}
	public Long getServiceId() {
		return serviceId;
	}
	public void setServiceId(Long serviceId) {
		this.serviceId = serviceId;
	}
	public String getCsadName() {
		return csadName;
	}
	public void setCsadName(String csadName) {
		this.csadName = csadName;
	}
	public String getCsadPicUrl() {
		return csadPicUrl;
	}
	public void setCsadPicUrl(String csadPicUrl) {
		this.csadPicUrl = csadPicUrl;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getUserMobile() {
		return userMobile;
	}
	public void setUserMobile(String userMobile) {
		this.userMobile = userMobile;
	}
	public String getUserPicUrl() {
		return userPicUrl;
	}
	public void setUserPicUrl(String userPicUrl) {
		this.userPicUrl = userPicUrl;
	}
	public MikuMineCourseDO getMineCourse() {
		return mineCourse;
	}
	public void setMineCourse(MikuMineCourseDO mineCourse) {
		this.mineCourse = mineCourse;
	}
	public List<MikuMineScProductDO> getProductList() {
		return productList;
	}
	public void setProductList(List<MikuMineScProductDO> productList) {
		this.productList = productList;
	}
    
    
    
}
