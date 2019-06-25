package com.yuntongxun.mcm.mcm.service;

import com.yuntongxun.mcm.core.exception.CCPServiceException;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMDataInner;
import com.yuntongxun.mcm.model.Connector;

public interface AgentService {

	/**
	 * @Description: 客服(坐席)签入(上班)
	 * @param sendMsg
	 * @param connector
	 * @param protoClientNo
	 * @throws CCPServiceException
	 */
	void onWork(MCMDataInner sendMsg, Connector connector, int protoClientNo) 
			throws CCPServiceException;

	/**
	 * @Description: 客服(坐席)签出(下班)
	 * @param sendMsg
	 * @param connector
	 * @param protoClientNo
	 * @throws CCPServiceException
	 */
	void offWork(MCMDataInner sendMsg, Connector connector, int protoClientNo) 
			throws CCPServiceException;

	/**
	 * @Description: 客服(坐席)就绪
	 * @param sendMsg
	 * @param connector
	 * @param protoClientNo
	 * @throws CCPServiceException
	 */
	void ready(MCMDataInner sendMsg, Connector connector, int protoClientNo) 
			throws CCPServiceException;

	/**
	 * @Description: 客服(坐席)未就绪
	 * @param sendMsg
	 * @param connector
	 * @param protoClientNo
	 * @throws CCPServiceException
	 */
	void notReady(MCMDataInner sendMsg, Connector connector, int protoClientNo) 
			throws CCPServiceException;

	/**
	 * @Description: 客服(坐席)开始为用户服务
	 * @param sendMsg
	 * @param connector
	 * @param protoClientNo
	 * @throws CCPServiceException
	 */
	void startSerWithUser(MCMDataInner sendMsg, Connector connector, int protoClientNo) 
			throws CCPServiceException;

	/**
	 * @Description: 客服(坐席)停止为用户服务
	 * @param sendMsg
	 * @param connector
	 * @param protoClientNo
	 * @throws CCPServiceException
	 */
	void stopSerWithUser(MCMDataInner sendMsg, Connector connector, int protoClientNo) 
			throws CCPServiceException;

	/**
	 * @Description: 客服(坐席)拒绝为用户服务
	 * @param sendMsg
	 * @param connector
	 * @param protoClientNo
	 * @throws CCPServiceException
	 */
	void rejectUser(MCMDataInner sendMsg, Connector connector, int protoClientNo) 
			throws CCPServiceException;
	
	/**
	 * @Description: 客服(坐席)发送普通消息
	 * @param sendMsg
	 * @param connector
	 * @param protoClientNo
	 * @throws CCPServiceException
	 */
	void sendMCM(MCMDataInner sendMsg, Connector connector, int protoClientNo) 
			throws CCPServiceException;

	/**
	 * @Description: 客服(坐席)发送通知消息
	 * @param sendMsg
	 * @param connector
	 * @param protoClientNo
	 * @throws CCPServiceException
	 */
	void sendNotify(MCMDataInner sendMsg, Connector connector, int protoClientNo) 
			throws CCPServiceException;

	/**
	 * @Description: 客服(坐席)发送请求外呼
	 * @param sendMsg
	 * @param connector
	 * @param protoClientNo
	 * @throws CCPServiceException
	 */
	void makeCall(MCMDataInner sendMsg, Connector connector, int protoClientNo) 
			throws CCPServiceException;

	/**
	 * @Description: 客服(坐席)发送请求应答
	 * @param sendMsg
	 * @param connector
	 * @param protoClientNo
	 * @throws CCPServiceException
	 */
	void answerCall(MCMDataInner sendMsg, Connector connector, int protoClientNo) 
			throws CCPServiceException;

	/**
	 * @Description: 客服(坐席)发送请求挂机
	 * @param sendMsg
	 * @param connector
	 * @param protoClientNo
	 * @throws CCPServiceException
	 */
	void releaseCall(MCMDataInner sendMsg, Connector connector, int protoClientNo) 
			throws CCPServiceException;

	/**
	 * @Description: 客服(坐席)开始会议
	 * @param sendMsg
	 * @param connector
	 * @param protoClientNo
	 * @throws CCPServiceException
	 */
	void startConf(MCMDataInner sendMsg, Connector connector, int protoClientNo) 
			throws CCPServiceException;
	
	/**
	 * @Description: 客服(坐席)加入会议
	 * @param sendMsg
	 * @param connector 
	 * @param protoClientNo
	 * @throws CCPServiceException
	 */
	void joinConf(MCMDataInner sendMsg, Connector connector, int protoClientNo) 
			throws CCPServiceException;

	/**
	 * @Description: 客服(坐席)退出会议
	 * @param sendMsg
	 * @param connector 
	 * @param protoClientNo
	 * @throws CCPServiceException
	 */
	void exitConf(MCMDataInner sendMsg, Connector connector, int protoClientNo) 
			throws CCPServiceException;
	
