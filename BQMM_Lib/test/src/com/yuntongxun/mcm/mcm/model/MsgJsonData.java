package com.yuntongxun.mcm.mcm.model;


public class MsgJsonData {

	private String sessionId;
	private String accessId;

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	
	public String toJson() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		
		sb.append("\"sessionId\":\"");
		sb.append(sessionId == null ? "" : sessionId);
		
		sb.append("\"");
		
		sb.append("}");
		return sb.toString();
	}

	public String getAccessId() {
		return accessId;
	}

	public void setAccessId(String accessId) {
		this.accessId = accessId;
	}
	
	
}
