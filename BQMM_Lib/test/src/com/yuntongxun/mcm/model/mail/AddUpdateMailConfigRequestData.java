package com.yuntongxun.mcm.model.mail;


public class AddUpdateMailConfigRequestData {

	private String action;
	private String actionId;
	private String mailId;
	private String type;
	private String password;
	private String mailDisplayName;
	private String receiveServer;
	private String sendServer;
	private String sendServerPort;
	
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public String getActionId() {
		return actionId;
	}
	public void setActionId(String actionId) {
		this.actionId = actionId;
	}
	public String getMailId() {
		return mailId;
	}
	public void setMailId(String mailId) {
		this.mailId = mailId;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getMailDisplayName() {
		return mailDisplayName;
	}
	public void setMailDisplayName(String mailDisplayName) {
		this.mailDisplayName = mailDisplayName;
	}
	public String getReceiveServer() {
		return receiveServer;
	}
	public void setReceiveServer(String receiveServer) {
		this.receiveServer = receiveServer;
	}
	public String getSendServer() {
		return sendServer;
	}
	public void setSendServer(String sendServer) {
		this.sendServer = sendServer;
	}
	public String getSendServerPort() {
		return sendServerPort;
	}
	public void setSendServerPort(String sendServerPort) {
		this.sendServerPort = sendServerPort;
	}

	
	
}
