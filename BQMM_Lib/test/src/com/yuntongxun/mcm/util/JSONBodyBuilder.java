package com.yuntongxun.mcm.util;

import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMChannelTypeInner;
import com.yuntongxun.mcm.model.AppAttrs;
import com.yuntongxun.mcm.model.McmWeiXinMsgInfo;
import com.yuntongxun.mcm.model.mail.McmMailAttchmentInfo;
import com.yuntongxun.mcm.model.mail.McmMailMsgInfo;


public class JSONBodyBuilder {

	/**
	 * 向第三方推送json格式的邮件消息体
	 * @param mailMsg
	 * @param appAttrs
	 * @param msgId
	 * @return
	 */
	public static String buildMailJSONBody(McmMailMsgInfo mailMsg, AppAttrs appAttrs, String msgId) {
		StringBuilder builder = new StringBuilder();
		builder.append("{");
		builder.append("\"userAccount\":\"");
		builder.append(mailMsg.getUserAccount());
		builder.append("\",\"osUnityAccount\":\"");
		builder.append(mailMsg.getOsUnityAccount());
		builder.append("\",\"appId\":\"");
		builder.append(mailMsg.getAppId());
		builder.append("\",\"customAppId\":\"");
		builder.append(appAttrs.getCustomer_appid());
		builder.append("\",\"msgSid\":\"");
		builder.append(msgId);
		builder.append("\",\"msgContent\":\"");
		builder.append(mailMsg.getMsgContent());
		builder.append("\",\"createTime\":\"");
		builder.append(System.currentTimeMillis());
		builder.append("\",\"channelType\":\"");
		builder.append(MCMChannelTypeInner.MCType_mail_VALUE);
		builder.append("\",\"msgType\":\"");
		builder.append(mailMsg.getMsgType());
		builder.append("\",\"attachment\":[");
		if(mailMsg.getAttachment()!=null&&mailMsg.getAttachment().size()>0){
			for(McmMailAttchmentInfo attchmentInfo:mailMsg.getAttachment()){
				builder.append("{");
				builder.append("\"fileName\":\"");
				builder.append(attchmentInfo.getFileName());
				builder.append("\",\"url\":\"");
				builder.append(attchmentInfo.getUrl());
				builder.append("\"},");
			}
			builder.substring(0, builder.length()-1);
		}
		builder.append("]");
		builder.append("}");
		
		return builder.toString();
	}
	
	/**
	 * 向第三方推送json格式的微信消息体
	 * @param mailMsg
	 * @param appAttrs
	 * @param msgId
	 * @return
	 */
	public static String buildWeiXinJSONBody(McmWeiXinMsgInfo weiXinMsg, AppAttrs appAttrs, String msgId) {
		StringBuilder builder = new StringBuilder();
		builder.append("{");
		builder.append("\"userAccount\":\"");
		builder.append(weiXinMsg.getUserID());
		builder.append("\",\"osUnityAccount\":\"");
		builder.append(weiXinMsg.getOpenID());
		builder.append("\",\"appId\":\"");
		builder.append(weiXinMsg.getAppId());
		builder.append("\",\"customAppId\":\"");
		builder.append(appAttrs.getCustomer_appid());
		builder.append("\",\"msgSid\":\"");
		builder.append(msgId);
		builder.append("\",\"msgContent\":\"");
		builder.append(weiXinMsg.getContent());
		builder.append("\",\"createTime\":\"");
		builder.append(weiXinMsg.getCreateTime());
		builder.append("\",\"channelType\":\"");
		builder.append(MCMChannelTypeInner.MCType_wx_VALUE);
		builder.append("\",\"msgType\":\"");
		builder.append(weiXinMsg.getMsgType());
		builder.append("\",\"fileName\":\"");
		builder.append(weiXinMsg.getFileName());
		builder.append("\",\"url\":\"");
		builder.append(weiXinMsg.getContent());
		builder.append("\"}");
		
		return builder.toString();
	}
}
