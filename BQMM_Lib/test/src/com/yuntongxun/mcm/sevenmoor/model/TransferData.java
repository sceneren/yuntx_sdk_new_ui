package com.yuntongxun.mcm.sevenmoor.model;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.yuntongxun.mcm.util.JsonUtils;

public class TransferData {
	private String ActionId;
	private String Action;
	
	private String ConnectionId;
	private int IoSessionId;
	private String ReceivedMsgIds;
	private String ContentType;
	private String Message;

	private String AccessId;
	private String UserId;
	private String UserName;
	private String NewVersion = "true";
	private String DeviceId;
	private String ConnectServer;
	private String Key;
	private String ApnsDeviceId;

	private boolean success;
	private String statusCode;
	
	private boolean Succeed;
	private String SessionId;
	private boolean IsNewVisitor = true;
	
	private List<SevenMoorMsg> data = new ArrayList<SevenMoorMsg>();
	
	private String Platform;
	
	private int VoiceSecond;
	
	private List<SevenMoorInvestigate> List = new ArrayList<SevenMoorInvestigate>();
	
	private SevenMoorInvestigate userInvestigate = new SevenMoorInvestigate();
	
	private String name;
	private String value;
	
	private String Type;
	
	private String userData;
	
	public String toRespJson() {
		StringBuffer strBuffer = new StringBuffer();
		strBuffer.append("{");
		strBuffer.append("\"success\":");
		strBuffer.append(success);
		strBuffer.append(",\"statusCode\":\"");
		strBuffer.append(statusCode);
		strBuffer.append("\"}");
		System.out.println(strBuffer.toString());
		return strBuffer.toString();
	}
		
	public String toLoginJson() {
		
		
		StringBuffer strBuffer = new StringBuffer();
		strBuffer.append("{\"data\":\"{");
		strBuffer.append("\\\"Command\\\":\\\"");
		strBuffer.append("Action");
		strBuffer.append("\\\",\\\"AccessId\\\":\\\"");
		strBuffer.append(AccessId);
		strBuffer.append("\\\",\\\"Key\\\":\\\"");
		strBuffer.append(Key);
		strBuffer.append("\\\",\\\"ConnectServer\\\":\\\"");
		strBuffer.append(ConnectServer);
		strBuffer.append("\\\",\\\"NewVersion\\\":\\\"");
		strBuffer.append(NewVersion);
		strBuffer.append("\\\",\\\"UserId\\\":\\\"");
		strBuffer.append(UserId);
		strBuffer.append("\\\",\\\"UserName\\\":\\\"");
		strBuffer.append(UserName);
		strBuffer.append("\\\",\\\"DeviceId\\\":\\\"");
		strBuffer.append(DeviceId);
		strBuffer.append("\\\",\\\"Action\\\":\\\"");
		strBuffer.append(Action);
		strBuffer.append("\\\",\\\"ActionId\\\":\\\"");
		strBuffer.append(ActionId);
		strBuffer.append("\\\",\\\"Platform\\\":\\\"");
		strBuffer.append(Platform);
		strBuffer.append("\\\",\\\"IoSessionId\\\":");
		strBuffer.append(IoSessionId);
		strBuffer.append("}\"}");
		return strBuffer.toString();
		
	}
	
	public static void main(String[] args) {
		Gson gson = new Gson();
		
		String json = "{\"key\":\"testkey\",\"value\":\"testvalue\"}";
//		json = json.replace("\"", "\\\\\\\"");
//		System.out.println(json);
		TransferData td = new TransferData();
		td.setAction("testaction");
		td.setConnectionId(json);
//		td.setData1(JsonUtils.bean2json(td));
//		System.out.println(td.toSdkGetInvestigateJson());
//		System.out.println(gson.toJson(td.toSdkGetInvestigateJson()));
//		System.out.println(JsonUtils.bean2json(td));
		JsonMsg jsonMsg = new JsonMsg();
		jsonMsg.setData(JsonUtils.bean2json(td));
		System.out.println(JsonUtils.bean2json(jsonMsg));
	}
	
