package com.yuntongxun.mcm.mcm.service;

import com.yuntongxun.mcm.core.exception.CCPServiceException;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMDataInner;
import com.yuntongxun.mcm.mcm.model.UserAndAgentDialog;
import com.yuntongxun.mcm.model.AppAttrs;
import com.yuntongxun.mcm.model.Connector;
import com.yuntongxun.mcm.model.McmWeiXinMsgInfo;

public interface IMUserService {

	/**
	 * @Description: 用户开始咨询
	 * @param sendMsg
	 * @param connector
	 * @param userAcc
	 * @param protoClientNo
	 * @throws CCPServiceException
	 */
	void imStartAsk(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException;

	/**
	 * @Description: 用户停止咨询
	 * @param sendMsg
	 * @param connector
	 * @param userAcc
	 * @param protoClientNo
	 * @throws CCPServiceException
	 */
	void endAsk(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException;

	/**
	 * @Description: 用户发送消息
	 * @param sendMsg
	 * @param connector
	 * @param userAcc
	 * @param protoClientNo
	 * @throws CCPServiceException
	 */
	void sendMSG(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException;

	/**
	 * @Description: 用户获取客服分组列表
	 * @param sendMsg
	 * @param connector
	 * @param userAcc
	 * @param protoClientNo
	 * @throws CCPServiceException
	 */
	void getAGList(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException;

	/**
	 * @Description: 用户平台交互控制命令与通知
	 * @param sendMsg
	 * @param connector
	 * @param userAcc
	 * @param protoClientNo
	 * @throws CCPServiceException
	 */
	void ircn(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException;

	/**
	 * @Description: 微信用户开始咨询
	 * @param weixinMsg
	 * @param appAttrs
	 * @throws CCPServiceException 
	 */
	void weixinStartAsk(McmWeiXinMsgInfo weixinMsg, AppAttrs appAttrs) throws CCPServiceException;

	/**
	 * @Description: 向IM发送微信推送过来的消息
	 * @param weixinMsg
	 * @param appAttrs
	 * @throws CCPServiceException 
	 */
	void sendWeixinMSG(McmWeiXinMsgInfo weixinMsg, AppAttrs appAttrs) throws CCPServiceException;
	
	/**
	 * @Description: 用户断线通知
	 * @param sendMsg
	 * @param connector
	 * @throws CCPServiceException
	 */
	void userDisconnect(Connector connector, String userAcc, UserAndAgentDialog userAndAgentDialog) 
			throws CCPServiceException;
	
}
