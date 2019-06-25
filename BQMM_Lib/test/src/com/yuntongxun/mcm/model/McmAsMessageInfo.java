package com.yuntongxun.mcm.model;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMEventDefInner;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMTypeInner;

public class McmAsMessageInfo {
	public final Logger logger = LogManager.getLogger(McmAsMessageInfo.class);
	//用户帐号
	private String userAccount;
	//多渠道消息统一客户服务号
	private String osUnityAccount;
	//云平台应用ID
	private String appId;
	//用户自定义应用ID
	private String customAppId;
	//消息id
	private String msgSid;
	//时间
	private long createTime;
	//消息渠道类型
	private int channelType;
	//消息类型
	private int msgType; // IM 消息 取值为1，文本消息  //多媒体 消息 -> 2：语音消息 3：视频消息  4：图片  6：文件
	//消息内容       //多媒体 消息 ->文本描述内容
	private String content;
	//文件下载地址
	private String fileDownUrl;
	//原文件名
	private String fileName;
	
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
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
	public String getCustomAppId() {
		return customAppId;
	}
	public void setCustomAppId(String customAppId) {
		this.customAppId = customAppId;
	}
	public String getMsgSid() {
		return msgSid;
	}
	public void setMsgSid(String msgSid) {
		this.msgSid = msgSid;
	}
	public long getCreateTime() {
		return createTime;
	}
	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}
	public int getChannelType() {
		return channelType;
	}
	public void setChannelType(int channelType) {
		this.channelType = channelType;
	}
	public int getMsgType() {
		return msgType;
	}
	public void setMsgType(int msgType) {
		this.msgType = msgType;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	
	public String toString(int MCMEvent) {
//		<MCM>
//		 <userAccount><![CDATA[userAccount]]></userAccount>
//		 <osUnityAccount><![CDATA[osUnityAcc]]></osUnityAccount>
//		 <appId>dfdslkfdjlsf</appId>
//		 <customAppId><![CDATA[cusappid]]></customAppId>
//		 <msgSid>1348831860</msgSid>
//		 <createTime>1348831987</createTime>
//		<MsgData>
//		  <Data>
//		     <msgType>1</msgType>
//		     <content><![CDATA[content]]></content>
//		  </Data>
//		</MsgData>
//		</MCM>

		StringBuilder builder = new StringBuilder();
		builder.append("<MCM>");
		builder.append("<userAccount><![CDATA["+userAccount+"]]></userAccount>");
		builder.append("<osUnityAccount><![CDATA["+osUnityAccount+"]]></osUnityAccount>");
		builder.append("<appId>"+appId+"</appId>");
		builder.append("<customAppId><![CDATA["+customAppId+"]]></customAppId>");
		builder.append("<msgSid>"+msgSid+"</msgSid>");
		builder.append("<createTime>"+createTime+"</createTime>");
		//开始咨询  结束咨询
		if(MCMEvent== MCMEventDefInner.UserEvt_StartAsk_VALUE || MCMEvent== MCMEventDefInner.UserEvt_EndAsk_VALUE)
		{
			builder.append("<msgType>"+msgType+"</msgType>");
		}
		else if(MCMEvent== MCMEventDefInner.UserEvt_SendMSG_VALUE)
		{
			builder.append("<MsgData>");
			builder.append("<Data>");
			builder.append("<msgType>"+msgType+"</msgType>");
			builder.append("<content><![CDATA["+content+"]]></content>");
			//多媒体消息   2：语音消息 3：视频消息  4：图片  6：文件
			if(msgType==MCMTypeInner.MCMType_audio_VALUE || msgType==MCMTypeInner.MCMType_video_VALUE
					|| msgType==MCMTypeInner.MCMType_emotion_VALUE || msgType==MCMTypeInner.MCMType_file_VALUE)
			{
				builder.append("<fileDownUrl><![CDATA["+fileDownUrl+"]]></fileDownUrl>");
				builder.append("<fileName><![CDATA["+fileName+"]]></fileName>");
			}
			builder.append("</Data>");
			builder.append("</MsgData>");
		}
		else
		{}
		builder.append("</MCM>");
		logger.info("++++++++++++++++++++++++++");
		logger.info("builder.toString():"+builder.toString());
		logger.info("++++++++++++++++++++++++++");
		return builder.toString();
	}
	public String getFileDownUrl() {
		return fileDownUrl;
	}
	public void setFileDownUrl(String fileDownUrl) {
		this.fileDownUrl = fileDownUrl;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
}
