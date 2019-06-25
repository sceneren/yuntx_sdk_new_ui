package com.yuntongxun.mcm.genesys.model;

public class ChatUserInfo {

	private int userType; // 0-client 1-agent
	private String nickName;

	public int getUserType() {
		return userType;
	}

	public void setUserType(int userType) {
		this.userType = userType;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}
}
