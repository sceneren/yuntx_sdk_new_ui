package com.yuntongxun.mcm.util;

import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMChannelTypeInner;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMTypeInner;
import com.yuntongxun.mcm.model.AppAttrs;
import com.yuntongxun.mcm.model.McmWeiXinMsgInfo;
import com.yuntongxun.mcm.model.mail.McmMailAttchmentInfo;
import com.yuntongxun.mcm.model.mail.McmMailMsgInfo;


public class XMLBodyBuilder {

	/**
	 * 向RM Server发送获取call manager 地址的请求包体
	 * @param appId
	 * @return
	 */
	public static String buildGetCMRouteBody(String appId){
		StringBuilder builder = new StringBuilder();
		builder.append("<Request>");
		builder.append("<appId>"+appId+"</appId>");
		builder.append("</Request>");
		return builder.toString();
	}

	/**
	 * 向第三方推送xml格式的邮件消息体
	 * @param mailMsg
	 * @param appAttrs
	 * @param msgId
	 * @return
	 */
	public static String buildMailXMLBody(McmMailMsgInfo mailMsg, AppAttrs appAttrs, String msgId) {
		StringBuilder builder = new StringBuilder();
		builder.append("<MCM>");
		builder.append("<userAccount><![CDATA["+mailMsg.getUserAccount()+"]]></userAccount>");
		builder.append("<osUnityAccount><![CDATA["+mailMsg.getOsUnityAccount()+"]]></osUnityAccount>");
		builder.append("<appId>"+mailMsg.getAppId()+"</appId>");
		builder.append("<customAppId><![CDATA["+appAttrs.getCustomer_appid()+"]]></customAppId>");
		builder.append("<msgSid>"+msgId+"</msgSid>");
		builder.append("<createTime>"+System.currentTimeMillis()+"</createTime>");
		builder.append("<channelType>"+MCMChannelTypeInner.MCType_mail_VALUE+"</channelType>");
		builder.append("<mutiMsg>");
		builder.append("<msg>");
		builder.append("<msgType>"+mailMsg.getMsgType()+"</msgType>");
		builder.append("<content><![CDATA["+mailMsg.getMsgContent()+"]]></content>");
		builder.append("</msg>");
		if(mailMsg.getAttachment()!=null&&mailMsg.getAttachment().size()>0){
			for(McmMailAttchmentInfo attchmentInfo:mailMsg.getAttachment()){
				builder.append("<msg>");
				builder.append("<msgType>"+MCMTypeInner.MCMType_file_VALUE+"</msgType>");
				builder.append("<fileDownUrl><![CDATA["+attchmentInfo.getUrl()+"]]></fileDownUrl>");
				builder.append("<fileName><![CDATA["+attchmentInfo.getFileName()+"]]></fileName>");
				builder.append("</msg>");
			}
		}
		builder.append("</mutiMsg>");
		builder.append("</MCM>");
		return builder.toString();
	}
	
	/**
	 * 向第三方推送xml格式的微信消息体
	 * @param mailMsg
	 * @param appAttrs
	 * @param msgId
	 * @return
	 */
	public static String buildWeiXinXMLBody(McmWeiXinMsgInfo weixinMsg, AppAttrs appAttrs, String msgId) {
		StringBuilder builder = new StringBuilder();
		builder.append("<MCM>");
		builder.append("<userAccount><![CDATA["+weixinMsg.getUserID()+"]]></userAccount>");
		builder.append("<osUnityAccount><![CDATA["+weixinMsg.getOpenID()+"]]></osUnityAccount>");
		builder.append("<appId>"+weixinMsg.getAppId()+"</appId>");
		builder.append("<customAppId><![CDATA["+appAttrs.getCustomer_appid()+"]]></customAppId>");
		builder.append("<msgSid>"+msgId+"</msgSid>");
		builder.append("<createTime>"+weixinMsg.getCreateTime()+"</createTime>");
		builder.append("<channelType>"+MCMChannelTypeInner.MCType_wx_VALUE+"</channelType>");
		builder.append("<mutiMsg>");
		builder.append("<msg>");
		builder.append("<msgType>"+weixinMsg.getMsgType()+"</msgType>");
		builder.append("<content><![CDATA["+weixinMsg.getContent()+"]]></content>");
		builder.append("<fileDownUrl><![CDATA["+weixinMsg.getUrl()+"]]></fileDownUrl>");
		builder.append("<fileName><![CDATA["+weixinMsg.getFileName()+"]]></fileName>");
		builder.append("</msg>");
		builder.append("</mutiMsg>");
		builder.append("</MCM>");
		return builder.toString();
	}

}
