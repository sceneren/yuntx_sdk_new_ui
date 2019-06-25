package com.yuntongxun.mcm.mcm.model;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ming.sample.util.Base64;

import com.yuntongxun.mcm.core.AbstractDataModel;
import com.yuntongxun.mcm.core.protobuf.SendMsg.SendMsgInner;

public class MessageInfo extends AbstractDataModel {
	
	public static final Logger logger = LogManager.getLogger(MessageInfo.class);

	public static final int MSG_MAX_WORDS = 2048;

	public static final int MSG_TEXT = 1;
	public static final int MSG_VOICE = 2;
	public static final int MSG_PIC = 4;
	public static final int MSG_NOTIFY = 7;
	public static final int MSG_CMD = 9;

	private String localFileName;
	private long version = -1; // 版本号
	private String msgId;// 服务器端的msgId
	private int msgType = 0;// 消息类型 1:文本消息 2：语音消息 3：视频消息 4：表情消息 5：位置消息 6：文件 20 协同消息 21 发送即焚消息通知 22 群组开关灯消息
	private String msgContent; // 消息内容 小于50字符在线推送，否则客户端拉取
	
	private String msgSender; // 发送者
	private String msgReceiver; // 接收者
	private String msgDomain; // 需要扩展的消息字段放在这里
	private String msgFileName;// 用户指定的文件名
	private String dateCreated;// 消息到达服务器时间
	
	private String fileUrl;
	private int msgCompressLen;// 消息内容源长度
	private long expired = 604800000; // 消息过期时间(ms)
	private int mcmEvent; // 是否是多渠道离线消息 1:多渠道消息 else:原消息
	private int msgLength; // 消息长度
	
	private String msgFileSize; // 文件大小(字节)
	private String senderDeviceNo;
	private int syncMsgFlag; //同步消息标记，1:同步消息，2：非同步消息
	private String extOpts;
	
	public MessageInfo() {
		super();
	}
	
	public MessageInfo(Object obj) {
		try {
			BeanUtils.copyProperties(this, obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (obj instanceof SendMsgInner) {
			com.google.protobuf.ByteString byteString = ((SendMsgInner) obj).getMsgContent();
			if (byteString == null) {
				return;
			}
			if (byteString.size() > MSG_MAX_WORDS) {
				logger.info("Compress's message content length over [" + MSG_MAX_WORDS + "], client should be cut...");
			}
			// Client used JZlib 压缩
			msgContent = Base64.encode(byteString.toByteArray());
		} else{

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
	 * 消息的长度
	 * 
	 * @return the msgLength
	 */
	public int getMsgLength() {
		StringBuffer sb = new StringBuffer();
		sb.append(version).append(msgType);
		if (msgContent != null) {
			sb.append(msgContent);
		}
		if (msgSender != null) {
			sb.append(msgSender);
		}
		if (msgReceiver != null) {
			sb.append(msgReceiver);
		}
		if (msgDomain != null) {
			sb.append(msgDomain);
		}
		if (msgFileName != null) {
			sb.append(msgFileName);
		}
		if (dateCreated != null) {
			sb.append(dateCreated);
		}
		if (fileUrl != null) {
			sb.append(fileUrl);
		}
		msgLength = sb.toString().length();
		return msgLength;
	}

	public int getMsgCompressLen() {
		return msgCompressLen;
	}

	public void setMsgCompressLen(int msgCompressLen) {
		this.msgCompressLen = msgCompressLen;
	}

	public String getFileUrl() {
		return fileUrl;
	}

	public void setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	public String getMsgId() {
		return msgId;
	}

	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}

	public int getMsgType() {
		return msgType;
	}

	public void setMsgType(int msgType) {
		this.msgType = msgType;
	}

	public com.google.protobuf.ByteString handleMsgContent() {
		try {
			if (this.msgContent != null) {
				return com.google.protobuf.ByteString.copyFrom(Base64.decode(msgContent));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void setMsgContent(String msgContent) {
		this.msgContent = msgContent;
	}

	public String getMsgContent() {
		return msgContent;
	}

	public String getMsgSender() {
		return msgSender;
	}

	public void setMsgSender(String msgSender) {
		this.msgSender = msgSender;
	}

	public String getMsgReceiver() {
		return msgReceiver;
	}

	public void setMsgReceiver(String msgReceiver) {
		this.msgReceiver = msgReceiver;
	}

	public String getMsgDomain() {
		return msgDomain;
	}

	public void setMsgDomain(String msgDomain) {
		this.msgDomain = msgDomain;
	}

	public String getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(String dateCreated) {
		this.dateCreated = dateCreated;
	}

	public String getMsgFileName() {
		return msgFileName;
	}

	public void setMsgFileName(String msgFileName) {
		this.msgFileName = msgFileName;
	}

	public long getExpired() {
		return expired;
	}

	public void setExpired(long expired) {
		this.expired = expired;
	}

	public int getMcmEvent() {
		return mcmEvent;
	}

	public void setMcmEvent(int mcmEvent) {
		this.mcmEvent = mcmEvent;
	}

	public String getLocalFileName() {
		return localFileName;
	}

	public void setLocalFileName(String localFileName) {
		this.localFileName = localFileName;
	}

	public String getMsgFileSize() {
		return msgFileSize;
	}

	public void setMsgFileSize(String msgFileSize) {
		this.msgFileSize = msgFileSize;
	}

	public String getSenderDeviceNo() {
		return senderDeviceNo;
	}

	public void setSenderDeviceNo(String senderDeviceNo) {
		this.senderDeviceNo = senderDeviceNo;
	}

	public int getSyncMsgFlag() {
		return syncMsgFlag;
	}

	public void setSyncMsgFlag(int syncMsgFlag) {
		this.syncMsgFlag = syncMsgFlag;
	}

	public void setMsgLength(int msgLength) {
		this.msgLength = msgLength;
	}

	public String getExtOpts() {
		return extOpts;
	}

	public void setExtOpts(String extOpts) {
		this.extOpts = extOpts;
	}
	
}
