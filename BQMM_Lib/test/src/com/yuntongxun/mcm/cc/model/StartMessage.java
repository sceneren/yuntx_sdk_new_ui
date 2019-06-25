package com.yuntongxun.mcm.cc.model;

import com.yuntongxun.mcm.cc.enumerate.ActionEnum;

public class StartMessage {

	private String userAccount;
	private String sid;
	private String osUnityAccount;
	private String appId;
	private String chanType;
	
	private String msgId;
	private String createTime;
	private String companyId;
	private String customData;

	public String getUserAccount() {
		return userAccount;
	}

	public void setUserAccount(String userAccount) {
		this.userAccount = userAccount;
	}

	public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}

	public String getOsUnityAccount() {
		return osUnityAccount;
	}

	public void setOsUnityAccount(String osUnityAccount) {
		this.osUnityAccount = osUnityAccount;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getChanType() {
		return chanType;
	}

	public void setChanType(String chanType) {
		this.chanType = chanType;
	}

	public String getMsgId() {
		return msgId;
	}

	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getCustomData() {
		return customData;
	}

	public void setCustomData(String customData) {
		this.customData = customData;
	}

	public String toJson() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"userAccount\":\"");
		sb.append(userAccount == null ? "" : userAccount);

		sb.append("\",\"sid\":\"");
		sb.append(sid == null ? "" : sid);

		sb.append("\",\"osUnityAccount\":\"");
		sb.append(osUnityAccount == null ? "" : osUnityAccount);

		sb.append("\",\"appId\":\"");
		sb.append(appId == null ? "" : appId);

		sb.append("\",\"chanType\":\"");
		sb.append(chanType == null ? "" : chanType);

		sb.append("\",\"msgId\":\"");
		sb.append(msgId == null ? "" : msgId);

		sb.append("\",\"createTime\":\"");
		sb.append(createTime == null ? "" : createTime);

		sb.append("\",\"action\":\"");
		sb.append(ActionEnum.CMD_USER_START_MESSAGE.getValue());

		sb.append("\",\"companyId\":\"");
		sb.append(companyId == null ? "" : companyId);

		sb.append("\"");
		sb.append("}");

		return sb.toString();
	}
}
