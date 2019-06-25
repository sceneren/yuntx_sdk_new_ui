package com.yuntongxun.mcm.genesys.model;

public class IxnLogin {
	
	private String agentId; //坐席编号
	private String nickName; //坐席昵称
	private String placeId; //坐席工号
	private String media;
	private String chatType;
	
	private String userAcc;

	public String getAgentId() {
		return agentId;
	}

	public void setAgentId(String agentId) {
		this.agentId = agentId;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getPlaceId() {
		return placeId;
	}

	public void setPlaceId(String placeId) {
		this.placeId = placeId;
	}
	
	public String getMedia() {
		return media;
	}

	public void setMedia(String media) {
		this.media = media;
	}

	public String getChatType() {
		return chatType;
	}

	public void setChatType(String chatType) {
		this.chatType = chatType;
	}

	public String getUserAcc() {
		return userAcc;
	}

	public void setUserAcc(String userAcc) {
		this.userAcc = userAcc;
	}

	@Override
	public String toString() {
		return "IxnLogin [agentId=" + agentId + ", nickName=" + nickName
				+ ", placeId=" + placeId + ", media=" + media + ", chatType="
				+ chatType + ", userAcc=" + userAcc + "]";
	}
	
}
