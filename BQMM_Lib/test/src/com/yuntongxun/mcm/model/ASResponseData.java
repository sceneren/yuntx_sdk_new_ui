package com.yuntongxun.mcm.model;

import com.yuntongxun.mcm.mcm.model.MCMMessageInfo;

public class ASResponseData {

	private boolean success;
	private String message;
	
	private MCMMessageInfo mcmMessageInfo;

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public MCMMessageInfo getMcmMessageInfo() {
		return mcmMessageInfo;
	}

	public void setMcmMessageInfo(MCMMessageInfo mcmMessageInfo) {
		this.mcmMessageInfo = mcmMessageInfo;
	}
	
}
