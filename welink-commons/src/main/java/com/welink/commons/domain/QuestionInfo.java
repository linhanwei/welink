package com.welink.commons.domain;

import java.util.Date;


//这个是物流的节点信息
public class QuestionInfo {
	private Long id;
    private String optionValue;
    private Long questionId;
    private String questionName;
    private Byte questionType;
    private Long questionnaireId;
    private Long userId;
    private String uuid;
    private Long parentQid;
    private String childids;
    private Long prevId;
    private String prevOptionId;
    
    
    //添加的是optionName,optionShowStyle,questionShorter,reportPrintArea
    private String optionName;
    private String optionRvalue;
    private String optionShowStyle;
    private String questionShorter;
    private Byte reportPrintArea;
   
    
    
	
	public String getOptionRvalue() {
		return optionRvalue;
	}
	public void setOptionRvalue(String optionRvalue) {
		this.optionRvalue = optionRvalue;
	}
	public Byte getReportPrintArea() {
		return reportPrintArea;
	}
	public void setReportPrintArea(Byte reportPrintArea) {
		this.reportPrintArea = reportPrintArea;
	}
	public String getOptionName() {
		return optionName;
	}
	public void setOptionName(String optionName) {
		this.optionName = optionName;
	}
	public String getOptionShowStyle() {
		return optionShowStyle;
	}
	public void setOptionShowStyle(String optionShowStyle) {
		this.optionShowStyle = optionShowStyle;
	}
	public String getQuestionShorter() {
		return questionShorter;
	}
	public void setQuestionShorter(String questionShorter) {
		this.questionShorter = questionShorter;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getOptionValue() {
		return optionValue;
	}
	public void setOptionValue(String optionValue) {
		this.optionValue = optionValue;
	}
	public Long getQuestionId() {
		return questionId;
	}
	public void setQuestionId(Long questionId) {
		this.questionId = questionId;
	}
	public String getQuestionName() {
		return questionName;
	}
	public void setQuestionName(String questionName) {
		this.questionName = questionName;
	}
	public Byte getQuestionType() {
		return questionType;
	}
	public void setQuestionType(Byte questionType) {
		this.questionType = questionType;
	}
	public Long getQuestionnaireId() {
		return questionnaireId;
	}
	public void setQuestionnaireId(Long questionnaireId) {
		this.questionnaireId = questionnaireId;
	}
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public Long getParentQid() {
		return parentQid;
	}
	public void setParentQid(Long parentQid) {
		this.parentQid = parentQid;
	}
	public String getChildids() {
		return childids;
	}
	public void setChildids(String childids) {
		this.childids = childids;
	}
	
	public Long getPrevId() {
		return prevId;
	}
	public void setPrevId(Long prevId) {
		this.prevId = prevId;
	}
	public String getPrevOptionId() {
		return prevOptionId;
	}
	public void setPrevOptionId(String prevOptionId) {
		this.prevOptionId = prevOptionId;
	} 
	
    
}
