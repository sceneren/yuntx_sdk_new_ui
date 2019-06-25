package com.yuntongxun.mcm.mcm.model;

import org.apache.commons.lang.StringUtils;
import org.yuntongxun.tools.util.EncryptUtil;

import com.yuntongxun.mcm.core.AbstractDataModel;

public class M3CSMessageHistory extends AbstractDataModel {

	private String sid; // 会话ID
	private String osUnityAccount; // 多渠道消息统一客户服务号
	private int CCSType; // 客服平台类型，0或为空：CCP平台；1：genesys；2：cisco；3：avaya
	private int eventType; // 事件类型，签入、签出、就绪、未就绪、接起会话等
	private String skillGroupId; // 技能组id

	private String channel; // 模块channel编号
	private String appIdSender; // 发送者应用ID
	private String msgSender; // 发送者账号
	private String appIdReceiver; // 消息接收者应用
	private String msgReceiver; // 消息接收者账号

	private String deviceNo; // 用户设备号
	private String deviceType; // 用户设备类型
	private String msgLen; // 消息长度
	private int msgType; // 消息类型 1:文本消息 2：语音消息 3：视频消息 4：表情消息 5：位置消息 6：文件
	private String msgContent; // 消息内容 base64

	private String msgFileUrl; // 附件url
	private String msgFileName; // 用户指定的文件名
	private long version; // 接收者消息版本号
	private String groupId; // 群组消息id
	private String msgId; // 发送者msgId

	private String msgFileSize; // 附件文件大小
	private String localFileName;
	private String msgDomain; // 需要扩展的消息字段放在这里
	private long msgCompressLen; // 消息内容源长度
	private int mcmEvent; // 是否是多渠道离线消息 1:多渠道消息 else:原消息

	private long expired; // 消息过期时间(ms)
	private int status; // 1 正常 2 撤销
	private int type; // 1 普通消息类型(包含群组消息) 2 群组管理类消息类型
	private int msgDomainFlag; // 扩展字段标志位 0：默认 1：base64
	private String agentId; //坐席Id
	
	private String monitorAgentId;	// 监控者座席ID
	private String resultCode; // 请求返回状态码

	public M3CSMessageHistory() {
		this.version = -1;
		this.expired = 604800000;
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

	public int getCCSType() {
		return CCSType;
	}

	public void setCCSType(int cCSType) {
		CCSType = cCSType;
	}

	public int getEventType() {
		return eventType;
	}

	public void setEventType(int eventType) {
		this.eventType = eventType;
	}

	public String getSkillGroupId() {
		return skillGroupId;
	}

	public void setSkillGroupId(String skillGroupId) {
		this.skillGroupId = skillGroupId;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getAppIdSender() {
		return appIdSender;
	}

	public void setAppIdSender(String appIdSender) {
		this.appIdSender = appIdSender;
	}

	public String getMsgSender() {
		return msgSender;
	}

	public void setMsgSender(String msgSender) {
		this.msgSender = msgSender;
	}

	public String getAppIdReceiver() {
		return appIdReceiver;
	}

	public void setAppIdReceiver(String appIdReceiver) {
		this.appIdReceiver = appIdReceiver;
	}

	public String getMsgReceiver() {
		return msgReceiver;
	}

	public void setMsgReceiver(String msgReceiver) {
		this.msgReceiver = msgReceiver;
	}

	public String getDeviceNo() {
		return deviceNo;
	}

	public void setDeviceNo(String deviceNo) {
		this.deviceNo = deviceNo;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	public String getMsgLen() {
		return msgLen;
	}

	public void setMsgLen(String msgLen) {
		this.msgLen = msgLen;
	}

	public int getMsgType() {
		return msgType;
	}

	public void setMsgType(int msgType) {
		this.msgType = msgType;
	}

	public String getMsgContent() {
		if(StringUtils.isNotBlank(msgContent)){
			msgContent = EncryptUtil.base64Encoder(msgContent);
		}
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

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getMsgId() {
		return msgId;
	}

	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}

	public String getMsgFileSize() {
		return msgFileSize;
	}

	public void setMsgFileSize(String msgFileSize) {
		this.msgFileSize = msgFileSize;
	}

	public String getLocalFileName() {
		return localFileName;
	}

	public void setLocalFileName(String localFileName) {
		this.localFileName = localFileName;
	}

	public String getMsgDomain() {
		return msgDomain;
	}

	public void setMsgDomain(String msgDomain) {
		this.msgDomain = msgDomain;
	}

	public long getMsgCompressLen() {
		return msgCompressLen;
	}

	public void setMsgCompressLen(long msgCompressLen) {
		this.msgCompressLen = msgCompressLen;
	}

	public int getMcmEvent() {
		return mcmEvent;
	}

	public void setMcmEvent(int mcmEvent) {
		this.mcmEvent = mcmEvent;
	}

	public long getExpired() {
		return expired;
	}

	public void setExpired(long expired) {
		this.expired = expired;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getMsgDomainFlag() {
		return msgDomainFlag;
	}

	public void setMsgDomainFlag(int msgDomainFlag) {
		this.msgDomainFlag = msgDomainFlag;
	}

	public String getAgentId() {
		return agentId;
	}

	public void setAgentId(String agentId) {
		this.agentId = agentId;
	}

	public String getMonitorAgentId() {
		return monitorAgentId;
	}

	public void setMonitorAgentId(String monitorAgentId) {
		this.monitorAgentId = monitorAgentId;
	}

	public String getResultCode() {
		return resultCode;
	}

	public void setResultCode(String resultCode) {
		this.resultCode = resultCode;
	}
	
}
