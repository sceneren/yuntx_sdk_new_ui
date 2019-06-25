package com.yuntongxun.mcm.mcm.service;

import com.yuntongxun.mcm.core.exception.CCPServiceException;
import com.yuntongxun.mcm.mcm.model.RMServerTransferData;
import com.yuntongxun.mcm.mcm.model.SeqInfo;

public interface RMServerResponseService {

	/**
	 * @Description: 登录坐席管理模块
	 * @param rmServerTransferData
	 * @throws CCPServiceException
	 */
	void respMCCCModuleLogIn(RMServerTransferData rmServerTransferData) throws CCPServiceException;
	
	/**
	 * @Description: IM坐席上班
	 * @param rmServerTransferData
	 * @param seqInfo
	 * @throws CCPServiceException
	 */
	void respAgentOnWork(RMServerTransferData rmServerTransferData, SeqInfo seqInfo) throws CCPServiceException;
	
	/**
	 * @Description: IM坐席下班
	 * @param rmServerTransferData
	 * @param seqInfo
	 * @throws CCPServiceException
	 */
	void respAgentOffWork(RMServerTransferData rmServerTransferData, SeqInfo seqInfo) throws CCPServiceException;

	/**
	 * @Description: IM坐席就绪
	 * @param rmServerTransferData
	 * @param seqInfo
	 * @throws CCPServiceException
	 */
	void respAgentReady(RMServerTransferData rmServerTransferData, SeqInfo seqInfo) throws CCPServiceException;
	
	/**
	 * @Description: 座席批量就绪(异常重启) 
	 * @param rmServerTransferData
	 * @param seqInfo
	 * @throws CCPServiceException
	 */
	void respCMUnexpectedRestart(RMServerTransferData rmServerTransferData, SeqInfo seqInfo) throws CCPServiceException;
	
	/**
	 * @Description: 锁定坐席
	 * @param rmServerTransferData 
	 * @param seqInfo
	 * @throws CCPServiceException
	 */
	void respLockAgent(RMServerTransferData rmServerTransferData, SeqInfo seqInfo) throws CCPServiceException;
	
	/**
	 * @Description: 座席其他状态更新
	 * @param rmServerTransferData 
	 * @param seqInfo
	 * @throws CCPServiceException
	 */
	void respAgentStateSwitch(RMServerTransferData rmServerTransferData, SeqInfo seqInfo) throws CCPServiceException;
	
	/**
	 * @Description: 用户进入排队
	 * @param rmServerTransferData 
	 * @param seqInfo
	 * @throws CCPServiceException
	 */
	void respEnterCCS(RMServerTransferData rmServerTransferData, SeqInfo seqInfo) throws CCPServiceException;
	
	/**
	 * @Description: 用户退出排队 
	 * @param rmServerTransferData
	 * @param seqInfo 
	 * @throws CCPServiceException
	 */
	void respExitCCSQueue(RMServerTransferData rmServerTransferData, SeqInfo seqInfo) throws CCPServiceException;
	
	/**
	 * @Description: 查询信息排队人数和空闲座席
	 * @param rmServerTransferData 
	 * @param seqInfo
	 * @throws CCPServiceException
	 */
	void respQueryQueueInfo(RMServerTransferData rmServerTransferData, SeqInfo seqInfo) throws CCPServiceException;
	
	/**
	 * @Description: 唤醒排队用户
	 * @param rmServerTransferData
	 * @param seqInfo
	 * @throws CCPServiceException
	 */
	void cmdWakeUpUser(RMServerTransferData rmServerTransferData, SeqInfo seqInfo) throws CCPServiceException;
	
	/**
	 * @Description: 分配IM坐席
	 * @param rmServerTransferData
	 * @param seqInfo
	   @throws CCPServiceException
	 */
	void respAllocImAgent(RMServerTransferData rmServerTransferData, SeqInfo seqInfo) throws CCPServiceException;

	/**
	 * @Description: IM坐席与用户服务结束
	 * @param rmServerTransferData
	 * @param seqInfo
	 * @throws CCPServiceException
	 */
	void respImAgentServiceEnd(RMServerTransferData rmServerTransferData, SeqInfo seqInfo) throws CCPServiceException;

	/**
	 * @Description: 坐席预留服务
	 * @param rmServerTransferData
	 * @param seqInfo
	 * @throws CCPServiceException
	 */
	void respAgentReservedService(RMServerTransferData rmServerTransferData, SeqInfo seqInfo) throws CCPServiceException;
	
	/**
	 * @Description: 坐席下班通知
	 * @param rmServerTransferData
	 * @param seqInfo
	 * @throws CCPServiceException
	 */
	void cmdNotifyAgentOffWork(RMServerTransferData rmServerTransferData, SeqInfo seqInfo) throws CCPServiceException;
	
}
