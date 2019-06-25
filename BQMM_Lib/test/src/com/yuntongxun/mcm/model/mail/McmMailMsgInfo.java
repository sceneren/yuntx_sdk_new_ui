package com.yuntongxun.mcm.model.mail;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by weily
 */
public class McmMailMsgInfo {
	
	private int MCMEvent;
    private String userAccount;
    private String osUnityAccount;
    private String msgType;
    private String msgDate;
    private String msgContent;
    private String mailTitle;
    private String mailFrom;
    private String mailFromDisplayName;
    
	private String action;
	private String actionId;
	private String mailId;
	private String type;
	private String password;
	private String mailDisplayName;
	private String receiveServer;
	private String sendServer;
	private String sendServerPort;
	
	private String pwd;
	private String to;
	private String cc;
	private String title;
	private String content;
    
	private String appId;
	
	private String configType;
	
	
	
    private List<McmMailAttchmentInfo> attachment = new ArrayList<McmMailAttchmentInfo>();
    
	public String getUserAccount() {
		return userAccount;
	}
	public void setUserAccount(String userAccount) {
		this.userAccount = userAccount;
	}
	public String getOsUnityAccount() {
		return osUnityAccount;
	}
	public void setOsUnityAccount(String osUnityAccount) {
		this.osUnityAccount = osUnityAccount;
	}
	public String getMsgType() {
		return msgType;
	}
	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}
	public String getMsgDate() {
		return msgDate;
	}
	public void setMsgDate(String msgDate) {
		this.msgDate = msgDate;
	}
	public String getMsgContent() {
		return msgContent;
	}
	public void setMsgContent(String msgContent) {
		this.msgContent = msgContent;
	}
	public String getMailTitle() {
		return mailTitle;
	}
	public void setMailTitle(String mailTitle) {
		this.mailTitle = mailTitle;
	}
	public String getMailFrom() {
		return mailFrom;
	}
	public void setMailFrom(String mailFrom) {
		this.mailFrom = mailFrom;
	}
	public String getMailFromDisplayName() {
		return mailFromDisplayName;
	}
	public void setMailFromDisplayName(String mailFromDisplayName) {
		this.mailFromDisplayName = mailFromDisplayName;
	}
	public List<McmMailAttchmentInfo> getAttachment() {
		return attachment;
	}
	public void setAttachment(List<McmMailAttchmentInfo> attachment) {
		this.attachment = attachment;
	}
	public int getMCMEvent() {
		return MCMEvent;
	}
	public void setMCMEvent(int mCMEvent) {
		MCMEvent = mCMEvent;
	}
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
	public String getPwd() {
		return pwd;
	}
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	public String getCc() {
		return cc;
	}
	public void setCc(String cc) {
		this.cc = cc;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
	
	public String getConfigType() {
		return configType;
	}
	public void setConfigType(String configType) {
		this.configType = configType;
	}
	
	public String toJsonForMailAddOrUpdate(){
		StringBuffer strBuffer = new StringBuffer();
		strBuffer.append("{");
		strBuffer.append("\"action\":\"");
		strBuffer.append(action==null?"":action);
		strBuffer.append("\",\"actionId\":\"");
		strBuffer.append(actionId==null?"":actionId);
		strBuffer.append("\",\"mailId\":\"");
		strBuffer.append(mailId==null?"":mailId);
		strBuffer.append("\",\"type\":");
		strBuffer.append(type==null?"":type);
		strBuffer.append("\",\"password\":\"");
		strBuffer.append(password==null?"":password);
		strBuffer.append("\",\"mailDisplayName\":\"");
		strBuffer.append(mailDisplayName==null?"":mailDisplayName);
		strBuffer.append("\",\"receiveServer\":\"");
		strBuffer.append(receiveServer==null?"":receiveServer);
		strBuffer.append("\",\"sendServer\":\"");
		strBuffer.append(sendServer==null?"":sendServer);
		strBuffer.append("\",\"sendServerPort\":\"");
		strBuffer.append(sendServerPort==null?"":sendServerPort);
		strBuffer.append(",\"appId\":\"");
		strBuffer.append(appId==null?"":appId);
		strBuffer.append(",\"osUnityAccount\":\"");
		strBuffer.append(osUnityAccount==null?"":osUnityAccount);
		strBuffer.append("\"}");
		return strBuffer.toString();
	}

	public String toJsonForMailDelete(){
		StringBuffer strBuffer = new StringBuffer();
		strBuffer.append("{");
		strBuffer.append("\"action\":\"");
		strBuffer.append(action==null?"":action);
		strBuffer.append("\",\"actionId\":\"");
		strBuffer.append(actionId==null?"":actionId);
		strBuffer.append("\",\"mailId\":\"");
		strBuffer.append(mailId==null?"":mailId);
		strBuffer.append("\"}");
		return strBuffer.toString();
	}
	
	public String toJsonForMailSend() throws UnsupportedEncodingException{
		StringBuffer strBuffer = new StringBuffer();
		strBuffer.append("{");
		strBuffer.append("\"action\":\"");
		strBuffer.append(action==null?"":action);
		strBuffer.append("\",\"actionId\":\"");
		strBuffer.append(actionId==null?"":actionId);
		strBuffer.append("\",\"mailId\":\"");
		strBuffer.append(mailId==null?"":mailId);
		strBuffer.append("\",\"to\":\"");
		strBuffer.append(to==null?"":to);
		strBuffer.append("\",\"cc\":\"");
		strBuffer.append(cc==null?"":cc);
		strBuffer.append("\",\"content\":\"");
		strBuffer.append(content==null?"":URLEncoder.encode(content, "UTF-8"));
		strBuffer.append("\",\"title\":\"");
		strBuffer.append(mailTitle==null?"":URLEncoder.encode(mailTitle, "UTF-8"));
		strBuffer.append("\"");
		if(attachment!=null&&attachment.size()>0){
			strBuffer.append(",\"attachment\":");
			strBuffer.append("[");
			for(McmMailAttchmentInfo temp:attachment){
				strBuffer.append("{");
				strBuffer.append("\"fileName\":\"");
				strBuffer.append(temp.getFileName()==null?"":URLEncoder.encode(temp.getFileName(), "UTF-8"));
				strBuffer.append("\"url\":\"");
				strBuffer.append(temp.getUrl()==null?"":temp.getUrl());
				strBuffer.append("\"},");
			}
			strBuffer.substring(0, strBuffer.length()-1);
		strBuffer.append("]");
		}
		strBuffer.append("}");
		return strBuffer.toString();
	}
    
}
