/**
 * 
 */
package com.yuntongxun.mcm.mcm.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ming.sample.util.Base64;

import com.yuntongxun.mcm.core.AbstractDataModel;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMEventDefInner;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMTypeInner;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MSGDataInner;

/**
 * @author chao
 */
public class MCMMessageInfo extends AbstractDataModel {
	
	public static final Logger logger = LogManager.getLogger(MCMMessageInfo.class);

	public static final int MSG_MAX_WORDS = 2048;

	public static final int MSG_TEXT = 1;
	public static final int MSG_VOICE = 2;
	public static final int MSG_PIC = 4;
	public static final int MSG_NOTIFY = 7;
	public static final int MSG_CMD = 9;

	private long version = -1; // 版本号
	private String msgId;// 服务器端的msgId
	private int msgType = 0;// 消息类型 1:文本消息 2：语音消息 3：视频消息 4：表情消息 5：位置消息 6：文件
	private String msgContent; // 消息内容 小于50字符在线推送，否则客户端拉取
	private String msgFileUrl;//文件下载地址
	private String msgFileName;//消息文件名
	private String mailTitle;//消息文件名
	
	private int msgCompressLen;// 消息内容源长度
	private long expired = 604800000; // 消息过期时间(ms)
	
	private int isMCM=0;//是否是多渠道离线消息 1:多渠道消息 else:原消息
	
	private String osUnityAccount; //多渠道消息统一客户服务号
	
	private String userAccount; //用户帐号
	
	private String  appId;//应用Id
	
	private int MCMEvent=0;
	private String msgDateCreated; //服务器接收消息时间（ms）
	private int chanType;
	private String agentAccount;
	private String agentId;
	private String transAgentId;
	private String userPhone;
	
	private int CCSType;
	private String nickName;

	private String notifyDes;
	private String userRespRet;
	
	private String key;
	private String value;
	
	private int imState;
	private int telState;
	private String number;
	private String pushVoipacc;
	private String telQueueType;
	private String imQueueType;
	private String userInfoCallbackurl;
	private int delayCall;
	private int answerTimeout;
	
	//
	private int optResult;
	private String optRetDes;
	private String msgJsonData;
	
	private List<MCMMsgDataInfo> mutiMsg = new ArrayList<MCMMsgDataInfo>();
	

	private String longitude;
	private String latitude;
	
	private String createTime;
	
	private String ccpCustomData;
	
	//private int queuecount;
	private int idlecount;
	/**
	 * 
	 */
	public MCMMessageInfo() {
		super();
	}

