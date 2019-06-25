package com.yuntongxun.mcm.genesys.model;

public class ChatSessionMessage{
	
	private String connId;
	private String mediaType;
	private String nickName;
	private String userType;
	private String textMessage;
	
	private String reason;
	private String time;
	private Integer visibility; //0-All, 1-Int, 2-Vip
	private Integer referenceId;
	private String operation;
	
	public String getConnId() {
		return connId;
	}

	public void setConnId(String connId) {
		this.connId = connId;
	}

	public String getMediaType() {
		return mediaType;
	}

	public void setMediaType(String mediaType) {
		this.mediaType = mediaType;
	}

	public String getTextMessage() {
		return textMessage;
	}

	public void setTextMessage(String textMessage) {
		this.textMessage = textMessage;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public Integer getVisibility() {
		return visibility;
	}

	public void setVisibility(Integer visibility) {
		this.visibility = visibility;
	}

	public Integer getReferenceId() {
		return referenceId;
	}

	public void setReferenceId(Integer referenceId) {
		this.referenceId = referenceId;
	}

	
	
	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("ChatSessionMessage[");
		sb.append("referenceId=" + referenceId);
		sb.append(", connId=" + connId);
		sb.append(", mediaType=" + mediaType);
		sb.append(", nickName=" + nickName);
		sb.append(", userType=" + userType);
		sb.append(", textMessage=" + textMessage);
		sb.append(", reason=" + reason );
		sb.append(", operation=" + operation + "]");
		return sb.toString();
	}
}