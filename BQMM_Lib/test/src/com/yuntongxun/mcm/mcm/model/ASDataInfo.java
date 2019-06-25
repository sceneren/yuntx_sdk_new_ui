package com.yuntongxun.mcm.mcm.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ASDataInfo {

	private String userAccount;
	private String sid;
	private String osUnityAccount;
	private String appId;
	private String customAppID;
	private String chanType;
	private String msgid;
	private String createTime;
	private String action;
	private String content;

	private String userData;
	private String result;// 操作结果值。0为成功，其他值为失败
	private String des;// 失败描述
	private String serverAgentId;// 分配成功的座席ID
	private String queueCount;// 队列的排队人数

	private String optResultCBUrl;
	private String mode;
	private String msgNotifyUrl;
	private String queueType;
	private String recordRoute;
	private String welcome;
	private String robotPara;
	private String subscribeTrace;
	private String subscribeUrl;
	private String agentidfirst;
	private boolean force;
	
	private String resultCode;
	private String resultDes;
	private List<String> agentIds;
	private String companyId;
	
	public String toStartAskJson(){
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
		
		sb.append("\",\"customAppID\":\"");
		sb.append(customAppID == null ? "" : customAppID);
		
		sb.append("\",\"chanType\":\"");
		sb.append(chanType == null ? "" : chanType);
		
		sb.append("\",\"msgid\":\"");
		sb.append(msgid == null ? "" : msgid);
		
		sb.append("\",\"createTime\":\"");
		sb.append(createTime == null ? "" : createTime);
		
		sb.append("\",\"action\":\"");
		sb.append(action == null ? "" : action);
		
		sb.append("\",\"companyId\":\"");
		sb.append(companyId == null ? "" : companyId);
		
		sb.append("\",\"content\":\"");
		sb.append(content == null ? "" : content);	
		
		sb.append("\"");
		sb.append("}");
		return sb.toString();
	}
	

	public String toEndAskJson(){
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"sid\":\"");
		sb.append(sid == null ? "" : sid);
		
		sb.append("\",\"osUnityAccount\":\"");
		sb.append(osUnityAccount == null ? "" : osUnityAccount);
		
		sb.append("\",\"appId\":\"");
		sb.append(appId == null ? "" : appId);
		
		sb.append("\",\"customAppID\":\"");
		sb.append(customAppID == null ? "" : customAppID);

		sb.append("\",\"createTime\":\"");
		sb.append(createTime == null ? "" : createTime);
		
		sb.append("\",\"chanType\":\"");
		sb.append(chanType == null ? "" : chanType);
		
		sb.append("\",\"action\":\"");
		sb.append(action == null ? "" : action);
		
		String temp = "";
		if(agentIds!=null&&agentIds.size()>0){
			for(String agentId:agentIds){
				temp = temp+"\""+agentId+"\",";
			}
			temp = temp.substring(0,temp.length()-1);
		}
		
		if(agentIds!=null&&agentIds.size()>0){
			sb.append("\",\"agentIds\":[");
			sb.append(temp);
			sb.append("]");
		}
		
		sb.append("");
		sb.append("}");
		return sb.toString();
	}
	
	public String toCallbackSetModeJson(){
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"action\":\"");
		sb.append(action == null ? "" : action);
		
		sb.append("\",\"sid\":\"");
		sb.append(sid == null ? "" : sid);
		
		sb.append("\",\"appId\":\"");
		sb.append(appId == null ? "" : appId);
		
		sb.append("\",\"userData\":\"");
		sb.append(userData == null ? "" : userData);
		
		sb.append("\",\"result\":\"");
		sb.append(result == null ? "" : result);
		
		sb.append("\",\"des\":\"");
		sb.append(des == null ? "" : des);
		
		sb.append("\",\"serverAgentId\":\"");
		sb.append(serverAgentId == null ? "" : serverAgentId);
		
		sb.append("\",\"queueCount\":\"");
		sb.append(queueCount == null ? "" : queueCount);
		
		sb.append("\"");
		sb.append("}");
		return sb.toString();
	}
	
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

	public String getCustomAppID() {
		return customAppID;
	}

	public void setCustomAppID(String customAppID) {
		this.customAppID = customAppID;
	}

	public String getChanType() {
		return chanType;
	}

	public void setChanType(String chanType) {
		this.chanType = chanType;
	}

	public String getMsgid() {
		return msgid;
	}

	public void setMsgid(String msgid) {
		this.msgid = msgid;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getUserData() {
		return userData;
	}

	public void setUserData(String userData) {
		this.userData = userData;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getDes() {
		return des;
	}

	public void setDes(String des) {
		this.des = des;
	}

	public String getServerAgentId() {
		return serverAgentId;
	}

	public void setServerAgentId(String serverAgentId) {
		this.serverAgentId = serverAgentId;
	}

	public String getQueueCount() {
		return queueCount;
	}

	public void setQueueCount(String queueCount) {
		this.queueCount = queueCount;
	}

	public String getOptResultCBUrl() {
		return optResultCBUrl;
	}

	public void setOptResultCBUrl(String optResultCBUrl) {
		this.optResultCBUrl = optResultCBUrl;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getMsgNotifyUrl() {
		return msgNotifyUrl;
	}

	public void setMsgNotifyUrl(String msgNotifyUrl) {
		this.msgNotifyUrl = msgNotifyUrl;
	}

	public String getQueueType() {
		return queueType;
	}

	public void setQueueType(String queueType) {
		this.queueType = queueType;
	}

	public String getRecordRoute() {
		return recordRoute;
	}

	public void setRecordRoute(String recordRoute) {
		this.recordRoute = recordRoute;
	}

	public String getWelcome() {
		return welcome;
	}

	public void setWelcome(String welcome) {
		this.welcome = welcome;
	}

	public String getRobotPara() {
		return robotPara;
	}

	public void setRobotPara(String robotPara) {
		this.robotPara = robotPara;
	}

	public String getSubscribeTrace() {
		return subscribeTrace;
	}

	public void setSubscribeTrace(String subscribeTrace) {
		this.subscribeTrace = subscribeTrace;
	}

	public String getSubscribeUrl() {
		return subscribeUrl;
	}

	public void setSubscribeUrl(String subscribeUrl) {
		this.subscribeUrl = subscribeUrl;
	}

	public String getAgentidfirst() {
		return agentidfirst;
	}

	public void setAgentidfirst(String agentidfirst) {
		this.agentidfirst = agentidfirst;
	}

	public boolean isForce() {
		return force;
	}

	public void setForce(boolean force) {
		this.force = force;
	}

	public List<String> getAgentIds() {
		return agentIds;
	}

	public void setAgentIds(List<String> agentIds) {
		this.agentIds = agentIds;
	}
	
	public String getResultCode() {
		return resultCode;
	}


	public void setResultCode(String resultCode) {
		this.resultCode = resultCode;
	}


	public String getResultDes() {
		return resultDes;
	}


	public void setResultDes(String resultDes) {
		this.resultDes = resultDes;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public static void main(String[] args) {
		ASDataInfo as = new ASDataInfo();
		List<String> test = new ArrayList<String>();
		test.add("1");
		test.add("2");
//		as.setAgentIds(test);
//		System.out.println((String[])test.toArray(new String[test.size()]));
//		System.out.println(as.toEndAskJson());
		
		System.out.println(Arrays.toString((String[]) test.toArray(new String[test.size()])));
		
	}
}