	public String toSendMsgJson() {
		StringBuffer strBuffer = new StringBuffer();
		strBuffer.append("{\"data\":\"{");
		strBuffer.append("\\\"Command\\\":\\\"");
		strBuffer.append("Action");
		strBuffer.append("\\\",\\\"ActionID\\\":\\\"");
		strBuffer.append(ActionId);
		strBuffer.append("\\\",\\\"ContentType\\\":\\\"");
		strBuffer.append(ContentType);
		strBuffer.append("\\\",\\\"ConnectionId\\\":\\\"");
		strBuffer.append(ConnectionId);
		strBuffer.append("\\\",\\\"UserData\\\":\\\"");
		strBuffer.append(userData);
		strBuffer.append("\\\",\\\"Message\\\":\\\"");
		strBuffer.append(Message);
		strBuffer.append("\\\"");
		if(VoiceSecond>0){
			strBuffer.append(",\\\"VoiceSecond\\\":");
			strBuffer.append(VoiceSecond);
		}
		
		strBuffer.append(",\\\"Action\\\":\\\"");
		strBuffer.append(Action);
		strBuffer.append("\\\"}\"}");
		return strBuffer.toString();
	}
	
	public String toGetMsgJson() {
		StringBuffer strBuffer = new StringBuffer();
		strBuffer.append("{\"data\":\"{");
		strBuffer.append("\\\"ReceivedMsgIds\\\":[");
		strBuffer.append(ReceivedMsgIds);
		strBuffer.append("],\\\"Action\\\":\\\"");
		strBuffer.append(Action);
		strBuffer.append("\\\",\\\"ConnectionId\\\":\\\"");
		strBuffer.append(ConnectionId);
		strBuffer.append("\\\"}\"}");
		return strBuffer.toString();
	}
	
	public String toSdkGetInvestigateJson() {
		StringBuffer strBuffer = new StringBuffer();
		strBuffer.append("{\"data\":\"{");
		strBuffer.append("\\\"Action\\\":\\\"");
		strBuffer.append(Action);
		strBuffer.append("\\\",\\\"ConnectionId\\\":\\\"");
		strBuffer.append(ConnectionId);
		strBuffer.append("\\\"}\"}");
		return strBuffer.toString();
	}
	
	public String tosdkBeginNewChatSessionJson() {
		StringBuffer strBuffer = new StringBuffer();
		strBuffer.append("{\"data\":\"{");
		strBuffer.append("\\\"Action\\\":\\\"");
		strBuffer.append(Action);
		strBuffer.append("\\\",\\\"ConnectionId\\\":\\\"");
		strBuffer.append(ConnectionId);
		strBuffer.append("\\\",\\\"IsNewVisitor\\\":");
		strBuffer.append(IsNewVisitor);
		strBuffer.append("}\"}");
		return strBuffer.toString();
	}
	
	public String toSubmitInvestigateJson(){
		StringBuffer strBuffer = new StringBuffer();
		strBuffer.append("{\"data\":\"{");	
		strBuffer.append("\\\"Action\\\":\\\"");
		strBuffer.append(Action);
		strBuffer.append("\\\",\\\"ConnectionId\\\":\\\"");
		strBuffer.append(ConnectionId);
		strBuffer.append("\\\",\\\"Name\\\":\\\"");
		strBuffer.append(name);
		strBuffer.append("\\\",\\\"Value\\\":\\\"");
		strBuffer.append(value);
		strBuffer.append("\\\"}\"}");
		return strBuffer.toString();
	}
	
