package com.yuntongxun.mcm.mcm.form;


public class ResponseMessage {
	private String statusCode;
	private String message;
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}
	@Override
	public String toString() {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Response><statusMsg>" + message + "</statusMsg><statusCode>"
				+ statusCode + "</statusCode></Response>";
	}
	
	
	
}
