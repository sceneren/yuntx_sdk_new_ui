package com.yuntongxun.mcm.model;

import org.yuntongxun.tools.util.EncryptUtil;

import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMTypeInner;
import com.yuntongxun.mcm.enumerate.WeiXinMsgTypeEnum;

/**
 * 微信消息数据
 * @author weily
 *
 */
public class McmWeiXinMsgInfo {

	private int MCMEvent;
    private String userAccount;
    private String osUnityAccount;
	private String openID; 
	private String userID;
	
	private String msgType;
	private String createTime;
	private String msgId;
	private String content;
	private String appId;
	
	private String fileName;
	private String url;
	private String EventKey;
	private String event;
	
	public int getMCMEvent() {
		return MCMEvent;
	}
	
	public void setMCMEvent(int mCMEvent) {
		MCMEvent = mCMEvent;
	}
	
	public String getOpenID() {
		return openID;
	}
	
	public void setOpenID(String openID) {
		this.openID = openID;
	}
	
	public String getUserID() {
		return userID;
	}
	
	public void setUserID(String userID) {
		this.userID = userID;
	}
	
	public String getMsgType() {
		return msgType;
	}
	
	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}
	
	public String getCreateTime() {
		return createTime;
	}
	
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	
	public String getMsgId() {
		return msgId;
	}
	
	public void setMsgId(String msgId) {
		this.msgId = msgId;
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
	
	public String getFileName() {
		return fileName;
	}
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getEventKey() {
		return EventKey;
	}
	
	public void setEventKey(String eventKey) {
		EventKey = eventKey;
	}
	
	public String getEvent() {
		return event;
	}
	
	public void setEvent(String event) {
		this.event = event;
	}
	
	public String toJsonForWeiXinSend() {
		StringBuffer strBuffer = new StringBuffer();
		strBuffer.append("{");
		strBuffer.append("\"userID\":\"");
		strBuffer.append(userID==null?"":userID);
		strBuffer.append("\",\"openID\":\"");
		strBuffer.append(openID==null?"":openID);
		strBuffer.append("\",\"msgType\":\"");
		strBuffer.append(msgType==null?"":msgType);
		strBuffer.append("\",\"content\":\"");
		strBuffer.append(content==null?"":EncryptUtil.base64Encoder(content));
		strBuffer.append("\",\"filePath\":\"");
		strBuffer.append(url==null?"":url);
		strBuffer.append("\"}");
		return strBuffer.toString();
	}
	
	public int getIMMsgType(String msgType){
		if(WeiXinMsgTypeEnum.TEXT.getValue().equals(msgType)){
			return MCMTypeInner.MCMType_txt_VALUE;
		}else if(WeiXinMsgTypeEnum.IMAGE.getValue().equals(msgType)){
			return MCMTypeInner.MCMType_emotion_VALUE;
		}else if(WeiXinMsgTypeEnum.VOICE.getValue().equals(msgType)){
			return MCMTypeInner.MCMType_audio_VALUE;
		}
		return MCMTypeInner.MCMType_txt_VALUE;
	}
	
	public String getWeixinMsgType(Integer msgType){
		if(MCMTypeInner.MCMType_txt_VALUE==msgType){
			return WeiXinMsgTypeEnum.TEXT.getValue();
		}else if(MCMTypeInner.MCMType_emotion_VALUE==msgType){
			return WeiXinMsgTypeEnum.IMAGE.getValue();
		}else if(MCMTypeInner.MCMType_audio_VALUE==msgType){
			return WeiXinMsgTypeEnum.VOICE.getValue();
		}
		return WeiXinMsgTypeEnum.TEXT.getValue();
	}
	
}