	public String toUserEndAskJson(){
		StringBuffer strBuffer = new StringBuffer();
		strBuffer.append("{\"data\":\"{");
		strBuffer.append("\\\"Command\\\":\\\"");
		strBuffer.append("Action");
		strBuffer.append("\\\",\\\"Action\\\":\\\"");
		strBuffer.append(Action);
		strBuffer.append("\\\",\\\"ActionID\\\":\\\"");
		strBuffer.append(ActionId);
		strBuffer.append("\\\",\\\"ConnectionId\\\":\\\"");
		strBuffer.append(ConnectionId);
		strBuffer.append("\\\"}\"}");
		return strBuffer.toString();
	}
	
	public String getActionId() {
		return ActionId;
	}

	public void setActionId(String actionId) {
		ActionId = actionId;
	}

	public String getConnectionId() {
		return ConnectionId;
	}

	public void setConnectionId(String connectionId) {
		ConnectionId = connectionId;
	}

	public int getIoSessionId() {
		return IoSessionId;
	}

	public void setIoSessionId(int ioSessionId) {
		IoSessionId = ioSessionId;
	}

	public boolean isIsNewVisitor() {
		return IsNewVisitor;
	}

	public void setIsNewVisitor(boolean isNewVisitor) {
		IsNewVisitor = isNewVisitor;
	}

	public String getReceivedMsgIds() {
		return ReceivedMsgIds;
	}

	public void setReceivedMsgIds(String receivedMsgIds) {
		ReceivedMsgIds = receivedMsgIds;
	}

	public String getContentType() {
		return ContentType;
	}

	public void setContentType(String contentType) {
		ContentType = contentType;
	}

	public String getMessage() {
		return Message;
	}

	public void setMessage(String message) {
		Message = message;
	}

	public String getAccessId() {
		return AccessId;
	}

	public void setAccessId(String accessId) {
		AccessId = accessId;
	}

	public String getUserId() {
		return UserId;
	}

	public void setUserId(String userId) {
		UserId = userId;
	}

	public String getUserName() {
		return UserName;
	}

	public void setUserName(String userName) {
		UserName = userName;
	}

	public String getDeviceId() {
		return DeviceId;
	}

	public void setDeviceId(String deviceId) {
		DeviceId = deviceId;
	}

	public String getConnectServer() {
		return ConnectServer;
	}

	public void setConnectServer(String connectServer) {
		ConnectServer = connectServer;
	}

	public String getKey() {
		return Key;
	}

	public void setKey(String key) {
		Key = key;
	}

	public String getApnsDeviceId() {
		return ApnsDeviceId;
	}

	public void setApnsDeviceId(String apnsDeviceId) {
		ApnsDeviceId = apnsDeviceId;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}

	public String getAction() {
		return Action;
	}

	public void setAction(String action) {
		Action = action;
	}

	public boolean isSucceed() {
		return Succeed;
	}

	public void setSucceed(boolean succeed) {
		Succeed = succeed;
	}

	public String getSessionId() {
		return SessionId;
	}

	public void setSessionId(String sessionId) {
		SessionId = sessionId;
	}

	public List<SevenMoorMsg> getData() {
		return data;
	}

	public void setData(List<SevenMoorMsg> data) {
		this.data = data;
	}

	public String getNewVersion() {
		return NewVersion;
	}

	public void setNewVersion(String newVersion) {
		NewVersion = newVersion;
	}

	public String getPlatform() {
		return Platform;
	}

	public void setPlatform(String platform) {
		Platform = platform;
	}

	public int getVoiceSecond() {
		return VoiceSecond;
	}

	public void setVoiceSecond(int voiceSecond) {
		VoiceSecond = voiceSecond;
	}

	public List<SevenMoorInvestigate> getList() {
		return List;
	}

	public void setList(List<SevenMoorInvestigate> list) {
		List = list;
	}

	public SevenMoorInvestigate getUserInvestigate() {
		return userInvestigate;
	}

	public void setUserInvestigate(SevenMoorInvestigate userInvestigate) {
		this.userInvestigate = userInvestigate;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getType() {
		return Type;
	}

	public void setType(String type) {
		Type = type;
	}

	public String getUserData() {
		return userData;
	}

	public void setUserData(String userData) {
		this.userData = userData;
	}

}
