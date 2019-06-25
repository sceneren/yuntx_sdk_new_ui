package com.yuntongxun.mcm.genesys.service;

import com.yuntongxun.mcm.core.exception.CCPServiceException;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMDataInner;
import com.yuntongxun.mcm.model.Connector;

public interface GenesysAgentService {

	/**
	 * @Description: 坐席签入
	 * @param sendMsg
	 * @param connector
	 * @param userAcc
	 * @param protoClientNo
	 * @throws CCPServiceException
	 */
	void onWork(MCMDataInner sendMsg, Connector connector, String userAcc, int protoClientNo) throws CCPServiceException;

	/**
	 * @Description: 坐席签出
	 * @param sendMsg
	 * @param connector
	 * @param userAcc
	 * @param protoClientNo
	 * @throws CCPServiceException
	 */
	void offWork(MCMDataInner sendMsg, Connector connector, String userAcc, int protoClientNo) throws CCPServiceException;

	/**
	 * @Description: 坐席就绪
	 * @param sendMsg
	 * @param connector
	 * @param userAcc
	 * @throws CCPServiceException
	 */
	void ready(MCMDataInner sendMsg, Connector connector, String userAcc) throws CCPServiceException;

	/**
	 * @Description: 坐席未就绪
	 * @param sendMsg
	 * @param connector
	 * @param userAcc
	 * @throws CCPServiceException
	 */
	void notReady(MCMDataInner sendMsg, Connector connector, String userAcc) throws CCPServiceException;

	/**
	 * @Description: 坐席接起会话
	 * @param sendMsg
	 * @param connector
	 * @param userAcc
	 * @throws CCPServiceException
	 */
	void startSerWithUser(MCMDataInner sendMsg, Connector connector, String userAcc) throws CCPServiceException;

	/**
	 * @Description: 坐席挂断会话
	 * @param sendMsg
	 * @param connector
	 * @param userAcc
	 * @throws CCPServiceException
	 */
	void stopSerWithUser(MCMDataInner sendMsg, Connector connector, String userAcc) throws CCPServiceException;

	/**
	 * @Description: 坐席发送普通消息
	 * @param sendMsg
	 * @param connector
	 * @param userAcc
	 * @throws CCPServiceException
	 */
	void sendMCM(MCMDataInner sendMsg, Connector connector, String userAcc) throws CCPServiceException;

	/**
	 * @Description: 坐席发送通知消息
	 * @param sendMsg
	 * @param connector
	 * @param userAcc
	 * @throws CCPServiceException
	 */
	void sendNotify(MCMDataInner sendMsg, Connector connector, String userAcc) throws CCPServiceException;

	/**
	 * @Description: 坐席发送请求外呼
	 * @param sendMsg
	 * @param connector
	 * @param userAcc
	 * @throws CCPServiceException
	 */
	void makeCall(MCMDataInner sendMsg, Connector connector, String userAcc) throws CCPServiceException;

	/**
	 * @Description: 坐席发送请求应答
	 * @param sendMsg
	 * @param connector
	 * @param userAcc
	 * @throws CCPServiceException
	 */
	void answerCall(MCMDataInner sendMsg, Connector connector, String userAcc) throws CCPServiceException;

	/**
	 * @Description: 坐席发送请求挂机
	 * @param sendMsg
	 * @param connector
	 * @param userAcc
	 * @throws CCPServiceException
	 */
	void releaseCall(MCMDataInner sendMsg, Connector connector, String userAcc) throws CCPServiceException;

	/**
	 * @Description: 坐席开始请求语音会议
	 * @param sendMsg
	 * @param connector
	 * @param userAcc
	 * @throws CCPServiceException
	 */
	void startConf(MCMDataInner sendMsg, Connector connector, String userAcc) throws CCPServiceException;

}
