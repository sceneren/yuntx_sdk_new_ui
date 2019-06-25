package com.yuntongxun.mcm.genesys.model;

import com.genesyslab.platform.voice.protocol.ConnectionId;

public class SipLogin {

	private String thisDN; // 座席号码
	private String queue; // 技能组
	private String agentId; // 座席工号
	private String passwd; // 密码
	private String userAcc;

	private String otherDN;
	private ConnectionId connId;
	private String workMode;
	private ConnectionId conferenceConnId;

	public String getAgentId() {
		return agentId;
	}

	public void setAgentId(String agentId) {
		this.agentId = agentId;
	}

	public String getUserAcc() {
		return userAcc;
	}

	public void setUserAcc(String userAcc) {
		this.userAcc = userAcc;
	}

	public ConnectionId getConnId() {
		return connId;
	}

	public void setConnId(ConnectionId connId) {
		this.connId = connId;
	}

	public String getThisDN() {
		return thisDN;
	}

	public void setThisDN(String thisDN) {
		this.thisDN = thisDN;
	}

	public String getQueue() {
		return queue;
	}

	public void setQueue(String queue) {
		this.queue = queue;
	}

	public String getOtherDN() {
		return otherDN;
	}

	public void setOtherDN(String otherDN) {
		this.otherDN = otherDN;
	}

	public String getPasswd() {
		return passwd;
	}

	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}

	public String getWorkMode() {
		return workMode;
	}

	public void setWorkMode(String workMode) {
		this.workMode = workMode;
	}

	public ConnectionId getConferenceConnId() {
		return conferenceConnId;
	}

	public void setConferenceConnId(ConnectionId conferenceConnId) {
		this.conferenceConnId = conferenceConnId;
	}

	@Override
	public String toString() {
		return "SipLogin [thisDN=" + thisDN + ", queue=" + queue + ", agentId="
				+ agentId + ", passwd=" + passwd + ", userAcc=" + userAcc
				+ ", otherDN=" + otherDN + ", connId=" + connId + ", workMode="
				+ workMode + "]";
	}
	
}
