package com.welink.commons.vo;

import java.util.List;

public class MikuGroupCsadsVO {
	
	private Long groupId;
	
	private String groupName;
	
	private List<MikuCsadVO> csadVOList;

	public Long getGroupId() {
		return groupId;
	}

	public void setGroupId(Long groupId) {
		this.groupId = groupId;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public List<MikuCsadVO> getCsadVOList() {
		return csadVOList;
	}

	public void setCsadVOList(List<MikuCsadVO> csadVOList) {
		this.csadVOList = csadVOList;
	}
	
	
}
