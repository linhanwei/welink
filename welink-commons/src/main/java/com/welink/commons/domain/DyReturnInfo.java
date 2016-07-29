package com.welink.commons.domain;

public class DyReturnInfo {
	
	private String result;
	private String returnCode;
	private String message;
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public String getReturnCode() {
		return returnCode;
	}
	public void setReturnCode(String returnCode) {
		this.returnCode = returnCode;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	@Override
	public String toString() {
		return "DyReturnInfo [result=" + result + ", returnCode=" + returnCode
				+ ", message=" + message + "]";
	}


}
