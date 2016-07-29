package com.welink.commons.domain;

import java.math.BigDecimal;


//总计的数据
public class MikuOperMeaureData {
	private Long id;
	private Long userId;
	private String timetype;
	private Byte  testPosition;
	private BigDecimal context;
	private BigDecimal measureValue;
	private BigDecimal moistureValue;
	private BigDecimal oilValue;
	private BigDecimal resilienceValue;
	private BigDecimal senilityValue;
	
	public Long getId() {
		return id;
	}
	public String getTimetype() {
		return timetype;
	}
	public void setTimetype(String timetype) {
		this.timetype = timetype;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	public Byte getTestPosition() {
		return testPosition;
	}
	public void setTestPosition(Byte testPosition) {
		this.testPosition = testPosition;
	}
	
	public BigDecimal getContext() {
		return context;
	}
	public void setContext(BigDecimal context) {
		this.context = context;
	}
	public BigDecimal getMeasureValue() {
		return measureValue;
	}
	public void setMeasureValue(BigDecimal measureValue) {
		this.measureValue = measureValue;
	}
	public BigDecimal getMoistureValue() {
		return moistureValue;
	}
	public void setMoistureValue(BigDecimal moistureValue) {
		this.moistureValue = moistureValue;
	}
	public BigDecimal getOilValue() {
		return oilValue;
	}
	public void setOilValue(BigDecimal oilValue) {
		this.oilValue = oilValue;
	}
	public BigDecimal getResilienceValue() {
		return resilienceValue;
	}
	public void setResilienceValue(BigDecimal resilienceValue) {
		this.resilienceValue = resilienceValue;
	}
	public BigDecimal getSenilityValue() {
		return senilityValue;
	}
	public void setSenilityValue(BigDecimal senilityValue) {
		this.senilityValue = senilityValue;
	}
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	@Override
	public String toString() {
		return "MikuOperMeaureData [id=" + id + ", userId=" + userId
				+ ", timetype=" + timetype + ", testPosition=" + testPosition
				+ ", context=" + context + ", measureValue=" + measureValue
				+ ", moistureValue=" + moistureValue + ", oilValue=" + oilValue
				+ ", resilienceValue=" + resilienceValue + ", senilityValue="
				+ senilityValue + "]";
	}
	
	
	
	
}