	/**
	 * @Description: 客服(坐席)状态操作
	 * @param sendMsg
	 * @param connector
	 * @param protoClientNo
	 * @throws CCPServiceException
	 */
	void stateOpt(MCMDataInner sendMsg, Connector connector, int protoClientNo) 
			throws CCPServiceException;

	/**
	 * @Description: 客服(坐席)将用户转接到其他客服
	 * @param sendMsg
	 * @param connector
	 * @param protoClientNo
	 * @throws CCPServiceException
	 */
	void transKF(MCMDataInner sendMsg, Connector connector, int protoClientNo) 
			throws CCPServiceException;

	/**
	 * @Description: 客服(坐席)转入电话服务
	 * @param sendMsg
	 * @param connector
	 * @param protoClientNo
	 * @throws CCPServiceException
	 */
	void enterCallService(MCMDataInner sendMsg, Connector connector, int protoClientNo) 
			throws CCPServiceException;

	/**
	 * @Description: 客服(坐席)转接队列
	 * @param sendMsg
	 * @param connector
	 * @param protoClientNo
	 * @throws CCPServiceException
	 */
	void transferQueue(MCMDataInner sendMsg, Connector connector, int protoClientNo) 
			throws CCPServiceException;
	
	/**
	 * @Description: 客服(坐席)强插
	 * @param sendMsg
	 * @param connector
	 * @param protoClientNo
	 * @throws CCPServiceException
	 */
	void forceJoinConf(MCMDataInner sendMsg, Connector connector, int protoClientNo) 
			throws CCPServiceException;
	
	/**
	 * @Description: 客服(坐席)强拆
	 * @param sendMsg
	 * @param connector
	 * @param protoClientNo
	 * @throws CCPServiceException
	 */
	void forceTransfer(MCMDataInner sendMsg, Connector connector, int protoClientNo) 
			throws CCPServiceException;

	/**
	 * @Description: 班长强制结束一个座席与用户的会话
	 * @param sendMsg
	 * @param connector
	 * @param protoClientNo
	 * @throws CCPServiceException 
	 */
	void forceEndService(MCMDataInner sendMsg, Connector connector, int protoClientNo) 
			throws CCPServiceException;

	/**
	 * @Description: 启动定时服务 
	 * @param sendMsg
	 * @param connector
	 * @param protoClientNo
	 * @throws CCPServiceException
	 */
	void startSessionTimer(MCMDataInner sendMsg, Connector connector, int protoClientNo) 
			throws CCPServiceException;

	/**
	 * @Description: 管理员监听座席
	 * @param sendMsg
	 * @param connector
	 * @param protoClientNo
	 * @throws CCPServiceException 
	 */
	void monitorAgent(MCMDataInner sendMsg, Connector connector, int protoClientNo) 
			throws CCPServiceException;
	
	/**
	 * @Description: 管理者取消监听座席
	 * @param sendMsg
	 * @param connector
	 * @param protoClientNo
	 * @throws CCPServiceException 
	 */
	void cancelMonitorAgent(MCMDataInner sendMsg, Connector connector, int protoClientNo) 
			throws CCPServiceException;

	/**
	 * @Description: 查询队列接口
	 * @param sendMsg
	 * @param connector
	 * @param protoClientNo
	 * @throws CCPServiceException 
	 */
	void queryQueueInfo(MCMDataInner sendMsg, Connector connector, int protoClientNo) 
			throws CCPServiceException;
	
	/**
	 * @Description: 查询座席信息接口
	 * @param sendMsg
	 * @param connector
	 * @param protoClientNo
	 * @throws CCPServiceException
	 */
	void queryAgentInfo(MCMDataInner sendMsg, Connector connector, int protoClientNo) 
			throws CCPServiceException;
	
	/**
	 * @Description: 座席为指定用户服务
	 * @param sendMsg
	 * @param connector
	 * @param protoClientNo
	 * @throws CCPServiceException
	 */
	void serWithTheUser(MCMDataInner sendMsg, Connector connector, int protoClientNo) 
			throws CCPServiceException;

	/**
	 * @Description: 用户预留服务
	 * @param sendMsg
	 * @param connector
	 * @param protoClientNo
	 * @throws CCPServiceException
	 */
	void reservedForUser(MCMDataInner sendMsg, Connector connector, int protoClientNo) 
			throws CCPServiceException;
	
	/**
	 * @Description: 取消用户预留服务
	 * @param sendMsg
	 * @param connector
	 * @param protoClientNo
	 * @throws CCPServiceException
	 */
	void cancelReserved(MCMDataInner sendMsg, Connector connector, int protoClientNo) 
			throws CCPServiceException;
	
}
