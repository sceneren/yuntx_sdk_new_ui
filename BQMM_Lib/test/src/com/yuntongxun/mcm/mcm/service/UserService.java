package com.yuntongxun.mcm.mcm.service;

import com.yuntongxun.mcm.core.exception.CCPServiceException;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMDataInner;
import com.yuntongxun.mcm.mcm.model.UserAndAgentDialog;
import com.yuntongxun.mcm.model.AppAttrs;
import com.yuntongxun.mcm.model.Connector;

public interface UserService {

	/**
	 * @Description: 用户开始咨询
	 * @param sendMsg
	 * @param connector
	 * @param protoClientNo 
	 * @param appAttrs
	 * @throws CCPServiceException
	 */
	void startAsk(MCMDataInner sendMsg, Connector connector, int protoClientNo, 
			AppAttrs appAttrs) throws CCPServiceException;

	/**
	 * @Description: 用户停止咨询
	 * @param sendMsg
	 * @param connector
	 * @param protoClientNo
	 * @throws CCPServiceException
	 */
	void endAsk(MCMDataInner sendMsg, Connector connector, int protoClientNo, 
			AppAttrs appAttrs) throws CCPServiceException;

	/**
	 * @Description: 用户发送消息
	 * @param sendMsg
	 * @param connector
	 * @param protoClientNo
	 * @throws CCPServiceException
	 */
	void sendMSG(MCMDataInner sendMsg, Connector connector, int protoClientNo) 
			throws CCPServiceException;

	/**
	 * @Description: 用户获取客服分组列表
	 * @param sendMsg
	 * @param connector
	 * @param protoClientNo
	 * @throws CCPServiceException
	 */
	void getAGList(MCMDataInner sendMsg, Connector connector, int protoClientNo) 
			throws CCPServiceException;

	/**
	 * @Description: 用户平台交互控制命令与通知
	 * @param sendMsg
	 * @param connector
	 * @param protoClientNo
	 * @throws CCPServiceException
	 */
	void ircn(MCMDataInner sendMsg, Connector connector, int protoClientNo) 
			throws CCPServiceException;
	
	/**
	 * @Description: 用户断线通知
	 * @param connector
	 * @param appAttrs
	 * @throws CCPServiceException
	 */
	void userDisconnect(Connector connector, AppAttrs appAttrs, String userAcc, 
			UserAndAgentDialog userAndAgentDialog) throws CCPServiceException;

	void submitInvestigate(MCMDataInner sendMsg, Connector connector, int protoClientNo);
	
}
