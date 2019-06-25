package com.yuntongxun.mcm.sevenmoor.model;

public class SdkNewMsgJsonData {
	private String Command;
	private String ActionID;
	private String ContentType;
	private String ConnectionId;
	private String UserData;
	private Integer VoiceSecond;
	private String Message;
	private String Action;

	public String getCommand() {
		return Command;
	}

	public void setCommand(String command) {
		Command = command;
	}

	public String getActionID() {
		return ActionID;
	}

	public void setActionID(String actionID) {
		ActionID = actionID;
	}

	public String getContentType() {
		return ContentType;
	}

	public void setContentType(String contentType) {
		ContentType = contentType;
	}

	public String getConnectionId() {
		return ConnectionId;
	}

	public void setConnectionId(String connectionId) {
		ConnectionId = connectionId;
	}

	public String getUserData() {
		return UserData;
	}

	public void setUserData(String userData) {
		UserData = userData;
	}

	public Integer getVoiceSecond() {
		return VoiceSecond;
	}

	public void setVoiceSecond(Integer voiceSecond) {
		VoiceSecond = voiceSecond;
	}

	public String getMessage() {
		return Message;
	}

	public void setMessage(String message) {
		Message = message;
	}

	public String getAction() {
		return Action;
	}

	public void setAction(String action) {
		Action = action;
	}

}
