package com.yuntongxun.mcm.mcm.service;

import com.yuntongxun.mcm.core.exception.CCPServiceException;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMDataInner;
import com.yuntongxun.mcm.model.Connector;

public interface IMAgentService {

	/**
	 * @Description: 客服(坐席)签入(上班)
	 * @param sendMsg
	 * @param connector
	 * @param userAcc
	 * @param protoClientNo
	 * @throws CCPServiceException
	 */
	void onWork(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException;

	/**
	 * @Description: 客服(坐席)签出(下班)
	 * @param sendMsg
	 * @param connector
	 * @param userAcc
	 * @param protoClientNo
	 * @throws CCPServiceException
	 */
	void offWork(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException;

	/**
	 * @Description: 客服(坐席)状态变化
	 * @param sendMsg
	 * @param connector
	 * @param userAcc
	 * @param protoClientNo
	 * @throws CCPServiceException
	 */
	void stateOpt(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException;
	
	/**
	 * @Description: 客服(坐席)就绪
	 * @param sendMsg
	 * @param connector
	 * @param userAcc
	 * @param protoClientNo
	 * @throws CCPServiceException
	 */
	void ready(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException;

	/**
	 * @Description: 客服(坐席)未就绪
	 * @param sendMsg
	 * @param connector
	 * @param userAcc
	 * @param protoClientNo
	 * @throws CCPServiceException
	 */
	void notReady(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException;

	/**
	 * @Description: 客服(坐席)开始为用户服务
	 * @param sendMsg
	 * @param connector
	 * @param userAcc
	 * @param protoClientNo
	 * @throws CCPServiceException
	 */
	void startSerWithUser(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException;

	/**
	 * @Description: 客服(坐席)停止为用户服务
	 * @param sendMsg
	 * @param connector
	 * @param userAcc
	 * @param protoClientNo
	 * @throws CCPServiceException
	 */
	void stopSerWithUser(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException;

	/**
	 * @Description: 客服(坐席)发送普通消息
	 * @param sendMsg
	 * @param connector
	 * @param userAcc
	 * @param protoClientNo
	 * @throws CCPServiceException
	 */
	void sendMCM(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException;

	/**
	 * @Description: 客服(坐席)将用户转接到其他客服事件
	 * @param sendMsg
	 * @param connector
	 * @param protoClientNo
	 * @throws CCPServiceException
	 */
	void transKF(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException;

	/**
	 * @Description: 客服(坐席)转入电话服务事件
	 * @param sendMsg
	 * @param connector
	 * @param userAcc
	 * @param protoClientNo
	 * @throws CCPServiceException
	 */
	void enterCallService(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException;

	/**
	 * @Description: 强制转接会话
	 * @param sendMsg
	 * @param connector
	 * @param protoClientNo
	 * @throws CCPServiceException 
	 */
	void forceTransfer(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException;
	
	/**
	 * @Description: 转接队列
	 * @param sendMsg
	 * @param connector
	 * @param protoClientNo
	 * @throws CCPServiceException 
	 */
	void transferQueue(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException;

	/**
	 * @Description: 强拆，班长强制结束座席人员和用户的会话
	 * @param sendMsg
	 * @param connector
	 * @param protoClientNo
	 * @throws CCPServiceException 
	 */
	void forceEndService(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException;

	/**
	 * @Description: 会话超时处理，启动定时器
	 * @param sendMsg
	 * @param connector
	 * @param userAcc
	 * @param protoClientNo
	 * @throws CCPServiceException 
	 */
	void startSessionTimer(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException;

	/**
	 * @Description: 班长监听座席会话
	 * @param sendMsg
	 * @param connector
	 * @param userAcc
	 * @param protoClientNo
	 * @throws CCPServiceException 
	 */
	void monitorAgent(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException;

	/**
	 * @Description: 班长取消监听座席会话
	 * @param sendMsg
	 * @param connector
	 * @param protoClientNo
	 * @throws CCPServiceException 
	 */
	void cancelMonitorAgent(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException;

	/**
	 * @Description: 座席开启会议
	 * @param sendMsg
	 * @param connector
	 * @param userAcc
	 * @param protoClientNo
	 * @throws CCPServiceException 
	 */
	void startConf(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException;
	
	/**
	 * @Description: 座席加入会议
	 * @param sendMsg
	 * @param connector
	 * @param userAcc
	 * @param protoClientNo
	 * @throws CCPServiceException 
	 */
	void joinConf(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException;
	
	/**
	 * @Description: 座席退出会议
	 * @param sendMsg
	 * @param connector
	 * @param userAcc
	 * @param protoClientNo
	 * @throws CCPServiceException 
	 */
	void exitConf(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException;
	
	/**
	 * @Description: 强插
	 * @param sendMsg
	 * @param connector
	 * @param userAcc
	 * @param protoClientNo
	 * @throws CCPServiceException 
	 */
	void forceJoinConf(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException;
	
	/**
	 * @Description: 拒绝用户
	 * @param sendMsg
	 * @param connector
	 * @param userAcc
	 * @param protoClientNo
	 * @throws CCPServiceException 
	 */
	void rejectUser(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException;
	
	/**
	 * @Description: 查询队列
	 * @param sendMsg
	 * @param connector
	 * @param userAcc
	 * @param protoClientNo
	 * @throws CCPServiceException 
	 */
	void queryQueueInfo(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException;
	
	/**
	 * @Description: 查询座席
	 * @param sendMsg
	 * @param connector
	 * @param userAcc
	 * @param protoClientNo
	 */
	void queryAgentInfo(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException;
	
	/**
	 * @Description: 座席为指定用户服务
	 * @param sendMsg
	 * @param connector
	 * @param userAcc
	 * @param protoClientNo
	 */
	void serWithTheUser(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException;
	
	/**
	 * @Description: 用户预留服务
	 * @param sendMsg
	 * @param connector
	 * @param userAcc
	 * @param protoClientNo
	 */
	void reservedForUser(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException;

	/**
	 * @Description: 取消用户预留服务
	 * @param sendMsg
	 * @param connector
	 * @param userAcc
	 * @param protoClientNo
	 */
	void cancelReserved(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException;
	
	/**
	 * @Description: 坐席端断开连接
	 * @param sendMsg
	 * @param connector
	 * @param userAcc
	 * @param protoClientNo
	 * @throws CCPServiceException
	 */
	void agentDisconnect(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException;
	
}
