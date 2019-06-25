package com.yuntongxun.mcm.service;

import com.yuntongxun.mcm.core.exception.CCPServiceException;
import com.yuntongxun.mcm.core.protobuf.MsgLite.MsgLiteInner;
import com.yuntongxun.mcm.mcm.model.MCMMessageInfo;
import com.yuntongxun.mcm.mcm.model.MessageInfo;
import com.yuntongxun.mcm.mcm.model.UserAndAgentDialog;
import com.yuntongxun.mcm.model.mail.McmMailMsgInfo;
import com.yuntongxun.mcm.sevenmoor.model.SevenMoorLoginRInfo;

public interface PushService {

	/**
	 * @Description: 推送消息
	 * @param receiver
	 * @param messageInfo
	 * @param protoClientNo 流水号
	 * @param errorCode 错误码
	 * @throws CCPServiceException
	 */
	public void doPushMsg(String receiver, MCMMessageInfo mcmMessageInfo, int protoClientNo, 
			int errorCode) throws CCPServiceException;

	/**
	 * @Description: 推送消息
	 * @param receiver
	 * @param messageInfo
	 * @param protoClientNo 流水号
	 * @param errorCode 错误码
	 * @param userAndAgentDialog
	 * @throws CCPServiceException
	 */
	public void doPushMsg(String receiver, MCMMessageInfo mcmMessageInfo, int protoClientNo, 
			int errorCode, UserAndAgentDialog userAndAgentDialog, String sendUserAcc) throws CCPServiceException;
	
	/**
	 * @Description: 推送消息
	 * @param receiver
	 * @param messageInfo
	 * @throws CCPServiceException
	 */
	public void doPushMsg(String receiver, MCMMessageInfo mcmMessageInfo) 
			throws CCPServiceException;
	
	/**
	 * @Description: 推送消息
	 * @param receiver
	 * @param messageInfo
	 * @param userAndAgentDialog
	 * @throws CCPServiceException
	 */
	public void doPushMsg(String receiver, MCMMessageInfo mcmMessageInfo, UserAndAgentDialog userAndAgentDialog, 
			String sendUserAcc) throws CCPServiceException;
	
	/**
	 * @Description: 向MQ推送邮件消息
	 * @param connector
	 * @param mcmMessageInfo
	 * @throws CCPServiceException
	 */
	public void doPushMailMsg(String userAcc, McmMailMsgInfo mailMsgInfo) 
			throws CCPServiceException;

	/**
	 * @Description: 推送消息
	 * @param pushResp
	 * @param destQueueName
	 */
	public void doPushMsg(MsgLiteInner pushResp, String destQueueName);

	public void replySuccToSDK(int protoClientNo, String connectorId);

	public boolean doPushMsgFor7Moor(SevenMoorLoginRInfo sevenMoorLoginRInfo, MCMMessageInfo mcmMessageInfo, int protoClientNo,
			int errorCode) throws CCPServiceException;
	
	public boolean doPushMsgFor7Moor(String userAcc, MessageInfo messageInfo) throws CCPServiceException;
	
	public void doPushMsg2(String channelId, MCMMessageInfo mcmMessageInfo, int protoClientNo, 
			int errorCode) throws CCPServiceException;
	
}
