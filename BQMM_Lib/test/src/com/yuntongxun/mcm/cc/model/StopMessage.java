package com.yuntongxun.mcm.cc.model;

import java.util.List;

import com.yuntongxun.mcm.cc.enumerate.ActionEnum;

public class StopMessage {

	private String sid;
	private String appId;
	private String osUnityAccount;
	private String chanType;
	private String createTime;

	private List<String> agentIds;

	public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getOsUnityAccount() {
		return osUnityAccount;
	}

	public void setOsUnityAccount(String osUnityAccount) {
		this.osUnityAccount = osUnityAccount;
	}

	public String getChanType() {
		return chanType;
	}

	public void setChanType(String chanType) {
		this.chanType = chanType;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public List<String> getAgentIds() {
		return agentIds;
	}

	public void setAgentIds(List<String> agentIds) {
		this.agentIds = agentIds;
	}

	public String toJson() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"sid\":\"");
		sb.append(sid == null ? "" : sid);

		sb.append("\",\"osUnityAccount\":\"");
		sb.append(osUnityAccount == null ? "" : osUnityAccount);

		sb.append("\",\"appId\":\"");
		sb.append(appId == null ? "" : appId);

		sb.append("\",\"createTime\":\"");
		sb.append(createTime == null ? "" : createTime);

		sb.append("\",\"chanType\":\"");
		sb.append(chanType == null ? "" : chanType);

		sb.append("\",\"action\":\"");
		sb.append(ActionEnum.CMD_USER_STOP_MESSAGE.getValue());

		String temp = "";
		if (agentIds != null && !agentIds.isEmpty()) {
			for (String agentId : agentIds) {
				temp = temp + "\"" + agentId + "\",";
			}
			temp = temp.substring(0, temp.length() - 1);
		}

		if (agentIds != null && agentIds.size() > 0) {
			sb.append("\",\"agentIds\":[");
			sb.append(temp);
			sb.append("]");
		}

		sb.append("");
		sb.append("}");
		return sb.toString();
	}
}
