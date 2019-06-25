package com.yuntongxun.mcm.mcm.model;

public class ReservedInfo {

	private int keyType; // 0 IM帐号、1 微信帐号、2邮件帐号、3短信发送号、4传真发送号、5自定义文本内容
	private String reservedKey; // 关键字内容

	public int getKeyType() {
		return keyType;
	}

	public void setKeyType(int keyType) {
		this.keyType = keyType;
	}

	public String getReservedKey() {
		return reservedKey;
	}

	public void setReservedKey(String reservedKey) {
		this.reservedKey = reservedKey;
	}

}
