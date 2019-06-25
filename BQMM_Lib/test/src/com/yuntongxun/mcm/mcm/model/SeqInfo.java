package com.yuntongxun.mcm.mcm.model;

import org.ming.sample.util.JSONUtil;
import org.yuntongxun.tools.util.EncryptUtil;

public class SeqInfo {

	private String agentAccount;
	private String userAccount;
	private String agentId;
	private boolean isReady; // 是否就绪，1.就绪 2.未就绪
	private int mcmEvent;
	
	private String superAccount; // 管理员
	private String appId;
	private int protoClientNo;
	private String asFlag;//1:AS座席，2：非AS座席
	private String asWelcome;//AS欢迎语
	
	private int msgType;//消息类型
	private String msgContent;//消息内容
	private String msgFileUrl;//附件URL
	private String msgFileName;
	private String transferAllocAgentCount;
	
	private String transferAllocAgentId;
	private String connectorId;
	private String logSessionId; // 日志sessionId
	private String superAgentId; // 管理者agentId
	private String chanType;
	
	private String osUnityAccount;
	private String sid;
	private String msgJsonData;
	
	public String getAgentAccount() {
		return agentAccount;
	}

	public void setAgentAccount(String agentAccount) {
		this.agentAccount = agentAccount;
	}

	public String getUserAccount() {
		return userAccount;
	}

	public void setUserAccount(String userAccount) {
		this.userAccount = userAccount;
	}

	public String getAgentId() {
		return agentId;
	}

	public void setAgentId(String agentId) {
		this.agentId = agentId;
	}

	public boolean isReady() {
		return isReady;
	}

	public void setReady(boolean isReady) {
		this.isReady = isReady;
	}

	public int getMcmEvent() {
		return mcmEvent;
	}

	public void setMcmEvent(int mcmEvent) {
		this.mcmEvent = mcmEvent;
	}

	public String getSuperAccount() {
		return superAccount;
	}

	public void setSuperAccount(String superAccount) {
		this.superAccount = superAccount;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}
	
	public int getProtoClientNo() {
		return protoClientNo;
	}

	public void setProtoClientNo(int protoClientNo) {
		this.protoClientNo = protoClientNo;
	}

	public String getAsFlag() {
		return asFlag;
	}

	public void setAsFlag(String asFlag) {
		this.asFlag = asFlag;
	}

	public String getAsWelcome() {
		return asWelcome;
	}

	public void setAsWelcome(String asWelcome) {
		this.asWelcome = asWelcome;
	}
	
	public String getTransferAllocAgentCount() {
		return transferAllocAgentCount;
	}

	public void setTransferAllocAgentCount(String transferAllocAgentCount) {
		this.transferAllocAgentCount = transferAllocAgentCount;
	}

	public String getTransferAllocAgentId() {
		return transferAllocAgentId;
	}

	public void setTransferAllocAgentId(String transferAllocAgentId) {
		this.transferAllocAgentId = transferAllocAgentId;
	}

	public int getMsgType() {
		return msgType;
	}

	public void setMsgType(int msgType) {
		this.msgType = msgType;
	}

	public String getMsgContent() {
		return msgContent;
	}

	public void setMsgContent(String msgContent) {
		this.msgContent = msgContent;
	}

	public String getMsgFileUrl() {
		return msgFileUrl;
	}

	public void setMsgFileUrl(String msgFileUrl) {
		this.msgFileUrl = msgFileUrl;
	}
	
	public String getMsgFileName() {
		return msgFileName;
	}

	public void setMsgFileName(String msgFileName) {
		this.msgFileName = msgFileName;
	}

	public String getConnectorId() {
		return connectorId;
	}

	public void setConnectorId(String connectorId) {
		this.connectorId = connectorId;
	}

	public String getLogSessionId() {
		return logSessionId;
	}

	public void setLogSessionId(String logSessionId) {
		this.logSessionId = logSessionId;
	}
	
	public String getSuperAgentId() {
		return superAgentId;
	}

	public void setSuperAgentId(String superAgentId) {
		this.superAgentId = superAgentId;
	}

	public String getChanType() {
		return chanType;
	}

	public void setChanType(String chanType) {
		this.chanType = chanType;
	}

	public String getOsUnityAccount() {
		return osUnityAccount;
	}

	public void setOsUnityAccount(String osUnityAccount) {
		this.osUnityAccount = osUnityAccount;
	}

	public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}
	
	public String getMsgJsonData() {
		return msgJsonData;
	}

	public void setMsgJsonData(String msgJsonData) {
		this.msgJsonData = msgJsonData;
	}

	@Override
	public String toString() {
		return EncryptUtil.base64Encoder(JSONUtil.object2json(this));
	}
	
}