	/**
	 * 
	 */
	public MCMMessageInfo(Object obj) {
		try {
			BeanUtils.copyProperties(this, obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (obj instanceof MSGDataInner) {
//			com.google.protobuf.ByteString byteString = ((MSGDataInner) obj).getMsgContent();
//			if (byteString == null) {
//				return;
//			}
//			if (byteString.size() > MSG_MAX_WORDS) {
//				logger.info("Compress's message content length over [" + MSG_MAX_WORDS + "], client should be cut...");
//			}
			byte[] b=((MSGDataInner) obj).getMsgContent().getBytes();
			if (b == null) {
				return;
			}
			if (b.length > MSG_MAX_WORDS) {
				logger.info("Compress's message content length over [" + MSG_MAX_WORDS + "], client should be cut...");
			}
			// Client used JZlib 压缩
			msgContent = Base64.encode(b);
//			msgContent = Base64.encode(byteString.toByteArray());
		} 
	}

	public byte[] compress(byte[] b) throws Exception {
		return null;
	}

	/**
	 * 数据解压缩
	 * 
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public static String decompress(byte[] data) throws Exception {
		return null;
	}

	/**
	 * 消息的长度  回头在写写
	 * 
	 * @return the msgLength
	 */
	public int getMsgLength() {
		StringBuffer sb = new StringBuffer();
//		sb.append(version).append(msgType);
		
		if (msgContent != null) {
			sb.append(msgContent);
		}
//		if (userAccount != null) {
//			sb.append(userAccount);
//		}
//		if (osUnityAccount != null) {
//			sb.append(osUnityAccount);
//		}
//		if (msgFileName != null) {
//			sb.append(msgFileName);
//		}
//		if (msgDateCreated != null) {
//			sb.append(msgDateCreated);
//		}
//		if (msgFileUrl != null) {
//			sb.append(msgFileUrl);
//		}
		return sb.toString().length();
	}

	/**
	 * @return the msgCompressLen
	 */
	public int getMsgCompressLen() {
		return msgCompressLen;
	}

	/**
	 * @param msgCompressLen
	 *            the msgCompressLen to set
	 */
	public void setMsgCompressLen(int msgCompressLen) {
		this.msgCompressLen = msgCompressLen;
	}

	public int getCCSType() {
		return CCSType;
	}

	public void setCCSType(int cCSType) {
		CCSType = cCSType;
	}

	/**
	 * @return the version
	 */
	public long getVersion() {
		return version;
	}

	/**
	 * @param version
	 *            the version to set
	 */
	public void setVersion(long version) {
		this.version = version;
	}

	/**
	 * @return the msgId
	 */
	public String getMsgId() {
		return msgId;
	}

	/**
	 * @param msgId
	 *            the msgId to set
	 */
	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}

	/**
	 * @return the msgType
	 */
	public int getMsgType() {
		return msgType;
	}

	/**
	 * @param msgType
	 *            the msgType to set
	 */
	public void setMsgType(int msgType) {
		this.msgType = msgType;
	}

	/**
	 * @return the msgContent
	 */
	public com.google.protobuf.ByteString handleMsgContent() {
		try {
			if (this.msgContent != null) {
				return com.google.protobuf.ByteString.copyFrom(Base64.decode(msgContent));
			}
		} catch (Exception e) {
		}
		return null;
	}

	/**
	 * @param msgContent
	 *            the msgContent to set
	 */
	public void setMsgContent(String msgContent) {
		this.msgContent = msgContent;
	}

	/**
	 * @return the msgContent
	 */
	public String getMsgContent() {
		return msgContent;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	/**
	 * @return the msgFileName
	 */
	public String getMsgFileName() {
		return msgFileName;
	}

	/**
	 * @param msgFileName
	 *            the msgFileName to set
	 */
	public void setMsgFileName(String msgFileName) {
		this.msgFileName = msgFileName;
	}

	/**
	 * @return the expired
	 */
	public long getExpired() {
		return expired;
	}

	/**
	 * @param expired
	 *            the expired to set
	 */
	public void setExpired(long expired) {
		this.expired = expired;
	}

	public int getIsMCM() {
		return isMCM;
	}

	public void setIsMCM(int isMCM) {
		this.isMCM = isMCM;
	}

	public String getOsUnityAccount() {
		return osUnityAccount;
	}

	public void setOsUnityAccount(String osUnityAccount) {
		this.osUnityAccount = osUnityAccount;
	}

	public String getUserAccount() {
		return userAccount;
	}

	public void setUserAccount(String userAccount) {
		this.userAccount = userAccount;
	}

	public int getMCMEvent() {
		return MCMEvent;
	}

	public void setMCMEvent(int mCMEvent) {
		MCMEvent = mCMEvent;
	}

	public String getMsgDateCreated() {
		return msgDateCreated;
	}

	public void setMsgDateCreated(String msgDateCreated) {
		this.msgDateCreated = msgDateCreated;
	}

	

	public int getChanType() {
		return chanType;
	}

	public void setChanType(int chanType) {
		this.chanType = chanType;
	}

	public String getAgentAccount() {
		return agentAccount;
	}

	public void setAgentAccount(String agentAccount) {
		this.agentAccount = agentAccount;
	}

	public String getAgentId() {
		return agentId;
	}

	public void setAgentId(String agentId) {
		this.agentId = agentId;
	}

	public String getTransAgentId() {
		return transAgentId;
	}

	public void setTransAgentId(String transAgentId) {
		this.transAgentId = transAgentId;
	}

	public String getUserPhone() {
		return userPhone;
	}

	public void setUserPhone(String userPhone) {
		this.userPhone = userPhone;
	}

	public String getNotifyDes() {
		return notifyDes;
	}

	public void setNotifyDes(String notifyDes) {
		this.notifyDes = notifyDes;
	}

	public String getUserRespRet() {
		return userRespRet;
	}

	public void setUserRespRet(String userRespRet) {
		this.userRespRet = userRespRet;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public int getImState() {
		return imState;
	}

	public void setImState(int imState) {
		this.imState = imState;
	}

	public int getTelState() {
		return telState;
	}

	public void setTelState(int telState) {
		this.telState = telState;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getPushVoipacc() {
		return pushVoipacc;
	}

	public void setPushVoipacc(String pushVoipacc) {
		this.pushVoipacc = pushVoipacc;
	}

	public String getTelQueueType() {
		return telQueueType;
	}

	public void setTelQueueType(String telQueueType) {
		this.telQueueType = telQueueType;
	}

	public String getImQueueType() {
		return imQueueType;
	}

	public void setImQueueType(String imQueueType) {
		this.imQueueType = imQueueType;
	}

	public String getUserInfoCallbackurl() {
		return userInfoCallbackurl;
	}

	public void setUserInfoCallbackurl(String userInfoCallbackurl) {
		this.userInfoCallbackurl = userInfoCallbackurl;
	}

	public int getDelayCall() {
		return delayCall;
	}

	public void setDelayCall(int delayCall) {
		this.delayCall = delayCall;
	}

	public int getAnswerTimeout() {
		return answerTimeout;
	}

	public void setAnswerTimeout(int answerTimeout) {
		this.answerTimeout = answerTimeout;
	}

	public int getOptResult() {
		return optResult;
	}

	public void setOptResult(int optResult) {
		this.optResult = optResult;
	}

	public String getOptRetDes() {
		return optRetDes;
	}

	public void setOptRetDes(String optRetDes) {
		this.optRetDes = optRetDes;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getMsgFileUrl() {
		return msgFileUrl;
	}

	public void setMsgFileUrl(String msgFileUrl) {
		this.msgFileUrl = msgFileUrl;
	}

	public String getMailTitle() {
		return mailTitle;
	}

	public void setMailTitle(String mailTitle) {
		this.mailTitle = mailTitle;
	}
	
	
	
	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String toAsString(String customAppId) {
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
		builder.append("<msgSid>"+msgId+"</msgSid>");
		builder.append("<createTime>"+msgDateCreated+"</createTime>");
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
			builder.append("<content><![CDATA["+msgContent+"]]></content>");
			//多媒体消息   2：语音消息 3：视频消息  4：图片  6：文件
			if(msgType==MCMTypeInner.MCMType_audio_VALUE || msgType==MCMTypeInner.MCMType_video_VALUE
					|| msgType==MCMTypeInner.MCMType_emotion_VALUE || msgType==MCMTypeInner.MCMType_file_VALUE)
			{
				builder.append("<fileDownUrl><![CDATA["+msgFileUrl+"]]></fileDownUrl>");
				builder.append("<fileName><![CDATA["+msgFileName+"]]></fileName>");
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

	public List<MCMMsgDataInfo> getMutiMsg() {
		return mutiMsg;
	}

	public void setMutiMsg(List<MCMMsgDataInfo> mutiMsg) {
		this.mutiMsg = mutiMsg;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	
	public String toJsonForMessageNotify(){
		StringBuffer strBuffer = new StringBuffer();
		strBuffer.append("{");
		strBuffer.append("\"userAccount\":\"");
		strBuffer.append(userAccount==null?"":userAccount);
		strBuffer.append("\",\"osUnityAccount\":\"");
		strBuffer.append(osUnityAccount==null?"":osUnityAccount);
		strBuffer.append("\",\"msgSid\":\"");
		strBuffer.append(msgId==null?"":msgId);
		strBuffer.append("\",\"appId\":\"");
		strBuffer.append(appId==null?"":appId);
		strBuffer.append("\",\"createTime\":\"");
		strBuffer.append(msgDateCreated==null?"":msgDateCreated);
		strBuffer.append("\",\"channelType\":\"");
		strBuffer.append(chanType);
		strBuffer.append("\",\"msgType\":\"");
		strBuffer.append(msgType);
		strBuffer.append("\",\"content\":\"");
		strBuffer.append(msgContent==null?"":msgContent);
		strBuffer.append("\",\"fileDownUrl\":\"");
		strBuffer.append(msgFileUrl==null?"":msgFileUrl);
		strBuffer.append("\",\"fileName\":\"");
		strBuffer.append(msgFileName==null?"":msgFileName);
		strBuffer.append("\",\"longitude\":\"");
		strBuffer.append(longitude==null?"":longitude);
		strBuffer.append("\",\"latitude\":\"");
		strBuffer.append(latitude==null?"":latitude);
		strBuffer.append("\",\"MCMEvent\":");
		strBuffer.append(MCMEvent);
		strBuffer.append("}");
		return strBuffer.toString();
	}
	
	public String toJsonForMailMessageNotify(){
		StringBuffer strBuffer = new StringBuffer();
		strBuffer.append("{");
		strBuffer.append("\"userAccount\":\"");
		strBuffer.append(userAccount==null?"":userAccount);
		strBuffer.append("\",\"osUnityAccount\":\"");
		strBuffer.append(osUnityAccount==null?"":osUnityAccount);
		strBuffer.append("\",\"msgSid\":\"");
		strBuffer.append(msgId==null?"":msgId);
		strBuffer.append("\",\"appId\":\"");
		strBuffer.append(appId==null?"":appId);
		strBuffer.append("\",\"createTime\":\"");
		strBuffer.append(msgDateCreated==null?"":msgDateCreated);
		strBuffer.append("\",\"channelType\":\"");
		strBuffer.append(chanType);
		strBuffer.append("\",\"msgType\":\"");
		strBuffer.append(msgType);
		strBuffer.append("\",\"content\":\"");
		strBuffer.append(msgContent==null?"":msgContent);
		strBuffer.append("\",\"fileDownUrl\":\"");
		strBuffer.append(msgFileUrl==null?"":msgFileUrl);
		strBuffer.append("\",\"fileName\":\"");
		strBuffer.append(msgFileName==null?"":msgFileName);
		strBuffer.append("\",\"longitude\":\"");
		strBuffer.append(longitude==null?"":longitude);
		strBuffer.append("\",\"latitude\":\"");
		strBuffer.append(latitude==null?"":latitude);
		strBuffer.append("\",\"MCMEvent\":");
		strBuffer.append(MCMEvent);
		strBuffer.append(",\"mutiMsg\":");
		strBuffer.append("[");
		if(mutiMsg!=null&&mutiMsg.size()>0){
			for(MCMMsgDataInfo dataInfo:mutiMsg){
				strBuffer.append("{");
				strBuffer.append("\"msgType\":");
				strBuffer.append(dataInfo.getMsgType());
				strBuffer.append(",\"msgContent\":\"");
				strBuffer.append(dataInfo.getMsgContent());
				strBuffer.append("\",\"msgFileUrl\":\"");
				strBuffer.append(dataInfo.getMsgFileUrl());
				strBuffer.append("\",\"msgFileName\":\"");
				strBuffer.append(dataInfo.getMsgFileName());
				strBuffer.append("\"},");
			}
			strBuffer.substring(0, strBuffer.length()-1);
		}
		strBuffer.append("]");
		
		strBuffer.append("}");
		return strBuffer.toString();
	}
	
	public String toJsonForEventSendIMMsg(){
		StringBuffer strBuffer = new StringBuffer();
		strBuffer.append("{");
		strBuffer.append("\"userAccount\":\"");
		strBuffer.append(userAccount==null?"":userAccount);
		strBuffer.append("\",\"osUnityAccount\":\"");
		strBuffer.append(osUnityAccount==null?"":osUnityAccount);
		strBuffer.append("\",\"msgSid\":\"");
		strBuffer.append(msgId==null?"":msgId);
		strBuffer.append("\",\"createTime\":");
		strBuffer.append(msgDateCreated==null?"":msgDateCreated);
		strBuffer.append(",\"appId\":\"");
		strBuffer.append(appId==null?"":appId);
		strBuffer.append("\",\"channelType\":\"");
		strBuffer.append(chanType);
		strBuffer.append("\",\"msgType\":\"");
		strBuffer.append(msgType);
		strBuffer.append("\",\"content\":\"");
		strBuffer.append(msgContent==null?"":msgContent);
		strBuffer.append("\",\"fileDownUrl\":\"");
		strBuffer.append(msgFileUrl==null?"":msgFileUrl);
		strBuffer.append("\",\"fileName\":\"");
		strBuffer.append(msgFileName==null?"":msgFileName);
		strBuffer.append("\",\"MCMEvent\":");
		strBuffer.append(MCMEvent);
		strBuffer.append("}");
		return strBuffer.toString();
	}	
	
	public String toJsonForEventSendMailMsg(){
		StringBuffer strBuffer = new StringBuffer();
		strBuffer.append("{");
		strBuffer.append("\"userAccount\":\"");
		strBuffer.append(userAccount==null?"":userAccount);
		strBuffer.append("\",\"osUnityAccount\":\"");
		strBuffer.append(osUnityAccount==null?"":osUnityAccount);
		strBuffer.append("\",\"msgSid\":\"");
		strBuffer.append(msgId==null?"":msgId);
		strBuffer.append("\",\"createTime\":");
		strBuffer.append(msgDateCreated==null?"":msgDateCreated);
		strBuffer.append(",\"appId\":\"");
		strBuffer.append(appId==null?"":appId);
		strBuffer.append("\",\"channelType\":\"");
		strBuffer.append(chanType);
		strBuffer.append("\",\"MCMEvent\":");
		strBuffer.append(MCMEvent);
		strBuffer.append(",\"mutiMsg\":[");
		MCMMsgDataInfo mutiMsgFile = null;
		for(int i=0;i<mutiMsg.size();i++){
			mutiMsgFile = mutiMsg.get(i);
			strBuffer.append("{");
			strBuffer.append("\"msgType\":\"");
			strBuffer.append(mutiMsgFile.getMsgType());
			strBuffer.append("\",\"content\":\"");
			strBuffer.append(mutiMsgFile.getMsgContent()==null?"":mutiMsgFile.getMsgContent());
			strBuffer.append("\",\"fileDownUrl\":\"");
			strBuffer.append(mutiMsgFile.getMsgFileUrl()==null?"":mutiMsgFile.getMsgFileUrl());
			strBuffer.append("\",\"fileName\":\"");
			strBuffer.append(mutiMsgFile.getMsgFileName()==null?"":mutiMsgFile.getMsgFileName());
			if(i==mutiMsg.size()-1){
				strBuffer.append("\"}");
			}else{
				strBuffer.append("\"},");
			}
		}
		strBuffer.append("]}");
		return strBuffer.toString();
	}

	public String getMsgJsonData() {
		return msgJsonData;
	}

	public void setMsgJsonData(String msgJsonData) {
		this.msgJsonData = msgJsonData;
	}

	public String getCcpCustomData() {
		return ccpCustomData;
	}

	public void setCcpCustomData(String ccpCustomData) {
		this.ccpCustomData = ccpCustomData;
	}

	/*public int getQueuecount() {
		return queuecount;
	}

	public void setQueuecount(int queuecount) {
		this.queuecount = queuecount;
	}*/

	public int getIdlecount() {
		return idlecount;
	}

	public void setIdlecount(int idlecount) {
		this.idlecount = idlecount;
	}
	
}
