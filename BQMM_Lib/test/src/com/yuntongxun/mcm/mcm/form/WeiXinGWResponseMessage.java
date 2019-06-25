package com.yuntongxun.mcm.mcm.form;



public class WeiXinGWResponseMessage {
	private boolean success;
	private String statusCode;
	private String message;
	
	 
	public String toJson() {
		StringBuffer strBuffer = new StringBuffer();
		strBuffer.append("{");
		strBuffer.append("\"success\":");
		strBuffer.append(success);
		strBuffer.append(",\"statusCode\":\"");
		strBuffer.append(statusCode == null ? "" : statusCode);
		strBuffer.append("\",\"message\":\"");
		strBuffer.append(message == null ? "" : message);
		strBuffer.append("\"}");
		return strBuffer.toString();
	}


	public boolean getSuccess() {
		return success;
	}


	public void setSuccess(boolean success) {
		this.success = success;
	}


	public String getStatusCode() {
		return statusCode;
	}


	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}


	public String getMessage() {
		return message;
	}


	public void setMessage(String message) {
		this.message = message;
	}
	
	
	
}
